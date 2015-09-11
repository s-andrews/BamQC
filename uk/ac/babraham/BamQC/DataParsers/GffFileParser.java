/**
 * Copyright Copyright 2010-15 Simon Andrews
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
package uk.ac.babraham.BamQC.DataParsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import uk.ac.babraham.BamQC.BamQCException;
import uk.ac.babraham.BamQC.DataTypes.DataCollection;
import uk.ac.babraham.BamQC.DataTypes.DataSet;
import uk.ac.babraham.BamQC.DataTypes.PairedDataSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Location;
import uk.ac.babraham.BamQC.DataTypes.Sequence.SequenceRead;
import uk.ac.babraham.BamQC.Utilities.ChromosomeWithOffset;

/**
 * Parses data from a GFFv2 data file.  Only the position part of the file
 * is parsed.  Tags and additional information are ignored.  This will
 * probably also work with GFFv3 since the fields used should be the same.
 * 
 * If you want to import GFF encoded features then you don't want to use
 * this, but use the GFFAnnotationParser instead.
 */
public class GffFileParser extends DataParser {
	
	private DataParserOptionsPanel prefs = new DataParserOptionsPanel(false, false, false,false);
	
	/**
	 * Instantiates a new gff file parser.
	 * 
	 * @param data The dataCollection to which new data will be added.
	 */
	public GffFileParser (DataCollection data) {
		super(data);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataParsers.DataParser#getFileFilter()
	 */
	@Override
	public FileFilter getFileFilter () {
		return new FileFilter() {
		
			@Override
			public String getDescription() {
				return "GFF Files";
			}
		
			@Override
			public boolean accept(File f) {
				if (f.isDirectory() || f.getName().toLowerCase().endsWith(".gff") || f.getName().toLowerCase().endsWith(".gff.gz")) {
					return true;
				}
				return false;
			}
		
		};
	}
			
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	
		File [] probeFiles = getFiles();
		DataSet [] newData = new DataSet [probeFiles.length];
		int extendBy = prefs.extendReads();
		
		try {
			for (int f=0;f<probeFiles.length;f++) {
				BufferedReader br;

				if (probeFiles[f].getName().toLowerCase().endsWith(".gz")) {
					br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(probeFiles[f]))));	
				}
				else {
					br = new BufferedReader(new FileReader(probeFiles[f]));
				}
				String line;
	
				if (prefs.isHiC()) {
					newData[f] = new PairedDataSet(probeFiles[f].getName(),probeFiles[f].getCanonicalPath(),prefs.removeDuplicates(),prefs.hiCDistance(),prefs.hiCIgnoreTrans());					
				}
				else {
					newData[f] = new DataSet(probeFiles[f].getName(),probeFiles[f].getCanonicalPath(),prefs.removeDuplicates());
				}
								
				int lineCount = 0;
				// Now process the file
				while ((line = br.readLine())!= null) {
					
					if (cancel) {
						br.close();
						progressCancelled();
						return;
					}
					
					if (line.trim().length() == 0) continue;  //Ignore blank lines
					
					++lineCount;
					if (lineCount%100000 == 0) {
						progressUpdated("Read "+lineCount+" lines from "+probeFiles[f].getName(),f,probeFiles.length);
					}
					String [] sections = line.split("\t");
					
					/*
					 * The GFF file fileds are:
					 *    1. name (which must be the chromosome here)
					 *    2. source (which we ignore)
					 *    3. feature (which we ignore)
					 *    4. start pos
					 *    5. end pos
					 *    6. score (which we ignore)
					 *    7. strand
					 *    
					 *    The other fields are frame and attributes which we don't care about.
					 *    
					 *    We will use fields 1,4,5 and optionally 7
					 *    
					 */
					
					// Check to see if we've got enough data to work with
					if (sections.length < 5) {
						progressWarningReceived(new BamQCException("Not enough data from line '"+line+"'"));
						continue; // Skip this line...						
					}
						
					int strand;
					int start;
					int end;
					
					try {
						
						start = Integer.parseInt(sections[3]);
						end = Integer.parseInt(sections[4]);
						
						// End must always be later than start
						if (start > end) {
							int temp = start;
							start = end;
							end = temp;
						}
						
						if (sections.length >= 7) {
							if (sections[6].equals("+")) {
								strand = Location.FORWARD;
							}
							else if (sections[6].equals("-")) {
								strand = Location.REVERSE;
							}
							else if (sections[6].equals(".")) {
								strand = Location.UNKNOWN;
							}
							else {
								progressWarningReceived(new BamQCException("Unknown strand character '"+sections[6]+"' marked as unknown strand"));
								strand = Location.UNKNOWN;
							}
						}
						else {
							strand = Location.UNKNOWN;
						}
						
						if (extendBy > 0) {
							if (strand == Location.FORWARD) {
								end += extendBy;
							}
							else if (strand == Location.REVERSE) {
								start -= extendBy;
							}
						}
					}
					catch (NumberFormatException e) {
						progressWarningReceived(new BamQCException("Location "+sections[3]+"-"+sections[4]+" was not an integer"));
						continue;
					}
					
					ChromosomeWithOffset c;
					try {
						c = dataCollection().genome().getChromosome(sections[0]);
					}
					catch (IllegalArgumentException sme) {
						progressWarningReceived(sme);
						continue;
					}
					
					start = c.position(start);
					end = c.position(end);
	
					// We also don't allow readings which are beyond the end of the chromosome
					if (end > c.chromosome().length()) {
						int overrun = end - c.chromosome().length();
						progressWarningReceived(new BamQCException("Reading position "+end+" was "+overrun+"bp beyond the end of chr"+c.chromosome().name()+" ("+c.chromosome().length()+")"));
						continue;
					}
	
					// We can now make the new reading
					try {
						long read = SequenceRead.packPosition(start,end,strand);
						newData[f].addData(c.chromosome(),read);
					}
					catch (BamQCException e) {
						progressWarningReceived(e);
						continue;
					}
				}
				
				// We're finished with the file.
				br.close();
				
				// Cache the data in the new dataset
				progressUpdated("Caching data from "+probeFiles[f].getName(), f, probeFiles.length);
				newData[f].finalise();
				
			}
			processingFinished(newData);
		}	

		catch (Exception ex) {
			progressExceptionReceived(ex);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataParsers.DataParser#description()
	 */
	@Override
	public String description() {
		return "Imports General Feature Format Files";
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataParsers.DataParser#getOptionsPanel()
	 */
	@Override
	public JPanel getOptionsPanel() {
		return prefs;
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataParsers.DataParser#hasOptionsPanel()
	 */
	@Override
	public boolean hasOptionsPanel() {
		return true;
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataParsers.DataParser#name()
	 */
	@Override
	public String name() {
		return "GFF File Importer";
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataParsers.DataParser#readyToParse()
	 */
	@Override
	public boolean readyToParse() {
		return true;
	}
}
