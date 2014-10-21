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

import uk.ac.babraham.BamQC.Modules.GenomeCoverage;

public class GenomeCoverageTest {

	private static Logger log = Logger.getLogger(GenomeCoverageTest.class);
	
	private GenomeCoverage genomeCoverage;
	private TestObjectFactory testObjectFactory;
	private List<SAMRecord> samRecords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		genomeCoverage = new GenomeCoverage();
	
		genomeCoverage.setBinNucleotides(1000, new long[]{0});
		
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testProcessSequence() {
		log.info("testProcessSequence");
		int count = 0;
		for (SAMRecord samRecord : samRecords) {
			genomeCoverage.processSequence(samRecord);
			
			double[] coverageReference = genomeCoverage.getCoverage();
			
			if (count == 0) {
				assertEquals(0.900, coverageReference[0], 0.0000001);
			}
			count++;
		}
		double[] coverageReference = genomeCoverage.getCoverage();
		
		assertEquals(1.4, coverageReference[0], 0.000001);
		assertEquals(1.5, coverageReference[1], 0.000001);
		assertEquals(0.6, coverageReference[2], 0.000001);
		
		genomeCoverage.reset();
		coverageReference = genomeCoverage.getCoverage();
		
		assertEquals(1.4, coverageReference[0], 0.000001);
		assertEquals(1.5, coverageReference[1], 0.000001);
		assertEquals(0.6, coverageReference[2], 0.000001);
	}
	
	@Test
	public void testBooleans() {
		log.info("testBooleans");
		
		assertFalse(genomeCoverage.ignoreInReport());
		assertFalse(genomeCoverage.needsToSeeAnnotation());
		assertFalse(genomeCoverage.raisesError());
		assertFalse(genomeCoverage.raisesWarning());
		
		assertTrue(genomeCoverage.needsToSeeSequences());
	}

}
