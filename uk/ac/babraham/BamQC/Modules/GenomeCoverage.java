package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class GenomeCoverage extends AbstractQCModule {

	private static final int NUCLEOTIDES_BIN = 1000;
	
	private static Logger log = Logger.getLogger(GenomeCoverage.class);
	
	private List<float[]> coverage = new ArrayList<float[]>();
	
	
	private float[] getNewReadReferenceCoverage(int referenceIndex, SAMFileHeader header) {
		SAMSequenceRecord samSequenceRecord = header.getSequence(referenceIndex);
		int referenceSequenceLength = samSequenceRecord.getSequenceLength();
		int coverageLength = (referenceSequenceLength / NUCLEOTIDES_BIN);
		
		if (referenceSequenceLength % NUCLEOTIDES_BIN != 0) coverageLength++;
		
		return new float[coverageLength];
	}
	
	private float[] getReadReferenceCoverage(int referenceIndex, SAMFileHeader header) {
		float[] readReferenceCoverage = null;
		
		if (referenceIndex < coverage.size()) {
			readReferenceCoverage = coverage.get(referenceIndex);
		}
		else {
			readReferenceCoverage = getNewReadReferenceCoverage(referenceIndex, header);
			
			if (referenceIndex >= coverage.size()) {
				for (int i = coverage.size(); i <= referenceIndex; i++) {
					coverage.add(null);
				}
			}
			coverage.set(referenceIndex, readReferenceCoverage);
		}
		return readReferenceCoverage;
	}
	
	private void recordCoverage(long alignmentStart, long alignmentEnd, float[] readReferenceCoverage) {
		int startIndex = (int) alignmentStart / NUCLEOTIDES_BIN;
		int endIndex = (int) alignmentEnd / NUCLEOTIDES_BIN;
		int index = startIndex;
		
		while (index <= endIndex) {
			long binStart = index * NUCLEOTIDES_BIN;
			long binEnd = (index + 1) * NUCLEOTIDES_BIN;
			long start = alignmentStart > binStart? alignmentStart : binStart;
			long end = alignmentEnd > binEnd ? binEnd : alignmentEnd;
			float length = (float) (end - start);
			float binCoverage = length / NUCLEOTIDES_BIN;
			
			readReferenceCoverage[index] += binCoverage;
		
			log.info(String.format("Start %d - End %d, index %d, binCoverage %f, ", alignmentStart, alignmentEnd, index, binCoverage, readReferenceCoverage[index]));
			
			index++;
		}
	}
	
	@Override
	public void processSequence(SAMRecord read) {
		SAMFileHeader header = read.getHeader();
		int referenceIndex = read.getReferenceIndex();
		long alignmentStart = read.getAlignmentStart();
		long alignmentEnd = read.getAlignmentEnd();
		float[] readReferenceCoverage = getReadReferenceCoverage(referenceIndex, header); 
		
		recordCoverage(alignmentStart, alignmentEnd, readReferenceCoverage);
		
		log.info("header = " + header);
		log.info("referenceIndex = " + referenceIndex);
	}

	@Override
	public void processFile(SequenceFile file) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
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
		return false ;
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub
	}

	public List<float[]> getCoverage() {
		return coverage;
	}
	
}
