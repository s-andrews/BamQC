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

	private static Logger log = Logger.getLogger(IndelFrequencies.class);	
	
	private String[] indelNames = {"Insertions", "Deletions"};
	
	// threshold for the plot y axis.
	private double firstMaxY=0.0d;
	private double secondMaxY=0.0d; 
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	
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
	
	
	// Private methods
	
	/**
	 * Computes the maximum value for the x axis.
	 * @return xMaxValue
	 */
	private int computeXMaxValue() {
		HashMap<Integer, Long> hm = variantCallDetection.getContributingReadsPerPos();
		Integer[] readLengths = hm.keySet().toArray(new Integer[hm.size()]);
		Long[] readCounts = hm.values().toArray(new Long[hm.size()]);
		int xMaxValue = 5; // sequences long at least 5.
		long moreFrequentReadLength = 0;
		// Computes a variable threshold depending on the read length distribution of read library
		for(int i=0; i<readCounts.length; i++) {
			if(readCounts[i] > moreFrequentReadLength) {
				moreFrequentReadLength = readCounts[i];
			}
		}
		double threshold = moreFrequentReadLength * ModuleConfig.getParam("variant_call_position_indel_seqpercent_xaxis_threshold", "ignore").intValue() / 100d;
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
		
		if(variantCallDetection == null) { 
			return new LineGraph(new double [][]{
					new double[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()],
					new double[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()]},
					0d, 100d, "Position in read (bp)", indelNames, 
					new String[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()], 
					"Indel Frequencies ( total insertions: 0.000 %, total deletions: 0.000 % )");
		}		
		
		long totIns = variantCallDetection.getTotalInsertions(),
			 totDel = variantCallDetection.getTotalDeletions(), 
			 totBases = variantCallDetection.getTotal();
		
		log.info("A insertions: " + variantCallDetection.getAInsertions());
		log.info("C insertions: " + variantCallDetection.getCInsertions());
		log.info("G insertions: " + variantCallDetection.getGInsertions());
		log.info("T insertions: " + variantCallDetection.getTInsertions());
		log.info("N insertions: " + variantCallDetection.getNInsertions());
		log.info("Total insertions: " + totIns + " ( " + totIns*100f/totBases + "% )");
		log.info("A deletions: " + variantCallDetection.getADeletions());
		log.info("C deletions: " + variantCallDetection.getCDeletions());
		log.info("G deletions: " + variantCallDetection.getGDeletions());
		log.info("T deletions: " + variantCallDetection.getTDeletions());
		log.info("N deletions: " + variantCallDetection.getNDeletions());		
		log.info("Total deletions: " + totDel + " ( " + totDel*100f/totBases + "% )");
		log.info("Skipped regions on the reads: " + variantCallDetection.getReadSkippedRegions());
		log.info("Skipped regions on the reference: " + variantCallDetection.getReferenceSkippedRegions());
		log.info("Skipped reads: " + variantCallDetection.getSkippedReads() + " ( "+ (variantCallDetection.getSkippedReads()*100.0f)/variantCallDetection.getTotalReads() + "% )");
		

		
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
		long[] firstInsertionPos = variantCallDetection.getFirstInsertionPos();
		long[] firstDeletionPos = variantCallDetection.getFirstDeletionPos();		
		double[] dFirstInsertionPos = new double[maxX];
		double[] dFirstDeletionPos = new double[maxX];		
		for(int i=0; i<maxX && i<firstInsertionPos.length; i++) {
			dFirstInsertionPos[i]= (firstInsertionPos[i] * 100d) / totalPos[i];
			dFirstDeletionPos[i]= (firstDeletionPos[i] * 100d) / totalPos[i];
			if(dFirstInsertionPos[i] > firstMaxY) { firstMaxY = dFirstInsertionPos[i]; }
			if(dFirstDeletionPos[i] > firstMaxY) { firstMaxY = dFirstDeletionPos[i]; }
			xCategories[i] = String.valueOf(i+1);
		}
		double[][] firstIndelData = new double [][] {dFirstInsertionPos,dFirstDeletionPos};

		// compute statistics from the SECOND segment data if there are paired reads.
		if(variantCallDetection.existPairedReads()) {
			resultsPanel.setLayout(new GridLayout(2,1));
			long[] secondInsertionPos = variantCallDetection.getSecondInsertionPos();
			long[] secondDeletionPos = variantCallDetection.getSecondDeletionPos();
			double[] dSecondInsertionPos = new double[maxX];
			double[] dSecondDeletionPos = new double[maxX];		
			for(int i=0; i<maxX && i<secondInsertionPos.length; i++) {
				dSecondInsertionPos[i]= (secondInsertionPos[i] * 100d) / totalPos[i];
				dSecondDeletionPos[i]= (secondDeletionPos[i] * 100d) / totalPos[i];			
				if(dSecondInsertionPos[i] > secondMaxY) { secondMaxY = dSecondInsertionPos[i]; }			
				if(dSecondDeletionPos[i] > secondMaxY) { secondMaxY = dSecondDeletionPos[i]; }			
			}
			double[][] secondIndelData = new double [][] {dSecondInsertionPos,dSecondDeletionPos};
			
			String title = String.format("First Read Indel Frequencies ( total insertions: %.3f %%, total deletions: %.3f %% )", 
					totIns*100.0f/totBases,totDel*100.0f/totBases);	
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(firstIndelData, 0d, firstMaxY+firstMaxY*0.1, "Position in read (bp)", indelNames, xCategories, title));	
			
			String title2 = "Second Read Indel Frequencies";	
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(secondIndelData, 0d, secondMaxY+secondMaxY*0.1, "Position in read (bp)", indelNames, xCategories, title2));
		} else {
			resultsPanel.setLayout(new GridLayout(1,1));
			String title = String.format("Read Indel Frequencies ( total insertions: %.3f %%, total deletions: %.3f %% )", 
					totIns*100.0f/totBases,totDel*100.0f/totBases);	
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new LineGraph(firstIndelData, 0d, firstMaxY+firstMaxY*0.1, "Position in read (bp)", indelNames, xCategories, title));	
		}

		return resultsPanel;
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
		if(firstMaxY+secondMaxY > ModuleConfig.getParam("variant_call_position_indel_threshold", "error").doubleValue())
			return true;		
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		if(firstMaxY+secondMaxY > ModuleConfig.getParam("variant_call_position_indel_threshold", "warn").doubleValue())
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
		if(variantCallDetection == null) { return true; }
		return variantCallDetection.getTotal() == 0;
	}

	@Override	
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "indel_frequencies.png", "Indel Frequencies", 800, 600);	
	}
	
}
