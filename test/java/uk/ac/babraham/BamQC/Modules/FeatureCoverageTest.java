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
import uk.ac.babraham.BamQC.Modules.FeatureCoverage;



public class FeatureCoverageTest {
	
	private static Logger log = Logger.getLogger(FeatureCoverageTest.class);
	
	private List<SAMRecord> samRecords = null;
	private AnnotationSet annotationSet = null;
	private FeatureCoverage featureCoverage = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : FeatureCoverageTest");	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : FeatureCoverageTest");
	}

	@Before
	public void setUp() throws Exception {	
		featureCoverage = new FeatureCoverage();
		annotationSet = new AnnotationSet();
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		featureCoverage = null;
	}

	@Test
	public void testFeatureCoverage() {
		log.info("testFeatureCoverage");
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

		
		
		String[] featureNames = featureCoverage.getFeatureNames();
		double[] readCounts = featureCoverage.getReadCounts();
		
//		for(int i=0; i<featureNames.length; i++) {
//			System.out.println(featureNames[i]);
//			System.out.println(readCounts[i]);
//		}
		
		assertEquals(7, featureNames.length);		
		assertEquals("gene_ensembl_havana", featureNames[3]);
		assertEquals(3L, (long)readCounts[3]);
		assertEquals("CDS_ensembl_havana", featureNames[6]);
		assertEquals(1L, (long)readCounts[6]);
		
	}

}
