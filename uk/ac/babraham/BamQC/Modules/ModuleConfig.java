/**
 * Copyright Copyright 2013-14 Simon Andrews
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
package uk.ac.babraham.BamQC.Modules;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import uk.ac.babraham.BamQC.BamQCConfig;

public class ModuleConfig {

	private static HashMap<String, Double> parameters = readParams();

	private static HashMap<String, Double> readParams() {

		HashMap<String, Double> params = new HashMap<String, Double>();

		// Set the defaults to use if we don't have any overrides
		// Please, use the following format: ClassName_field
		params.put("AnnotationSet_annotation_cache_capacity:ignore", 50000d);
		
		params.put("VariantCallPosition_array_length:ignore", 150d);
		params.put("VariantCallPosition_indel_seqpercent_xaxis_threshold:ignore", 5d);
		params.put("VariantCallPosition_indel_threshold:warn", 0.5d);	
		params.put("VariantCallPosition_indel_threshold:error", 1d);
		params.put("VariantCallPosition_snp_seqpercent_xaxis_threshold:ignore", 5d); 		
		params.put("VariantCallPosition_snp_threshold:warn", 1d);	
		params.put("VariantCallPosition_snp_threshold:error", 2d);

		// if this option is set to >0, then no threshold is applied and the chromosomes 
		// are always plotted separately.
		params.put("GenomeCoverage_always_plot_separate_chromosomes:ignore", 0d);		
		params.put("GenomeCoverage_plot_separate_chromosomes:ignore", 50d);
		params.put("GenomeCoverage_plot_bins_all_chromosomes:ignore", 500d);		
		params.put("GenomeCoverage_plot_bins_per_chromosome:ignore", 100d);
		
		params.put("InsertLengthDistribution_max_insert_size:ignore", 5000.0d);
		params.put("InsertLengthDistribution_bin_size:ignore", 25.0d);
		params.put("InsertLengthDistribution_percentage_deviation:error", 50.0d);
		params.put("InsertLengthDistribution_percentage_deviation:warn", 75.0d);

		params.put("RpkmReference_max_size:ignore", 5000.0d);
		params.put("RpkmReference_bin_size:ignore", 400.0d);
		
		params.put("MappingQualityDistribution_fraction:error", 0.50d);
		params.put("MappingQualityDistribution_fraction:warn", 0.75d);
			

		params.put("ChromosomeReadDensity:ignore",0d);
		params.put("FeatureCoverage:ignore",0d);
		params.put("GenomeCoverage:ignore",0d);
		params.put("IndelFrequencies:ignore",0d);
		params.put("InsertLengthDistribution:ignore",0d);
		params.put("MappingQualityDistribution:ignore",0d);
		params.put("RpkmReference:ignore",1d);                // ignore this module
		params.put("SequenceQualityDistribution:ignore",1d);  // ignore this module
		params.put("SNPFrequencies:ignore",0d);
		params.put("SNPFrequenciesByType:ignore",0d);
		params.put("SoftClipDistribution:ignore",0d);
		// The following option switches off InsertFrequencies, SNPFrequencies, SNPFrequenciesByType and some statistics in BasicStatistics
		params.put("VariantCallDetection:ignore",0d);		
		
		
		// Now read the config file to see if there are updated values for any
		// of these.

		BufferedReader br = null;

		try {
			if (BamQCConfig.getInstance().limits_file == null) {
				InputStream rsrc = ModuleConfig.class.getResourceAsStream("/Configuration/limits.txt");
				if (rsrc == null) throw new FileNotFoundException("cannot find Configuration/limits.txt");
				br = new BufferedReader(new InputStreamReader(rsrc));
			}
			else {
				br = new BufferedReader(new FileReader(BamQCConfig.getInstance().limits_file));
			}

			String line;
			while ((line = br.readLine()) != null) {

				if (line.startsWith("#")) continue;

				if (line.trim().length() == 0) continue;

				String[] sections = line.split("\\s+");
				if (sections.length != 3) {
					System.err.println("Config line '" + line + "' didn't contain the 3 required sections");
				}

				if (!(sections[1].equals("warn") || sections[1].equals("error") || sections[1].equals("ignore"))) {
					System.err.println("Second config field must be error, warn or ignore, not '" + sections[1] + "'");
					continue;
				}

				double value;
				try {
					value = Double.parseDouble(sections[2]);
				}
				catch (NumberFormatException nfe) {
					System.err.println("Value " + sections[2] + " didn't look like a number");
					continue;
				}
				String key = sections[0] + ":" + sections[1];
				params.put(key, value);

			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return params;

	}

	public static Double getParam(String module, String level) {

		if (!(level.equals("warn") || level.equals("error") || level.equals("ignore"))) {
			throw new IllegalArgumentException("Level must be warn, error or ignore");
		}

		String key = module + ":" + level;

		if (!parameters.containsKey(key)) {
			throw new IllegalArgumentException("No key called " + key + " in the config data");
		}
		return parameters.get(key);
	}

}
