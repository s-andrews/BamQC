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
		assertEquals("48m1uTC37m1uCT4m", combinedCigarMDtagList.get(3));
		assertEquals("43m1uTC11m1uAG24m2uGATG9m", combinedCigarMDtagList.get(4));  // reversed and complemented
		assertEquals("3m1uGT14m1uAT9m1uTA33m2uTGGA24m1uAG1m", combinedCigarMDtagList.get(5));		
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
		assertEquals("6m1dT34m1uTC13m1uCA36m", combinedCigarMDtagList.get(1));  // reversed and complemented
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
		assertEquals("65m3iCGA22m", combinedCigarMDtagList.get(0));  // reversed and complemented
		assertEquals("57m1iT31m", combinedCigarMDtagList.get(1));
		assertEquals("20m1iT70m", combinedCigarMDtagList.get(2)); // reversed and complemented
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
		assertEquals("21m1uCT10m2dTG56m1dA2m", combinedCigarMDtagList.get(1)); // reversed and complemented
		assertEquals("1uAC17m1dT3m1iG14m1uAT2m1uCT29m", combinedCigarMDtagList.get(2));
		assertEquals("49m1uCG2m1iG5m2dAA24m1uGA7m", combinedCigarMDtagList.get(3));	 // reversed and complemented	
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
		assertEquals("65m3iCGA22m", combinedCigarMDtagList.get(3));
		assertEquals("57m1iT31m", combinedCigarMDtagList.get(4));
		assertEquals("20m1iT70m", combinedCigarMDtagList.get(5));
		assertEquals("14m1uCA1m1uCG8m1uTA16m1dT20m2uCTCT27m", combinedCigarMDtagList.get(6));
		assertEquals("6m1dT34m1uTC13m1uCA36m", combinedCigarMDtagList.get(7));
		assertEquals("20m1dA62m1uCT8m", combinedCigarMDtagList.get(8));		
		assertEquals("6m1iT2m1dT82m", combinedCigarMDtagList.get(9));
		assertEquals("21m1uCT10m2dTG56m1dA2m", combinedCigarMDtagList.get(10));
		assertEquals("1uAC17m1dT3m1iG14m1uAT2m1uCT29m", combinedCigarMDtagList.get(11));
		assertEquals("49m1uCG2m1iG5m2dAA24m1uGA7m", combinedCigarMDtagList.get(12));
		assertEquals("49m1uCG2m1iG5m2dAA22m3uCAATGA7m", combinedCigarMDtagList.get(13));		
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
		assertEquals("68m1uTA17m", combinedCigarMDtagList.get(1));
		assertEquals("86m", combinedCigarMDtagList.get(2));
		assertEquals("58m1uCT27m", combinedCigarMDtagList.get(3));
		assertEquals("21m2dTT63m", combinedCigarMDtagList.get(4));
		assertEquals("57m2dAA27m", combinedCigarMDtagList.get(5));
		assertEquals("36m2iCC50m", combinedCigarMDtagList.get(6));
		assertEquals("53m2iGG33m", combinedCigarMDtagList.get(7));			
	}		
	
	
	
	
	@Test
	public void testStatistics() {
		log.info("testStatistics");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_M.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MI.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MD.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MID.sam");
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_full.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/snp_examples.fastq_bowtie2.sam");		
		//String filename = new String(new File("").getAbsolutePath() + "/../../Documents/BamQC_Examples/example.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/../../Documents/BamQC_Examples/large_example.bam");	
		
		// Parse the file read by read as it happens normally
		File file = new File(filename);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException ex) { 
			System.err.println("File " + filename + " does not exist"); 
			return;
		}
		SAMFileReader samReader = new SAMFileReader(fis);
		Iterator<SAMRecord> it = samReader.iterator();
		while(it.hasNext()) {
			try {
				SAMRecord samRecord = it.next();
				//printCigarAndMD(samRecord);				
				variantCallDetection.processSequence(samRecord);
				log.debug("VariantCallDetectionTest.java: CigarMD: " + variantCallDetection.getCigarMD().toString());
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
		log.info("A->C: " + variantCallDetection.getA2C());
		log.info("A->G: " + variantCallDetection.getA2G());
		log.info("A->T: " + variantCallDetection.getA2T());
		log.info("C->A: " + variantCallDetection.getC2A());
		log.info("C->G: " + variantCallDetection.getC2G());
		log.info("C->T: " + variantCallDetection.getC2T());
		log.info("G->A: " + variantCallDetection.getG2A());
		log.info("G->C: " + variantCallDetection.getG2C());
		log.info("G->T: " + variantCallDetection.getG2T());
		log.info("T->A: " + variantCallDetection.getT2A());
		log.info("T->C: " + variantCallDetection.getT2C());
		log.info("T->G: " + variantCallDetection.getT2G());
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
		log.info("Total: " + variantCallDetection.getTotal());		
		log.info("Tot. Matches: " + variantCallDetection.getTotalMatches());
		log.info("Skipped regions on the reads: " + variantCallDetection.getReadSkippedRegions());
		log.info("Skipped regions on the reference: " + variantCallDetection.getReferenceSkippedRegions());

		log.info("SNP/Indels density for each read position:");
		long[] snpPos = variantCallDetection.getSNPPos();
		long[] insertionPos = variantCallDetection.getInsertionPos();
		long[] deletionPos = variantCallDetection.getDeletionPos();
		log.info("Position\tSNP   \t\tIns   \t\tDel   ");
		for(int i=0; i<snpPos.length; i++) {
			// the above arrays have all the same length (see VariantCallDetection.java for details)
			log.info(i + "\t\t" + snpPos[i] + "\t\t" + insertionPos[i] + "\t\t" + deletionPos[i]);
		}
	}	

	
}
