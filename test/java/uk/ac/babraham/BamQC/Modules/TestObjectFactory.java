/**
 * Copyright Copyright 2014 Bart Ailey Eagle Genomics Ltd
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
		
		samRecord1.setBaseQualities(convertToByte(new int[]{5, 1, 4})); // avg 3
		samRecord2.setBaseQualities(convertToByte(new int[]{5, 0, 5})); // avg 3
		samRecord3.setBaseQualities(convertToByte(new int[]{1,20,2,3,4,5}));// avg 6
		
		samRecord1.setInferredInsertSize(3);
		samRecord2.setInferredInsertSize(-1);
		samRecord3.setInferredInsertSize(3);
		
		samRecords.add(samRecord1);
		samRecords.add(samRecord2);
		samRecords.add(samRecord3);
	}

	
	private byte[] convertToByte(int[] numbers) {
		byte[] converted = new byte[numbers.length];
		int i = 0;
		
		for (int number : numbers) {
			converted[i++] = (byte) number;
		}
		return converted;
	}
	
	public List<SAMRecord> getSamRecords() {
		return samRecords;
	}

	public static SAMFileHeader getInstance() {
		String sequenceName = "sequence1";
		SAMFileHeader samFileHeader = new SAMFileHeader();
		SAMSequenceRecord samSequenceRecord = new SAMSequenceRecord(sequenceName, 3000);

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
