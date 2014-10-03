/**
 * Copyright Copyright 2010-14 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.BamQC.Analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.BamQCConfig;
import uk.ac.babraham.BamQC.Annotation.AnnotationParser;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Annotation.GFF3AnnotationParser;
import uk.ac.babraham.BamQC.Annotation.GTFParser;
import uk.ac.babraham.BamQC.Modules.QCModule;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Sequence.SequenceFormatException;

public class AnalysisRunner implements Runnable {

	private SequenceFile file;
	private QCModule [] modules;
	private List<AnalysisListener> listeners = new ArrayList<AnalysisListener>();
	private int percentComplete = 0;
	
	public AnalysisRunner (SequenceFile file) {
		this.file = file;
	}
	
	public void addAnalysisListener (AnalysisListener l) {
		if (l != null && !listeners.contains(l)) {
			listeners.add(l);
		}
	}

	public void removeAnalysisListener (AnalysisListener l) {
		if (l != null && listeners.contains(l)) {
			listeners.remove(l);
		}
	}

	
	public void startAnalysis (QCModule [] modules) {
		this.modules = modules;
		for (int i=0;i<modules.length;i++) {
			modules[i].reset();
		}
		AnalysisQueue.getInstance().addToQueue(this);
	}

	public void run() {

		Iterator<AnalysisListener> i = listeners.iterator();
		while (i.hasNext()) {
			i.next().analysisStarted(file);
		}
		
		AnnotationSet annotation = new AnnotationSet();
				
		if (BamQCConfig.getInstance().gff_file != null) {
			AnnotationParser parser;
			
			if (BamQCConfig.getInstance().gff_file.getName().toLowerCase().endsWith("gtf")) {
				parser = new GTFParser();
			}
			else {
				parser = new GFF3AnnotationParser();
			}
			try {
				parser.parseAnnotation(annotation, BamQCConfig.getInstance().gff_file);
			}
			catch (Exception e) {
				Iterator<AnalysisListener> i2 = listeners.iterator();
				while (i2.hasNext()) {
					i2.next().analysisExceptionReceived(file, e);
					return;
				}
				
			}
		}

		for (int m=0;m<modules.length;m++) {
			modules[m].processFile(file);
		}
		
		int seqCount = 0;
		while (file.hasNext()) {
			++seqCount;
			SAMRecord seq;
			try {
				seq = file.next();
			}
			catch (SequenceFormatException e) {
				i = listeners.iterator();
				while (i.hasNext()) {
					i.next().analysisExceptionReceived(file,e);
				}
				return;
			}
			
			annotation.processSequence(seq);
			
			for (int m=0;m<modules.length;m++) {
				if (modules[m].needsToSeeSequences()) {
					modules[m].processSequence(seq);
				}
			}
			
			if (seqCount % 1000 == 0) {
			if (file.getPercentComplete() >= percentComplete+5) {
			
				percentComplete = (((int)file.getPercentComplete())/5)*5;
				
				i = listeners.iterator();
					while (i.hasNext()) {
						i.next().analysisUpdated(file,seqCount,percentComplete);
					}
					try {
						Thread.sleep(10);
					} 
					catch (InterruptedException e) {}
			}
			}
		}
		
		// Now send the compiled annotation around the modules which 
		// need to see it
		for (int m=0;m<modules.length;m++) {
			if (modules[m].needsToSeeAnnotation()) {
				modules[m].processAnnotationSet(annotation);
			}
		}
		
		
		i = listeners.iterator();
		while (i.hasNext()) {
			i.next().analysisComplete(file,modules);
		}

	}
	
}
