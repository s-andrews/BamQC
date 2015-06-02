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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMFileReader;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.VariantCallDetection;

public class VariantCallDetectionTest {

	private static Logger log = Logger.getLogger(VariantCallDetectionTest.class);
	
	private List<SAMRecord> samRecords = null;
	private VariantCallDetection variantCallDetection = null;


	// Load the whole SAM file, as this is very short. (3-10 lines).
	// Clearly this is not a correct approach generally.
	private boolean loadSAMFile(String filename) {
		File file = new File(filename);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException ex) { 
			System.err.println("File " + filename + " does not exist"); 
			return false;
		}
		SAMFileReader samReader = new SAMFileReader(fis);
		Iterator<SAMRecord> it = samReader.iterator();
		SAMRecord samRecord;	
		while(it.hasNext()) {
			try { 
				samRecord = it.next();
				samRecords.add(samRecord);
			} catch (SAMFormatException sfe) { 
				System.out.println("SAMFormatException");
			}
		}
		// close the file streams
		try {
			fis.close();
			samReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return true;
	}
	
	private void debugCigarAndMD(SAMRecord samRecord) { 
		// Get the CIGAR list and MD tag string.
		log.debug("CIGAR: " + samRecord.getCigarString());
		log.debug("MDtag: " + samRecord.getStringAttribute("MD"));
		log.debug("--------------");				
	}
	
	
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : VariantCallDetection");
		SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : VariantCallDetection");
	}		
	
	@Before
	public void setUp() throws Exception {
		samRecords = new ArrayList<SAMRecord>();		
		variantCallDetection = new VariantCallDetection();		
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		variantCallDetection = null;		
	}

	
	
	@Test
	public void testRecordWithoutMDString() {
		log.info("testRecordWithoutMDString");
		
		TestObjectFactory testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();	
		variantCallDetection = new VariantCallDetection();
		
		for (SAMRecord samRecord : samRecords) {
			variantCallDetection.processSequence(samRecord);
		}	
	}
	
	@Test
	public void testCigarOperM() {
		log.info("testCigarOperM");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_M.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();
		for (SAMRecord samRecord : samRecords) {
			//printCigarAndMD(samRecord);
			variantCallDetection.processSequence(samRecord);
			combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());
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
		log.info("testCigarOperMD");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MD.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for (SAMRecord samRecord : samRecords) {
	        //printCigarAndMD(samRecord);	
			variantCallDetection.processSequence(samRecord);
			combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());	
		}
		assertEquals("14m1uCA1m1uCG8m1uTA16m1dT20m2uCTCT27m", combinedCigarMDtagList.get(0));
		assertEquals("6m1dA34m1uAG13m1uGT36m", combinedCigarMDtagList.get(1));  // reversed and complemented (first+backward)
		assertEquals("20m1dA62m1uCT8m", combinedCigarMDtagList.get(2));		
	}	
	
	@Test
	public void testCigarOperMI() {
		log.info("testCigarOperMI");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MI.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();		
		for (SAMRecord samRecord : samRecords) {
			//printCigarAndMD(samRecord);
			variantCallDetection.processSequence(samRecord);
			combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());			
		}
		assertEquals("65m3iGCT22m", combinedCigarMDtagList.get(0));  // reversed and complemented (first+backward)
		assertEquals("57m1iT31m", combinedCigarMDtagList.get(1));
		assertEquals("20m1iA70m", combinedCigarMDtagList.get(2)); // reversed and complemented (first+backward)
	}
	
	@Test
	public void testCigarOperMID() {
		log.info("testCigarOperMID");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MID.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for (SAMRecord samRecord : samRecords) {
          //printCigarAndMD(samRecord);		
		  variantCallDetection.processSequence(samRecord);
		  combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());		  
		}
		assertEquals("6m1iT2m1dT82m", combinedCigarMDtagList.get(0));
		assertEquals("2m1dA56m2dGT10m1uCT21m", combinedCigarMDtagList.get(1)); // (second+backward)	
		assertEquals("29m1uGA2m1uTA14m1iC3m1dA17m1uTG", combinedCigarMDtagList.get(2));  // reversed and complemented (second+forward)
		assertEquals("49m1uGC2m1iC5m2dTT24m1uCT7m", combinedCigarMDtagList.get(3));	 // reversed and complemented (first+backward)	
	}	
	
	@Test
	public void testCigarOperFull() {
		log.info("testCigarOperFull");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_full.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for (SAMRecord samRecord : samRecords) {
          //printCigarAndMD(samRecord);		
		  variantCallDetection.processSequence(samRecord);
		  combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());		  
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
		assertEquals("", combinedCigarMDtagList.get(14));			
		assertEquals("", combinedCigarMDtagList.get(15));			
	}		
	
	
	@Test
	public void testReversedReads() {
		log.info("testReversedReads");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/snp_examples.fastq_bowtie2.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();
		for (SAMRecord samRecord : samRecords) {
		  variantCallDetection.processSequence(samRecord);
		  log.debug("Name: " + samRecord.getReadName());
		  log.debug("String: " + samRecord.getReadString());
		  log.debug("Flags: " + samRecord.getFlags());		  
		  log.debug("CigarMD: " + variantCallDetection.getCigarMD().toString());	  		  
		  debugCigarAndMD(samRecord);
          combinedCigarMDtagList.add(variantCallDetection.getCigarMD().toString());		  
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
		log.info("testStatistics");
		String filename;
		// some test cases
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_M.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MI.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MD.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MID.sam");
		filename = new String(new File("").getAbsolutePath() + "/test/resources/example_full.sam");
		//filename = new String(new File("").getAbsolutePath() + "/test/resources/snp_examples.fastq_bowtie2.sam");		
		filename = new String(new File("").getAbsolutePath() + "/../../Documents/BamQC_Examples/example.sam");
		//filename = new String(new File("").getAbsolutePath() + "/../../Documents/BamQC_Examples/HG00106.chrom20.illumina.mosaik.GBR.low_coverage.20111114.bam"); // nice test on a potentially corrupted file
		
		// Parse the file read by read as it happens normally
		File file = new File(filename);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException ex) { 
			log.warn("File " + filename + " does not exist"); 
			return;
		}
		SAMFileReader samReader = new SAMFileReader(fis);
		Iterator<SAMRecord> it = samReader.iterator();
		while(it.hasNext()) {
			try {
				SAMRecord samRecord = it.next();
				//printCigarAndMD(samRecord);				
				variantCallDetection.processSequence(samRecord);
				log.debug("CigarMD: " + variantCallDetection.getCigarMD().toString());
			} catch (SAMFormatException sfe) { 
				log.warn("SAMFormatException");
			}
		}
		// close the file streams
		try {
			fis.close();
			samReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		log.info("A->C: " + variantCallDetection.getFirstA2C());
		log.info("A->G: " + variantCallDetection.getFirstA2G());
		log.info("A->T: " + variantCallDetection.getFirstA2T());
		log.info("C->A: " + variantCallDetection.getFirstC2A());
		log.info("C->G: " + variantCallDetection.getFirstC2G());
		log.info("C->T: " + variantCallDetection.getFirstC2T());
		log.info("G->A: " + variantCallDetection.getFirstG2A());
		log.info("G->C: " + variantCallDetection.getFirstG2C());
		log.info("G->T: " + variantCallDetection.getFirstG2T());
		log.info("T->A: " + variantCallDetection.getFirstT2A());
		log.info("T->C: " + variantCallDetection.getFirstT2C());
		log.info("T->G: " + variantCallDetection.getFirstT2G());
		log.info("A->C: " + variantCallDetection.getSecondA2C());
		log.info("A->G: " + variantCallDetection.getSecondA2G());
		log.info("A->T: " + variantCallDetection.getSecondA2T());
		log.info("C->A: " + variantCallDetection.getSecondC2A());
		log.info("C->G: " + variantCallDetection.getSecondC2G());
		log.info("C->T: " + variantCallDetection.getSecondC2T());
		log.info("G->A: " + variantCallDetection.getSecondG2A());
		log.info("G->C: " + variantCallDetection.getSecondG2C());
		log.info("G->T: " + variantCallDetection.getSecondG2T());
		log.info("T->A: " + variantCallDetection.getSecondT2A());
		log.info("T->C: " + variantCallDetection.getSecondT2C());
		log.info("T->G: " + variantCallDetection.getSecondT2G());		
		log.info("Tot. Mut.: " + variantCallDetection.getTotalMutations());
		log.info("A Ins: " + variantCallDetection.getAInsertions());
		log.info("C Ins: " + variantCallDetection.getCInsertions());
		log.info("G Ins: " + variantCallDetection.getGInsertions());
		log.info("T Ins: " + variantCallDetection.getTInsertions());
		log.info("N Ins: " + variantCallDetection.getNInsertions());
		log.info("Tot. Ins.: " + variantCallDetection.getTotalInsertions());
		log.info("A Del: " + variantCallDetection.getADeletions());
		log.info("C Del: " + variantCallDetection.getCDeletions());
		log.info("G Del: " + variantCallDetection.getGDeletions());
		log.info("T Del: " + variantCallDetection.getTDeletions());
		log.info("N Del: " + variantCallDetection.getNDeletions());		
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

	
}
