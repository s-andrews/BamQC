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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;

/** 
 * The purpose of this class is to load a small SAM/BAM file in an ArrayList 
 * and return it to the user. It is convenient for test cases.
 * @author Piero Dalle Pezze
 */
public class SAMRecordLoader {

	private static Logger log = Logger.getLogger(SAMRecordLoader.class);	
	
	// Load the whole SAM file, as this is very short. (3-10 lines).
	// Clearly this is not a correct approach generally.
	public static ArrayList<SAMRecord> loadSAMFile(String filename) {
		ArrayList<SAMRecord> samRecords = new ArrayList<SAMRecord>();
		
		File file = new File(filename);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException ex) { 
			log.error("File " + filename + " does not exist", ex); 
			return samRecords;
		}
		// Set the default validation Stringency
		SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);
		SAMFileReader samReader = new SAMFileReader(fis);
		Iterator<SAMRecord> it = samReader.iterator();
		SAMRecord samRecord;
		while(it.hasNext()) {
			try {
				samRecord = it.next();
				samRecords.add(samRecord);
			} catch (SAMFormatException sfe) { 
				log.error(sfe, sfe);
			}
		}
		// close the file streams
		try {
			samReader.close();
			fis.close();
		} catch (IOException ioe) {
			log.error(ioe, ioe);
			return samRecords;
		}
		return samRecords;
	}
}
