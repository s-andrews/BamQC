package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class QualityDistribution extends AbstractQCModule {

	private static Logger log = Logger.getLogger(QualityDistribution.class);
	
	private int[] distribution = new int[256];
	private int[] label = new int[256];
	
	public QualityDistribution(){
		for (int i = 0; i < 256; i++) {
			label[i] = i;
		}
	}
	
	@Override
	public void processSequence(SAMRecord read) {
		int quality = read.getMappingQuality();
		
		log.info("quality = " + quality);
		
		distribution[quality]++;
	}

	@Override
	public void processFile(SequenceFile file) {
		// TODO Auto-generated method stub
	}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		// TODO Auto-generated method stub
	}

	@Override
	public JPanel getResultsPanel() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

	}

	@Override
	public boolean raisesError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean raisesWarning() {
		// TODO Auto-generated method stub
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

}
