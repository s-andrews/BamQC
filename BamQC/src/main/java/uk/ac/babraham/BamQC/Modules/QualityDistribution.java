package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class QualityDistribution extends AbstractQCModule {

	private static Logger log = Logger.getLogger(QualityDistribution.class);
	
	private final static int QUALITY_MAP_SIZE = 5;
	
	private int[] distribution = new int[QUALITY_MAP_SIZE];
	private String[] label = new String[QUALITY_MAP_SIZE];
	
	public QualityDistribution(){
		for (int i = 0; i < QUALITY_MAP_SIZE; i++) {
			label[i] = Integer.toString(i);
		}
	}
	
	@Override
	public void processSequence(SAMRecord read) {
		int quality = read.getMappingQuality();
		
		log.info("quality = " + quality);
		
		distribution[quality]++;
	}

	@Override
	public void processFile(SequenceFile file) { }

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}

	@Override
	public JPanel getResultsPanel() {
		float[] distributionFloat = getDistributionFolat();
		
		return new HorizontalBarGraph(label, distributionFloat, "Quality Map Distribution");
	}

	@Override
	public String name() {
		return "Quality Mapping Distribution";
	}

	@Override
	public String description() {
		return "Quality Mapping Distribution";
	}

	@Override
	public void reset() {
		distribution = new int[QUALITY_MAP_SIZE];
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

	public int[] getDistribution() {
		return distribution;
	}
	
	public float[] getDistributionFolat() {
		float[] distributionFloat = new float[QUALITY_MAP_SIZE];
		
		for (int i = 0; i < QUALITY_MAP_SIZE; i++) {
			distributionFloat[i] = distribution[i];
		}
		return distributionFloat;
	}

}
