package test.java.uk.ac.babraham.BamQC.Modules;

import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;

public class TestObjectFactory {

	
	private SAMFileHeader samFileHeader;
	private SAMRecord samRecord1, samRecord2, samRecord3;
	private List<SAMRecord> samRecords = new ArrayList<SAMRecord>();
	
	public TestObjectFactory() {
		samFileHeader = new SAMFileHeader();

		samRecord1 = new SAMRecord(samFileHeader);
		samRecord2 = new SAMRecord(samFileHeader);
		samRecord3 = new SAMRecord(samFileHeader);
		
		samRecord1.setMappingQuality(0);
		samRecord2.setMappingQuality(255);
		samRecord3.setMappingQuality(10);
		
		samRecord1.setFlags(1027);
		samRecord2.setFlags(513);
		samRecord3.setFlags(1026);
		
		samRecords.add(samRecord1);
		samRecords.add(samRecord2);
		samRecords.add(samRecord3);
	}

	public List<SAMRecord> getSamRecords() {
		return samRecords;
	}
	
	

}
