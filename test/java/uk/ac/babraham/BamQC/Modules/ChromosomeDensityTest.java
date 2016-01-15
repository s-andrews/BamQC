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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser;
import uk.ac.babraham.BamQC.AnnotationParsers.GTFAnnotationParser;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Modules.ChromosomeReadDensity;


/**
 * 
 * @author Piero Dalle Pezze
 *
 */
public class ChromosomeDensityTest {
	
	private static Logger log = Logger.getLogger(ChromosomeDensityTest.class);
	
	private List<SAMRecord> samRecords = null;
	private AnnotationSet annotationSet = null;
	private ChromosomeReadDensity chromosomeReadDensity = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {	
		chromosomeReadDensity = new ChromosomeReadDensity();
		annotationSet = new AnnotationSet();
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_annot.sam");
		String annotationFile = new String(new File("").getAbsolutePath() + "/test/resources/example_annot.gtf");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
	
		AnnotationParser parser = new GTFAnnotationParser();
		try {
			parser.parseAnnotation(annotationSet, new File(annotationFile));
		}
		catch (Exception e) {
			log.error("Annotation not parsed correctly!! Sort it out please..!", e);
			return;
		}


		for(SAMRecord read : samRecords) {
			annotationSet.processSequenceNoCache(read);
		}
		
		chromosomeReadDensity.processAnnotationSet(annotationSet);
		
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		chromosomeReadDensity = null;
	}

	@Test
	public void testChromosomeDensity() {
		System.out.println("Running test ChromosomeReadDensityTest.testChromosomeReadDensity");	
		log.info("Running test ChromosomeReadDensityTest.testChromosomeReadDensity");
		
		String[] chromosomeNames = chromosomeReadDensity.getChromosomeNames();
		double[] logReadNumber = chromosomeReadDensity.getLogReadNumber();
		double[] logChromosomeLength = chromosomeReadDensity.getLogChromosomeLength();
		
//		for(int i=0; i<chromosomeNames.length; i++) {
//			System.out.println(chromosomeNames[i]);
//			System.out.println(logReadNumber[i]);
//			System.out.println(logChromosomeLength[i]);
//		}
		
		assertEquals(4, chromosomeNames.length);
		
		assertEquals("3", chromosomeNames[0]);
		assertEquals(0.0, logReadNumber[0], 0.01);
		assertEquals(14.95, logChromosomeLength[0], 0.01);
		
		assertEquals("9", chromosomeNames[1]);
		assertEquals(0.0, logReadNumber[1], 0.01);
		assertEquals(17.38, logChromosomeLength[1], 0.01);
		
		assertEquals("5", chromosomeNames[2]);
		assertEquals(0.0, logReadNumber[2], 0.01);
		assertEquals(18.13, logChromosomeLength[2], 0.01);

		assertEquals("1", chromosomeNames[3]);
		assertEquals(2.4, logReadNumber[3], 0.01);
		assertEquals(18.41, logChromosomeLength[3], 0.01);
		
	}

	@Test
	public void testBooleans() {
		System.out.println("Running test ChromosomeReadDensityTest.testBooleans");	
		log.info("Running test ChromosomeReadDensityTest.testBooleans");
		
		assertFalse(chromosomeReadDensity.ignoreInReport());
		assertTrue(chromosomeReadDensity.needsToSeeAnnotation());
		assertFalse(chromosomeReadDensity.raisesError());
		assertFalse(chromosomeReadDensity.raisesWarning());
		assertFalse(chromosomeReadDensity.needsToSeeSequences());
	}
	
}