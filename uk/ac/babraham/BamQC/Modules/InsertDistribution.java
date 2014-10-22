package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.BarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class InsertDistribution extends AbstractQCModule {

	public final static int MAX_INSERT_SIZE = 1000;
	public final static int BIN_SIZE = 10;

	private static Logger log = Logger.getLogger(InsertDistribution.class);

	private List<Long> distribution = new ArrayList<Long>();
	private long negativeInsertSizeCount = 0;
	private long aboveMaxInsertSizeCount = 0;
	private long unpairedReads = 0;

	public InsertDistribution() {}

	@Override
	public void processSequence(SAMRecord read) {
		int inferredInsertSize = Math.abs(read.getInferredInsertSize());

		if (read.getReadPairedFlag() && read.getProperPairFlag()) {

			// if (inferredInsertSize < 0) {
			// negativeInsertSizeCount++;
			// log.info("inferredInsertSize = " + inferredInsertSize);
			// }
			// else
			if (inferredInsertSize > MAX_INSERT_SIZE) {
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

		label[0] = "-ve";
		label[binNumber - 1] = "N";

		for (int i = 1; i < (label.length - 1); i++) {
			label[i] = Integer.toString((i - 1) * 10);
		}
		return label;
	}

	private double percent(long value, long total) {
		return ((double) value / total) * 100.0;
	}

	@Override
	public JPanel getResultsPanel() {
		log.info("Number of inferred insert sizes with a negative value = " + negativeInsertSizeCount);
		log.info("Number of inferred insert sizes above the maximum allowed = " + aboveMaxInsertSizeCount);
		log.info("Number of unpaired reads = " + unpairedReads);
		// +3 = fraction, negative and exceeding max values N
		int binNumber = (distribution.size() / BIN_SIZE) + 3;
		String[] label = buildLabels(binNumber);
		double[] distributionDouble = new double[binNumber];
		long maxCount = negativeInsertSizeCount > aboveMaxInsertSizeCount ? negativeInsertSizeCount : aboveMaxInsertSizeCount;
		long total = negativeInsertSizeCount + aboveMaxInsertSizeCount;

		for (long count : distribution) {
			if (count > maxCount) maxCount = count;
			total += count;
		}
		int lastIndexBin = binNumber - 1;

		distributionDouble[0] = percent(negativeInsertSizeCount, total);
		distributionDouble[lastIndexBin] = percent(aboveMaxInsertSizeCount, total);

		for (int i = 0; i < distribution.size(); i++) {
			// + 1 allow for negative value at zero.
			int index = (i / BIN_SIZE) + 1; 

			distributionDouble[index] += percent(distribution.get(i), total);
		}
		double maxVaule = percent(maxCount, total);

		return new BarGraph(distributionDouble, 0.0D, maxVaule, "Infered Insert Size bp", label, "Insert Size Distribution (Max size " + MAX_INSERT_SIZE + " bp)");
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
		negativeInsertSizeCount = 0;
		aboveMaxInsertSizeCount = 0;
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
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub
	}

	public List<Long> getDistribution() {
		return distribution;
	}

	public long getNegativeInsertSizeCount() {
		return negativeInsertSizeCount;
	}

}
