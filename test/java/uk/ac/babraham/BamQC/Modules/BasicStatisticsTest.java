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

import uk.ac.babraham.BamQC.Modules.BasicStatistics;
import uk.ac.babraham.BamQC.Modules.VariantCallDetection;


/**
 * 
 * @author Piero Dalle Pezze
 *
 */
public class BasicStatisticsTest {
	
	private static Logger log = Logger.getLogger(BasicStatisticsTest.class);
	
	private List<SAMRecord> samRecords = null;
	private BasicStatistics basicStatistics = null;
	private VariantCallDetection variantCallDetection = null;
	

	@Before
	public void setUp() throws Exception {	
		variantCallDetection = new VariantCallDetection();
		basicStatistics = new BasicStatistics(variantCallDetection);
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/test_header.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}

		for(SAMRecord read : samRecords) {
			variantCallDetection.processSequence(read);
			basicStatistics.processSequence(read);
		}
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		variantCallDetection = null;
		basicStatistics = null;
	}

	@Test
	public void testBasicStats() {
		System.out.println("Running test BasicStatisticsTest.testBasicStatistics");	
		log.info("Running test BasicStatisticsTest.testBasicStatistics");
	
		basicStatistics.getResultsPanel();
		
		assertEquals("", basicStatistics.getFilename());
		assertEquals(true, basicStatistics.isHeaderParsed());
		assertEquals("ID:bwa VN:0.5.4 (header copied manually)\nID:GATK TableRecalibration VN:1.0.3471 CL:Covariates=[ReadGroupCovariate, QualityScoreCovariate, CycleCovariate, DinucCovariate, TileCovariate], default_read_group=null, default_platform=null, force_read_group=null, force_platform=null, solid_recal_mode=SET_Q_ZERO, window_size_nqs=5, homopolymer_nback=7, exception_if_no_tile=false, ignore_nocall_colorspace=false, pQ=5, maxQ=40, smoothing=1\n", basicStatistics.getCommand());
		assertEquals(false, basicStatistics.isHasAnnotation());
		assertEquals("", basicStatistics.getAnnotationFile());
		assertEquals(0, basicStatistics.getFeatureTypeCount());		
		assertEquals(17, basicStatistics.getActualCount());
		assertEquals(17, basicStatistics.getPrimaryCount());
		assertEquals(17, basicStatistics.getPairedCount());
		assertEquals(15, basicStatistics.getProperPairCount());
		assertEquals(2, basicStatistics.getUnmappedCount());
		assertEquals(1, basicStatistics.getDuplicateCount());
		assertEquals(1, basicStatistics.getQcFailCount());
		assertEquals(0, basicStatistics.getSingletonCount());
		assertEquals(1, basicStatistics.getTotalSplicedReads());
		assertEquals(2, basicStatistics.getTotalSkippedReads());
		assertEquals(17, basicStatistics.getVariantCallDetectionTotalReads());
		assertEquals(10, basicStatistics.getTotalInsertions());
		assertEquals(14, basicStatistics.getTotalDeletions());
		assertEquals(22, basicStatistics.getTotalMutations());
		assertEquals(1348, basicStatistics.getTotalBases());
	}
	
	@Test
	public void testBooleans() {
		System.out.println("Running test BasicStatisticsTest.testBooleans");	
		log.info("Running test BasicStatisticsTest.testBooleans");
		
		assertFalse(basicStatistics.ignoreInReport());
		assertTrue(basicStatistics.needsToSeeAnnotation());
		assertFalse(basicStatistics.raisesError());
		assertFalse(basicStatistics.raisesWarning());
		assertTrue(basicStatistics.needsToSeeSequences());
	}

}
