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
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.Graphs.LineWithHorizontalBarGraph;
import uk.ac.babraham.BamQC.Graphs.SeparateLineGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Statistics.SimpleStats;
import uk.ac.babraham.BamQC.Modules.ModuleConfig;

public class GenomeCoverage extends AbstractQCModule {

	private int plotTypeChromosomesThreshold = ModuleConfig.getParam("GenomeCoverage_plot_type_chromosomes_threshold", "ignore").intValue();

	private String [] chromosomeNames = null;
	private double [][] binCounts = null;
	private long [] coverage = null;
	private double maxCoverage = 0.0;
	
	private int maxBins = 1;
	


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
		chromosomeNames = null;
		binCounts = null;
		coverage = null;
	}

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
		return true;
	}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {

		Chromosome [] chromosomes = annotation.chromosomeFactory().getAllChromosomes();
		
		if(chromosomes.length <= plotTypeChromosomesThreshold) {
			// This will plot the chromosomes from 1 (top) to n (bottom)
			Arrays.sort(chromosomes, Collections.reverseOrder());
		} else {
			// This will plot the chromosomes from 1 (left) to n (right)
			Arrays.sort(chromosomes);
		}
		
		chromosomeNames = new String [chromosomes.length];
		binCounts = new double[chromosomes.length][];

		// We'll plot everything on the same scale, which means we'll reduce everything to a 
		// common scale.  Our limit is going to be that we'll put 200 points on the longest
		// chromosome
		
		maxBins = 1;
		for (int c=0;c<chromosomes.length;c++) {
			if(chromosomes[c].getBinCountData().length <= 1) {
			} else if (chromosomes[c].getBinCountData().length>maxBins) { 
				maxBins = chromosomes[c].getBinCountData().length;
			}
		}
		
		// configuration of how many bins per chromosome we want to plot.
		// This is the number of bins per chromosome for the official plot getResultsPanel()
		int plotBinsPerChromosome = 0; 
		if(chromosomeNames.length <= plotTypeChromosomesThreshold) {
			plotBinsPerChromosome = ModuleConfig.getParam("GenomeCoverage_plot_bins_per_chromosome", "ignore").intValue();
		} else {
			plotBinsPerChromosome = ModuleConfig.getParam("GenomeCoverage_plot_bins_all_chromosomes", "ignore").intValue() / chromosomes.length;
		}

		
		int binsToUse = plotBinsPerChromosome;
		
		double binRatio = maxBins/(double)plotBinsPerChromosome;
				
		if (maxBins<plotBinsPerChromosome) {
			binRatio = 1;
			binsToUse = maxBins;
		}
				
		for (int c=0;c<chromosomes.length;c++) {
			chromosomeNames[c] = chromosomes[c].name();
//			System.out.println("Chromosome is " + chromosomes[c].name());
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
					if(binCounts[c][i] <= 0) {
						// Let's label these points having null coverage so that we don't miss them
						binCounts[c][i] = Double.NEGATIVE_INFINITY;
						continue;
					} 
					// scale to log to enlarge the data differences. log_e makes it smaller than log_10.
					if (binCounts[c][i] > 0) binCounts[c][i] = Math.log(binCounts[c][i]);
				}
			}				
						
			// Now convert to z-scores
			double [] validValues = new double[firstInvalidBin];
			for (int i=0;i<validValues.length;i++) {
				validValues[i] = binCounts[c][i];				
			}
			// The following two methods remove NaN and Infinite values from the computation.
			double mean = SimpleStats.mean(validValues);
			double sd = SimpleStats.stdev(validValues, mean);
			for (int i=0;i<validValues.length;i++) {
				if(Double.isInfinite(binCounts[c][i])) continue;
				if(sd > 0) binCounts[c][i] = (binCounts[c][i]-mean)/sd;
				if (binCounts[c][i] > maxCoverage) maxCoverage = binCounts[c][i];
				if (0-binCounts[c][i] > maxCoverage) maxCoverage = 0-binCounts[c][i];
				
				// if(binCounts[c][i] < 0) System.out.println(binCounts[c][i]);
			}
			
		}
	}

	@Override
	public JPanel getResultsPanel() {

		for(int i=0; i<chromosomeNames.length; i++) {
			if(chromosomeNames[i].toLowerCase().startsWith("chr")) 
				chromosomeNames[i] = chromosomeNames[i].substring(3);
		}
		
		if(chromosomeNames.length <= plotTypeChromosomesThreshold) {
			// plots the genome coverage for each chromosome separately
			return getSeparateChromosomeResultsPanel();
		}
		// plots the genome coverage lining all chromosomes
		return getAllChromosomeResultsPanel();
	}
		
	
	
	public JPanel getSeparateChromosomeResultsPanel() {
		
		int maxBins = 0;
		for (int i=0;i<binCounts.length;i++) {
			if (binCounts[i].length > maxBins) 
				maxBins = binCounts[i].length;
		}
		
		String [] labels = new String[maxBins];
		for (int i=0;i<maxBins;i++) {
			labels[i] = ""+(i*Chromosome.COVERAGE_BIN_SIZE);
		}
		String title = "Genome Coverage ( red: z-score, blue: region with no coverage )";
		String xLabel = "Genome Position";
		String yLabel = "Chromosomes";
		return new SeparateLineGraph(binCounts, 0-maxCoverage, maxCoverage, xLabel, yLabel, chromosomeNames, labels, title);				
	}
	
	
	public JPanel getAllChromosomeResultsPanel() {	
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
		double[] fullBinCounts = new double[fullBinCountsLength];
		double[] fullBinLengths = new double[binCounts.length];
		int k=0;
		for(int i=0; i<binCounts.length; i++) {
			for(int j=0; j<scaffoldLengths[i]; j++) {
				fullBinCounts[k] = binCounts[i][j];
				k++;
			}
			fullBinLengths[i] = scaffoldLengths[i]*Chromosome.COVERAGE_BIN_SIZE;
		}
		int maxBins = fullBinCounts.length;
		String[] labels = new String[maxBins];
		for(int i=0; i<maxBins; i++) {
			labels[i] = ""+(i*Chromosome.COVERAGE_BIN_SIZE);
		}
		
		
		/* Set up of a stacked row chart representing chromosome coverages. */
		double maxLimit = maxCoverage*(1.5*maxCoverage);
		String title = "Genome Coverage ( red: z-score, blue: region with no coverage )";
		
		
		/* plot the data */
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new javax.swing.BoxLayout(resultsPanel, javax.swing.BoxLayout.PAGE_AXIS));
		resultsPanel.add(new LineWithHorizontalBarGraph(fullBinLengths, fullBinCounts, 0-maxLimit, maxLimit, "Genome Position", chromosomeNames, "", labels, title, "Scaffold ( for name:position, hover the mouse on the red bars )"));
		
		return resultsPanel;
	}
	
	@Override
	public boolean ignoreInReport() {
		if(ModuleConfig.getParam("GenomeCoverage", "ignore") > 0 || chromosomeNames == null || chromosomeNames.length == 0 || maxBins == 1) {
			return true; 
		}
		return false;
	}
	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "genome_coverage.png", "Genome Coverage", 800, 600);

		if(chromosomeNames == null || chromosomeNames.length == 0 || maxBins == 1) { return; }
	
		StringBuffer sb = report.dataDocument();
		sb.append("Chromosome_name\tGenome_position\n");
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

	public String[] getChromosomeNames() {
		return chromosomeNames;
	}
	
	public long[] getCoverage() {
		return coverage;
	}
	
}
