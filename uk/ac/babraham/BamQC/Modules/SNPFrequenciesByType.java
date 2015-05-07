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
	
	// original threshold for the plot x axis.
	private double maxX=0.0d; 
	
	// The analysis collecting all the results.
	VariantCallDetection variantCallDetection = null;	
	
	// data fields for plotting
	private static String[] snpTypeNames = {
		"A->C", "A->G", "A->T",
		"C->A", "C->G", "C->T", 
		"G->A", "G->C", "G->T", 
		"T->A", "T->C", "T->G"};
	private double[] snpFrequenciesByType = new double[12];
	
	
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
			String title = String.format("SNP frequencies by Type ( total SNPs: 0 (0.000 %) )");
			return new HorizontalBarGraph(snpTypeNames, new double[12], title, 0, 1);
		}		
		
		
		long ac = variantCallDetection.getA2C(),
			 ag = variantCallDetection.getA2G(),
			 at = variantCallDetection.getA2T(),
			 ca = variantCallDetection.getC2A(),
			 cg = variantCallDetection.getC2G(),
			 ct = variantCallDetection.getC2T(),
			 ga = variantCallDetection.getG2A(),
			 gc = variantCallDetection.getG2C(),
			 gt = variantCallDetection.getG2T(),
			 ta = variantCallDetection.getT2A(),
			 tc = variantCallDetection.getT2C(),
			 tg = variantCallDetection.getT2G();
		
		log.info("SNP A->C: " + ac);
		log.info("SNP A->G: " + ag);
		log.info("SNP A->T: " + at);
		log.info("SNP C->A: " + ca);
		log.info("SNP C->G: " + cg);
		log.info("SNP C->T: " + ct);
		log.info("SNP G->A: " + ga);
		log.info("SNP G->C: " + gc);
		log.info("SNP G->T: " + gt);
		log.info("SNP T->A: " + ta);
		log.info("SNP T->C: " + tc);
		log.info("SNP T->G: " + tg);
		
		
		long totSNPs = variantCallDetection.getTotalMutations(), 
			 totBases = variantCallDetection.getTotal();
		snpFrequenciesByType = new double[12];
		snpFrequenciesByType[0] = ac * 100f / totBases;
		snpFrequenciesByType[1] = ag * 100f / totBases;
		snpFrequenciesByType[2] = at * 100f / totBases;
		snpFrequenciesByType[3] = ca * 100f / totBases;
		snpFrequenciesByType[4] = cg * 100f / totBases;
		snpFrequenciesByType[5] = ct * 100f / totBases;
		snpFrequenciesByType[6] = ga * 100f / totBases;
		snpFrequenciesByType[7] = gc * 100f / totBases;
		snpFrequenciesByType[8] = gt * 100f / totBases;
		snpFrequenciesByType[9] = ta * 100f / totBases;
		snpFrequenciesByType[10] = tc * 100f / totBases;
		snpFrequenciesByType[11] = tg * 100f / totBases;
		
		for(int i=0; i< snpFrequenciesByType.length; i++) {
			if(maxX < snpFrequenciesByType[i]) 
				maxX = snpFrequenciesByType[i];
		}

		String title = String.format("SNP frequencies by Type ( total SNPs: %.3f %% )", totSNPs*100.0f/totBases);
		// add 10% to the top for improving the visualisation of the plot.
		return new HorizontalBarGraph(snpTypeNames, snpFrequenciesByType, title, 0d, maxX+maxX*0.1f);
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
		if(maxX > ModuleConfig.getParam("variant_call_position_snp_by_type_threshold", "error").floatValue())
			return true;		
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		if(maxX > ModuleConfig.getParam("variant_call_position_snp_by_type_threshold", "warn").floatValue())
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
