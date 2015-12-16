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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Imported from SeqMonk and adjusted for BamQC (added annotation etc..)
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import uk.ac.babraham.BamQC.Dialogs.ProgressTextDialog;
import uk.ac.babraham.BamQC.Network.GenomeDownloader;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.DownloadableGenomeSet;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeAssembly;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeSpecies;
import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;

/**
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 *
 */
public class BamQCConfig {
	
	private static BamQCConfig instance = new BamQCConfig();
	public boolean nogroup = false;
	public boolean expgroup = false;
	public boolean quiet = false;
	public boolean show_version = false;
	public boolean show_available_genomes = false;
	public boolean show_saved_genomes = false;	
	public File gff_file = null;
	public File genome = null;  // this is a directory
	public String species = null;
	public String assembly = null;
	public int threads = 1;
	public boolean showUpdates = true;
	public File output_dir = null;
	public boolean do_unzip = false;
	public String lineSeparator = System.getProperty("line.separator");
	public String sequence_format = null;
	public File limits_file = null;
	public File biotype_mapping_file = null;

	private BamQCConfig () {
		
		// Show version
		if (System.getProperty("bamqc.show_version") != null && System.getProperty("bamqc.show_version").equals("true")) {
			show_version = true;
		}
		
		// Show available genomes
		if (System.getProperty("bamqc.show_available_genomes") != null && System.getProperty("bamqc.show_available_genomes").equals("true")) {
			show_available_genomes = true;
		}
		
		// Show saved genomes
		if (System.getProperty("bamqc.show_saved_genomes") != null && System.getProperty("bamqc.show_saved_genomes").equals("true")) {
			show_saved_genomes = true;
		}
		
		
		// Output dir
		if (System.getProperty("bamqc.output_dir") != null) {
			output_dir = new File(System.getProperty("bamqc.output_dir"));
			if (!(output_dir.exists() && output_dir.canWrite())) {
				throw new IllegalArgumentException("Output dir "+output_dir+" does not exist or isn't writeable");
			}
		}
		
		// GFF file
		if (System.getProperty("bamqc.gff_file") != null) {
			gff_file = new File(System.getProperty("bamqc.gff_file"));
			if (!(gff_file.exists() && gff_file.canRead())) {
				throw new IllegalArgumentException("\nGFF file "+gff_file+" does not exist or cannot be read");
			}
		}
		
		if (System.getProperty("bamqc.genome") != null) {
			genome = new File(System.getProperty("bamqc.genome"));
			if (!(genome.exists() && genome.canRead())) {
				File f = new File (System.getProperty("bamqc.genome"));
				String species = f.getParentFile().getName();
				String assembly = f.getName();
				System.setProperty("bamqc.species", species);
				System.setProperty("bamqc.assembly", assembly);
				// Instead of throwing an exception, we try the next step (to download it).
				//throw new IllegalArgumentException("Genome "+genome+" does not exist or cannot be read");
			}
		}
		
		// Genome configuration folder for command line only. If these do not exist, it will try to download them first.
		if (System.getProperty("bamqc.species") != null && System.getProperty("bamqc.assembly") != null) {
			File genomeBaseLocation;
			try {
				genomeBaseLocation = BamQCPreferences.getInstance().getGenomeBase();
			} catch (FileNotFoundException e1) {
				throw new IllegalArgumentException("\nCould not find your genome base location. Please check your file preference.");				
			}
			String species = System.getProperty("bamqc.species");
			String assembly = System.getProperty("bamqc.assembly");
			genome = new File(genomeBaseLocation.getAbsolutePath() + File.separator + 
							 species + File.separator + 
							 assembly);
			if (!genome.exists()) {
				System.out.println("\nGenome '"+species+":"+assembly+"' does not exist locally");
				// try to retrieve before throwing an exception.

				int genomeSize = 0;
				
				System.out.println("\nChecking whether this exists on the server "+BamQCPreferences.getInstance().getGenomeDownloadLocation()+"\n");				
				DownloadableGenomeSet dgs;
				try {
					dgs = new DownloadableGenomeSet();
				} catch (IOException e) {
					throw new IllegalArgumentException("\nImpossible to connect to the server. Please check your Internet connection");
				}
				GenomeSpecies[] gs = dgs.species();
				boolean found = false; 
				for(int i=0; i<gs.length && !found; i++) {
					if(gs[i].name().equals(species)) {
						GenomeAssembly[] ga = gs[i].assemblies();
						for(int j=0; j<ga.length && !found; j++) {
							if(ga[j].assembly().equals(assembly)) {
								genomeSize = ga[j].fileSize();
								found = true;
							}
						}
					}
				}
				if(!found) {
					throw new IllegalArgumentException("\nGenome '"+species+":"+assembly+"' does not exist remotely \nPlease use option -b for a list of the available genomes");
				}
				System.out.println("Genome '"+species+":"+assembly+"' was found remotely");
												
				GenomeDownloader d = new GenomeDownloader();
				//d.addProgressListener();

				ProgressTextDialog ptd = new ProgressTextDialog("\nDownloading genome ...");
				d.addProgressListener(ptd);
							
				d.downloadGenome(species,assembly,genomeSize,true);
				
				// let's try again (note this requires the code
				// t.join() code in the constructor of GenomeDownloader to work correctly.
				System.setProperty("bamqc.genome", genomeBaseLocation.getAbsolutePath() + File.separator + 
							 species + File.separator + 
							 assembly);
				genome = new File(System.getProperty("bamqc.genome"));
				if(!genome.exists()) {
					throw new IllegalArgumentException("\nGenome '"+genome+"' does not exist or cannot be read");
				}
			}
		}

		// Limits file
		if (System.getProperty("bamqc.limits_file") != null) {
			limits_file = new File(System.getProperty("bamqc.limits_file"));
			if (!(limits_file.exists() && limits_file.canRead())) {
				throw new IllegalArgumentException("Limits file "+limits_file+" does not exist or cannot be read");
			}
		}

		// Limits file
		if (System.getProperty("bamqc.biotype_mapping_file") != null) {
			biotype_mapping_file = new File(System.getProperty("bamqc.biotype_mapping_file"));
			if (!(biotype_mapping_file.exists() && biotype_mapping_file.canRead())) {
				throw new IllegalArgumentException("Biotype mapping file "+biotype_mapping_file+" does not exist or cannot be read");
			}
		}

		
		// Threads
		if (System.getProperty("bamqc.threads") != null) {
			threads = Integer.parseInt(System.getProperty("bamqc.threads"));
			if (threads < 1) {
				throw new IllegalArgumentException("Number of threads must be >= 1");
			}
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
