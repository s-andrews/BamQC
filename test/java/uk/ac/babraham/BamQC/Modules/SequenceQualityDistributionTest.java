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

import static org.junit.Assert.*;

import java.util.List;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.SequenceQualityDistribution;

public class SequenceQualityDistributionTest {

	private static Logger log = Logger.getLogger(SequenceQualityDistributionTest.class);
	
	private SequenceQualityDistribution sequenceQualityDistribution = null;
	private TestObjectFactory testObjectFactory = null;
	private List<SAMRecord> samRecords = null;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : SequenceQualityDistributionTest");	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : SequenceQualityDistributionTest");	
	}

	@Before
	public void setUp() throws Exception {
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
		sequenceQualityDistribution = new SequenceQualityDistribution();
	}

	@After
	public void tearDown() throws Exception {
		testObjectFactory = null;
		samRecords = null;
		sequenceQualityDistribution = null;
	}

	@Test
	public void testProcessSequence() {
		log.info("testDistribution");
		
		for (SAMRecord samRecord : samRecords) {
			sequenceQualityDistribution.processSequence(samRecord);
		}
		List<Integer> distribution = sequenceQualityDistribution.getDistribution();
		
		assertEquals(0, (int) distribution.get(0));
		assertEquals(0, (int) distribution.get(1));
		assertEquals(0, (int) distribution.get(2));
		assertEquals(2, (int) distribution.get(3));
		assertEquals(0, (int) distribution.get(4));
		assertEquals(0, (int) distribution.get(5));
		assertEquals(1, (int) distribution.get(6));
	}

	@Test(expected= IndexOutOfBoundsException.class)
	public void testProcessSequenceException() {
		log.info("testProcessSequenceException");
		
		for (SAMRecord samRecord : samRecords) {
			sequenceQualityDistribution.processSequence(samRecord);
		}
		List<Integer> distribution = sequenceQualityDistribution.getDistribution();
		
		assertEquals(0, (int) distribution.get(7));
	}
	
}