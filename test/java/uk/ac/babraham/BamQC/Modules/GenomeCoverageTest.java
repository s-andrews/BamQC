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
 * - Piero Dalle Pezze: added printouts, testBooleans, adapted to the new GenomeCoverage module.
 * - Bart Ailey: Class creation.
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
	private List<SAMRecord> samRecords = null;
	
	@Before
	public void setUp() throws Exception {
		genomeCoverage = new GenomeCoverage();
		annotationSet = new AnnotationSet();
		
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
	}

	@After
	public void tearDown() throws Exception {
		genomeCoverage = null;
		annotationSet = null;
		samRecords = null;
	}

	@Test
	public void testGenomeCoverage() {
		System.out.println("Running test GenomeCoverageTest.testGenomeCoverage");	
		log.info("Running test GenomeCoverageTest.testGenomeCoverage");

		String[] chromosomeNames = genomeCoverage.getChromosomeNames();
		long[] coverage = genomeCoverage.getCoverage();	
		
//		for(int i=0; i < chromosomeNames.length; i++) {
//			System.out.println(chromosomeNames[i]);
//		}
//		for(int i=0; i < coverage.length; i++) {
//			System.out.println(coverage[i]);
//		}
		
		assertEquals(2, chromosomeNames.length);		
		assertEquals("13", chromosomeNames[0]);
		assertEquals("6", chromosomeNames[1]);

		assertEquals(20, coverage.length);
		assertEquals(0, coverage[0]);
		assertEquals(0, coverage[1]);
		assertEquals(0, coverage[2]);
		assertEquals(0, coverage[3]);
		assertEquals(0, coverage[4]);
		assertEquals(0, coverage[5]);
		assertEquals(0, coverage[6]);
		assertEquals(0, coverage[7]);
		assertEquals(0, coverage[8]);
		assertEquals(0, coverage[9]);
		assertEquals(0, coverage[10]);
		assertEquals(0, coverage[11]);
		assertEquals(0, coverage[12]);
		assertEquals(0, coverage[13]);
		assertEquals(0, coverage[14]);
		assertEquals(0, coverage[15]);
		assertEquals(0, coverage[16]);
		assertEquals(0, coverage[17]);
		assertEquals(0, coverage[18]);
		assertEquals(2, coverage[19]);
	}
	
	@Test
	public void testBooleans() {
		System.out.println("Running test GenomeCoverageTest.testBooleans");	
		log.info("Running test GenomeCoverageTest.testBooleans");
		
		assertFalse(genomeCoverage.ignoreInReport());
		assertTrue(genomeCoverage.needsToSeeAnnotation());
		assertFalse(genomeCoverage.raisesError());
		assertFalse(genomeCoverage.raisesWarning());
		assertFalse(genomeCoverage.needsToSeeSequences());
	}

}
