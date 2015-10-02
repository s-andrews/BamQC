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
package uk.ac.babraham.BamQC.AnnotationParsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import uk.ac.babraham.BamQC.BamQCException;
import uk.ac.babraham.BamQC.DataTypes.ProgressListener;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.DataTypes.Genome.Feature;
import uk.ac.babraham.BamQC.DataTypes.Genome.Genome;
import uk.ac.babraham.BamQC.DataTypes.Genome.SplitLocation;
import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;
import uk.ac.babraham.BamQC.Utilities.FileFilters.DatSimpleFileFilter;
import uk.ac.babraham.BamQC.Utilities.FileFilters.GFFSimpleFileFilter;

/**
 * The Class GenomeParser loads all of the features for a CoreGenomeAnnotationSet.
 * It can either do a full parse of the original EMBL format files, or take
 * a shortcut if there are pre-cached object files present.
 */
public class GenomeParser {

	/** The listeners. */
	private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
	
	/** The genome. */
	private Genome genome = null;
	
	/** The base location. */
	private File baseLocation;
	
	/** The current offset. */
	private int currentOffset = 0;
	
	/** The prefs. */
	private BamQCPreferences prefs = BamQCPreferences.getInstance();
	
	
	/** 
	 * The parsed genome or null if no genome has been parsed.
	 * @return the parsed genome or null
	 */
	public Genome genome() {
		return genome;
	}
	
	/**
	 * Parses the genome.
	 * 
	 * @param baseLocation the base location
	 */
	public void parseGenome (File baseLocation) {
		this.baseLocation = baseLocation;

		try {
			genome = new Genome(baseLocation);
			
		} catch (BamQCException ex) {
			Enumeration<ProgressListener> en = listeners.elements();
			
			while (en.hasMoreElements()) {
				en.nextElement().progressExceptionReceived(ex);
				return;
			}
		}
		parseGenomeFiles(genome);
	}
	
	
	/**
	 * Adds the progress listener.
	 * 
	 * @param pl the pl
	 */
	public void addProgressListener (ProgressListener pl) {
		if (pl != null && ! listeners.contains(pl))
			listeners.add(pl);
	}
	
	/**
	 * Removes the progress listener.
	 * 
	 * @param pl the pl
	 */
	public void removeProgressListener (ProgressListener pl) {
		if (pl != null && listeners.contains(pl))
				listeners.remove(pl);
	}
	
	
	
	private void parseGenomeFiles (Genome genome) {
	
		// We need a list of all of the .dat files inside the baseLocation
		File [] files = baseLocation.listFiles(new DatSimpleFileFilter());
		
		AnnotationSet annotationSet = new AnnotationSet();
		
		for (int i=0;i<files.length;i++) {
			// Update the listeners
			Enumeration<ProgressListener> e = listeners.elements();
			
			while (e.hasMoreElements()) {
				e.nextElement().progressUpdated("Loading Genome File "+files[i].getName(),i,files.length);
			}
			try {
				processEMBLFile(files[i],annotationSet);
			} 
			catch (Exception ex) {
				Enumeration<ProgressListener> en = listeners.elements();
				
				while (en.hasMoreElements()) {
					en.nextElement().progressExceptionReceived(ex);
				}
				return;
			}			
		}
		
		// Update the listeners
		Enumeration<ProgressListener> e = listeners.elements();
		if(files.length > 0) {
			while (e.hasMoreElements()) {
				e.nextElement().progressUpdated("Parsed annotation data for .dat files",1,1);
			}
		}
		
		
		// Now do the same thing for gff files.
		
		// We need a list of all of the .gff/gtf files inside the baseLocation
		files = baseLocation.listFiles(new GFFSimpleFileFilter());
		
		GFF3AnnotationParser gffParser = new GFF3AnnotationParser();
		
		for (int i=0;i<files.length;i++) {
//			System.err.println("Parsing "+files[i]);
			// Update the listeners
			e = listeners.elements();
			
			while (e.hasMoreElements()) {
				e.nextElement().progressUpdated("Loading Genome File "+files[i].getName(),i,files.length);
			}
			try {
				AnnotationSet newSet = new AnnotationSet(); 
				gffParser.parseAnnotation(newSet, files[i]);
				Feature [] features = newSet.getAllFeatures();
				for (int f=0;f<features.length;f++) {
					annotationSet.addFeature(features[f]);
				}
			} 
			catch (Exception ex) {
				Enumeration<ProgressListener> en = listeners.elements();
				
				while (en.hasMoreElements()) {
					en.nextElement().progressExceptionReceived(ex);
				}
				return;
			}			
		}

		// Update the listeners
		e = listeners.elements();
		if(files.length > 0) {
			while (e.hasMoreElements()) {
				e.nextElement().progressUpdated("Parsed annotation data for .gff/.gtf files",1,1);
			}
		}
		
		genome.setAnnotationSet(annotationSet);		

	}
	
	protected void progressWarningReceived (Exception e) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressWarningReceived(e);
		}
	}

	
	/**
	 * Process EMBL file.
	 * 
	 * @param f the f
	 * @param annotation the annotation
	 * @throws Exception the exception
	 */
	private void processEMBLFile (File f, AnnotationSet annotation) throws Exception {
		
		int processedLines = 0;
		int processedFeatures = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		Chromosome c = null;
		// We need to find and read the accession line to find out
		// which chromosome and location we're dealing with.
		
		// Each physical file can contain more than one EMBL file.  We 
		// need to account for this in our processing.
		
		while ((c = parseChromosome(br)) != null) {
			processedLines++;
			String line;			
			// We can now skip through to the start of the feature table
			while ((line=br.readLine())!=null) {
				processedLines++;
				if (line.startsWith("FH") || line.startsWith("SQ")) {
					break;
				}
			}
			
			// We can now start reading the features one at a time by
			// concatenating them and then passing them on for processing
			StringBuffer currentAttribute = new StringBuffer();
			boolean skipping = true;
			Feature feature = null;
			while ((line=br.readLine())!=null) {
				
				if (processedLines % 100000 == 0) {
					System.err.println ("Processed "+processedLines+" lines currently holding "+processedFeatures+" features");
				}
				
				processedLines++;
//				System.err.println("Read line '"+line+"'");
				
				if (line.startsWith("XX") || line.startsWith("SQ") || line.startsWith("//")) {
					skipToEntryEnd(br);
					break;
				}
				
				if (line.length() < 18) continue; // Just a blank line.
				
				String type = line.substring(5,18).trim();
//				System.out.println("Type is "+type);
				if (type.length()>0) {
					//We're at the start of a new feature.
					
					// Check whether we need to process the old feature
					if (skipping) {
						// We're either on the first feature, or we've
						// moving past this one
						skipping = false;
					}
					else {						
						// We need to process the last attribute from the
						// old feature
						processAttributeReturnSkip(currentAttribute.toString(), feature);
						annotation.addFeature(feature);
						processedFeatures++;
					}
					
					// We can check to see if we're bothering to load this type of feature
					if (prefs.loadAnnotation(type)) {
//						System.err.println("Creating new feature of type "+type);
						feature = new Feature(type,c);
						currentAttribute=new StringBuffer("location=");
						currentAttribute.append(line.substring(21).trim());
//						System.out.println(currentAttribute.toString());
						continue;
					}
					skipping = true;
					
				}
				
				if (skipping) continue;
				
				String data = line.substring(21).trim();

				if (data.startsWith("/")) {
					// We're at the start of a new attribute
										
					//Process the last attribute (extract the location)
					skipping = processAttributeReturnSkip(currentAttribute.toString(), feature);
					currentAttribute = new StringBuffer();
				}
				
				// Our default action is just to append onto the existing information

				// Descriptions which run on to multiple lines need a space adding
				// before the next lot of text.
				if (currentAttribute.indexOf("description=") >= 0) currentAttribute.append(" ");

				currentAttribute.append(data);
				
			}
			
			// We've finished, but we need to process the last feature
			// if there was one
			if (!skipping) {
				// We need to process the last attribute from the
				// old feature
				processAttributeReturnSkip(currentAttribute.toString(), feature);
				annotation.addFeature(feature);
				processedFeatures++;
			}
		}
		br.close();
		System.err.println ("Total processed features: "+processedFeatures);
	}	
		
	
	/**
	 * Process attribute return skip.
	 * 
	 * @param attribute the attribute
	 * @param feature the feature
	 * @return true, if successful
	 * @throws BamQCException the bamqc exception
	 */
	private boolean processAttributeReturnSkip (String attribute, Feature feature) throws BamQCException {
//		System.out.println("Adding feature - current attribute is "+attribute);
		String [] nameValue = attribute.split("=",2);

		// We used to insist on key value pairs, but the EMBL spec
		// allows a key without a value, so one value is OK.
		
		// extract the location
		if (nameValue[0].equals("location")) {
			
			// A location has to have a value
			if (nameValue.length < 2) {
				throw new BamQCException("Location didn't have an '=' delimiter");
			}
			
			// TODO just a checkpoint for a print showing that we are collecting the location correctly.
			// Print the location for this feature type
// 			System.out.println("Location is "+nameValue[1]);
			//Check to see if this is a location we can support
			
			if (nameValue[1].indexOf(":")>=0) {
				// Some locations are given relative to other sequences
				// (where a feature splits across more than one sequence).
				// We can't handle this so we don't try.
				return true;
			}

			feature.setLocation(new SplitLocation(nameValue[1],currentOffset));
		}

		return false;
	}
	
	/**
	 * Parses the chromosome.
	 * 
	 * @param br the br
	 * @return the chromosome
	 * @throws BamQCException the seq monk exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Chromosome parseChromosome (BufferedReader br) throws BamQCException, IOException {
		String line;
		while ((line=br.readLine())!=null) {
			
			if (line.startsWith("AC")) {
				String [] sections = line.split(":");
				if (sections.length != 6) {
					// It's not a chromosome file.  We probably just want to
					// skip it and move onto the next entry
					progressWarningReceived(new BamQCException("AC line didn't have 6 sections '"+line+"'"));
					skipToEntryEnd(br);
					continue;
				}
				if (line.indexOf("supercontig")>=0) {
					// It's not a chromosome file.  We probably just want to
					// skip it and move onto the next entry
					skipToEntryEnd(br);
					continue;
				}
			
				// Add a new chromosome to the factory if this does not exist.
				Chromosome c = genome.annotationSet().chromosomeFactory().getChromosome(sections[2]);

								
				c.setLength(Integer.parseInt(sections[4]));
				
				// Since the positions of all features are given relative
				// to the current sequence we need to add the current
				// start position to all locations as an offset.
				currentOffset = Integer.parseInt(sections[3])-1;
				return c;
			}
			
			if (line.startsWith("//")) {
				throw new BamQCException("Couldn't find AC line");
			}
		}
		return null;
	}
	

	/**
	 * Skip to entry end.
	 * 
	 * @param br the br
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void skipToEntryEnd (BufferedReader br) throws IOException {
		String line;
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
		}

		while ((line=br.readLine())!=null) {
			if (line.startsWith("//"))
				return;
		}
	}
	
		
}

