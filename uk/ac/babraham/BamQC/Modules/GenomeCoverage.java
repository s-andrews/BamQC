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
import uk.ac.babraham.BamQC.Graphs.LineWithHorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Statistics.SimpleStats;

public class GenomeCoverage extends AbstractQCModule {

	private int plotBinsPerChromosome = ModuleConfig.getParam("GenomeCoverage_plot_bins_per_chromosome", "ignore").intValue();

	private static Logger log = Logger.getLogger(GenomeCoverage.class);
	private double genomeCoverageZeroWarningFraction = ModuleConfig.getParam("GenomeCoverage_zero_fraction", "warn");
	private double genomeCoverageZeroErrorFraction = ModuleConfig.getParam("GenomeCoverage_zero_fraction", "error");
	private double genomeCoverageRsdWarningFraction = ModuleConfig.getParam("GenomeCoverage_rsd_fraction", "warn");
	private double genomeCoverageRsdErrorFraction = ModuleConfig.getParam("GenomeCoverage_rsd_fraction", "error");

	Chromosome [] chromosomes = null;
	private String [] chromosomeNames;
	private double [][] binCounts;
	private String [] binNames;
	private long [] coverage = null;
	
	private boolean raiseError = false;
	private boolean raiseWarning = false;
	private double maxCoverage = 0.0;
	private int errorReads = 0;
	private int readNumber = 0;
	


	@Override
	public void processSequence(SAMRecord read) { }

	@Override
	public void processFile(SequenceFile file) { }

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
		chromosomes = null;
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
		if(chromosomes == null || chromosomes.length == 0) { 
			return true; 
		}
		return false;
	}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {

		chromosomes = annotation.chromosomeFactory().getAllChromosomes();	
	
		chromosomeNames = new String [chromosomes.length];
		binCounts = new double[chromosomes.length][];

		// We'll plot everything on the same scale, which means we'll reduce everything to a 
		// common scale.  Our limit is going to be that we'll put 200 points on the longest
		// chromosome
		
		int maxBins = 0;
		
		for (int c=0;c<chromosomes.length;c++) {
			if (chromosomes[c].getBinCountData().length>maxBins) maxBins = chromosomes[c].getBinCountData().length;
		}
		
		int binsToUse = plotBinsPerChromosome;
		
		double binRatio = maxBins/(double)plotBinsPerChromosome;
				
		if (maxBins<plotBinsPerChromosome) {
			binRatio = 1;
			binsToUse = maxBins;
		}
		
		for (int c=0;c<chromosomes.length;c++) {
			chromosomeNames[c] = chromosomes[c].name();
//			System.err.println("Chromosome is "+chromosomes[c].name());
			coverage = chromosomes[c].getBinCountData();
			binCounts[c] = new double[binsToUse];
			
			int [] replicateCounts = new int[binsToUse];
						
			for (int i=0;i<coverage.length;i++) {
				
				int thisIndex = (int)(i/binRatio);
												
				++replicateCounts[thisIndex];
				
				if (coverage[i] > 0) {
					binCounts[c][thisIndex] += coverage[i];
				}
			}
			
			int firstInvalidBin = 1 + (int)((coverage.length-1)/binRatio);
			for (int i=firstInvalidBin;i<binsToUse;i++) {
				binCounts[c][i] = Double.NaN;
			}
			
			// Now average the replicates	
			for (int i=0;i<replicateCounts.length;i++) {
				if (replicateCounts[i]>0) {
					binCounts[c][i] /= replicateCounts[i];
//					if (binCounts[c][i] > 0) binCounts[c][i] = Math.log10(binCounts[c][i]);
				}
			}
			
			// Now convert to z-scores
			double [] validValues = new double[firstInvalidBin];
			for (int i=0;i<validValues.length;i++) {
				validValues[i] = binCounts[c][i];				
			}
			double mean = SimpleStats.mean(validValues);
			double sd = SimpleStats.stdev(validValues, mean);
			for (int i=0;i<validValues.length;i++) {
				binCounts[c][i] = (binCounts[c][i]-mean)/sd;
				
				if (binCounts[c][i] > maxCoverage) maxCoverage = binCounts[c][i];
				if (0-binCounts[c][i] > maxCoverage) maxCoverage = 0-binCounts[c][i];

			}
			
		}
	}

	
//  Old plot showing the genome coverage per chromosome nicely. 
//  possibly this plot should be shown if chromosomes below a certain threshold?
//	@Deprecated
//	public JPanel getResultsPanelOld() {
//		
//		int maxBins = 0;
//		for (int i=0;i<binCounts.length;i++) {
//			if (binCounts[i].length > maxBins) maxBins = binCounts[i].length;
//		}
//		
//		String [] labels = new String[maxBins];
//		for (int i=0;i<maxBins;i++) {
//			labels[i] = ""+(i*Chromosome.COVERAGE_BIN_SIZE);
//		}
//		
//		return new SeparateLineGraph(binCounts, 0-maxCoverage, maxCoverage, "Genome Position", chromosomeNames, labels, "Genome Coverage");		
//		
//	}
	
	
	@Override
	public JPanel getResultsPanel() {
		
		/* Set up for separate line chart representing chromosome coverages. */
		int[] scaffoldLengths = new int[binCounts.length];
		
		int fullBinCountsLength=0;
		for(int i=0; i<binCounts.length; i++) {
			boolean nanFound = false;
			for(scaffoldLengths[i]=0; scaffoldLengths[i]<binCounts[i].length && !nanFound; scaffoldLengths[i]++) {
				if(Double.isNaN(binCounts[i][scaffoldLengths[i]])) {
					nanFound = true;
					scaffoldLengths[i]--;
				}
			}
			fullBinCountsLength = fullBinCountsLength + scaffoldLengths[i];
		}
		double[][] fullBinCounts = new double[1][fullBinCountsLength];
		double[] fullBinLengths = new double[binCounts.length];
		int k=0;
		for(int i=0; i<binCounts.length; i++) {
			for(int j=0; j<scaffoldLengths[i]; j++) {
				fullBinCounts[0][k] = binCounts[i][j];
				k++;
			}
			fullBinLengths[i] = scaffoldLengths[i]*Chromosome.COVERAGE_BIN_SIZE;
		}
		int maxBins = fullBinCounts[0].length;
		String[] labels = new String[maxBins];
		for(int i=0; i<maxBins; i++) {
			labels[i] = ""+(i*Chromosome.COVERAGE_BIN_SIZE);
		}
		
		
		/* Set up for stacked row chart representing chromosome coverages. */
		
		double maxLimit = maxCoverage*(1.5*maxCoverage);
		String title = "Genome Coverage";
		
		
		/* plot the data */
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new javax.swing.BoxLayout(resultsPanel, javax.swing.BoxLayout.PAGE_AXIS));
		resultsPanel.add(new LineWithHorizontalBarGraph(fullBinLengths, fullBinCounts, 0-maxLimit, maxLimit, "Genome Position", new String[]{""}, labels, title));
		
		return resultsPanel;
	}
	

	private void raiseWarningErrorsZeroCoverage(int zeroCoverageBins) {
//		double zeroCoverageBinFraction = (double) zeroCoverageBins / BIN_NUMBER;
//
//		log.info(String.format("zeroCoverageBins %d, zeroCoverageBinFraction %f", zeroCoverageBins, zeroCoverageBinFraction));
//
//		if (zeroCoverageBinFraction >= genomeCoverageZeroErrorFraction) {
//			raiseError = true;
//		}
//		else if (zeroCoverageBinFraction >= genomeCoverageZeroWarningFraction) {
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
//		if (rsdFraction >= genomeCoverageRsdErrorFraction) {
//			raiseError = true;
//		}
//		else if (rsdFraction >= genomeCoverageRsdWarningFraction) {
//			raiseWarning = true;
//		}
	}

	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "GenomeCoverage.png", "Reference Sequence(s) Coverage", 800, 600);

		if(chromosomeNames == null || binCounts == null || binCounts.length == 0) { return; }
	
		StringBuffer sb = report.dataDocument();
		sb.append("ChromosomeName\tGenomePosition\n");
		sb.append("Name\t");
		for (int i=0;i<binCounts[0].length;i++) {
			sb.append(i*Chromosome.COVERAGE_BIN_SIZE).append("\t");			
		}
		sb.append("\n");
		
		for (int i=0;i<chromosomeNames.length;i++) {
			sb.append(chromosomeNames[i]).append("\t");
			for (int j=0;j<binCounts[i].length;j++) {
				sb.append(binCounts[i][j]).append("\t");
			}
			sb.append("\n");
		}
		
	}
	
//	public double[] getCoverage() {
//		return coverage;
//	}

}
