package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.BarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class InsertDistribution extends AbstractQCModule {

	public final static int MAX_INSERT_SIZE = 5000;
	public final static int BIN_SIZE = 25;
	public final static double CHI_SQUARED_ERROR = 6.635; // 0.75 0.102, 0.455,
															// 0.05 3.841
	public final static double CHI_SQUARED_WARN = 0.455; // 0.95 0.00393, 0.9
															// 0.0157, 0.01
															// 6.635, 0.5 0.455

	private static Logger log = Logger.getLogger(InsertDistribution.class);

	private List<Long> distribution = new ArrayList<Long>();
	private long aboveMaxInsertSizeCount = 0;
	private long unpairedReads = 0;
	private long reads = 0;
	
	private double chiSquared = 0.0;
	private boolean chiSquaredCalculated = false;

	public InsertDistribution() {}

	@Override
	public void processSequence(SAMRecord read) {
		int inferredInsertSize = Math.abs(read.getInferredInsertSize());

		reads++;

		if (read.getReadPairedFlag() && read.getProperPairFlag()) {
			if (inferredInsertSize > MAX_INSERT_SIZE) {
				log.info("inferredInsertSize = " + inferredInsertSize);
				aboveMaxInsertSizeCount++;
			}
			else {
				if (inferredInsertSize >= distribution.size()) {
					for (long i = distribution.size(); i < inferredInsertSize; i++) {
						distribution.add(0L);
					}
					distribution.add(1L);
				}
				else {
					long existingValue = distribution.get(inferredInsertSize);

					distribution.set(inferredInsertSize, ++existingValue);
				}
			}
		}
		else {
			unpairedReads++;
		}
	}

	@Override
	public void processFile(SequenceFile file) {
		// Method called but not needed
	}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}

	private String[] buildLabels(int binNumber) {
		String[] label = new String[binNumber];

		label[binNumber - 1] = "N";

		for (int i = 0; i < (label.length - 1); i++) {
			label[i] = Integer.toString(i * 10);
		}
		return label;
	}

	private double percent(long value, long total) {
		return ((double) value / total) * 100.0;
	}

	@Override
	public JPanel getResultsPanel() {
		log.info("Number of inferred insert sizes above the maximum allowed = " + aboveMaxInsertSizeCount);
		log.info("Number of unpaired reads = " + unpairedReads);

		// +2 = fraction and exceeding max values N
		int binNumber = (distribution.size() / BIN_SIZE) + 2;
		double[] distributionDouble = new double[binNumber];
		long total = aboveMaxInsertSizeCount;

		for (long count : distribution) {
			total += count;
		}
		distributionDouble[binNumber - 1] = percent(aboveMaxInsertSizeCount, total);

		for (int i = 0; i < distribution.size(); i++) {
			int index = (i / BIN_SIZE);

			distributionDouble[index] += percent(distribution.get(i), total);
		}
		double maxPercent = 0.0;

		for (double percent : distributionDouble)
			if (percent > maxPercent) maxPercent = percent;

		String title = String.format("Paired read insert size Distribution, a %d bp max size and %.3f %% unpaired reads", MAX_INSERT_SIZE, (((double) unpairedReads / reads) * 100.0));
		String[] label = buildLabels(binNumber);

		return new BarGraph(distributionDouble, 0.0D, maxPercent, "Infered Insert Size bp", label, title);
	}

	@Override
	public String name() {
		return "Insert Distribution";
	}

	@Override
	public String description() {
		return "Distribution of the read insert size";
	}

	@Override
	public void reset() {
		distribution = new ArrayList<Long>();
		aboveMaxInsertSizeCount = 0;
		chiSquaredCalculated = false;
	}

//	private List<Long> removeLeadingZeroes(List<Long> list) {
//		List<Long> processedList = new ArrayList<Long>();
//		boolean process = false;
//		
//		for (long value : list) {
//			if (process) {
//				processedList.add(value);
//			}
//			else if (value > 0) {
//				process = true;
//			}
//		}
//		return processedList;
//	}

	private double calculateMean() {
		double total = 0.0;
		long i = 0;

		for (long count : distribution) {
			total += (double) count;
			i++;
		}
		return total / i;
	}

	private double calculateStandardDeviation(double mean) {
		double totalDeviation = 0.0;
		long i = 0;

		for (long count : distribution) {
			double deviation = (double) count - mean;

			totalDeviation += deviation * deviation;

			i++;
		}
		return Math.sqrt(totalDeviation / i);
	}

	private double calculateChiSquared() {
		if (!chiSquaredCalculated) {
			double mean = calculateMean();
			double standardDeviation = calculateStandardDeviation(mean);
			long insertSize = 0;
			double sqrt2pi = Math.sqrt( 2 * Math.PI);
			
			chiSquared = 0.0;

			for (long count : distribution) {
				//double expected = normalDistribution.density(insertSize);
				double differenceOverSd = (insertSize - mean) / standardDeviation;
				double expected = Math.exp( -0.5 * differenceOverSd * differenceOverSd ) /  (standardDeviation * sqrt2pi);
				double difference = expected - count;
				double divergeFraction = (difference * difference) / expected;

				log.info(String.format("count %d, insertSize %d, expected = %f", count, insertSize, expected));

				chiSquared += divergeFraction;
				insertSize++;
				// log.info(insertSize + " count = " + count +
				// " divergeFraction = " + divergeFraction + " " + chiSquared);
			}
			chiSquaredCalculated = true;

			log.info(String.format("mean = %f, sd = %f", mean, standardDeviation));

			log.info("chiSquared = " + chiSquared);
		}
		return chiSquared;
	}

	@Override
	public boolean raisesError() {
		return calculateChiSquared() > CHI_SQUARED_ERROR;
	}

	@Override
	public boolean raisesWarning() {
		return calculateChiSquared() > CHI_SQUARED_WARN;
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
		return distribution.size() == 0;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		String title = String.format("Paired read insert size Distribution (Max %d bp), %d unpaired reads ", MAX_INSERT_SIZE, unpairedReads);

		super.writeDefaultImage(report, "InsertDistribution.png", title, 800, 600); // TODO
	}

	public List<Long> getDistribution() {
		return distribution;
	}

	public long getUnpairedReads() {
		return unpairedReads;
	}

}
