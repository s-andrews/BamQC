/**
 * Copyright Copyright 2014 Simon Andrews
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

package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Annotation.FeatureClass;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class FeatureCoverage extends AbstractQCModule {

	private String [] featureNames = null;
	private double [] readCounts;
	
	public void processSequence(SAMRecord read) {}

	public void processFile(SequenceFile file) {}

	public void processAnnotationSet(AnnotationSet annotation) {

		featureNames = annotation.listFeatureTypes();
		
		Vector<String> names = new Vector<String>();
		Vector<Float> values = new Vector<Float>();		
		
		for (int i=0;i<featureNames.length;i++) {
			
			FeatureClass fc = annotation.getFeatureClassForType(featureNames[i]);
			
			String [] subclasses = fc.getSubclassNames();
			
			for (int s=0;s<subclasses.length;s++) {
				if (subclasses[s].equals("")) {
					names.add(featureNames[i]);
				}
				else {
					names.add(""+featureNames[i]+"_"+subclasses[s]);
				}
				values.add((float)annotation.getFeatureClassForType(featureNames[i]).getSubclassForName(subclasses[s]).count());
			}			
		}
		
		featureNames = names.toArray(new String[0]);
		readCounts = new double[featureNames.length];
		for (int i=0;i<readCounts.length;i++) {
			readCounts[i] = values.elementAt(i);
		}
		
		
	}

	public JPanel getResultsPanel() {
		return new HorizontalBarGraph(featureNames, readCounts, "Feature read counts");
	}

	public String name() {
		return "Feature type read counts";
	}

	public String description() {
		return "Tells how reads are distributed between feature types";
	}

	public void reset() {
		// TODO Auto-generated method stub

	}

	public boolean raisesError() {
		//TODO: Set this
		return false;
	}

	public boolean raisesWarning() {
		//TODO: Set this
		return false;
	}

	public boolean needsToSeeSequences() {
		return false;
	}

	public boolean needsToSeeAnnotation() {
		return true;
	}

	public boolean ignoreInReport() {
		if (featureNames != null) {
			if (featureNames.length == 0) {
				return true;
			}
			return false;
		}
		return false;
	}

	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub

	}

}
