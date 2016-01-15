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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.VariantCallDetection;


/**
 * 
 * @author Piero Dalle Pezze
 */
public class VariantCallDetectionTest {

	private static Logger log = Logger.getLogger(VariantCallDetectionTest.class);
	
	private List<SAMRecord> samRecords = null;
	private VariantCallDetection variantCallDetection = null;

	
	private void debugCigarAndMD(SAMRecord samRecord) { 
		// Get the CIGAR list and MD tag string.
		log.debug("CIGAR: " + samRecord.getCigarString());
		log.debug("MDtag: " + samRecord.getStringAttribute("MD"));
		log.debug("--------------");				
	}
	
	@Before
	public void setUp() throws Exception {	
		variantCallDetection = new VariantCallDetection();		
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		variantCallDetection = null;		
	}
	
	@Test
	public void testRecordWithoutMDString() {
		System.out.println("Running test VariantCallDetection.testRecordWithoutMDString");
		log.info("Running test VariantCallDetection.testRecordWithoutMDString");
		
		TestObjectFactory testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();	
		variantCallDetection = new VariantCallDetection();
		
		for(SAMRecord samRecord : samRecords) {
			variantCallDetection.processSequence(samRecord);
		}	
	}
	
	@Test
	public void testCigarOperM() {
		System.out.println("Running test VariantCallDetection.testCigarOperM");
		log.info("Running test VariantCallDetection.testCigarOperM");
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_M.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		List<String> combinedCigarMDtagList = new ArrayList<String>();
		for(SAMRecord samRecord : samRecords) {
			//printCigarAndMD(samRecord);
			variantCallDetection.processSequence(samRecord);
			if(variantCallDetection.getCigarMD() != null)
				combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());
			else {
				log.debug("CigarMD: not computed!");
			}
		}
		assertEquals("89m", combinedCigarMDtagList.get(0));
		assertEquals("91m", combinedCigarMDtagList.get(1));
		assertEquals("8m1uCA41m1uCT38m", combinedCigarMDtagList.get(2));
		assertEquals("4m1uGA37m1uAG48m", combinedCigarMDtagList.get(3));  // reversed and complemented (second+forward)
		assertEquals("43m1uAG11m1uTC24m2uCTAC9m", combinedCigarMDtagList.get(4));  // reversed and complemented (first+backward)
		assertEquals("1m1uTC24m2uCTAC33m1uAT9m1uTA14m1uCA3m", combinedCigarMDtagList.get(5));  // reversed and complemented (second+forward)		
	}

	@Test
	public void testCigarOperMD() {
		System.out.println("Running test VariantCallDetection.testCigarOperMD");
		log.info("Running test VariantCallDetection.testCigarOperMD");
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MD.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for(SAMRecord samRecord : samRecords) {
	        //printCigarAndMD(samRecord);	
			variantCallDetection.processSequence(samRecord);
			if(variantCallDetection.getCigarMD() != null)
				combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());
			else {
				log.debug("CigarMD: not computed!");
			}
		}
		assertEquals("14m1uCA1m1uCG8m1uTA16m1dT20m2uCTCT27m", combinedCigarMDtagList.get(0));
		assertEquals("6m1dA34m1uAG13m1uGT36m", combinedCigarMDtagList.get(1));  // reversed and complemented (first+backward)
		assertEquals("20m1dA62m1uCT8m", combinedCigarMDtagList.get(2));		
	}	
	
	@Test
	public void testCigarOperMI() {
		System.out.println("Running test VariantCallDetection.testCigarOperMI");
		log.info("Running test VariantCallDetection.testCigarOperMI");
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MI.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		List<String> combinedCigarMDtagList = new ArrayList<String>();		
		for(SAMRecord samRecord : samRecords) {
			//printCigarAndMD(samRecord);
			variantCallDetection.processSequence(samRecord);
			if(variantCallDetection.getCigarMD() != null)
				combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());
			else {
				log.debug("CigarMD: not computed!");
			}			
		}
		assertEquals("65m3iGCT22m", combinedCigarMDtagList.get(0));  // reversed and complemented (first+backward)
		assertEquals("57m1iT31m", combinedCigarMDtagList.get(1));
		assertEquals("20m1iA70m", combinedCigarMDtagList.get(2)); // reversed and complemented (first+backward)
	}
	
	@Test
	public void testCigarOperMID() {
		System.out.println("Running test VariantCallDetection.testCigarOperMID");
		log.info("Running test VariantCallDetection.testCigarOperMID");
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MID.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for(SAMRecord samRecord : samRecords) {
          //printCigarAndMD(samRecord);		
		  variantCallDetection.processSequence(samRecord);
			if(variantCallDetection.getCigarMD() != null)
				combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());
			else {
				log.debug("CigarMD: not computed!");
			}  
		}
		assertEquals("6m1iT2m1dT82m", combinedCigarMDtagList.get(0));
		assertEquals("2m1dA56m2dGT10m1uCT21m", combinedCigarMDtagList.get(1)); // (second+backward)	
		assertEquals("29m1uGA2m1uTA14m1iC3m1dA17m1uTG", combinedCigarMDtagList.get(2));  // reversed and complemented (second+forward)
		assertEquals("49m1uGC2m1iC5m2dTT24m1uCT7m", combinedCigarMDtagList.get(3));	 // reversed and complemented (first+backward)	
	}	
	
	@Test
	public void testCigarOperFull() {
		System.out.println("Running test VariantCallDetection.testCigarOperFull");
		log.info("Running test VariantCallDetection.testCigarOperFull");
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_full.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for(SAMRecord samRecord : samRecords) {
          //printCigarAndMD(samRecord);		
		  variantCallDetection.processSequence(samRecord);
			if(variantCallDetection.getCigarMD() != null)
				combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());
			else {
				log.debug("CigarMD: not computed!");
			} 
		}
		assertEquals("89m", combinedCigarMDtagList.get(0));
		assertEquals("91m", combinedCigarMDtagList.get(1));
		assertEquals("91m", combinedCigarMDtagList.get(2));
		assertEquals("65m3iGCT22m", combinedCigarMDtagList.get(3));
		assertEquals("57m1iT31m", combinedCigarMDtagList.get(4));
		assertEquals("20m1iA70m", combinedCigarMDtagList.get(5));
		assertEquals("14m1uCA1m1uCG8m1uTA16m1dT20m2uCTCT27m", combinedCigarMDtagList.get(6));
		assertEquals("6m1dA34m1uAG13m1uGT36m", combinedCigarMDtagList.get(7));
		assertEquals("20m1dA62m1uCT8m", combinedCigarMDtagList.get(8));		
		assertEquals("6m1iT2m1dT82m", combinedCigarMDtagList.get(9));
		assertEquals("2m1dA56m2dGT10m1uCT21m", combinedCigarMDtagList.get(10));
		assertEquals("29m1uGA2m1uTA14m1iC3m1dA17m1uTG", combinedCigarMDtagList.get(11));
		assertEquals("49m1uGC2m1iC5m2dTT24m1uCT7m", combinedCigarMDtagList.get(12));
		assertEquals("49m1uGC2m1iC5m2dTT22m3uGTTACT7m", combinedCigarMDtagList.get(13));		
		
	}		
	
	
	@Test
	public void testReversedReads() {
		System.out.println("Running test VariantCallDetection.testReversedReads");
		log.info("Running test VariantCallDetection.testReversedReads");
		
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/snp_examples.fastq_bowtie2.sam");
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		List<String> combinedCigarMDtagList = new ArrayList<String>();
		for(SAMRecord samRecord : samRecords) {
		  variantCallDetection.processSequence(samRecord);
		  log.debug("Name: " + samRecord.getReadName());
		  log.debug("String: " + samRecord.getReadString());
		  log.debug("Flags: " + samRecord.getFlags());		  
		  log.debug("CigarMD: " + variantCallDetection.getCigarMD().toString());	  		  
		  debugCigarAndMD(samRecord);
			if(variantCallDetection.getCigarMD() != null)
				combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());
			else {
				log.debug("CigarMD: not computed!");
			}  
		}
		assertEquals("86m", combinedCigarMDtagList.get(0));
		assertEquals("68m1uAT17m", combinedCigarMDtagList.get(1));	 // reversed and complemented (unpaired)
		assertEquals("86m", combinedCigarMDtagList.get(2)); 	 // reversed and complemented (unpaired)
		assertEquals("58m1uCT27m", combinedCigarMDtagList.get(3));
		assertEquals("21m2dTT63m", combinedCigarMDtagList.get(4)); 
		assertEquals("57m2dTT27m", combinedCigarMDtagList.get(5));	 // reversed and complemented (unpaired)
		assertEquals("36m2iCC50m", combinedCigarMDtagList.get(6));
		assertEquals("53m2iCC33m", combinedCigarMDtagList.get(7));	 // reversed and complemented (unpaired)	
	}		
	
	
	
	
	@Test
	public void testStatistics() {
		System.out.println("Running test VariantCallDetection.testStatistics");
		log.info("Running test VariantCallDetection.testStatistics");
		
		String filename;
		// some test cases
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_M.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MI.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MD.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MID.sam");
		filename = new String(new File("").getAbsolutePath() + "/test/resources/example_full.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/snp_examples.fastq_bowtie2.sam");		
		//filename = new String(new File("").getAbsolutePath() + "/../../Documents/BamQC_Examples/HG00106.chrom20.illumina.mosaik.GBR.low_coverage.20111114.bam"); // nice test on a potentially corrupted file
		
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		for(SAMRecord read : samRecords) {
			//printCigarAndMD(samRecord);				
			variantCallDetection.processSequence(read);
			if(variantCallDetection.getCigarMD() != null)
				log.debug("CigarMD: " + variantCallDetection.getCigarMD().toString());
			else {
				log.debug("CigarMD: not computed!");
			}
		}
		
		
		// compute statistics from the FIRST segment data
		HashMap<String, Long> firstSNPs = variantCallDetection.getFirstSNPs();		
		String[] snpTypeNames = firstSNPs.keySet().toArray(new String[0]);
		// sort the labels so that they are nicely organised.
		Arrays.sort(snpTypeNames);
		
		double[] dFirstSNPFrequenciesByType = new double[snpTypeNames.length];
		for(int i=0; i<snpTypeNames.length; i++) {
			dFirstSNPFrequenciesByType[i] = firstSNPs.get(snpTypeNames[i]);
		}
				
		// compute statistics from the SECOND segment data if there are paired reads.
		HashMap<String, Long> secondSNPs = variantCallDetection.getSecondSNPs();		
		double[] dSecondSNPFrequenciesByType = new double[snpTypeNames.length];
		for(int i=0; i<snpTypeNames.length; i++) {
			dSecondSNPFrequenciesByType[i] = secondSNPs.get(snpTypeNames[i]);
		}
		
		log.info("First group of SNPs");
		for(int i=0; i<snpTypeNames.length; i++) {
			log.info(snpTypeNames[i] + ": " + dFirstSNPFrequenciesByType[i]);
		}
		
		log.info("Second group of SNPs");
		for(int i=0; i<snpTypeNames.length; i++) {
			log.info(snpTypeNames[i] + ": " + dSecondSNPFrequenciesByType[i]);
		}
		
		log.info("Tot. Mut.: " + variantCallDetection.getTotalMutations());
		log.info("Tot. Ins.: " + variantCallDetection.getTotalInsertions());
		log.info("Tot. Del.: " + variantCallDetection.getTotalDeletions());
		log.info("Tot. Matches: " + variantCallDetection.getTotalMatches());
		log.info("Tot. Soft Clips: " + variantCallDetection.getTotalSoftClips());
		log.info("Tot. Hard Clips: " + variantCallDetection.getTotalHardClips());
		log.info("Tot. Paddings: " + variantCallDetection.getTotalPaddings());
		log.info("Tot. Skipped Regions: " + variantCallDetection.getTotalSkippedRegions());
		log.info("Total: " + variantCallDetection.getTotal());
		
		log.info("Unknown bases on the reads: " + variantCallDetection.getReadUnknownBases());
		log.info("Unknown bases on the reference: " + variantCallDetection.getReferenceUnknownBases());

		log.info("SNP/Indels density for each read position:");
		long[] firstSNPPos = variantCallDetection.getFirstDeletionPos();
		long[] firstInsertionPos = variantCallDetection.getFirstInsertionPos();
		long[] firstDeletionPos = variantCallDetection.getFirstDeletionPos();
		long[] secondSNPPos = variantCallDetection.getSecondDeletionPos();
		long[] secondInsertionPos = variantCallDetection.getSecondInsertionPos();
		long[] secondDeletionPos = variantCallDetection.getSecondDeletionPos();		
		log.info("Position\t1st SNP   \t1st Ins   \t1st Del   \t2nd SNP   \t2nd Ins   \t2nd Del");
		for(int i=0; i<firstSNPPos.length; i++) {
			// the above arrays have all the same length (see VariantCallDetection.java for details)
			log.info(i + "\t\t" + firstSNPPos[i] + "\t\t" + firstInsertionPos[i] + "\t\t" + firstDeletionPos[i] + "\t\t" + secondSNPPos[i] + "\t\t" + secondInsertionPos[i] + "\t\t" + secondDeletionPos[i]);
		}
	}	

	
	@Test
	public void testErrors() {
		// This is THE most important test for this module. All reads intentionally contain errors.
		System.out.println("Running test VariantCallDetection.testErrors");
		log.info("Running test VariantCallDetection.testErrors");
		
		String filename;
		filename = new String(new File("").getAbsolutePath() + "/test/resources/example_vc_errors.sam");
		
		samRecords = SAMRecordLoader.loadSAMFile(filename);		
		if(samRecords.isEmpty()) { 
			log.warn("Impossible to run the test as " + filename + " seems empty");
			return; 
		}
		for(SAMRecord read : samRecords) {
			//printCigarAndMD(read);
			variantCallDetection.processSequence(read);
			if(variantCallDetection.getCigarMD() != null)
				log.info(variantCallDetection.getCigarMD().toString());
			else {
				log.info("CigarMD: not computed!");
			}
		}
	}
	
	@Test
	public void testBooleans() {
		System.out.println("Running test VariantCallDetection.testBooleans");	
		log.info("Running test VariantCallDetection.testBooleans");
		
		TestObjectFactory testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
		for (SAMRecord samRecord : samRecords) {
			variantCallDetection.processSequence(samRecord);
		}
		
		assertTrue(variantCallDetection.ignoreInReport());
		assertFalse(variantCallDetection.needsToSeeAnnotation());
		assertFalse(variantCallDetection.raisesError());
		assertFalse(variantCallDetection.raisesWarning());
		assertTrue(variantCallDetection.needsToSeeSequences());
	}
	
}
