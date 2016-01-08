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
/*
 * Changelog: 
 * - Piero Dalle Pezze: added printouts, adapted to the new GenomeCoverage module.
 * - Bart Ailey: Class creation.
 */
package test.java.uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Modules.GenomeCoverage;

/**
 * @author Bart Ailey
 * @author Piero Dalle Pezze
 *
 */
public class GenomeCoverageTest {

	private static Logger log = Logger.getLogger(GenomeCoverageTest.class);
	
	private GenomeCoverage genomeCoverage = null;
	private AnnotationSet annotationSet = null;
//	private TestObjectFactory testObjectFactory = null;
	private List<SAMRecord> samRecords = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : GenomeCoverageTest");	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : GenomeCoverageTest");	
	}

	@Before
	public void setUp() throws Exception {
		genomeCoverage = new GenomeCoverage();
		annotationSet = new AnnotationSet();
//		genomeCoverage.setBinNucleotides(1000, new long[]{0});
//		testObjectFactory = new TestObjectFactory();
//		samRecords = testObjectFactory.getSamRecords();
	}

	@After
	public void tearDown() throws Exception {
		genomeCoverage = null;
		annotationSet = null;
//		testObjectFactory = null;
		samRecords = null;
	}

	@Test
	public void testGenomeCoverage() {
		log.info("testGenomeCoverage");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/genome_coverage.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}

		for(SAMRecord read : samRecords) {
			annotationSet.processSequenceNoCache(read);
		}
		genomeCoverage.processAnnotationSet(annotationSet);

		String[] chromosomeNames = genomeCoverage.getChromosomeNames();
		assertEquals(2, chromosomeNames.length);		
		assertEquals("13", chromosomeNames[0]);
		assertEquals("6", chromosomeNames[1]);

		long[] coverage = genomeCoverage.getCoverage();	
		assertEquals(20, coverage.length);
		assertEquals(0, coverage[0]);
		assertEquals(2, coverage[19]);
		
	}
	
	
//	@Test
//	public void testProcessSequence() {
//		log.info("testProcessSequence");
//		int count = 0;
//		for (SAMRecord samRecord : samRecords) {
//			genomeCoverage.processSequence(samRecord);
//			
//			double[] coverageReference = genomeCoverage.getCoverage();
//			
//			if (count == 0) {
//				assertEquals(0.900, coverageReference[0], 0.0000001);
//			}
//			count++;
//		}
//		double[] coverageReference = genomeCoverage.getCoverage();
//		
//		assertEquals(1.4, coverageReference[0], 0.000001);
//		assertEquals(1.5, coverageReference[1], 0.000001);
//		assertEquals(0.6, coverageReference[2], 0.000001);
//		
//		genomeCoverage.reset();
//		coverageReference = genomeCoverage.getCoverage();
//		
//		assertEquals(1.4, coverageReference[0], 0.000001);
//		assertEquals(1.5, coverageReference[1], 0.000001);
//		assertEquals(0.6, coverageReference[2], 0.000001);
//	}

//	@Test
//	public void testBooleans() {
//		log.info("testBooleans");
//		
//		assertFalse(genomeCoverage.ignoreInReport());
//		assertFalse(genomeCoverage.needsToSeeAnnotation());
//		assertFalse(genomeCoverage.raisesError());
//		assertFalse(genomeCoverage.raisesWarning());
//		
//		assertTrue(genomeCoverage.needsToSeeSequences());
//	}

}
