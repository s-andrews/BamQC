/**
 * Copyright Copyright 2014 Bart Ailey Eagle Genomics Ltd
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

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Annotation.Chromosome;
import uk.ac.babraham.BamQC.Graphs.LineGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class GenomeCoverage extends AbstractQCModule {

	private static final int PLOT_BINS_PER_CHROMOSOME = 100;

	private static Logger log = Logger.getLogger(GenomeCoverage.class);
	private static double binCoverageZeroWarningFraction = ModuleConfig.getParam("binCoverageZeroFraction", "warn");
	private static double binCoverageZeroErrorFraction = ModuleConfig.getParam("binCoverageZeroFraction", "error");
	private static double binCoverageRsdWarningFraction = ModuleConfig.getParam("binCoverageRsdFraction", "warn");
	private static double binCoverageRsdErrorFraction = ModuleConfig.getParam("binCoverageRsdFraction", "error");

	
	private String [] chromosomeNames;
	private double [][] binCounts;
	private String [] binNames;
	
	private boolean raiseError = false;
	private boolean raiseWarning = false;
	private double maxCoverage = 0.0;
	private int errorReads = 0;
	private int readNumber = 0;

	
	public void processSequence(SAMRecord read) {
	}

	public void processFile(SequenceFile file) {
		log.info("processFile called");
	}

	@Override
	public String name() {
		return "Genome Coverage";
	}

	@Override
	public String description() {
		return "Genome Coverage";
	}

	@Override
	public void reset() {
		raiseError = false;
		raiseWarning = false;
		errorReads = 0;
		readNumber = 0;
	}

	@Override
	public boolean raisesError() {
		return raiseError;
	}

	@Override
	public boolean raisesWarning() {
		return raiseWarning;
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
		return false;
	}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {

		Chromosome [] chromosomes = annotation.chromosomeFactory().getAllChromosomes();
	
		chromosomeNames = new String [chromosomes.length];
		binCounts = new double[chromosomes.length][];

		// We'll plot everything on the same scale, which means we'll reduce everything to a 
		// common scale.  Our limit is going to be that we'll put 200 points on the longest
		// chromsome
		
		int maxBins = 0;
		
		for (int c=0;c<chromosomes.length;c++) {
			if (chromosomes[c].getBinCountData().length>maxBins) maxBins = chromosomes[c].getBinCountData().length;
		}
		
		int binsToUse = PLOT_BINS_PER_CHROMOSOME;
		
		double binRatio = maxBins/(double)(PLOT_BINS_PER_CHROMOSOME+1);
		
		if (maxBins<PLOT_BINS_PER_CHROMOSOME) {
			binRatio = 1;
			binsToUse = maxBins;
		}
		
		for (int c=0;c<chromosomes.length;c++) {
			chromosomeNames[c] = chromosomes[c].name();
			long [] coverage = chromosomes[c].getBinCountData();
			binCounts[c] = new double[binsToUse];
			
			int [] replicateCounts = new int[binsToUse];
						
			for (int i=0;i<coverage.length;i++) {
				
				int thisIndex = (int)(i/binRatio);
				
//				System.err.println("Plot bin from "+i+" is "+thisIndex);
				
				if (thisIndex>=binsToUse) thisIndex = binsToUse-1;
				
				++replicateCounts[thisIndex];
				
				if (coverage[i] == 0) {
					binCounts[c][thisIndex] += 0;
				}
				else {
					binCounts[c][thisIndex] += coverage[i];
				}
			}
			
			// Now average the replicates
			
			for (int i=0;i<replicateCounts.length;i++) {
				if (replicateCounts[i]>0) {
					binCounts[c][i] /= replicateCounts[i];
				}
			
				if (binCounts[c][i] > maxCoverage) maxCoverage = binCounts[c][i];
			}
		}
	}

	public JPanel getResultsPanel() {
		
		int maxBins = 0;
		for (int i=0;i<binCounts.length;i++) {
			if (binCounts[i].length > maxBins) maxBins = binCounts[i].length;
		}
		
		String [] labels = new String[maxBins];
		for (int i=0;i<maxBins;i++) {
			labels[i] = ""+(i*Chromosome.COVERAGE_BIN_SIZE);
		}
		
		return new LineGraph(binCounts, 0, maxCoverage, "Genome Position", chromosomeNames, labels, "Genome Coverage");		
		
	}

	private void raiseWarningErrorsZeroCoverage(int zeroCoverageBins) {
//		double zeroCoverageBinFraction = (double) zeroCoverageBins / BIN_NUMBER;
//
//		log.info(String.format("zeroCoverageBins %d, zeroCoverageBinFraction %f", zeroCoverageBins, zeroCoverageBinFraction));
//
//		if (zeroCoverageBinFraction >= binCoverageZeroErrorFraction) {
//			raiseError = true;
//		}
//		else if (zeroCoverageBinFraction >= binCoverageZeroWarningFraction) {
//			raiseWarning = true;
//		}
	}

	private void raiseWarningErrorsStandardDeviation() {
//		double total = 0.0;
//
//		for (double binCoverage : coverage) {
//			total += binCoverage;
//		}
//		double mean = total / coverage.length;
//		double variance = 0.0;
//
//		for (double binCoverage : coverage) {
//			variance += Math.pow((binCoverage - mean), 2.0);
//		}
//		double rsdFraction = Math.sqrt((variance / coverage.length)) / mean;
//		log.info("rsdFraction = " + rsdFraction);
//
//		if (rsdFraction >= binCoverageRsdErrorFraction) {
//			raiseError = true;
//		}
//		else if (rsdFraction >= binCoverageRsdWarningFraction) {
//			raiseWarning = true;
//		}
	}

	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		String title = "Reference Sequence(s) Coverage"; //String.format("");
		
		super.writeDefaultImage(report, "GenomeCoverage.png", title, 800, 600);  // TODO
	}
	
//	public double[] getCoverage() {
//		return coverage;
//	}

}
