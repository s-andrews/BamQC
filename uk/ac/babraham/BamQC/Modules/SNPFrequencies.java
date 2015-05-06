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
import java.util.HashMap;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.LineGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;



/** 
 * This class re-uses the computation collected by the class VariantCallDetection
 * and plots the SNP Frequencies.
 * @author Piero Dalle Pezze
 */
public class SNPFrequencies extends AbstractQCModule {

	private static Logger log = Logger.getLogger(SNPFrequencies.class);	
	
	// original threshold for the plot y axis.
	private double maxY=0.0d; 
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	// data fields for plotting
	private static String[] snpName = {"SNPs"};
	
	
	// Constructors
	/**
	 * Default constructor
	 */
	public SNPFrequencies() {	}

	
	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public SNPFrequencies(VariantCallDetection vcd) {	
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
		double threshold = moreFrequentReadLength * ModuleConfig.getParam("variant_call_position_snp_xaxis_threshold", "ignore").intValue() / 100d;
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
					0d, 100d, "Position in read (bp)", snpName, 
					new String[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()], 
					"SNP Frequencies ( total SNPs: 0.000 % )");
		}		
		

		long totSNPs = variantCallDetection.getTotalMutations(),
			 totBases = variantCallDetection.getTotal();		
		log.info("Total SNPs: " + totSNPs + " ( " + totSNPs*100f/totBases + "% )");
		
		
		// We do not need a BaseGroup here
		long[] snpPos = variantCallDetection.getSNPPos();
		
//		//////////////////////
//		// OLD PLOT 
//		//////////////////////
//		// initialise and configure the LineGraph
//		// compute the maximum value for the X axis
//		int maxX = computeXMaxValue();
//				
//		String[] xCategories = new String[maxX];		
//		double[] dSNPPos = new double[maxX];
//		maxY = 0.0d;
//		for(int i=0; i<maxX && i<snpPos.length; i++) {		
//			dSNPPos[i]= (double)snpPos[i];
//			if(dSNPPos[i] > maxY) { maxY = dSNPPos[i]; }
//			xCategories[i] = String.valueOf(i+1);
//		}
//		// add 10% to the maximum for improving the plot rendering
//		maxY = maxY + maxY*0.1; 		

		long[] totalPos = variantCallDetection.getTotalPos();
     	// initialise and configure the LineGraph
		// compute the maximum value for the X axis
		int maxX = computeXMaxValue();
		//maxX = insertionPos.length;
		String[] xCategories = new String[maxX];		
		double[] dSNPPos = new double[maxX];
		for(int i=0; i<maxX && i<snpPos.length; i++) {
			dSNPPos[i]= (snpPos[i] * 100d) / totalPos[i];
			if(dSNPPos[i] > maxY) { maxY = dSNPPos[i]; }
			xCategories[i] = String.valueOf(i+1);
		}
		// add 10% to the top for improving the visualisation of the plot.
		maxY = maxY + maxY*0.1;
		
		double[][] snpData = new double [][] {dSNPPos};
		String title = String.format("SNP frequencies ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
		return new LineGraph(snpData, 0d, maxY, "Position in read (bp)", snpName, xCategories, title);
	}

	@Override	
	public String name() {
		return "SNP Frequencies";
	}

	@Override	
	public String description() {
		return "Looks at the SNP frequencies in the data";
	}

	@Override	
	public void reset() { }

	@Override	
	public boolean raisesError() {
		if(maxY > ModuleConfig.getParam("variant_call_position_snp_threshold", "error"))
			return true;		
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		if(maxY > ModuleConfig.getParam("variant_call_position_snp_threshold", "warn"))
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
		super.writeDefaultImage(report, "snp_frequencies.png", "SNP Frequencies", 800, 600);
	}
	
}
