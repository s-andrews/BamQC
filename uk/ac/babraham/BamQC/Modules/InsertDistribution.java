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

	private static Logger log = Logger.getLogger(InsertDistribution.class);

	private List<Integer> distribution = new ArrayList<Integer>();
	private long negativeInsertSize = 0;
	
	
	public InsertDistribution() {}

	@Override
	public void processSequence(SAMRecord read) {
		int inferredInsertSize = read.getInferredInsertSize();

		if (inferredInsertSize >= 0) {
			if (inferredInsertSize >= distribution.size()) {
				for (int i = distribution.size(); i < inferredInsertSize; i++) {
					distribution.add(0);
				}
				distribution.add(1);
			}
			else {
				int existingValue = distribution.get(inferredInsertSize);

				distribution.set(inferredInsertSize, ++existingValue);
			}
		}
		else {
			negativeInsertSize++;
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

	@Override
	public JPanel getResultsPanel() {
		log.info("Number of inferred insert sizes with a negative value = " + negativeInsertSize);
		
		String[] label = new String[distribution.size()];
		
		for (int i = 0; i < label.length; i++) {
			label[i] = Integer.toString(i);
		}
		double[] distributionDouble = new double[distribution.size()];
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
		
		return new BarGraph(distributionDouble, 0.0D, maxVaule, "Infered Insert Size", label, "Insert Size Distribution");
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
		distribution = new ArrayList<Integer>();
		negativeInsertSize = 0;
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
	
	public List<Integer> getDistribution() {
		return distribution;
	}

}
