package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class ReadFlagStatistics extends AbstractQCModule {

	private static final int FIRST_BIT = 0x01;
	private static final int SECOND_BIT = 0x02;
	private static final int ELEVENTH_BIT = 0x400;

	private static Logger log = Logger.getLogger(ReadFlagStatistics.class);

	private int readNumber = 0;
	private int alignedReadNumber = 0;
	private int duplicateReadNumber = 0;
	private int failedReadNumber = 0;

	@Override
	public void reset() {
		readNumber = 0;
		alignedReadNumber = 0;
		duplicateReadNumber = 0;
		failedReadNumber = 0;
	}

	@Override
	public void processSequence(SAMRecord read) {
		int flag = read.getFlags();
		boolean pairedRead = (flag & FIRST_BIT) == FIRST_BIT;
		boolean flagAlignedProperley = (flag & SECOND_BIT) == SECOND_BIT;
		boolean duplicateRead = (flag & ELEVENTH_BIT) == ELEVENTH_BIT;

		readNumber++;

		// if (pairedRead) {
		if (duplicateRead) duplicateReadNumber++;
		if (flagAlignedProperley) alignedReadNumber++;

		log.info("flag = " + flag);
		log.info("flagAlignedProperley = " + flagAlignedProperley);
		log.info(String.format("percentage = %7.3f %%", getPercentage(alignedReadNumber, readNumber)));
	}
	
	private double getPercentage(int count, int total) {
		return ((double) count / (double) total) * 100.0;
	}

	@Override
	public void processFile(SequenceFile file) {}

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
		return "Read Flag Statistics";
	}

	@Override
	public String description() {
		return "Statistics of the Read's Flag field";
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

	public int getReadNumber() {
		return readNumber;
	}

	public int getAlignedReadNumber() {
		return alignedReadNumber;
	}

	public int getDuplicateReadNumber() {
		return duplicateReadNumber;
	}

}
