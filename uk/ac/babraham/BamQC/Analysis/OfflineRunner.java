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
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;


import uk.ac.babraham.BamQC.BamQCConfig;
import uk.ac.babraham.BamQC.Modules.ModuleFactory;
import uk.ac.babraham.BamQC.Modules.QCModule;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFactory;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class OfflineRunner implements AnalysisListener {
	
	private AtomicInteger filesRemaining;
	private boolean showUpdates = true;
	
	public OfflineRunner (String[] filenames) {	
		
		// See if we need to show updates
		showUpdates = !BamQCConfig.getInstance().quiet;
		
		
		// a simple parser
		
		String bamqcUsageError = "The inserted parameters are not correct. Please use option -h (or --help) for help.";
		
		if(filenames.length == 0) { 
			// no parameter. Just for completeness, as we will generally start the GUI if no parameter is passed.
			System.out.println(bamqcUsageError);
		
		} else 
			if(BamQCConfig.getInstance().gff_file != null) {
				System.out.println("Annotation file: " + BamQCConfig.getInstance().gff_file.getAbsolutePath());
			}
			if(BamQCConfig.getInstance().genome != null) {
				System.out.println("Genome: " + BamQCConfig.getInstance().genome.getAbsolutePath());			
			}
			runMappedFiles(filenames);
				
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
				
				if(!bamfiles[f].toLowerCase().endsWith(".sam") && !bamfiles[f].toLowerCase().endsWith(".bam")) {
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
						
		AnalysisRunner runner = new AnalysisRunner(sequenceFile);
		
		runner.addAnalysisListener(this);
			
		QCModule [] module_list = ModuleFactory.getStandardModuleList();

		runner.startAnalysis(module_list);

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
