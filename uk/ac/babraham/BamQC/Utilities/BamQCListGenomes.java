/**
 * Copyright Copyright 2010-14 Simon Andrews
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
package uk.ac.babraham.BamQC.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.DownloadableGenomeSet;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeAssembly;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeSpecies;



public class BamQCListGenomes {
	

	/**
	 * Return the list of genomes available on the Babraham server or null if this list cannot be downloaded.
	 * Each species will be shown as follows: species [assembly1, assembly2, ... assemblyN].
	 * @return the list of available genomes or null
	 */
	public static GenomeSpecies[] listAvailableGenomes() {
		GenomeSpecies[] gs = null;
		System.out.println("Available genomes at the Babraham Servers:");
		try {
			DownloadableGenomeSet dgs = new DownloadableGenomeSet();		
			System.out.println("List of species+assemblies:");
			gs = dgs.species();
			for(int i=0;i<gs.length;i++) {
				System.out.print(gs[i].name() + " [ ");
				GenomeAssembly[] ga = gs[i].assemblies();
				for(int j=0;j<ga.length;j++) {
					System.out.print(ga[j].assembly());
					if(j < ga.length-1) {
						System.out.print(" | ");
					}
				}
				System.out.println(" ]");
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gs;
	}

	
	/**
	 * Return the list of downloaded genomes or null if this is empty
	 * @return the list of downloaded genomes or null
	 */
	public static File[] listSavedGenomes() {
		File[] genomes = null;
		try {
			genomes = BamQCPreferences.getInstance().getGenomeBase().listFiles();
			if (genomes == null) {
				throw new FileNotFoundException();
			}
		} 
		catch (FileNotFoundException e) {
			System.out.println("Couldn't find the folder containing your genomes.  Please check your file preferences.");
			return genomes;
		}
		System.out.println("Downloaded genomes:");
		for(int i=0; i<genomes.length; i++) {
			System.out.println(genomes[i]);
		}
		return genomes;
	}
	

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Please, use the option '--saved-genomes' or '--available-genomes'");
			return;
		}
		if(args[0].equals("--saved-genomes")) {
			BamQCListGenomes.listSavedGenomes();			
		} else if(args[0].equals("--available-genomes")) {
			BamQCListGenomes.listAvailableGenomes();			
		}

	}
	
}
