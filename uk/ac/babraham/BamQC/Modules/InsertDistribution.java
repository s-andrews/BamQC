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
	private long aboveMaxInsertSizeCount = 0;
	private long unpairedReads = 0;

	public InsertDistribution() {}

	@Override
	public void processSequence(SAMRecord read) {
		int inferredInsertSize = Math.abs(read.getInferredInsertSize());
		
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
		String[] label = buildLabels(binNumber);
		double[] distributionDouble = new double[binNumber];
		long total = aboveMaxInsertSizeCount;

		for (long count : distribution) total += count;
	
		distributionDouble[binNumber - 1] = percent(aboveMaxInsertSizeCount, total);

		for (int i = 0; i < distribution.size(); i++) {
			int index = (i / BIN_SIZE);

			distributionDouble[index] += percent(distribution.get(i), total);
		}
		double maxPercent = 0.0;
		
		for (double percent : distributionDouble) if (percent > maxPercent) maxPercent = percent;
		
		return new BarGraph(distributionDouble, 0.0D, maxPercent, "Infered Insert Size bp", label, "Insert Size Distribution (Max size " + MAX_INSERT_SIZE + " bp)");
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

	public long getUnpairedReads() {
		return unpairedReads;
	}
	
	

}
