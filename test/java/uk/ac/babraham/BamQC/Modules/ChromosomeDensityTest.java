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

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.ChromosomeReadDensity;



public class ChromosomeDensityTest {
	
	private static Logger log = Logger.getLogger(ChromosomeDensityTest.class);
	
	private List<SAMRecord> samRecords = null;
	private ChromosomeReadDensity chromosomeDensity = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : ChromosomeDensityTest");	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : ChromosomeDensityTest");
	}

	@Before
	public void setUp() throws Exception {	
		chromosomeDensity = new ChromosomeReadDensity();
	}

	@After
	public void tearDown() throws Exception { 
		samRecords = null;
		chromosomeDensity = null;
	}

	@Test
	public void testChromosomeDensity() {
		log.info("testChromosomeDensity - NOT YET DEFINED");
	}

}