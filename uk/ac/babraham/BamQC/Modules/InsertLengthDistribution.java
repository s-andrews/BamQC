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
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.BarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;




public class InsertLengthDistribution extends AbstractQCModule {

	public final static int MAX_INSERT_SIZE = 5000;
	public final static int BIN_SIZE = 25;
	public final static double PERCENTAGE_DEVIATION_ERROR = ModuleConfig.getParam("InsertLengthDistribution_percentage_deviation", "error"); 
	public final static double PERCENTAGE_DEVIATION_WARN = ModuleConfig.getParam("InsertLengthDistribution_percentage_deviation", "warn"); 
	
	private static Logger log = Logger.getLogger(InsertLengthDistribution.class);

	private ArrayList<Long> insertLengthCounts = new ArrayList<Long>();
	private double[] distributionDouble = null;
	private long aboveMaxInsertLengthCount = 0;
	private double [] graphCounts = null;
	private String [] xCategories = new String[0];
	private double max = 0;
	private boolean calculated = false;
	
	private long unpairedReads = 0;
	private long reads = 0;
	
	private double percentageDeviation = 0.0;
	private boolean percentageDeviationCalculated = false;

	public InsertLengthDistribution() {}

	
	@Override
	public void processFile(SequenceFile file) { }

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}	
	
	@Override
	public void processSequence(SAMRecord read) {

		int inferredInsertSize = Math.abs(read.getInferredInsertSize());

		reads++;

		if (read.getReadPairedFlag() && read.getProperPairFlag()) {
			if (inferredInsertSize > MAX_INSERT_SIZE) {
				log.debug("inferredInsertSize = " + inferredInsertSize);
				aboveMaxInsertLengthCount++;
			}
			else {
				if (inferredInsertSize >= insertLengthCounts.size()) {
					for (long i = insertLengthCounts.size(); i < inferredInsertSize; i++) {
						insertLengthCounts.add(0L);
					}
					insertLengthCounts.add(1L);
				}
				else {
					long existingValue = insertLengthCounts.get(inferredInsertSize);

					insertLengthCounts.set(inferredInsertSize, ++existingValue);
				}
			}
		}
		else {
			unpairedReads++;
		}
	}
	
	
	
	private double percent(long value, long total) {
		return ((double) value / total) * 100.0;
	}
	
	private void prepareDistribution() {
		// +2 = fraction and exceeding max values N
		int binNumber = (insertLengthCounts.size() / BIN_SIZE) + 2;
		distributionDouble = new double[binNumber];
		long total = aboveMaxInsertLengthCount;
		
		for (long count : insertLengthCounts) {
			total += count;
		}
		distributionDouble[binNumber - 1] = percent(aboveMaxInsertLengthCount, total);

		for (int i = 0; i < insertLengthCounts.size(); i++) {
			int index = (i / BIN_SIZE);
			distributionDouble[index] += percent(insertLengthCounts.get(i), total);
		}
	}
	
	private int [] getSizeDistribution(int min, int max) {
		int base = 1;
		while (base > (max-min)) {
			base /= 10;
		}
		int interval;
		int starting;
		int [] divisions = new int [] {1,2,5};
		OUTER: while (true) {
			for (int d=0;d<divisions.length;d++) {
				int tester = base * divisions[d];
				if (((max-min) / tester) <= 50) {
					interval = tester;
					break OUTER;
				}
			}
			base *=10;
		}
		// Now we work out the first value to be plotted
		int basicDivision = min/interval;	
		int testStart = basicDivision * interval;	
		starting = testStart;
		return new int[] {starting,interval};
		
	}	
	
	private void calculateDistribution() {
		int maxLen = 0;
		int minLen = -1;
		max = 0;
		
		prepareDistribution();	
		
		// Find the min and max lengths		
		for (int i=0;i<distributionDouble.length;i++) {
			if (distributionDouble[i] > 0.0d) {
				if (minLen < 0) {
					minLen = i;
				}
				maxLen = i;
			}
		}
		
		// We put one extra category either side of the actual size
		if (minLen>0) minLen--;
		maxLen++;
		
		int [] startAndInterval = getSizeDistribution(minLen, maxLen);
				
		// Work out how many categories we need
		int categories = 0;
		int currentValue = startAndInterval[0];
		while (currentValue<= maxLen) {
			++categories;
			currentValue+= startAndInterval[1];
		}
		
		graphCounts = new double[categories];
		xCategories = new String[categories];
		
		for (int i=0;i<graphCounts.length;i++) {
			
			int minValue = startAndInterval[0]+(startAndInterval[1]*i);
			int maxValue = (startAndInterval[0]+(startAndInterval[1]*(i+1)))-1;

			if (maxValue > maxLen) {
				maxValue = maxLen;
			}
			
			for (int bp=minValue;bp<=maxValue;bp++) {
				if (bp < distributionDouble.length) {
					graphCounts[i] += distributionDouble[bp];
				}
			}
			
			if (startAndInterval[1] == 1) {
				xCategories[i] = ""+Integer.toString(minValue * 10);
			}
			else {
				xCategories[i] = Integer.toString(minValue * 10)+"-"+Integer.toString(maxValue * 10);
			}
			if (graphCounts[i] > max) max = graphCounts[i];
		}
	}
	
	
	@Override
	public JPanel getResultsPanel() {
		log.info("Number of inferred insert sizes above the maximum allowed = " + aboveMaxInsertLengthCount);
		log.info("Number of unpaired reads = " + unpairedReads);
		
		if (!calculated) calculateDistribution();		

		String title = String.format("Paired read insert length distrib, a %d bp max size and %.3f %% unpaired reads", MAX_INSERT_SIZE, (((double) unpairedReads / reads) * 100.0));
		return new BarGraph(graphCounts, 0.0d, max, "Inferred Insert Length bp", xCategories, title);
	}
	
	
	
	@Override
	public String name() {
		return "Insert Length Distribution";
	}

	@Override
	public String description() {
		return "Distribution of the read insert length";
	}

	@Override
	public void reset() {
		insertLengthCounts = new ArrayList<Long>();
		aboveMaxInsertLengthCount = 0;
		percentageDeviationCalculated = false;
		percentageDeviation = 0.0;
	}

	private double calculatePercentageDeviation() {
		if (!percentageDeviationCalculated) {
			List<Double> distributionDouble = new ArrayList<Double>();
			
			for (long count : insertLengthCounts) {
				distributionDouble.add((double) count);
			}
			NormalDistributionModeler normalDistributionModeler = new NormalDistributionModeler();
			
			normalDistributionModeler.setDistribution(distributionDouble);
			normalDistributionModeler.calculateDistribution();
			percentageDeviation = normalDistributionModeler.getDeviationPercent();
			
			if (Double.isNaN(percentageDeviation)) percentageDeviation = 100.0;
			
			log.info("percentageDeviation = " + percentageDeviation);
			
			percentageDeviationCalculated = true;
		}
		return percentageDeviation;
	}

	@Override
	public boolean raisesError() {
		return calculatePercentageDeviation() > PERCENTAGE_DEVIATION_ERROR;
	}

	@Override
	public boolean raisesWarning() {
		return calculatePercentageDeviation() > PERCENTAGE_DEVIATION_WARN;
	}

	@Override
	public boolean needsToSeeSequences() {
		return true;
	}

	@Override
	public boolean needsToSeeAnnotation() {
		return false;
	}

	@Override
	public boolean ignoreInReport() {
		return insertLengthCounts.size() == 0;
	}

	private String[] buildLabels(int binNumber) {
		String[] label = new String[binNumber];
		for (int i = 0; i < label.length; i++) {
			label[i] = Integer.toString(i * 10);
		}
		return label;
	}
	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		String title = String.format("Paired read insert length Distribution (Max %d bp), %d unpaired reads ", MAX_INSERT_SIZE, unpairedReads);
		super.writeDefaultImage(report, "InsertLengthDistribution.png", title, 800, 600);
		
		if(insertLengthCounts == null) { return; }
		
		int binNumber = (insertLengthCounts.size() / BIN_SIZE) + 2;
		String[] label = buildLabels(binNumber);
		
		StringBuffer sb = report.dataDocument();
		sb.append("InferredInsertLength(bp)\tPairedReadInsertLengthDistribution\n");
		for (int i=0;i<distributionDouble.length;i++) {
			sb.append(label[i]).append("\t").append(distributionDouble[i]).append("\n");
		}
		
	}

	public ArrayList<Long> getInsertLengthCounts() {
		return insertLengthCounts;
	}

	public long getUnpairedReads() {
		return unpairedReads;
	}

}
