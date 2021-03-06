/**
 * Copyright Copyright 2014 Simon Andrews
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
 * - Piero Dalle Pezze: Added printout. Added testBooleans.
 * - Bart Ailey: Class creation.
 */
package test.java.uk.ac.babraham.BamQC.Modules;

/**
 * 
 * @author Bart Ailey
 * @author Piero Dalle Pezze
 *
 */
import static org.junit.Assert.*;

import java.util.List;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.RpkmReference;

public class RpkmReferenceTest {

	private static Logger log = Logger.getLogger(RpkmReferenceTest.class);
	
	private RpkmReference rpkmReference = null;
	private TestObjectFactory testObjectFactory = null;
	private List<SAMRecord> samRecords = null;
	
	@Before
	public void setUp() throws Exception {
		rpkmReference = new RpkmReference();
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
	}

	@After
	public void tearDown() throws Exception {
		rpkmReference = null;
		testObjectFactory = null;
		samRecords = null;
	}

	@Test
	public void testProcessSequence() {
		System.out.println("Running test RpkmReferenceTest.testProcessSequence");
		log.info("Running test RpkmReferenceTest.testProcessSequence");
		
		int count = 0;
		for (SAMRecord samRecord : samRecords) {
			rpkmReference.processSequence(samRecord);
			
			double[] coverageReference = rpkmReference.getCoverage();
			
			if (count == 0) {
				assertEquals(9.0E-4, coverageReference[0], 0.0000001);
			}
			count++;
		}
		double[] coverageReference = rpkmReference.getCoverage();
		
		assertEquals(3.5E-3, coverageReference[0], 0.000001);
	}

	@Test
	public void testBooleans() {
		System.out.println("Running test RpkmReferenceTest.testBooleans");	
		log.info("Running test RpkmReferenceTest.testBooleans");
		
		assertTrue(rpkmReference.ignoreInReport());
		assertFalse(rpkmReference.needsToSeeAnnotation());
		assertFalse(rpkmReference.raisesError());
		assertFalse(rpkmReference.raisesWarning());
		assertTrue(rpkmReference.needsToSeeSequences());
	}
	
}
