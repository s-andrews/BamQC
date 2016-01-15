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
 * - Piero Dalle Pezze: Class creation.
 */
package test.java.uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.SoftClipDistribution;


/**
 * 
 * @author Piero Dalle Pezze
 *
 */
public class SoftClipDistributionTest {
	
	private static Logger log = Logger.getLogger(SoftClipDistributionTest.class);
	
	private SoftClipDistribution softClipDistribution = null;
	private List<SAMRecord> samRecords = null;
	
	@Before
	public void setUp() throws Exception {
		softClipDistribution = new SoftClipDistribution();
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/test_header.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		
		for (SAMRecord read : samRecords) {
			softClipDistribution.processSequence(read);
		}
	}

	@After
	public void tearDown() throws Exception {
		samRecords = null;
		softClipDistribution = null;
	}

	@Test
	public void testSoftClipDistribution() {
		System.out.println("Running test SoftClipDistributionTest.testSoftClipDistribution");
		log.info("Running test SoftClipDistributionTest.testSoftClipDistribution");
		
		long[] leftClipCounts = softClipDistribution.getLeftClipCounts();
		long[] rightClipCounts = softClipDistribution.getRightClipCounts();
		
//		for(int i=0; i<leftClipCounts.length; i++) {
//			System.out.println(leftClipCounts[i]);
//		}
//		for(int i=0; i<rightClipCounts.length; i++) {
//			System.out.println(rightClipCounts[i]);
//		}
		
		assertEquals(1, leftClipCounts.length);
		assertEquals(15, leftClipCounts[0]);
		assertEquals(15, rightClipCounts[0]);
	}
	
	@Test
	public void testBooleans() {
		System.out.println("Running test SoftClipDistributionTest.testBooleans");	
		log.info("Running test SoftClipDistributionTest.testBooleans");
		
		assertTrue(softClipDistribution.ignoreInReport());
		assertFalse(softClipDistribution.needsToSeeAnnotation());
		assertFalse(softClipDistribution.raisesError());
		assertFalse(softClipDistribution.raisesWarning());
		assertTrue(softClipDistribution.needsToSeeSequences());
	}

}
