package test.java.uk.ac.babraham.BamQC.Modules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


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
