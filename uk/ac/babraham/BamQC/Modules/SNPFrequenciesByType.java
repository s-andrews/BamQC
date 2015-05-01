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
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	// data fields for plotting
	private static String[] snpTypeNames = {
		"A->C", "A->G", "A->T",
		"C->A", "C->G", "C->T", 
		"G->A", "G->C", "G->T", 
		"T->A", "T->C", "T->G"};
	private float[] snpFrequenciesByType = new float[12];
	
	
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
			String title = String.format("SNP frequencies by Type ( SNPs: 0 (0.000 %) )");
			return new HorizontalBarGraph(snpTypeNames, new float[12], title, 0);
		}		
		
		
		log.info("SNP A->C: " + variantCallDetection.getA2C());
		log.info("SNP A->G: " + variantCallDetection.getA2G());
		log.info("SNP A->T: " + variantCallDetection.getA2T());
		log.info("SNP C->A: " + variantCallDetection.getC2A());
		log.info("SNP C->G: " + variantCallDetection.getC2G());
		log.info("SNP C->T: " + variantCallDetection.getC2T());
		log.info("SNP G->A: " + variantCallDetection.getG2A());
		log.info("SNP G->C: " + variantCallDetection.getG2C());
		log.info("SNP G->T: " + variantCallDetection.getG2T());
		log.info("SNP T->A: " + variantCallDetection.getT2A());
		log.info("SNP T->C: " + variantCallDetection.getT2C());
		log.info("SNP T->G: " + variantCallDetection.getT2G());
		
		
		long totalMutations = variantCallDetection.getTotalMutations(), 
			 totalMatches = variantCallDetection.getTotalMatches();
		snpFrequenciesByType = new float[12];
		snpFrequenciesByType[0] = variantCallDetection.getA2C() * 1.0f;
		snpFrequenciesByType[1] = variantCallDetection.getA2G() * 1.0f;
		snpFrequenciesByType[2] = variantCallDetection.getA2T() * 1.0f;
		snpFrequenciesByType[3] = variantCallDetection.getC2A() * 1.0f;
		snpFrequenciesByType[4] = variantCallDetection.getC2G() * 1.0f;
		snpFrequenciesByType[5] = variantCallDetection.getC2T() * 1.0f;
		snpFrequenciesByType[6] = variantCallDetection.getG2A() * 1.0f;
		snpFrequenciesByType[7] = variantCallDetection.getG2C() * 1.0f;
		snpFrequenciesByType[8] = variantCallDetection.getG2T() * 1.0f;
		snpFrequenciesByType[9] = variantCallDetection.getT2A() * 1.0f;
		snpFrequenciesByType[10] = variantCallDetection.getT2C() * 1.0f;
		snpFrequenciesByType[11] = variantCallDetection.getT2G() * 1.0f;
		String title = String.format("SNP frequencies by Type ( SNPs: %d , %.3f %% )", totalMutations, (((double) totalMutations / (totalMutations+totalMatches)) * 100.0));		
		return new HorizontalBarGraph(snpTypeNames, snpFrequenciesByType, title, totalMutations);
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
		super.writeDefaultImage(report, "snp_frequencies_by_type.png", "SNP Frequencies by Type", 800, 600);
	}
	
}
