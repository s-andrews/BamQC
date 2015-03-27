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

import uk.ac.babraham.BamQC.Modules.SNPFrequencies;
import uk.ac.babraham.BamQC.Sequence.SequenceFormatException;

public class SNPFrequenciesTest {

	private static Logger log = Logger.getLogger(SNPFrequenciesTest.class);
	
	private List<SAMRecord> samRecords = null;
	private SNPFrequencies snpFrequencies = null;
	
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
	
	private void printCigarAndMD(SAMRecord samRecord) { 
		// Get the CIGAR list and MD tag string.
		System.out.println("--------------");			
		System.out.println("CIGAR: " + samRecord.getCigarString());
		System.out.println("MDtag: " + samRecord.getStringAttribute("MD"));	
	}
	
	
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : SNPFrequencies");
		SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : SNPFrequencies");
	}		
	
	@Before
	public void setUp() throws Exception {
		samRecords = new ArrayList<SAMRecord>();		
		snpFrequencies = new SNPFrequencies();		
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		snpFrequencies = null;		
	}

	
	
	@Test
	public void testRecordWithoutMDString() {
		log.info("testRecordWithoutMDString");
		
		TestObjectFactory testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();	
		snpFrequencies = new SNPFrequencies();
		
		for (SAMRecord samRecord : samRecords) {
			snpFrequencies.processSequence(samRecord);
		}	
	}
	
	@Test
	public void testCigarOperM() {
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_M.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();
		for (SAMRecord samRecord : samRecords) {
			//printCigarAndMD(samRecord);
			snpFrequencies.processSequence(samRecord);
			combinedCigarMDtagList.add(snpFrequencies.getCigarMD().toString());
		}
		assertEquals("89m", combinedCigarMDtagList.get(0));
		assertEquals("91m", combinedCigarMDtagList.get(1));
		assertEquals("8m1uCA41m1uCT38m", combinedCigarMDtagList.get(2));
		assertEquals("48m1uTC37m1uCT4m", combinedCigarMDtagList.get(3));
		assertEquals("9m1uTG1uGA24m1uAG11m1uTC43m", combinedCigarMDtagList.get(4));
		assertEquals("3m1uGT14m1uAT9m1uTA33m1uTG1uGA24m1uAG1m", combinedCigarMDtagList.get(5));		
	}

	@Test
	public void testCigarOperMD() {
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MD.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for (SAMRecord samRecord : samRecords) {
	        //printCigarAndMD(samRecord);	
			snpFrequencies.processSequence(samRecord);
			combinedCigarMDtagList.add(snpFrequencies.getCigarMD().toString());	
		}
		assertEquals("14m1uCA1m1uCG8m1uTA16m1dT20m1uCT1uCT27m", combinedCigarMDtagList.get(0));
		assertEquals("36m1uCA13m1uTC34m1dT6m", combinedCigarMDtagList.get(1));
		assertEquals("20m1dA62m1uCT8m", combinedCigarMDtagList.get(2));		
	}	
	
	@Test
	public void testCigarOperMI() {
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MI.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();		
		for (SAMRecord samRecord : samRecords) {
			//printCigarAndMD(samRecord);
			snpFrequencies.processSequence(samRecord);
			combinedCigarMDtagList.add(snpFrequencies.getCigarMD().toString());			
		}
		assertEquals("22m3iAGC65m", combinedCigarMDtagList.get(0));
		assertEquals("57m1iT31m", combinedCigarMDtagList.get(1));
		assertEquals("70m1iT20m", combinedCigarMDtagList.get(2));
	}
	
	@Test
	public void testCigarOperMID() {
		String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MID.sam");
		if(!loadSAMFile(filename)) { return; }
		List<String> combinedCigarMDtagList = new ArrayList<String>();			
		for (SAMRecord samRecord : samRecords) {
          //printCigarAndMD(samRecord);		
		  snpFrequencies.processSequence(samRecord);
		  combinedCigarMDtagList.add(snpFrequencies.getCigarMD().toString());		  
		}
		assertEquals("6m1iT2m1dT82m", combinedCigarMDtagList.get(0));
		assertEquals("2m1dA56m2dGT10m1uCT21m", combinedCigarMDtagList.get(1));
		assertEquals("1uAC17m1dT3m1iG14m1uAT2m1uCT29m", combinedCigarMDtagList.get(2));
		assertEquals("7m1uGA24m2dAA5m1iG2m1uCG49m", combinedCigarMDtagList.get(3));		
	}	
	
	
	@Test
	public void testStatistics() {
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_M.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MI.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MD.sam");
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_MID.sam");		
		//String filename = new String(new File("").getAbsolutePath() + "/test/resources/example_full.sam");
		String filename = new String(new File("").getAbsolutePath() + "/../../Documents/BamQC_Examples/example.sam");
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
				snpFrequencies.processSequence(it.next());
				System.out.println(snpFrequencies.getCigarMD().toString());
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
		System.out.println("A->C: " + snpFrequencies.getA2C());
		System.out.println("A->G: " + snpFrequencies.getA2G());
		System.out.println("A->T: " + snpFrequencies.getA2T());
		System.out.println("C->A: " + snpFrequencies.getC2A());
		System.out.println("C->G: " + snpFrequencies.getC2G());
		System.out.println("C->T: " + snpFrequencies.getC2T());
		System.out.println("G->A: " + snpFrequencies.getG2A());
		System.out.println("G->C: " + snpFrequencies.getG2C());
		System.out.println("G->T: " + snpFrequencies.getG2T());
		System.out.println("T->A: " + snpFrequencies.getT2A());
		System.out.println("T->C: " + snpFrequencies.getT2C());
		System.out.println("T->G: " + snpFrequencies.getT2G());
		System.out.println("Tot. Mut.: " + snpFrequencies.getTotalMutations());
		System.out.println("A Ins: " + snpFrequencies.getAInsertions());
		System.out.println("C Ins: " + snpFrequencies.getCInsertions());
		System.out.println("G Ins: " + snpFrequencies.getGInsertions());
		System.out.println("T Ins: " + snpFrequencies.getTInsertions());
		System.out.println("Tot. Ins.: " + snpFrequencies.getTotalInsertions());
		System.out.println("A Del: " + snpFrequencies.getADeletions());
		System.out.println("C Del: " + snpFrequencies.getCDeletions());
		System.out.println("G Del: " + snpFrequencies.getGDeletions());
		System.out.println("T Del: " + snpFrequencies.getTDeletions());
		System.out.println("N Del: " + snpFrequencies.getNDeletions());		
		System.out.println("Tot. Del.: " + snpFrequencies.getTotalDeletions());
		System.out.println("Total: " + snpFrequencies.getTotal());		
		System.out.println("Tot. Matches: " + snpFrequencies.getTotalMatches());
		System.out.println("Skipped regions on the reads: " + snpFrequencies.getReadSkippedRegions());
		System.out.println("Skipped regions on the reference: " + snpFrequencies.getReferenceSkippedRegions());
	}	

	
}
