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

import uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser;
import uk.ac.babraham.BamQC.AnnotationParsers.GTFAnnotationParser;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Modules.FeatureCoverage;


/**
 * 
 * @author Piero Dalle Pezze
 *
 */
public class FeatureCoverageTest {
	
	private static Logger log = Logger.getLogger(FeatureCoverageTest.class);
	
	private List<SAMRecord> samRecords = null;
	private AnnotationSet annotationSet = null;
	private FeatureCoverage featureCoverage = null;
	
	private String[] featureNames = null;
	private double[] readCounts = null;
	

	@Before
	public void setUp() throws Exception {	
		featureCoverage = new FeatureCoverage();
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
			System.out.println("Annotation not parsed correctly!! Sort it out please..!");
			return;
		}


		for(SAMRecord read : samRecords) {
			annotationSet.processSequenceNoCache(read);
			featureCoverage.processSequence(read);
		}
		
		featureCoverage.processAnnotationSet(annotationSet);

		featureNames = featureCoverage.getFeatureNames();
		readCounts = featureCoverage.getReadCounts();
		
		// Use a simple BubbleSort to sort featuresNames (and their readCounts)
		// NOTE: for some reason jvm 8 returns the feature names (and their readCounts) 
		// with a different order, and therefore this test fails on that jvm.
	    for (int i = 0; i < featureNames.length; i++) {
	        for (int j = 1; j < featureNames.length - i; j++) {
	          if (featureNames[j-1].compareTo(featureNames[j]) > 0) {
	        	  String tempName = featureNames[j-1];
	        	  featureNames[j-1] = featureNames[j];
	        	  featureNames[j] = tempName;
	        	  double tempCount = readCounts[j-1];
	        	  readCounts[j-1] = readCounts[j];
	        	  readCounts[j] = tempCount;
	          }
	        }
	      }
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		annotationSet = null;
		featureCoverage = null;
		featureNames = null;
		readCounts = null;
	}

	@Test
	public void testFeatureCoverage() {
		System.out.println("Running test FeatureCoverageTest.testFeatureCoverage");
		log.info("Running test FeatureCoverageTest.testFeatureCoverage");
		
//		for(int i=0; i<featureNames.length; i++) {
//			System.out.println(featureNames[i]);
//			System.out.println(readCounts[i]);
//		}
		
		assertEquals(7, featureNames.length);
		
		assertEquals("CDS_ensembl_havana", featureNames[0]);
		assertEquals(1, (long)readCounts[0]);
		
		assertEquals("gene_ensembl", featureNames[1]);
		assertEquals(0L, (long)readCounts[1]);
		
		assertEquals("gene_ensembl_havana", featureNames[2]);
		assertEquals(3L, (long)readCounts[2]);
		
		assertEquals("gene_havana", featureNames[3]);
		assertEquals(1L, (long)readCounts[3]);
		
		assertEquals("transcript_ensembl", featureNames[4]);
		assertEquals(0L, (long)readCounts[4]);
		
		assertEquals("transcript_ensembl_havana", featureNames[5]);
		assertEquals(2L, (long)readCounts[5]);
		
		assertEquals("transcript_havana", featureNames[6]);
		assertEquals(3L, (long)readCounts[6]);
	}
	
	@Test
	public void testBooleans() {
		System.out.println("Running test FeatureCoverageTest.testBooleans");	
		log.info("Running test FeatureCoverageTest.testBooleans");
		
		assertFalse(featureCoverage.ignoreInReport());
		assertTrue(featureCoverage.needsToSeeAnnotation());
		assertFalse(featureCoverage.raisesError());
		assertFalse(featureCoverage.raisesWarning());
		assertFalse(featureCoverage.needsToSeeSequences());
	}

}
