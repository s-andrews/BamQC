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

public class SequenceQualityDistribution extends AbstractQCModule {

	private static Logger log = Logger.getLogger(SequenceQualityDistribution.class);

	private List<Integer> distribution = new ArrayList<Integer>();
	
	private double[] distributionDouble = null;

	public SequenceQualityDistribution() {}

	private void addAverage(int average) {
		if (average >= distribution.size()) {
			for (int i = distribution.size(); i < average; i++) {
				distribution.add(0);
			}
			distribution.add(1);
		}
		else {
			int existingValue = distribution.get(average);

			distribution.set(average, ++existingValue);
		}
	}

	@Override
	public void processSequence(SAMRecord read) {
		byte[] baseQuality = read.getBaseQualities();
		
		int count = 0;
		int total = 0;
		for (byte quality : baseQuality) {
			total = total + quality;
			//log.debug(String.format("%d) int %d total %d", count, (int) quality, total));
			count++;
		}
		int average = (int) Math.round((double) total / count);

		addAverage(average);
		log.debug("average = " + average);
	}
	
	@Override
	public JPanel getResultsPanel() {
		String[] label = new String[distribution.size()];
		
		for (int i = 0; i < label.length; i++) {
			label[i] = Integer.toString(i);
		}
		distributionDouble = new double[distribution.size()];
		int maxCount = 0;
		int i = 0;
		int total = 0;
		
		for (int count : distribution) {
			if (count > maxCount) maxCount = count;
			total += count;
		}
		for (int count : distribution) {
			distributionDouble[i++] = ( (double) count / total) * 100.0;
		}
		double maxVaule = ( (double) maxCount / total) * 100.0;
		
		return new BarGraph(distributionDouble, 0.0D, maxVaule, "Sequence Quality (Phred)", label, "Sequence Quality Distribution");
	}
	
	@Override
	public void processFile(SequenceFile file) {}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}

	@Override
	public String name() {
		return "Sequence Quality Distribution";
	}

	@Override
	public String description() {
		return "Sequence Quality Distribution";
	}

	@Override
	public void reset() {
		distribution = new ArrayList<Integer>();
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
		super.writeDefaultImage(report, "SequenceQualityDistribution.png", "Sequence Quality Distribution", 800, 600);

		if(distribution == null) { return; }
	
		StringBuffer sb = report.dataDocument();
		sb.append("Sequence Quality (Phred)\tSequence Quality Distribution\n");
		for (int i=0;i<distributionDouble.length;i++) {
			sb.append(i).append("\t").append(distributionDouble[i]).append("\n");
		}
		
	}
	
	public List<Integer> getDistribution() {
		return distribution;
	}
	
}
