/**
 * Copyright Copyright 2015 Piero Dalle Pezze
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

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Graphs.LineGraph;



/** 
 * This class re-uses the computation collected by the class VariantCallDetection
 * and plots the Indel Frequencies.
 * @author Piero Dalle Pezze
 */
public class IndelFrequencies extends AbstractQCModule {

	
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	// data fields for plotting
	private static String[] indelNames = {
		"Insertions",
		"Deletions"};
	
	// Constructors
	/**
	 * Default constructor
	 */
	public IndelFrequencies() {	}

	
	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public IndelFrequencies(VariantCallDetection vcd) {	
		variantCallDetection = vcd;
	}
	
	
	// @Override methods
	
	@Override
	public void processSequence(SAMRecord read) { }
	
	
	@Override	
	public void processFile(SequenceFile file) { }

	@Override	
	public void processAnnotationSet(AnnotationSet annotation) {

	}		

	@Override	
	public JPanel getResultsPanel() {
		if(variantCallDetection == null) { 
			return new LineGraph(new double [][]{
					new double[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()],
					new double[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()]},
					0d, 100d, "Position in read (bp)", indelNames, 
					new String[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()], 
					"Indel Frequencies ( Insertions: 0, Deletions: 0 )");
		}		
		
		// We do not need a BaseGroup here
		// These two arrays have same length.
		long[] insertionPos = variantCallDetection.getInsertionPos();
		long[] deletionPos = variantCallDetection.getDeletionPos();
		
		// initialise and configure the LineGraph
		// compute the maximum value for the X axis
		int maxX = insertionPos.length;
		boolean found = false;
		for(int i=insertionPos.length-1; i>=0 && !found; i--) {
			if(insertionPos[i] > 0 || deletionPos[i] > 0) { 
				maxX = i+1;
				found = true;
			}
		}
		String[] xCategories = new String[maxX];		
		double[] dInsertionPos = new double[maxX];
		double[] dDeletionPos = new double[maxX];
		double maxY = 0.0d;
		for(int i=0; i<maxX; i++) {
			dInsertionPos[i]= (double)insertionPos[i];
			dDeletionPos[i]= (double)deletionPos[i];
			if(dInsertionPos[i] > maxY) { maxY = dInsertionPos[i]; }
			if(dDeletionPos[i] > maxY) { maxY = dDeletionPos[i]; }
			xCategories[i] = String.valueOf(i);
		}
//		String[] xCategories = new String[insertionPos.length];		
//		double[] dInsertionPos = new double[insertionPos.length];
//		double[] dDeletionPos = new double[insertionPos.length];
//		double maxY = 0.0d;
//		for(int i=0; i<insertionPos.length; i++) {
//			dInsertionPos[i]= (double)insertionPos[i];
//			dDeletionPos[i]= (double)deletionPos[i];
//			if(dInsertionPos[i] > maxY) { maxY = dInsertionPos[i]; }
//			if(dDeletionPos[i] > maxY) { maxY = dDeletionPos[i]; }
//			xCategories[i] = String.valueOf(i);
//		}
		// add 10% to the maximum for improving the plot rendering
		maxY = maxY + maxY*0.05; 
		double[][] indelData = new double [][] {dInsertionPos,dDeletionPos};
//		String title = String.format("Indel Frequencies \n(Insertions: %d (A:%d,C:%d,G:%d,T:%d); Deletions: %d (A:%d,C:%d,G:%d,T:%d))", 
//				variantCallDetection.getTotalInsertions(), 
//				variantCallDetection.getAInsertions(),
//				variantCallDetection.getCInsertions(),
//				variantCallDetection.getGInsertions(),
//				variantCallDetection.getTInsertions(),
//				variantCallDetection.getTotalDeletions(), 
//				variantCallDetection.getADeletions(),
//				variantCallDetection.getCDeletions(),
//				variantCallDetection.getGDeletions(),
//				variantCallDetection.getTDeletions());
		String title = String.format("Indel Frequencies ( Insertions: %d, Deletions: %d )", 
				variantCallDetection.getTotalInsertions(),variantCallDetection.getTotalDeletions());		
	
		return new LineGraph(indelData, 0d, maxY, "Position in read (bp)", indelNames, xCategories, title);
	}

	@Override	
	public String name() {
		return "Indel Frequencies";
	}

	@Override	
	public String description() {
		return "Looks at the Indel frequencies in the data";
	}

	@Override	
	public void reset() { }

	@Override	
	public boolean raisesError() {
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		return false;
	}

	@Override	
	public boolean needsToSeeSequences() {
		return false;
	}

	@Override	
	public boolean needsToSeeAnnotation() {
		return false;
	}

	@Override	
	public boolean ignoreInReport() {
		if(variantCallDetection == null) { return true; }
		return variantCallDetection.getTotal() == 0;
	}

	@Override	
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "indel_frequencies.png", "Indel Frequencies", 800, 600);
		
	
//		StringBuffer sb = report.dataDocument();
//		
//		sb.append("#Total Deduplicated Percentage\t");
//		sb.append(percentDifferentSeqs);
//		sb.append("\n");
//		
//		sb.append("#Duplication Level\tPercentage of deduplicated\tPercentage of total\n");
//		for (int i=0;i<labels.length;i++) {
//			sb.append(labels[i]);
//			if (i == labels.length-1) {
//				sb.append("+");
//			}
//			sb.append("\t");
//			sb.append(deduplicatedPercentages[i]);
//			sb.append("\t");
//			sb.append(totalPercentages[i]);
//			sb.append("\n");
//		}
				
		
		
		
	}
	
}
