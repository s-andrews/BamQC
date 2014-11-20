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

import uk.ac.babraham.BamQC.Modules.MappingQualityDistribution;

public class MappingQualityDistributionTest {

	private static Logger log = Logger.getLogger(MappingQualityDistributionTest.class);
	
	private MappingQualityDistribution qualityDistribution;
	private TestObjectFactory testObjectFactory;
	private List<SAMRecord> samRecords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
		
		qualityDistribution = new MappingQualityDistribution();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testDistribution() {
		log.info("testDistribution");
		for (SAMRecord samRecord : samRecords) {
			qualityDistribution.processSequence(samRecord);
		}
		int[] distribution = qualityDistribution.getDistribution();
		
		assertEquals(1, distribution[0]);
		assertEquals(1, distribution[255]);
		assertEquals(1, distribution[10]);
		assertEquals(1, qualityDistribution.getMaxCount());
		
		for (int i = 1; i < 256; i++) {
			if (i != 10 && i != 255) {
				assertEquals(0, distribution[i]);
			}
		}
		for (SAMRecord samRecord : samRecords) {
			qualityDistribution.processSequence(samRecord);
		}
		assertEquals(2, distribution[0]);
		assertEquals(2, distribution[255]);
		assertEquals(2, distribution[10]);
		assertEquals(2, qualityDistribution.getMaxCount());
		
		for (int i = 1; i < 256; i++) {
			if (i != 10 && i != 255) {
				assertEquals(0, distribution[i]);
			}
		}
		double[] distributionFloat = qualityDistribution.getDistributionDouble();
		
		assertEquals(33.333333, distributionFloat[0], 0.001);
		assertEquals(33.333333, distributionFloat[255], 0.001);
		assertEquals(33.333333, distributionFloat[10], 0.001);
		
		// test fraction 
		assertEquals(.33333d, qualityDistribution.getFraction(), 0.0001);
		
		// test reset
		qualityDistribution.reset();
		
		distribution = qualityDistribution.getDistribution();
		
		assertEquals(0, qualityDistribution.getMaxCount());
		for (int i = 0; i < 256; i++) {
			assertEquals(0, distribution[i]);
		}
		distributionFloat = qualityDistribution.getDistributionDouble();
		assertEquals(0, distributionFloat.length);
		
		assertEquals(0.0d, qualityDistribution.getFraction(), 0.0001);
	}
}
