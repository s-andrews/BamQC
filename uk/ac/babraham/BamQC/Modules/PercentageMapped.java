package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class PercentageMapped extends AbstractQCModule {

	private static final int TWO = 0x02;  // Second bit is set
	
	private static Logger log = Logger.getLogger(PercentageMapped.class);
	
	private boolean flagAlignedProperley = false;
	private int readNumber = 0;
	private int alignedReadNumber = 0;
	
	@Override
	public void processSequence(SAMRecord read) {
		int flag = read.getFlags();
		
		flagAlignedProperley = (flag & TWO) == TWO;
		
		readNumber++;
		if (flagAlignedProperley) alignedReadNumber++;
		
		log.info("flag = " + flag);
		log.info("flagAlignedProperley = " + flagAlignedProperley);
	}

	@Override
	public void processFile(SequenceFile file) { }

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}

	@Override
	public JPanel getResultsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		return "Percentage Mapped";
	}

	@Override
	public String description() {
		return "Percentage of reads that are mapped to the reference sequence";
	}

	@Override
	public void reset() {
		readNumber = 0;
		alignedReadNumber = 0;
	}

	@Override
	public boolean raisesError() {
		// TODO Auto-generated method stub
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

	public boolean isFlagAlignedProperley() {
		return flagAlignedProperley;
	}

	public int getReadNumber() {
		return readNumber;
	}

	public int getAlignedReadNumber() {
		return alignedReadNumber;
	}
	
	

}
