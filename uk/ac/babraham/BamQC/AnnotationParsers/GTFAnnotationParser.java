/**
 * Copyright 2010-14 Simon Andrews
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
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.DataTypes.Genome.Feature;
import uk.ac.babraham.BamQC.DataTypes.Genome.Genome;
import uk.ac.babraham.BamQC.DataTypes.Genome.Location;
import uk.ac.babraham.BamQC.DataTypes.Genome.SplitLocation;


/**
 * The Class GTFAnnotationParser reads sequence features from GTF files
 */
public class GTFAnnotationParser extends AnnotationParser {

	private static Logger log = Logger.getLogger(GTFAnnotationParser.class);

	
	public GTFAnnotationParser () { 
		super();
	}
	
	/**
	 * Instantiates a new GFF annotation parser.
	 * 
	 * @param genome the genome
	 */
	public GTFAnnotationParser (Genome genome) {
		super(genome);		
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser#requiresFile()
	 */
	@Override
	public boolean requiresFile() {
		return true;
	}


	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser#name()
	 */
	@Override
	public String name() {
		return "GTF Parser";
	}	
	
	
	
	@Override
	public void parseAnnotation(AnnotationSet annotationSet, File file) throws Exception {

		annotationSet.setFile(file);
		
		HashMap<String, Transcript> groupedFeatures = new HashMap<String, Transcript>();
		BufferedReader br = null;

		try { 
			br = new BufferedReader(new FileReader(file));
			String line;

			BiotypeMapping bm = BiotypeMapping.getInstance();

			int count = 0;
			while ((line = br.readLine())!= null) {

				//			if (cancel) {
				//				progressCancelled();
				//				br.close();
				//				return null;
				//			}
				//			
				//			if (count % 1000 == 0) {
				//				progressUpdated("Read "+count+" lines from "+file.getName(), 0, 1);
				//			}
				//			
				//			if (count>1000000 && count%1000000 == 0) {
				//				progressUpdated("Caching...",0,1);
				//				annotationSet.finalise();
				//				annotationSet = new GenomeAnnotationSet(genome, file.getName()+"["+annotationSets.size()+"]");
				//				annotationSets.add(annotationSet);
				//			}

				count++;

				if (count % 100000 == 0) {
					System.out.println ("Processed "+count+" lines currently holding "+groupedFeatures.size()+" features");
				}			



				if (line.trim().length() == 0) continue;  //Ignore blank lines
				if (line.startsWith("#")) continue; //Skip comments

				String [] sections = line.split("\t");

				/*
				 * The GFFv3 file fileds are:
				 *    1. name (which must be the chromosome here)
				 *    2. source (which is actually the biotype for Ensembl GTF files)
				 *    3. feature type
				 *    4. start pos
				 *    5. end pos
				 *    6. score (which we ignore)
				 *    7. strand
				 *    8. frame (which we ignore)
				 *    9. attributes (structured field allowing us to group features together)
				 *    
				 */

				// Check to see if we've got enough data to work with
				if (sections.length < 9) {
					throw new Exception("Not enough data from line '"+line+"'");
				}

				// Check if we need to modify the biotype or maybe even discard the feature
				sections[1] = bm.getEffectiveBiotype(sections[1]);
				if (sections[1].equals("DELETE")) continue;

				int strand;
				int start;
				int end;

				try {

					start = Integer.parseInt(sections[3]);
					end = Integer.parseInt(sections[4]);

					// End must always be later than start
					if (end < start) {
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
						else {
							strand = Location.UNKNOWN;
						}
					}
					else {
						strand = Location.UNKNOWN;
					}
				}
				catch (NumberFormatException e) {
					//	progressWarningReceived(new BamQCException("Location "+sections[3]+"-"+sections[4]+" was not an integer"));
					continue;
				}

				Chromosome c = annotationSet.chromosomeFactory().getChromosome(sections[0]);

				// Now see what we're doing.  The only primary features we care about are genes and transcripts
				// If we've got one of these we just make up a new feature and get out.

				if (sections[2].equals("gene")) {
					Feature feature = new Feature(sections[2],sections[1],c);
					feature.setLocation(new Location(start,end,strand));
					annotationSet.addFeature(feature);
				}

				else if (sections[2].equals("transcript")) {
					// We do the same but we add it to the grouped features set waiting to have
					// some exons and maybe a start/stop codon
					Feature feature = new Feature(sections[2],sections[1],c);
					Transcript transcript = new Transcript(feature);


					// We need to get the transcript id.
					String transcriptID = getTranscriptIDFromAttributes(sections[8]);
					groupedFeatures.put(transcriptID, transcript);

				}

				else if (sections[2].equals("exon")) {
					// We need to find the transcript to which this exon belongs and then add this
					// location as a sub-location for that transcript.
					String transcriptID = getTranscriptIDFromAttributes(sections[8]);
					if (! groupedFeatures.containsKey(transcriptID)) {
						// Not sure if this can ever be valid, but we'll treat it as an error for now.
						throw new Exception("Found exon with transcript ID "+transcriptID+" but there was no matching transcript feature");
					}

					groupedFeatures.get(transcriptID).addSublocation(new Location(start, end, strand));
				}

				else if (sections[2].equals("stop_codon")) {
					String transcriptID = getTranscriptIDFromAttributes(sections[8]);

					if (! groupedFeatures.containsKey(transcriptID)) {
						// Not sure if this can ever be valid, but we'll treat it as an error for now.
						throw new Exception("Found stop_codon with transcript ID "+transcriptID+" but there was no matching transcript feature");
					}
					if (strand == Location.FORWARD) {
						groupedFeatures.get(transcriptID).addStopCodon(start);
					}
					else {
						groupedFeatures.get(transcriptID).addStopCodon(end);					
					}
				}

				else if (sections[2].equals("start_codon")) {
					String transcriptID = getTranscriptIDFromAttributes(sections[8]);

					if (! groupedFeatures.containsKey(transcriptID)) {
						// Not sure if this can ever be valid, but we'll treat it as an error for now.
						throw new Exception("Found start_codon with transcript ID "+transcriptID+" but there was no matching transcript feature");
					}
					if (strand == Location.FORWARD) {
						groupedFeatures.get(transcriptID).addStartCodon(end);
					}
					else {
						groupedFeatures.get(transcriptID).addStartCodon(start);					
					}
				}

				else if (sections[2].equals("UTR")) {
					// I don't think we need to do anything with these.  We can probably
					// figure them out from the transcript and codon positions.
				}

				else {
					// We assume that anything else we don't understand is a single span feature
					// class so we just enter it directly.

					Feature feature = new Feature(sections[2],sections[1],c);
					feature.setLocation(new Location(start,end,strand));
					annotationSet.addFeature(feature);
				}


			}

			log.info("Parsed " + groupedFeatures.size()+ " features");


			// Now go through the grouped features adding them to the annotation set
			Transcript[] t = groupedFeatures.values().toArray(new Transcript[0]);
			for(int j=0; j<t.length; j++) {
				annotationSet.addFeature(t[j].feature());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if(br != null) {
				br.close();
			}
		}


	}

	private String getTranscriptIDFromAttributes (String attribString) throws Exception {
		String [] attributes = attribString.split(" *; *"); // Should check for escaped colons
		for (int a=0;a<attributes.length;a++) {
			String [] keyValue = attributes[a].split(" +");
			if (keyValue.length !=2) {
				throw new Exception("Not 2 values from splitting "+attributes[a]);
			}
			if (keyValue[0].equals("transcript_id")) {
				return new String(keyValue[1]);
			}
		}

		throw new Exception("Coudn't find transcript_id from within "+attribString);

	}

	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser#parseAnnotation(java.io.File, uk.ac.babraham.BamQC.DataTypes.Genome.Genome)
	 */
	@Override
	public AnnotationSet parseAnnotation(File file, Genome genome) throws Exception {
		AnnotationSet annotationSet = new AnnotationSet(genome, file.getName());
		parseAnnotation(annotationSet, file);
		return annotationSet;
	}
	
	
	/**
	 * The Class featureGroup.
	 */
	private class Transcript {

		/** The feature. */
		private Feature feature;

		/** The sub locations. */
		private ArrayList<Location> subLocations = new ArrayList<Location>();

		private int startCodon;
		private int stopCodon;

		/** The location */
		private Location location;

		/**
		 * Instantiates a new feature group.
		 * 
		 * @param feature the feature
		 * @param strand the strand
		 * @param location the location
		 */
		public Transcript (Feature feature) {
			this.feature = feature;
		}

		/**
		 * Adds a sublocation.
		 * 
		 * @param location the location
		 */
		public void addSublocation (Location location) {
			subLocations.add(location);
		}

		public void addStartCodon (int startCodon) {
			this.startCodon = startCodon;
		}

		public void addStopCodon (int stopCodon) {
			this.stopCodon = stopCodon;
		}


		/**
		 * Feature.
		 * 
		 * @return the feature
		 */
		public Feature feature () {
			if (subLocations.size() == 0) {
				feature.setLocation(location);					
			}
			else if (subLocations.size() == 1) {
				feature.setLocation(subLocations.get(0));					
			}
			else {
				feature.setLocation(new SplitLocation(subLocations.toArray(new Location[0])));
			}

			return feature;
		}


	}

}
