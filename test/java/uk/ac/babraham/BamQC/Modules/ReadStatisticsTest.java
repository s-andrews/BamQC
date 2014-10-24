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

import uk.ac.babraham.BamQC.Modules.ReadStatistics;

public class ReadStatisticsTest {
	
	private static Logger log = Logger.getLogger(ReadStatisticsTest.class);
	
	private ReadStatistics readFlagStatistics;
	private TestObjectFactory testObjectFactory;
	private List<SAMRecord> samRecords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		readFlagStatistics = new ReadStatistics();
		
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testProcessSequence() {
		log.info("testProcessSequence");
		
		for (SAMRecord samRecord : samRecords) {
			readFlagStatistics.processSequence(samRecord);
		}
		
		assertEquals(3, readFlagStatistics.getReadNumber());
		assertEquals(2, readFlagStatistics.getPairNumber());
		assertEquals(1, readFlagStatistics.getMappedPairNumber());
		assertEquals(1, readFlagStatistics.getFailedQualityControlNumber());
		assertEquals(2, readFlagStatistics.getDuplicateNumber());
	}

}
