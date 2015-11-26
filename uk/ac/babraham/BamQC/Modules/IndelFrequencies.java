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

import java.awt.GridLayout;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Graphs.LineGraph;



/** 
 * This class re-uses the computation collected by the class VariantCallDetection
 * and plots the Indel Frequencies.
 * @author Piero Dalle Pezze
 */
public class IndelFrequencies extends AbstractQCModule {

	private static Logger log = Logger.getLogger(IndelFrequencies.class);	
	
	private String[] indelNames = {"Deletions", "Insertions"};
	
	double[] dFirstDeletionPos = null;		
	double[] dFirstInsertionPos = null;
	double[] dSecondDeletionPos = null;		
	double[] dSecondInsertionPos = null;
	
	// threshold for the plot y axis.
	private double firstMaxY=0.0d;
	private double secondMaxY=0.0d; 
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	
	// Constructors
//	/**
//	 * Default constructor
//	 */
//	public IndelFrequencies() {	}

	
	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public IndelFrequencies(VariantCallDetection vcd) {	
		super();
		variantCallDetection = vcd;
	}
	
	
	// Private methods
	
	/**
	 * Computes the maximum value for the x axis.
	 * @return xMaxValue
	 */
	private int computeXMaxValue() {
		HashMap<Integer, Long> hm = variantCallDetection.getContributingReadsPerPos();
		Integer[] readLengths = hm.keySet().toArray(new Integer[0]);
		Long[] readCounts = hm.values().toArray(new Long[0]);
		int xMaxValue = 5; // sequences long at least 5.
		long moreFrequentReadLength = 0;
		// Computes a variable threshold depending on the read length distribution of read library
		for(int i=0; i<readCounts.length; i++) {
			if(readCounts[i] > moreFrequentReadLength) {
				moreFrequentReadLength = readCounts[i];
			}
		}
		double threshold = moreFrequentReadLength * ModuleConfig.getParam("VariantCallPosition_indel_seqpercent_xaxis_threshold", "ignore").intValue() / 100d;
		// Filters the reads to show based on a the threshold computed previously.
		for(int i=0; i<readCounts.length; i++) {
			if(readCounts[i] >= threshold && xMaxValue < readLengths[i]) {
				xMaxValue = readLengths[i];
			}
			log.debug("Read Length: " + readLengths[i] + ", Num Reads: " + readCounts[i] + ", Min Accepted Length: " + threshold);
		}
		return xMaxValue+1;	//this will be used for array sizes (so +1).
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
		
		
		long totDel = variantCallDetection.getTotalDeletions(), 
			 totIns = variantCallDetection.getTotalInsertions(),
			 totBases = variantCallDetection.getTotal();
		
		log.debug("Total deletions: " + totDel + " ( " + totDel*100f/totBases + "% )");
		log.debug("Total insertions: " + totIns + " ( " + totIns*100f/totBases + "% )");	
		log.debug("Skipped reads: " + variantCallDetection.getSkippedReads() + " ( "+ (variantCallDetection.getSkippedReads()*100.0f)/variantCallDetection.getTotalReads() + "% )");
		
		
		JPanel resultsPanel = new JPanel();
		// We do not need a BaseGroup here
		// These two arrays have same length.
		// first/second identify the first or second segments respectively. 
		long[] totalPos = variantCallDetection.getTotalPos();
     	// initialise and configure the LineGraph
		// compute the maximum value for the X axis
		int maxX = computeXMaxValue();
		String[] xCategories = new String[maxX];
		
		// compute statistics from the FIRST segment data
		long[] firstDeletionPos = variantCallDetection.getFirstDeletionPos();		
		long[] firstInsertionPos = variantCallDetection.getFirstInsertionPos();
		dFirstDeletionPos = new double[maxX];		
		dFirstInsertionPos = new double[maxX];
		for(int i=0; i<maxX && i<firstDeletionPos.length; i++) {
			dFirstDeletionPos[i]= (firstDeletionPos[i] * 100d) / totalPos[i];
			dFirstInsertionPos[i]= (firstInsertionPos[i] * 100d) / totalPos[i];
			if(dFirstDeletionPos[i] > firstMaxY) { firstMaxY = dFirstDeletionPos[i]; }
			if(dFirstInsertionPos[i] > firstMaxY) { firstMaxY = dFirstInsertionPos[i]; }
			xCategories[i] = String.valueOf(i+1);
		}
		double[][] firstIndelData = new double [][] {dFirstDeletionPos, dFirstInsertionPos};

		// compute statistics from the SECOND segment data if there are paired reads.
		if(variantCallDetection.existPairedReads()) {
			resultsPanel.setLayout(new GridLayout(2,1));
			long[] secondDeletionPos = variantCallDetection.getSecondDeletionPos();
			long[] secondInsertionPos = variantCallDetection.getSecondInsertionPos();
			dSecondDeletionPos = new double[maxX];
			dSecondInsertionPos = new double[maxX];
			for(int i=0; i<maxX && i<secondDeletionPos.length; i++) {
				dSecondDeletionPos[i]= (secondDeletionPos[i] * 100d) / totalPos[i];			
				dSecondInsertionPos[i]= (secondInsertionPos[i] * 100d) / totalPos[i];
				if(dSecondDeletionPos[i] > secondMaxY) { secondMaxY = dSecondDeletionPos[i]; }
				if(dSecondInsertionPos[i] > secondMaxY) { secondMaxY = dSecondInsertionPos[i]; }			
			}
			double[][] secondIndelData = new double [][] {dSecondDeletionPos, dSecondInsertionPos};
			
			String title = String.format("First Read Indel Frequencies ( total deletions: %.3f %%, total insertions: %.3f %% )", 
					totDel*100.0f/totBases, totIns*100.0f/totBases);	
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(firstIndelData, 0d, firstMaxY+firstMaxY*0.1, "Position in Read (bp)", "Frequency", indelNames, xCategories, title));	
			
			String title2 = "Second Read Indel Frequencies";	
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(secondIndelData, 0d, secondMaxY+secondMaxY*0.1, "Position in Read (bp)", "Frequency", indelNames, xCategories, title2));
		} else {
			resultsPanel.setLayout(new GridLayout(1,1));
			String title = String.format("Read Indel Frequencies ( total deletions: %.3f %%, total insertions: %.3f %% )", 
					totDel*100.0f/totBases, totIns*100.0f/totBases);	
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(firstIndelData, 0d, firstMaxY+firstMaxY*0.1, "Position in Read (bp)", "Frequency", indelNames, xCategories, title));	
		}

		return resultsPanel;
	}

	@Override	
	public String name() {
		return "Indel Frequencies";
	}

	@Override	
	public String description() {
		return "Looks at the indel frequencies in the data";
	}

	@Override	
	public void reset() { }

	@Override	
	public boolean raisesError() {
		double indelPercent = 100.0d*(variantCallDetection.getTotalDeletions() + variantCallDetection.getTotalInsertions() ) 
							  / variantCallDetection.getTotal();
		if(indelPercent > ModuleConfig.getParam("VariantCallPosition_indel_threshold", "error").doubleValue())
			return true;		
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		double indelPercent = 100.0d*(variantCallDetection.getTotalDeletions() + variantCallDetection.getTotalInsertions() ) 
				  / variantCallDetection.getTotal();
		if(indelPercent > ModuleConfig.getParam("VariantCallPosition_indel_threshold", "warn").doubleValue())
			return true;		
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
		if(ModuleConfig.getParam("IndelFrequencies", "ignore") > 0 || variantCallDetection == null || 
		   (variantCallDetection.getTotalDeletions() == 0 && variantCallDetection.getTotalInsertions() == 0)) 
			return true; 
		return false;
	}

	@Override	
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "indel_frequencies.png", "Indel Frequencies", 800, 600);
		
		// write raw data in a report
		if(dFirstDeletionPos == null) { return; }
		
		StringBuffer sb = report.dataDocument();
		if(dSecondInsertionPos != null) {
			sb.append("Position\t1st_read_del_freq\t1st_read_ins_freq\t2nd_read_del_freq\t2nd_read_ins_freq\n");
			for (int i=0;i<dFirstInsertionPos.length;i++) {
				sb.append((i+1));
				sb.append("\t");
				sb.append(dFirstDeletionPos[i]);
				sb.append("\t");
				sb.append(dFirstInsertionPos[i]);
				sb.append("\t");
				sb.append(dSecondDeletionPos[i]);
				sb.append("\t");
				sb.append(dSecondInsertionPos[i]);
				sb.append("\n");
			}
		} else {
			sb.append("Position\tRead_del_freq\tRead_ins_freq\n");
			for (int i=0;i<dFirstInsertionPos.length;i++) {
				sb.append((i+1));
				sb.append("\t");
				sb.append(dFirstDeletionPos[i]);
				sb.append("\t");
				sb.append(dFirstInsertionPos[i]);
				sb.append("\n");
			}
		}
	}
	
}
