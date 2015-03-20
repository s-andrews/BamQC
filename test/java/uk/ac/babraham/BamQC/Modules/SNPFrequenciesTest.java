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
		//System.out.println(new File("").getAbsolutePath() + "/test/resources/" + filename);
		File file = new File(new File("").getAbsolutePath() + "/test/resources/" + filename);
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
		String filename = new String("example_M.sam");
		if(!loadSAMFile(filename)) { return; }
		for (SAMRecord samRecord : samRecords) {
			printCigarAndMD(samRecord);
			snpFrequencies.processSequence(samRecord);
		}
//		assertEquals(89, distribution[0]);
//		assertEquals(91, distribution[255]);
//		assertEquals(91, distribution[10]);
	}
	
//	@Test
//	public void testCigarOperMI() {
//		String filename = new String("example_MI.sam");
//		if(!loadSAMFile(filename)) { return; }
//		for (SAMRecord samRecord : samRecords) {
//			printCigarAndMD(samRecord);
//			snpFrequencies.processSequence(samRecord);
//		}	
//	}
	
//	@Test
//	public void testCigarOperMD() {
//		String filename = new String("example_MD.sam");
//		if(!loadSAMFile(filename)) { return; }
//		for (SAMRecord samRecord : samRecords) {
//	        printCigarAndMD(samRecord);	
//			snpFrequencies.processSequence(samRecord);
//		}	
//	}
//	
//	@Test
//	public void testCigarOperMID() {
//		String filename = new String("example_MID.sam");
//		if(!loadSAMFile(filename)) { return; }
//		for (SAMRecord samRecord : samRecords) {
//          printCigarAndMD(samRecord);		
//			snpFrequencies.processSequence(samRecord);
//		}	
//	}	
//	
//	@Test
//	public void testComplete() {
//		String filename = new String("example_full.sam");
//		if(!loadSAMFile(filename)) { return; }
//		for (SAMRecord samRecord : samRecords) {
//    		printCigarAndMD(samRecord);	
//			snpFrequencies.processSequence(samRecord);
//		}	
//	}	
	
	
	
	
	
	
	
	
	
	
//	public void testSNPFrequencies() {
//		
//		log.info("testSNPFrequencies");
//		for (SAMRecord samRecord : samRecords) {
//			snpFrequencies.processSequence(samRecord);
//		}
		
//		int[] distribution = snpFrequencies.getDistribution();
//		assertEquals(1, distribution[0]);
//		assertEquals(1, distribution[255]);
//		assertEquals(1, distribution[10]);
//		assertEquals(1, snpFrequencies.getMaxCount());
//		
//		for (int i = 1; i < 256; i++) {
//			if (i != 10 && i != 255) {
//				assertEquals(0, distribution[i]);
//			}
//		}
//		for (SAMRecord samRecord : samRecords) {
//			snpFrequencies.processSequence(samRecord);
//		}
//		assertEquals(2, distribution[0]);
//		assertEquals(2, distribution[255]);
//		assertEquals(2, distribution[10]);
//		assertEquals(2, snpFrequencies.getMaxCount());
//		
//		for (int i = 1; i < 256; i++) {
//			if (i != 10 && i != 255) {
//				assertEquals(0, distribution[i]);
//			}
//		}
//		double[] distributionFloat = snpFrequencies.getDistributionDouble();
//		
//		assertEquals(33.333333, distributionFloat[0], 0.001);
//		assertEquals(33.333333, distributionFloat[255], 0.001);
//		assertEquals(33.333333, distributionFloat[10], 0.001);
//		
//		// test fraction 
//		assertEquals(.33333d, snpFrequencies.getFraction(), 0.0001);
//		
//		// test reset
//		snpFrequencies.reset();
//		
//		distribution = snpFrequencies.getDistribution();
//		
//		assertEquals(0, snpFrequencies.getMaxCount());
//		for (int i = 0; i < 256; i++) {
//			assertEquals(0, distribution[i]);
//		}
//		distributionFloat = snpFrequencies.getDistributionDouble();
//		assertEquals(0, distributionFloat.length);
//		
//		assertEquals(0.0d, snpFrequencies.getFraction(), 0.0001);


	
}
