/**
 * Copyright Copyright 2014 Simon Andrews
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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Added y axis label, antialiasing, axes numbers resizing to avoid overlapping, reports.
 * - Bart Ailey: Class creation.
 */
package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.BarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Utilities.CalculateDistribution;

/**
 * @author Bart Ailey
 * @author Piero Dalle Pezze
 *
 */
public class InsertLengthDistribution extends AbstractQCModule {

	public final static int MAX_INSERT_SIZE = ModuleConfig.getParam("InsertLengthDistribution_max_insert_size", "ignore").intValue();
	public final static int BIN_SIZE = ModuleConfig.getParam("InsertLengthDistribution_bin_size", "ignore").intValue();
	public final static double PERCENTAGE_DEVIATION_ERROR = ModuleConfig.getParam("InsertLengthDistribution_percentage_deviation", "error"); 
	public final static double PERCENTAGE_DEVIATION_WARN = ModuleConfig.getParam("InsertLengthDistribution_percentage_deviation", "warn"); 
	
	private static Logger log = Logger.getLogger(InsertLengthDistribution.class);

	private ArrayList<Long> insertLengthCounts = new ArrayList<Long>();
	private double[] distributionDouble = null;
	private double aboveMaxInsertLengthCount = 0L;
	private double [] graphCounts = null;
	private String [] xCategories = null;
	private double max = 0.0d;
	private boolean calculated = false;
	
	private long unpairedReads = 0;
	private long reads = 0;
	
	private double percentageDeviation = 0.0;
	private boolean percentageDeviationCalculated = false;

	public InsertLengthDistribution() {}

	
	@Override
	public void processFile(SequenceFile file) { }

	@Override
	public void processAnnotationSet(AnnotationSet annotation) { }	
	
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
	
	
	@Override
	public JPanel getResultsPanel() {
		log.debug("Number of inferred insert sizes above the maximum allowed = " + aboveMaxInsertLengthCount);
		log.debug("Number of unpaired reads = " + unpairedReads);
		
		if (!calculated) {
			double[] rawCounts = new double[insertLengthCounts.size()];
			for(int i=0; i<rawCounts.length; i++) {
				rawCounts[i] = insertLengthCounts.get(i);
			}
			CalculateDistribution cd = new CalculateDistribution(rawCounts, aboveMaxInsertLengthCount, BIN_SIZE);
			graphCounts = cd.getGraphCounts();
			xCategories = cd.getXCategories();
			max = cd.getMax();
			distributionDouble = cd.getDistributionDouble();
			calculated = true;
		}
				
		String title = String.format("Paired Read Insert Length Distrib ( %d bp max size and %.3f %% unpaired reads )", MAX_INSERT_SIZE, (((double) unpairedReads / reads) * 100.0));
		String xLabel = "Inferred Insert Length bp";
		String yLabel = "Percent of Reads";
		return new BarGraph(graphCounts, 0.0d, max, xLabel, yLabel, xCategories, title);
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
		aboveMaxInsertLengthCount = 0L;
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
			
			log.debug("percentageDeviation = " + percentageDeviation);
			
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
		if(ModuleConfig.getParam("InsertLengthDistribution", "ignore") > 0 || insertLengthCounts == null || insertLengthCounts.size() == 0) {
			return true;
		}
		return false;
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
		String title = String.format("Paired Read Insert Length Distribution ( %d bp max size and %.3f %% unpaired reads )", MAX_INSERT_SIZE, (((double) unpairedReads / reads) * 100.0));
		super.writeDefaultImage(report, "InsertLengthDistribution.png", title, 800, 600);
		
		if(insertLengthCounts == null || insertLengthCounts.size() == 0) { return; }
		
		int binNumber = (insertLengthCounts.size() / BIN_SIZE) + 2;
		String[] label = buildLabels(binNumber);
		
		StringBuffer sb = report.dataDocument();
		sb.append("Inferred_insert_length(bp)\tPaired_read_insert_length_distribution\n");
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
