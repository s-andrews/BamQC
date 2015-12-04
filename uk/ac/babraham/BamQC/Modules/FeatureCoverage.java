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
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.FeatureClass;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class FeatureCoverage extends AbstractQCModule {

	private String [] featureNames = null;
	
	private double [] readCounts;
	
	private boolean datasetIsEmpty = true;
	
	@Override
	public void processSequence(SAMRecord read) {}

	@Override
	public void processFile(SequenceFile file) {}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {

		featureNames = annotation.listFeatureTypes();
		
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Float> values = new ArrayList<Float>();		
		
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
			readCounts[i] = values.get(i);
			if(readCounts[i] > 0.0) { 
				datasetIsEmpty = false;
			}
		}
		
		
	}

	@Override
	public JPanel getResultsPanel() {
		return new HorizontalBarGraph(featureNames, readCounts, "Number of Features", "Feature Type Read Counts");
	}

	@Override
	public String name() {
		return "Feature Coverage";
	}

	@Override
	public String description() {
		return "Tells how reads are distributed between feature types";
	}

	@Override
	public void reset() { }

	@Override
	public boolean raisesError() {
		return false;
	}

	@Override
	public boolean raisesWarning() {
		if(datasetIsEmpty) 
			return true;
		return false;
	}

	@Override
	public boolean needsToSeeSequences() {
		return false;
	}

	@Override
	public boolean needsToSeeAnnotation() {
		return true;
	}

	@Override
	public boolean ignoreInReport() {
		if(ModuleConfig.getParam("FeatureCoverage", "ignore") > 0 || featureNames == null || featureNames.length == 0) { // || datasetIsEmpty) { 
			return true;
		}
		return false;	
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "feature_coverage.png", "Feature Type Read Counts", 800, 600);
		
		if(featureNames == null) { return; }
	
		StringBuffer sb = report.dataDocument();
		sb.append("Feature_name\tFeature_type_read_counts\n");
		for (int i=0;i<featureNames.length;i++) {
			sb.append(featureNames[i]);
			sb.append("\t");
			sb.append(readCounts[i]);
			sb.append("\n");
		}

	}

	public String[] getFeatureNames() {
		return featureNames;
	}

	public double[] getReadCounts() {
		return readCounts;
	}

}
