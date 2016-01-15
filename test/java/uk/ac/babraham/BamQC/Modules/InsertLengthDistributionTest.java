/**
 * Copyright Copyright 2015 Simon Andrews
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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Added printouts, testBooleans
 * - Bart Ailey: Class creation.
 */
package test.java.uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.InsertLengthDistribution;

/**
 * 
 * @author Bart Ailey
 * @author Piero Dalle Pezze
 *
 */
public class InsertLengthDistributionTest {

	private static Logger log = Logger.getLogger(InsertLengthDistributionTest.class);

	private InsertLengthDistribution insertLengthDistribution = null;
	private TestObjectFactory testObjectFactory = null;
	private List<SAMRecord> samRecords = null;

	@Before
	public void setUp() throws Exception {
		insertLengthDistribution = new InsertLengthDistribution();
	}

	@After
	public void tearDown() throws Exception {
		testObjectFactory = null;
		samRecords = null;
		insertLengthDistribution = null;
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testProcessSequence() {
		System.out.println("Running test InsertLengthDistributionTest.testProcessSequence");
		log.info("Running test InsertLengthDistributionTest.testProcessSequence");

		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
		
		for (SAMRecord samRecord : samRecords) {
			insertLengthDistribution.processSequence(samRecord);
		}
		ArrayList<Long> insertLengthCounts = insertLengthDistribution.getInsertLengthCounts();

		// distribution now takes account of negative values
		assertEquals(0, (long) insertLengthCounts.get(0));
		assertEquals(0, (long) insertLengthCounts.get(1));
		assertEquals(0, (long) insertLengthCounts.get(2));
		assertEquals(1, (long) insertLengthCounts.get(3));

		assertEquals(2, insertLengthDistribution.getUnpairedReads());

		assertTrue(insertLengthDistribution.raisesWarning());
		assertTrue(insertLengthDistribution.raisesError());

		// throws exception
		insertLengthCounts.get(4);
	}

	@Test
	public void testProcessSequenceRaise() {
		System.out.println("Running test InsertLengthDistributionTest.testProcessSequenceRaise");
		log.info("Running test InsertLengthDistributionTest.testProcessSequenceRaise");

		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
		
		samRecords.get(0).setInferredInsertSize(7);
		samRecords.get(1).setInferredInsertSize(3);
		samRecords.get(2).setInferredInsertSize(1);

		for (SAMRecord samRecord : samRecords) {
			insertLengthDistribution.processSequence(samRecord);
		}
		assertTrue(insertLengthDistribution.raisesError());
		assertTrue(insertLengthDistribution.raisesWarning());
	}

	@Test
	public void testProcessSequenceRaiseCalculation() {

		System.out.println("Running test InsertLengthDistributionTest.testProcessSequenceRaiseCalculation");
		log.info("Running test InsertLengthDistributionTest.testProcessSequenceRaiseCalculation");
			
		List<Long> insertSizes = UtilityTest.readInsertSizesLong();
		int index = 0;
		SAMFileHeader samFileHeader = TestObjectFactory.getInstance();
		SAMRecord samRecord = new SAMRecord(samFileHeader);

		samRecord.setProperPairFlag(true);
		samRecord.setReadPairedFlag(true);

		for (long count : insertSizes) {
			for (int i = 0; i < count; i++) {
				samRecord.setInferredInsertSize(index);
				
				insertLengthDistribution.processSequence(samRecord);
			}
			index++;
		}
		assertFalse(insertLengthDistribution.raisesError());
		assertFalse(insertLengthDistribution.raisesWarning());
	}

	@Test
	public void testBooleans() {
		System.out.println("Running test InsertLengthDistributionTest.testBooleans");	
		log.info("Running test InsertLengthDistributionTest.testBooleans");
		
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
		samRecords.get(0).setInferredInsertSize(7);
		samRecords.get(1).setInferredInsertSize(3);
		samRecords.get(2).setInferredInsertSize(1);
		for (SAMRecord samRecord : samRecords) {
			insertLengthDistribution.processSequence(samRecord);
		}
		
		assertFalse(insertLengthDistribution.ignoreInReport());
		assertFalse(insertLengthDistribution.needsToSeeAnnotation());
		assertTrue(insertLengthDistribution.raisesError());
		assertTrue(insertLengthDistribution.raisesWarning());
		assertTrue(insertLengthDistribution.needsToSeeSequences());
	}
	
}
