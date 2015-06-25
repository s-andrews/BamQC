/**
 * Copyright Copyright 2012-14 Simon Andrews
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
package uk.ac.babraham.BamQC;

import java.io.File;

public class BamQCConfig {
	
	private static BamQCConfig instance = new BamQCConfig();
	public boolean nogroup = false;
	public boolean expgroup = false;
	public boolean quiet = false;
	public boolean show_version = false;
	public Integer kmer_size = null;
	public File gff_file = null;
	public Integer threads = null;
	public boolean showUpdates = true;
	public File output_dir = null;
	public Boolean do_unzip = null;
	public String lineSeparator = System.getProperty("line.separator");
	public String sequence_format = null;
	public File limits_file = null;
	public File biotype_mapping_file = null;

	private BamQCConfig () {
		
		// Output dir
		if (System.getProperty("bamqc.output_dir") != null) {
			output_dir = new File(System.getProperty("bamqc.output_dir"));
			if (!(output_dir.exists() && output_dir.canWrite())) {
				throw new IllegalArgumentException("Output dir "+output_dir+" doesn't exist or isn't writeable");
			}
		}
		
		// GFF file
		if (System.getProperty("bamqc.gff_file") != null) {
			gff_file = new File(System.getProperty("bamqc.gff_file"));
			if (!(gff_file.exists() && gff_file.canRead())) {
				throw new IllegalArgumentException("GFF file "+gff_file+" doesn't exist or can't be read");
			}
		}

		// Limits file
		if (System.getProperty("bamqc.limits_file") != null) {
			limits_file = new File(System.getProperty("bamqc.limits_file"));
			if (!(limits_file.exists() && limits_file.canRead())) {
				throw new IllegalArgumentException("Limits file "+limits_file+" doesn't exist or can't be read");
			}
		}

		// Limits file
		if (System.getProperty("bamqc.biotype_mapping_file") != null) {
			biotype_mapping_file = new File(System.getProperty("bamqc.biotype_mapping_file"));
			if (!(biotype_mapping_file.exists() && biotype_mapping_file.canRead())) {
				throw new IllegalArgumentException("Biotype mapping file "+biotype_mapping_file+" doesn't exist or can't be read");
			}
		}

		
		// Threads
		if (System.getProperty("bamqc.threads") != null) {
			threads = Integer.parseInt(System.getProperty("bamqc.threads"));
			if (threads < 1) {
				throw new IllegalArgumentException("Number of threads must be >= 1");
			}
		}
		
		// Threads
		if (System.getProperty("bamqc.kmer_size") != null) {
			kmer_size = Integer.parseInt(System.getProperty("bamqc.kmer_size"));
		}
		
		// Quiet
		if (System.getProperty("bamqc.quiet") != null && System.getProperty("bamqc.quiet").equals("true")) {
			quiet = true;
		}
		
		
		// No group
		if (System.getProperty("bamqc.nogroup") != null && System.getProperty("bamqc.nogroup").equals("true")) {
			nogroup = true;
		}

		// Exponential group
		if (System.getProperty("bamqc.expgroup") != null && System.getProperty("bamqc.expgroup").equals("true")) {
			expgroup = true;
		}

		// Unzip
		if (System.getProperty("bamqc.unzip") != null && System.getProperty("bamqc.unzip").equals("true")) {
			do_unzip = true;
		}
		
	}

	public static BamQCConfig getInstance() {
		return instance;
	}


}
