package test.java.uk.ac.babraham.BamQC.Modules;

import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;

public class TestObjectFactory {

	//private static Logger log = Logger.getLogger(TestObjectFactory.class);

	private SAMFileHeader samFileHeader;
	private SAMRecord samRecord1, samRecord2, samRecord3;
	private List<SAMRecord> samRecords = new ArrayList<SAMRecord>();

	public TestObjectFactory() {
		samFileHeader = getInstance();

		samRecord1 = new SAMRecord(samFileHeader);
		samRecord2 = new SAMRecord(samFileHeader);
		samRecord3 = new SAMRecord(samFileHeader);

		for (SAMRecord samRecord : new SAMRecord[] { samRecord1, samRecord2, samRecord3 }) {
			samRecord.setReferenceIndex(0);
		}
		samRecord1.setMappingQuality(0);
		samRecord2.setMappingQuality(255);
		samRecord3.setMappingQuality(10);

		samRecord1.setFlags(1027);
		samRecord2.setFlags(513);
		samRecord3.setFlags(1026);

		samRecord1.setAlignmentStart(0);
		samRecord2.setAlignmentStart(500);
		samRecord3.setAlignmentStart(1500);
		
		samRecord1.setCigar(buildCigar(901));
		samRecord2.setCigar(buildCigar(1601));
		samRecord3.setCigar(buildCigar(1001));
		
		samRecords.add(samRecord1);
		samRecords.add(samRecord2);
		samRecords.add(samRecord3);
	}

	public List<SAMRecord> getSamRecords() {
		return samRecords;
	}

	private SAMFileHeader getInstance() {
		String sequenceName = "sequence1";
		SAMFileHeader samFileHeader = new SAMFileHeader();
		SAMSequenceRecord samSequenceRecord = new SAMSequenceRecord(sequenceName, 2500);

		samFileHeader.addSequence(samSequenceRecord);

		return samFileHeader;
	}

	private Cigar buildCigar(int length) {
		CigarOperator cigarOperator = CigarOperator.M;
		CigarElement cigarElement = new CigarElement(length, cigarOperator);
		List<CigarElement> cigarElements = new ArrayList<CigarElement>();

		cigarElements.add(cigarElement);

		Cigar cigar = new Cigar(cigarElements);

		return cigar;
	}

}
