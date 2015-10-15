/**
 * Copyright Copyright 2015 Piero Dalle Pezze
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



public class ChromosomeDensityTest {
	
	private static Logger log = Logger.getLogger(ChromosomeDensityTest.class);
	
	private List<SAMRecord> samRecords = null;
	private AnnotationSet annotationSet = null;
	private ChromosomeReadDensity chromosomeReadDensity = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : ChromosomeReadDensityTest");	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : ChromosomeReadDensityTest");
	}

	@Before
	public void setUp() throws Exception {	
		chromosomeReadDensity = new ChromosomeReadDensity();
		annotationSet = new AnnotationSet();
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		chromosomeReadDensity = null;
	}

	@Test
	public void testChromosomeDensity() {
		log.info("testChromosomeReadDensity");

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
			System.out.println("Annotation not parsed correctly!! Sort it out please..!");
			return;
		}


		for(SAMRecord read : samRecords) {
			annotationSet.processSequenceNoCache(read);
			chromosomeReadDensity.processSequence(read);
		}
		
		chromosomeReadDensity.processAnnotationSet(annotationSet);

		
		
		String[] chromosomeNames = chromosomeReadDensity.getChromosomeNames();
		double[] logReadNumber = chromosomeReadDensity.getLogReadNumber();
		double[] logChromosomeLength = chromosomeReadDensity.getLogChromosomeLength();
		
//		for(int i=0; i<chromosomeNames.length; i++) {
//			System.out.println(chromosomeNames[i]);
//			System.out.println(logReadNumber[i]);
//			System.out.println(logChromosomeLength[i]);
//		}
		
		assertEquals(4, chromosomeNames.length);		
		assertEquals("1", chromosomeNames[3]);
		assertEquals(2.4, logReadNumber[3], 0.01);
		assertEquals(18.41, logChromosomeLength[3], 0.01);
		
	}

}