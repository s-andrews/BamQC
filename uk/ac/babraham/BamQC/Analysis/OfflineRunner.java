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
package uk.ac.babraham.BamQC.Analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import uk.ac.babraham.BamQC.BamQCConfig;
import uk.ac.babraham.BamQC.Dialogs.ProgressTextDialog;
import uk.ac.babraham.BamQC.Modules.ModuleFactory;
import uk.ac.babraham.BamQC.Modules.QCModule;
import uk.ac.babraham.BamQC.Network.GenomeDownloader;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.DownloadableGenomeSet;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeAssembly;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeSpecies;
import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFactory;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class OfflineRunner implements AnalysisListener {
	
	private AtomicInteger filesRemaining;
	private boolean showUpdates = true;
	
	private File genomeAnnotation = null;
	
	public OfflineRunner (String[] args) {	
		
		// See if we need to show updates
		showUpdates = !BamQCConfig.getInstance().quiet;
		
		
		// a simple parser
		
		String bamqcUsage = "How to use BamQC";
		
		if(args.length == 0) { 
			// no parameter
			System.out.println(bamqcUsage);

			
		} else if(args[0].toLowerCase().endsWith(".sam") || args[0].toLowerCase().endsWith(".bam")) {
			// we have one or more sam/bam files to parse without annotation.
			runMappedFiles(args);
		
			
		} else if(args[0].equals("-a") || args[0].equals("--annotation-file")) {
			if(args.length < 3 || 
			   (!args[1].endsWith(".gtf") && !args[1].endsWith(".gff")) ||
			   (!args[2].endsWith(".sam") && !args[2].endsWith(".bam"))) { 
				// wrong parameters
				System.out.println(bamqcUsage);
			} else {
				if(setAnnotationFile(args[1])) {
					runMappedFiles(Arrays.copyOfRange(args, 2, args.length));
				}
			}
			
			
		} else if(args[0].equals("-g") || args[0].equals("--genome")) {
			if(args.length < 3 || 
			   (!args[2].endsWith(".sam") && !args[2].endsWith(".bam"))) { 
				// wrong parameters
				System.out.println(bamqcUsage);
			} else {
				System.out.println("Use the provided genome declared as 'species|assembly'. If not downloaded, download it first");
				if(setAnnotationGenome(args[1])) {
					runMappedFiles(Arrays.copyOfRange(args, 2, args.length));
				}
				runMappedFiles(Arrays.copyOfRange(args, 2, args.length));
			}			
			
			
		} else if(args[0].equals("-n") || args[0].equals("--available-genomes")) {
			listAvailableGenomes();
			
			
		} else if(args[0].equals("-d") || args[0].equals("--downloaded-genomes")) {
			listDownloadedGenomes();			
		}		
				
	}
				
				
				
				
				
				
				
				
	public void runMappedFiles(String[] bamfiles) {		
		
		Vector<File> files = new Vector<File>();
		
		// We make a special case if they supply a single filename
		// which is stdin.  In this case we'll take data piped to us
		// rather than trying to read the actual file.  We'll also
		// skip the existence check.
				
		if (bamfiles.length == 1 && bamfiles[0].equals("stdin")) {
			files.add(new File("stdin"));
		}
		else {
			for (int f=0;f<bamfiles.length;f++) {
				
				if(!bamfiles[f].toLowerCase().endsWith(".sam") || !bamfiles[f].toLowerCase().endsWith(".bam")) {
					System.err.println("Skipping '"+bamfiles[f]+"' as not a .sam or .bam file");
					continue;
				}
				
				File file = new File(bamfiles[f]);
				if (!file.exists() || ! file.canRead()) {
					System.err.println("Skipping '"+bamfiles[f]+"' which didn't exist, or couldn't be read");
					continue;
				}

				files.add(file);
			}
		}
		
				
		// See if we need to group together files from a casava group
		
		filesRemaining = new AtomicInteger(files.size());
		
		for (int i=0;i<files.size();i++) {

			try {
				processFile(files.elementAt(i));
			}
			catch (Exception e) {
				System.err.println("Failed to process "+files.elementAt(i));
				e.printStackTrace();
				filesRemaining.decrementAndGet();
			}
		}
		
		// We need to hold this class open as otherwise the main method
		// exits when it's finished.
		while (filesRemaining.intValue() > 0) {
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {}
		}
		System.exit(0);
		
	}
	
	public void processFile (File file) throws Exception {
		if (!file.getName().equals("stdin") && !file.exists()) {
			throw new IOException(file.getName()+" doesn't exist");
		}
		SequenceFile sequenceFile = SequenceFactory.getSequenceFile(file);			
						
		AnalysisRunner runner;
		if(genomeAnnotation != null) 
			runner = new AnalysisRunner(sequenceFile, genomeAnnotation);
		else 
			runner = new AnalysisRunner(sequenceFile);
		runner.addAnalysisListener(this);
			
		QCModule [] module_list = ModuleFactory.getStandardModuleList();

		runner.startAnalysis(module_list);

	}	
	
	
	
	
	public boolean setAnnotationFile(String filename) {
		File file = new File(filename);
		if (!(file.exists() && file.canRead())) {
			System.out.println("GFF/GTF file " + file + " doesn't exist or can't be read");
			return false;
		}
		BamQCConfig.getInstance().gff_file = file;
		return true;
	}
	
	
	public boolean setAnnotationGenome(String filename) {
		String[] genomeName = filename.split("|");
		if(genomeName.length != 2) { return false;}
		 // TODO check whether this is correct!
		String species = genomeName[0].replaceAll(" ", "\u0020");
		String assembly = genomeName[1];
		File file = null;
		try {
			file = new File(BamQCPreferences.getInstance().getGenomeBase().getAbsolutePath() + File.pathSeparator + species + File.pathSeparator + assembly);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't find your file preference. Please check that this exists.");
			return false;
		}
		
		if (!(file.exists() && file.isDirectory())) {
			System.out.println("The selected species|assembly " + species + "|" + assembly + " doesn't exist");
			if(!downloadAnnotationGenome(species, assembly)) 
				return false;
		}
		genomeAnnotation = file;
		return true;
	}	
	
	
	
	public boolean downloadAnnotationGenome(String species, String assembly) {
		System.out.println("Downloading the assembly " + assembly + " for the species " + species + " ... ");
		GenomeDownloader d = new GenomeDownloader();
//		d.addProgressListener(this);

		ProgressTextDialog ptd = new ProgressTextDialog("Downloading genome...");
		d.addProgressListener(ptd);
				
		// TODO HOW TO RETRIEVE the genome size ?????
		//d.downloadGenome(species,assembly,size,true);
		d.downloadGenome(species,assembly,0,true);
		
		return true;		
	}
	
	
	
	public void listDownloadedGenomes() {
		File[] genomes;
		try {
			genomes = BamQCPreferences.getInstance().getGenomeBase().listFiles();
			if (genomes == null) {
				throw new FileNotFoundException();
			}
		} 
		catch (FileNotFoundException e) {
			System.out.println("Couldn't find the folder containing your genomes.  Please check your file preferences.");
			return;
		}
		System.out.println("Downloaded genomes:");
		for(int i=0; i<genomes.length; i++) {
			System.out.println(genomes[i]);
		}
	}	
	
	
	
	public void listAvailableGenomes() {
		System.out.println("Available genomes at the Babraham Servers:");
		try {
			DownloadableGenomeSet dgs = new DownloadableGenomeSet();		
			System.out.println("List of species+assemblies:");
			GenomeSpecies[] gs = dgs.species();
			for(int i=0;i<gs.length;i++) {
				System.out.print(gs[i].name() + " [ ");
				GenomeAssembly[] ga = gs[i].assemblies();
				for(int j=0;j<ga.length;j++) {
					System.out.print(ga[j].assembly());
					if(j < ga.length-1) {
						System.out.println(" | ");
					}
				}
				System.out.println(" ]");
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void analysisComplete(SequenceFile file, QCModule[] results) {
		File reportFile;
		
		if (showUpdates) System.out.println("Analysis complete for "+file.name());

		
		if (BamQCConfig.getInstance().output_dir != null) {
			String fileName = file.getFile().getName().replaceAll("\\.gz$","").replaceAll("\\.bz2$","").replaceAll("\\.txt$","").replaceAll("\\.fastq$", "").replaceAll("\\.fastq$", "").replaceAll("\\.csfastq$", "").replaceAll("\\.sam$", "").replaceAll("\\.bam$", "")+"_bamqc.html";
			reportFile = new File(BamQCConfig.getInstance().output_dir+"/"+fileName);						
		}
		else {
			reportFile = new File(file.getFile().getAbsolutePath().replaceAll("\\.gz$","").replaceAll("\\.bz2$","").replaceAll("\\.txt$","").replaceAll("\\.fastq$", "").replaceAll("\\.fq$", "").replaceAll("\\.csfastq$", "").replaceAll("\\.sam$", "").replaceAll("\\.bam$", "")+"_bamqc.html");			
		}
		
		try {
			new HTMLReportArchive(file, results, reportFile);
		}
		catch (Exception e) {
			analysisExceptionReceived(file, e);
			return;
		}
		filesRemaining.decrementAndGet();

	}

	@Override
	public void analysisUpdated(SequenceFile file, int sequencesProcessed, int percentComplete) {
		
		if (percentComplete % 5 == 0) {
			if (percentComplete == 105) {
				if (showUpdates) System.err.println("It seems our guess for the total number of records wasn't very good.  Sorry about that.");
			}
			if (percentComplete > 100) {
				if (showUpdates) System.err.println("Still going at "+percentComplete+"% complete for "+file.name());
			}
			else {
				if (showUpdates) System.err.println("Approx "+percentComplete+"% complete for "+file.name());
			}
		}
	}

	@Override
	public void analysisExceptionReceived(SequenceFile file, Exception e) {
		System.err.println("Failed to process file "+file.name());
		e.printStackTrace();
		filesRemaining.decrementAndGet();
	}

	@Override
	public void analysisStarted(SequenceFile file) {
		if (showUpdates) System.err.println("Started analysis of "+file.name());
		
	}
	
}
