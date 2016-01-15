/**
 * Copyright Copyright 2014 Simon Andrews
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
 * - Bart Ailey: Class creation.
 */
package test.java.uk.ac.babraham.BamQC.Modules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author Bart Ailey
 *
 */
public class UtilityTest {
	
	private static Logger log = Logger.getLogger(UtilityTest.class);	

	private static final String INSERT_SIZES_FILENAME = "test/resources/bamInsertSizeCounts.txt";
	
	public UtilityTest() {}

	public static List<Double> readInsertSizesDouble() {
		List<Double> insertSizes = new ArrayList<Double>();

		BufferedReader reader = null;
		
		try {
			
			reader = new BufferedReader(new FileReader(INSERT_SIZES_FILENAME));
			
			String line;

			while ((line = reader.readLine()) != null) {
				String[] words = line.split("\\s+");

				insertSizes.add(Double.parseDouble(words[1]));
			}
		}
		catch (IOException e) {
			log.error(e, e);
			throw new RuntimeException("Cannot open file " + INSERT_SIZES_FILENAME);
		} finally {
			try {
				if(reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				log.error(e, e);
			}
		}
		return insertSizes;
	}
	
	public static List<Long> readInsertSizesLong() {
		List<Long> insertSizes = new ArrayList<Long>();

		BufferedReader reader = null;
		
		try {
			
			reader = new BufferedReader(new FileReader(INSERT_SIZES_FILENAME));
		
			String line;

			while ((line = reader.readLine()) != null) {
				String[] words = line.split("\\s+");

				insertSizes.add(Long.parseLong(words[1]));
			}
		}
		catch (IOException e) {
			log.error(e, e);
			throw new RuntimeException("Cannot open file " + INSERT_SIZES_FILENAME);
		} finally {
			try {
				if(reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				log.error(e, e);
			}
		}
		return insertSizes;
	}
	

}
