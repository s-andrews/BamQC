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

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;



/** 
 * This class re-uses the computation collected by the class VariantCallDetection
 * and plots the SNP Frequencies by type.
 * @author Piero Dalle Pezze
 */
public class SNPFrequenciesByType extends AbstractQCModule {

	private static Logger log = Logger.getLogger(SNPFrequenciesByType.class);
	
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
	private double[] firstSNPFrequenciesByType;
	private double[] secondSNPFrequenciesByType;
	
	// Constructors
	/**
	 * Default constructor
	 */
	public SNPFrequenciesByType() {	}

	
	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public SNPFrequenciesByType(VariantCallDetection vcd) {	
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
		
		log.info("SNP A->C: " + firstAC);
		log.info("SNP A->G: " + firstAG);
		log.info("SNP A->T: " + firstAT);
		log.info("SNP C->A: " + firstCA);
		log.info("SNP C->G: " + firstCG);
		log.info("SNP C->T: " + firstCT);
		log.info("SNP G->A: " + firstGA);
		log.info("SNP G->C: " + firstGC);
		log.info("SNP G->T: " + firstGT);
		log.info("SNP T->A: " + firstTA);
		log.info("SNP T->C: " + firstTC);
		log.info("SNP T->G: " + firstTG);

		firstSNPFrequenciesByType = new double[12];
		firstSNPFrequenciesByType[0] = firstAC * 100d / totBases;
		firstSNPFrequenciesByType[1] = firstAG * 100d / totBases;
		firstSNPFrequenciesByType[2] = firstAT * 100d / totBases;
		firstSNPFrequenciesByType[3] = firstCA * 100d / totBases;
		firstSNPFrequenciesByType[4] = firstCG * 100d / totBases;
		firstSNPFrequenciesByType[5] = firstCT * 100d / totBases;
		firstSNPFrequenciesByType[6] = firstGA * 100d / totBases;
		firstSNPFrequenciesByType[7] = firstGC * 100d / totBases;
		firstSNPFrequenciesByType[8] = firstGT * 100d / totBases;
		firstSNPFrequenciesByType[9] = firstTA * 100d / totBases;
		firstSNPFrequenciesByType[10] = firstTC * 100d / totBases;
		firstSNPFrequenciesByType[11] = firstTG * 100d / totBases;
		
		for(int i=0; i< firstSNPFrequenciesByType.length; i++) {
			if(firstMaxX < firstSNPFrequenciesByType[i]) 
				firstMaxX = firstSNPFrequenciesByType[i];
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
			
			log.info("SNP A->C: " + secondAC);
			log.info("SNP A->G: " + secondAG);
			log.info("SNP A->T: " + secondAT);
			log.info("SNP C->A: " + secondCA);
			log.info("SNP C->G: " + secondCG);
			log.info("SNP C->T: " + secondCT);
			log.info("SNP G->A: " + secondGA);
			log.info("SNP G->C: " + secondGC);
			log.info("SNP G->T: " + secondGT);
			log.info("SNP T->A: " + secondTA);
			log.info("SNP T->C: " + secondTC);
			log.info("SNP T->G: " + secondTG);
			
			secondSNPFrequenciesByType = new double[12];
			secondSNPFrequenciesByType[0] = secondAC * 100d / totBases;
			secondSNPFrequenciesByType[1] = secondAG * 100d / totBases;
			secondSNPFrequenciesByType[2] = secondAT * 100d / totBases;
			secondSNPFrequenciesByType[3] = secondCA * 100d / totBases;
			secondSNPFrequenciesByType[4] = secondCG * 100d / totBases;
			secondSNPFrequenciesByType[5] = secondCT * 100d / totBases;
			secondSNPFrequenciesByType[6] = secondGA * 100d / totBases;
			secondSNPFrequenciesByType[7] = secondGC * 100d / totBases;
			secondSNPFrequenciesByType[8] = secondGT * 100d / totBases;
			secondSNPFrequenciesByType[9] = secondTA * 100d / totBases;
			secondSNPFrequenciesByType[10] = secondTC * 100d / totBases;
			secondSNPFrequenciesByType[11] = secondTG * 100d / totBases;
			
			for(int i=0; i< secondSNPFrequenciesByType.length; i++) {
				if(secondMaxX < secondSNPFrequenciesByType[i]) 
					secondMaxX = secondSNPFrequenciesByType[i];
			}
			
			String title = String.format("First Read SNP frequencies by Type ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new HorizontalBarGraph(snpTypeNames, firstSNPFrequenciesByType, title, 0d, firstMaxX+firstMaxX*0.1d));
			
			String title2 = "Second Read SNP frequencies by Type";
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new HorizontalBarGraph(snpTypeNames, secondSNPFrequenciesByType, title2, 0d, secondMaxX+secondMaxX*0.1d));
			
		} else {
			resultsPanel.setLayout(new GridLayout(1,1));
			String title = String.format("Read SNP frequencies by Type ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
			// add 10% to the top for improving the visualisation of the plot.
			resultsPanel.add(new HorizontalBarGraph(snpTypeNames, firstSNPFrequenciesByType, title, 0d, firstMaxX+firstMaxX*0.1d));			
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
		if(firstMaxX+secondMaxX> ModuleConfig.getParam("variant_call_position_snp_by_type_threshold", "error").doubleValue())
			return true;		
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		if(firstMaxX+secondMaxX > ModuleConfig.getParam("variant_call_position_snp_by_type_threshold", "warn").doubleValue())
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
		super.writeDefaultImage(report, "snp_frequencies_by_type.png", "SNP Frequencies by Type", 800, 600);
	}
	
}
