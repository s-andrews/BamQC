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

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

//import org.apache.log4j.Logger;




import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;



/** 
 * This class re-uses the computation collected by the class VariantCallDetection
 * and plots the SNP Frequencies by type.
 * @author Piero Dalle Pezze
 */
public class SNPFrequenciesByType extends AbstractQCModule {

	//private static Logger log = Logger.getLogger(SNPFrequenciesByType.class);
	
	// original threshold for the plot x axis.
	private double firstMaxX=0.0d; 
	private double secondMaxX=0.0d; 	
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	// data fields for plotting
	private static String[] snpTypeNames = {
		"A->C", "A->G", "A->T",
		"C->A", "C->G", "C->T", 
		"G->A", "G->C", "G->T", 
		"T->A", "T->C", "T->G"};
	private double[] dFirstSNPFrequenciesByType = null;
	private double[] dSecondSNPFrequenciesByType = null;
	
	// Constructors
//	/**
//	 * Default constructor
//	 */
//	public SNPFrequenciesByType() {	}

	
	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public SNPFrequenciesByType(VariantCallDetection vcd) {	
		super();
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
			String title = String.format("Read SNP frequencies by Type ( total SNPs: 0 (0.000 %) )");
			return new HorizontalBarGraph(snpTypeNames, new double[12], title, 0, 1);
		}		
		//variantCallDetection.computeTotals();
		
		JPanel resultsPanel = new JPanel();
		// first/second identify the first or second segments respectively.
		
		long totSNPs = variantCallDetection.getTotalMutations(), 
				 totBases = variantCallDetection.getTotal();
		
		// compute statistics from the FIRST segment data
		long firstAC = variantCallDetection.getFirstA2C(), firstAG = variantCallDetection
				.getFirstA2G(), firstAT = variantCallDetection.getFirstA2T(), firstCA = variantCallDetection
				.getFirstC2A(), firstCG = variantCallDetection.getFirstC2G(), firstCT = variantCallDetection
				.getFirstC2T(), firstGA = variantCallDetection.getFirstG2A(), firstGC = variantCallDetection
				.getFirstG2C(), firstGT = variantCallDetection.getFirstG2T(), firstTA = variantCallDetection
				.getFirstT2A(), firstTC = variantCallDetection.getFirstT2C(), firstTG = variantCallDetection
				.getFirstT2G();

		dFirstSNPFrequenciesByType = new double[12];
		dFirstSNPFrequenciesByType[0] = firstAC * 100d / totBases;
		dFirstSNPFrequenciesByType[1] = firstAG * 100d / totBases;
		dFirstSNPFrequenciesByType[2] = firstAT * 100d / totBases;
		dFirstSNPFrequenciesByType[3] = firstCA * 100d / totBases;
		dFirstSNPFrequenciesByType[4] = firstCG * 100d / totBases;
		dFirstSNPFrequenciesByType[5] = firstCT * 100d / totBases;
		dFirstSNPFrequenciesByType[6] = firstGA * 100d / totBases;
		dFirstSNPFrequenciesByType[7] = firstGC * 100d / totBases;
		dFirstSNPFrequenciesByType[8] = firstGT * 100d / totBases;
		dFirstSNPFrequenciesByType[9] = firstTA * 100d / totBases;
		dFirstSNPFrequenciesByType[10] = firstTC * 100d / totBases;
		dFirstSNPFrequenciesByType[11] = firstTG * 100d / totBases;
		
		for(int i=0; i< dFirstSNPFrequenciesByType.length; i++) {
			if(firstMaxX < dFirstSNPFrequenciesByType[i]) 
				firstMaxX = dFirstSNPFrequenciesByType[i];
		}
		
		// compute statistics from the SECOND segment data if there are paired reads.
		if(variantCallDetection.existPairedReads()) {
			resultsPanel.setLayout(new GridLayout(2,1));
			long secondAC = variantCallDetection.getFirstA2C(), secondAG = variantCallDetection
					.getSecondA2G(), secondAT = variantCallDetection.getSecondA2T(), secondCA = variantCallDetection
					.getSecondC2A(), secondCG = variantCallDetection.getSecondC2G(), secondCT = variantCallDetection
					.getSecondC2T(), secondGA = variantCallDetection.getSecondG2A(), secondGC = variantCallDetection
					.getSecondG2C(), secondGT = variantCallDetection.getSecondG2T(), secondTA = variantCallDetection
					.getSecondT2A(), secondTC = variantCallDetection.getSecondT2C(), secondTG = variantCallDetection
					.getSecondT2G();	
			
			dSecondSNPFrequenciesByType = new double[12];
			dSecondSNPFrequenciesByType[0] = secondAC * 100d / totBases;
			dSecondSNPFrequenciesByType[1] = secondAG * 100d / totBases;
			dSecondSNPFrequenciesByType[2] = secondAT * 100d / totBases;
			dSecondSNPFrequenciesByType[3] = secondCA * 100d / totBases;
			dSecondSNPFrequenciesByType[4] = secondCG * 100d / totBases;
			dSecondSNPFrequenciesByType[5] = secondCT * 100d / totBases;
			dSecondSNPFrequenciesByType[6] = secondGA * 100d / totBases;
			dSecondSNPFrequenciesByType[7] = secondGC * 100d / totBases;
			dSecondSNPFrequenciesByType[8] = secondGT * 100d / totBases;
			dSecondSNPFrequenciesByType[9] = secondTA * 100d / totBases;
			dSecondSNPFrequenciesByType[10] = secondTC * 100d / totBases;
			dSecondSNPFrequenciesByType[11] = secondTG * 100d / totBases;
			
			for(int i=0; i< dSecondSNPFrequenciesByType.length; i++) {
				if(secondMaxX < dSecondSNPFrequenciesByType[i]) 
					secondMaxX = dSecondSNPFrequenciesByType[i];
			}
			
			String title = String.format("First Read SNP frequencies by Type ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new HorizontalBarGraph(snpTypeNames, dFirstSNPFrequenciesByType, title, 0d, firstMaxX+firstMaxX*0.1d));
			
			String title2 = "Second Read SNP frequencies by Type";
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new HorizontalBarGraph(snpTypeNames, dSecondSNPFrequenciesByType, title2, 0d, secondMaxX+secondMaxX*0.1d));
			
		} else {
			resultsPanel.setLayout(new GridLayout(1,1));
			String title = String.format("Read SNP frequencies by Type ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new HorizontalBarGraph(snpTypeNames, dFirstSNPFrequenciesByType, title, 0d, firstMaxX+firstMaxX*0.1d));			
		}
		return resultsPanel;
	}

	@Override	
	public String name() {
		return "SNP Frequencies by Type";
	}

	@Override	
	public String description() {
		return "Looks at the SNP frequencies by type in the data";
	}

	@Override	
	public void reset() { }

	@Override	
	public boolean raisesError() {
		if(firstMaxX+secondMaxX> ModuleConfig.getParam("VariantCallPosition_snp_by_type_threshold", "error").doubleValue())
			return true;		
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		if(firstMaxX+secondMaxX > ModuleConfig.getParam("VariantCallPosition_snp_by_type_threshold", "warn").doubleValue())
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
		if(variantCallDetection == null || 
		   variantCallDetection.getTotalMutations() == 0) 
			return true; 
		return false;
	}

	@Override	
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "snp_frequencies_by_type.png", "SNP Frequencies by Type", 800, 600);
		
		
		// write raw data in a report
		if(dFirstSNPFrequenciesByType == null) { return; }
		
		StringBuffer sb = report.dataDocument();
		if(dSecondSNPFrequenciesByType != null) {
			sb.append("SNPType\t1stReadFreq\t2ndReadFreq\n");
			for (int i=0;i<dFirstSNPFrequenciesByType.length;i++) {
				sb.append(snpTypeNames[i]);
				sb.append("\t");
				sb.append(dFirstSNPFrequenciesByType[i]);
				sb.append("\t");
				sb.append(dSecondSNPFrequenciesByType[i]);
				sb.append("\n");
			}
		} else {
			sb.append("SNPType\tReadSNPFreq\n");
			for (int i=0;i<dFirstSNPFrequenciesByType.length;i++) {
				sb.append(snpTypeNames[i]);
				sb.append("\t");
				sb.append(dFirstSNPFrequenciesByType[i]);
				sb.append("\n");
			}
		}
		
	}
	
}
