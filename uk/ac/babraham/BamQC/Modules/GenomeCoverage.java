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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.LineGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class GenomeCoverage extends AbstractQCModule {

	private static final int BIN_NUMBER = 2000;

	private static Logger log = Logger.getLogger(GenomeCoverage.class);
	private static double binCoverageZeroWarningFraction = ModuleConfig.getParam("binCoverageZeroFraction", "warn");
	private static double binCoverageZeroErrorFraction = ModuleConfig.getParam("binCoverageZeroFraction", "error");
	private static double binCoverageRsdWarningFraction = ModuleConfig.getParam("binCoverageRsdFraction", "warn");
	private static double binCoverageRsdErrorFraction = ModuleConfig.getParam("binCoverageRsdFraction", "error");

	private boolean raiseError = false;
	private boolean raiseWarning = false;
	private long binNucleotides = 50000;
	private List<Long> sequenceStarts = new ArrayList<Long>();
	private double[] coverage = new double[BIN_NUMBER];
	private double maxCoverage = 0.0;
	private boolean isBinNucleotidesSet = false;
	private int errorReads = 0;
	private int readNumber = 0;

	
	public void setBinNucleotides(long binNucleotides, long[] sequenceStarts) {
		this.binNucleotides = binNucleotides;

		for (long sequenceStart : sequenceStarts) {
			this.sequenceStarts.add(sequenceStart);
		}
		isBinNucleotidesSet = true;
	}

	private void setBinNucleotides(SAMSequenceDictionary samSequenceDictionary) {
		List<SAMSequenceRecord> samSequenceRecords = samSequenceDictionary.getSequences();
		long totalNucleotideNumber = 0;

		for (SAMSequenceRecord samSequenceRecord : samSequenceRecords) {
			sequenceStarts.add(totalNucleotideNumber);

			totalNucleotideNumber += samSequenceRecord.getSequenceLength();

			log.debug(String.format("%s sequence length = %d, total = %d", samSequenceRecord.getSequenceName(), samSequenceRecord.getSequenceLength(), totalNucleotideNumber));
		}
		binNucleotides = (int) (totalNucleotideNumber / BIN_NUMBER);

		log.info(String.format("%d / %d = %d", totalNucleotideNumber, BIN_NUMBER, binNucleotides));

		isBinNucleotidesSet = true;
	}

	private void recordCoverage(long alignmentStartAbsolute, long alignmentEndAbsolute) {
		int startIndex = (int) (alignmentStartAbsolute / binNucleotides);
		int endIndex = (int) (alignmentEndAbsolute / binNucleotides);
		int index = startIndex;

		log.debug(String.format("startIndex %d endIndex %d", startIndex, endIndex));

		while (index <= endIndex) {
			long binStart = index * binNucleotides;
			long binEnd = (index + 1) * binNucleotides;
			long start = alignmentStartAbsolute > binStart ? alignmentStartAbsolute : binStart;
			long end = alignmentEndAbsolute > binEnd ? binEnd : alignmentEndAbsolute;
			double length = end - start;

			log.debug(String.format("binStart %d binEnd %d, start = %d, end %d, length = %d", binStart, binEnd, start, end, (end - start)));
			log.debug("index = " + index);

			double binCoverage = length / (double) binNucleotides;

			coverage[index] += binCoverage;

			if (coverage[index] > maxCoverage) maxCoverage = coverage[index];

			log.debug(String.format("Start %d - End %d, length %d, index %d, binCoverage %f, ", alignmentStartAbsolute, alignmentEndAbsolute, (end - start), index, binCoverage, coverage[index]));

			if (binCoverage < 0.0) throw new RuntimeException("negative binCoverage");

			index++;
		}
	}

	@Override
	public void processSequence(SAMRecord read) {
		SAMFileHeader header = read.getHeader();
		SAMSequenceDictionary samSequenceDictionary = header.getSequenceDictionary();
		int referenceIndex = read.getReferenceIndex();
		long alignmentStart = read.getAlignmentStart();
		long alignmentEnd = read.getAlignmentEnd();

		readNumber++;

		if (referenceIndex > -1) {
			if (!isBinNucleotidesSet) {
				setBinNucleotides(samSequenceDictionary);
			}
			if (alignmentEnd > alignmentStart) {
				long referenceStart = sequenceStarts.get(referenceIndex);
				long alignmentStartAbsolute = alignmentStart + referenceStart;
				long alignmentEndAbsolute = alignmentEnd + referenceStart;

				recordCoverage(alignmentStartAbsolute, alignmentEndAbsolute);
			}
			else {
				errorReads++;
			}
		}
		log.debug("header = " + header);
		log.debug("referenceIndex = " + referenceIndex);
	}

	@Override
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
		isBinNucleotidesSet = false;
		sequenceStarts = new ArrayList<Long>();
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
		return true;
	}

	@Override
	public boolean needsToSeeAnnotation() {
		return false;
	}

	@Override
	public boolean ignoreInReport() {
		return false;
	}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}

	@Override
	public JPanel getResultsPanel() {

		if (errorReads > 0) log.error(String.format("%d (%7.3f %%) reads in error is from %d reads , end > start", errorReads, (((double) errorReads / readNumber) * 100.0), readNumber));

		double[][] coverageData = getCoverageData();
		double minY = 0.0D;
		double maxY = maxCoverage;
		String xLabel = String.format("%d bins with a size of %7.3E nucleotides", BIN_NUMBER, (double) binNucleotides);
		String[] xTitles = new String[] { "" };
		int[] xCategories = new int[coverageData[0].length];
		String graphTitle = "Reference Sequence(s) Coverage";

		for (int i = 0; i < coverageData.length; i++) {
			xCategories[i] = i;
		}
		log.info("maxCoverage = " + maxCoverage);
		// log.info("xCategories.length = " + xCategories.length);

		return new LineGraph(coverageData, minY, maxY, xLabel, xTitles, xCategories, graphTitle);
	}

	private void raiseWarningErrorsZeroCoverage(int zeroCoverageBins) {
		double zeroCoverageBinFraction = (double) zeroCoverageBins / BIN_NUMBER;

		log.info(String.format("zeroCoverageBins %d, zeroCoverageBinFraction %f", zeroCoverageBins, zeroCoverageBinFraction));

		if (zeroCoverageBinFraction >= binCoverageZeroErrorFraction) {
			raiseError = true;
		}
		else if (zeroCoverageBinFraction >= binCoverageZeroWarningFraction) {
			raiseWarning = true;
		}
	}

	private void raiseWarningErrorsStandardDeviation() {
		double total = 0.0;

		for (double binCoverage : coverage) {
			total += binCoverage;
		}
		double mean = total / coverage.length;
		double variance = 0.0;

		for (double binCoverage : coverage) {
			variance += Math.pow((binCoverage - mean), 2.0);
		}
		double rsdFraction = Math.sqrt((variance / coverage.length)) / mean;
		log.info("rsdFraction = " + rsdFraction);

		if (rsdFraction >= binCoverageRsdErrorFraction) {
			raiseError = true;
		}
		else if (rsdFraction >= binCoverageRsdWarningFraction) {
			raiseWarning = true;
		}
	}

	private double[][] getCoverageData() {
		List<Double> data = new ArrayList<Double>();
		int zeroCoverageBins = 0;

		for (double binCoverage : coverage) {
			log.debug("binCoverage = " + binCoverage);

			if (binCoverage == 0) zeroCoverageBins++;

			data.add(binCoverage);
		}
		raiseWarningErrorsZeroCoverage(zeroCoverageBins);
		raiseWarningErrorsStandardDeviation();

		double[][] coverageData = new double[1][data.size()];
		int i = 0;

		for (double binCoverage : data) {
			coverageData[0][i++] = binCoverage;
		}
		return coverageData;
	}
	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		String title = "Reference Sequence(s) Coverage"; //String.format("");
		
		super.writeDefaultImage(report, "GenomeCoverage.png", title, 800, 600);  // TODO
	}
	
	public double[] getCoverage() {
		return coverage;
	}

}
