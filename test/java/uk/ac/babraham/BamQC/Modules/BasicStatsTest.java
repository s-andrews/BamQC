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

import uk.ac.babraham.BamQC.Modules.BasicStats;
import uk.ac.babraham.BamQC.Modules.VariantCallDetection;



public class BasicStatsTest {
	
	private static Logger log = Logger.getLogger(BasicStatsTest.class);
	
	private BasicStats basicStats;
	private List<SAMRecord> samRecords = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : BasicStatsTest");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : BasicStatsTest");
	}

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testBasicStats() {
		log.info("testBasicStats");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/test_header.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);
		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as the provided SAM file seems empty");
			return; 
		}
		VariantCallDetection vcd = new VariantCallDetection();
		basicStats = new BasicStats(vcd);
		
		for (SAMRecord read : samRecords) {
			vcd.processSequence(read);
			basicStats.processSequence(read);
		}
	
		//assertEquals(3, basicStats.getReadNumber());
		assertEquals("", basicStats.getFilename());
		assertEquals(true, basicStats.isHeaderParsed());
		assertEquals("ID:bwa VN:0.5.4\nID:GATK TableRecalibration VN:1.0.3471 CL:Covariates=[ReadGroupCovariate, QualityScoreCovariate, CycleCovariate, DinucCovariate, TileCovariate], default_read_group=null, default_platform=null, force_read_group=null, force_platform=null, solid_recal_mode=SET_Q_ZERO, window_size_nqs=5, homopolymer_nback=7, exception_if_no_tile=false, ignore_nocall_colorspace=false, pQ=5, maxQ=40, smoothing=1\n", basicStats.getCommand());
		assertEquals(false, basicStats.isHasAnnotation());
		assertEquals("", basicStats.getAnnotationFile());
		assertEquals(3, basicStats.getActualCount());
		assertEquals(3, basicStats.getPrimaryCount());
		assertEquals(3, basicStats.getPairedCount());
		assertEquals(3, basicStats.getProperPairCount());
		assertEquals(3, basicStats.getMappedCount());
		assertEquals(0, basicStats.getDuplicateCount());
		assertEquals(0, basicStats.getQcFailCount());
		assertEquals(0, basicStats.getSingletonCount());
		assertEquals(0, basicStats.getTotalSplicedReads());
		assertEquals(0, basicStats.getTotalSkippedReads());
		assertEquals(0, basicStats.getVariantCallDetectionTotalReads());
		assertEquals(0, basicStats.getTotalInsertions());
		assertEquals(0, basicStats.getTotalDeletions());
		assertEquals(0, basicStats.getTotalMutations());
		assertEquals(0, basicStats.getTotalBases());
	
	}

}
