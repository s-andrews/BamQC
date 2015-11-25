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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import uk.ac.babraham.BamQC.DataTypes.ProgressListener;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.DataTypes.Genome.Feature;
import uk.ac.babraham.BamQC.DataTypes.Genome.Location;
import uk.ac.babraham.BamQC.DataTypes.Genome.SplitLocation;


/**
 * The Class GTFAnnotationParser reads sequence features from GTF files
 */
public class GTFAnnotationParser extends AnnotationParser {

	
	public GTFAnnotationParser () { 
		super();
	}
	

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser#requiresFile()
	 */
	@Override
	public boolean requiresFile() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser#parseGenome(java.io.File)
	 */
	@Override
	public void parseGenome (File baseLocation) throws Exception {}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser#name()
	 */
	@Override
	public String name() {
		return "GTF Parser";
	}	
	
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser#parseAnnotation(uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet, java.io.File)
	 */
	@Override
	public void parseAnnotation(AnnotationSet annotationSet, File file) throws Exception {
		
		// Update the listeners
		Enumeration<ProgressListener> e = listeners.elements();
		while (e.hasMoreElements()) {
			e.nextElement().progressUpdated("Loading annotation file "+file.getName(), 0, 1);
		}
		
		
		annotationSet.setFile(file);
		
		HashMap<String, Transcript> groupedFeatures = new HashMap<String, Transcript>();
		HashMap<String, ProtoFeature> protoFeatures = new HashMap<String, ProtoFeature>();
		BufferedReader br = null;
		
		
        long totalBytes = file.length();                    
        long bytesRead = 0;
        int previousPercent = 0;
	
        
		try { 
		
			br = new BufferedReader(new FileReader(file));
			
			String line;

			BiotypeMapping bm = BiotypeMapping.getInstance();

			while ((line = br.readLine())!= null) {

				
	            bytesRead += line.length();
	            int percent = (int)(bytesRead * 100 / totalBytes);          
	            if (previousPercent < percent && percent%5 == 0){
	        		// Update the listeners
	        		e = listeners.elements();
	        		while (e.hasMoreElements()) {
	        			e.nextElement().progressUpdated("Parsing annotation file " + file.getName() + " (" + percent + "%)", percent, 100);
	        		}
	                previousPercent = percent;
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
				catch (NumberFormatException ex) {
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

				  // TODO THIS CODE HERE CAN BE DETRIMENTAL FOR COMPUTATION
				  // The creation of the annotation set can fail if the file is too large.
				  // There are just too many objects which can cause a GC crash
					//Feature feature = new Feature(sections[2],sections[1],c);
					//feature.setLocation(new Location(start,end,strand));
					//annotationSet.addFeature(feature);
					
				  // POSSIBLY A SOLUTION is that we group these features as well (maybe in another HashMap 
				  // if we need to keep these distinct, but possibly not). Anyway, this would not solve the 
				  // problem because the HashMap would simply become too large. The solution would be that 
				  // we adjust the sublocation array ( using splitLocation() ) after a certain number of 
				  // features inserted. That method could become handy to compact these locations so that 
				  // not much space is used.
//					Feature feature = new Feature(sections[2],sections[1],c);
//	        		Transcript transcript = new Transcript(feature);
//	        		transcript.addSublocation(new Location(start,end,strand));
//	        		groupedFeatures.put(sections[2]+"_"+sections[1], transcript);
//				}
//				if(percent%10 == 0) {
//					  for(Transcript t : groupedFeatures.values()) {
//					    t.compact();
//					  }
//				}
					
				  
				  // OKAY SOLUTION 2: Instead of using sublocations or locations, the extremes start, end and strand 
				  // are saved. Every time you process a new feature, you store the feature and the values above. 
				  // Instead of adding a new locations to the array of sublocaitons, just pass those three values 
				  // and compare them with the current stored ones. This is equivalent to a sort+SplitLocation+etc. 
				  // When the method feature() is called, this just create 1 location with the values start, end and 
				  // strand; add this location to feature and return the usual. 
				  // There is no need to do any call to the method compact().
				  // Might be worth extracting the object FeatureGroup and call it differently (e.g. ProtoFeature). 
				  // Transcript would extend an object of type FeatureGroup including codons.
				  // The parser should become quite faster because not much memory is used and the computation is similar. 
				  // The parser of the following bam file should be fine too.
					String str = sections[2]+"_"+sections[1];
					if(protoFeatures.containsKey(str)) {
						protoFeatures.get(str).update(start, end, strand);
					} else {
						Feature feature = new Feature(sections[2],sections[1],c);
						ProtoFeature protoFeature = new ProtoFeature(feature, start, end, strand);
						protoFeatures.put(str, protoFeature);
					}
				}


			}

			for(Transcript t : groupedFeatures.values()) {
				annotationSet.addFeature(t.feature());
			}
			
			for(ProtoFeature pf : protoFeatures.values()) {
				annotationSet.addFeature(pf.getFeature());
			}
			
					
		} catch (Exception ex) {
			throw ex;
		} finally {
			if(br != null) {
				br.close();
				// Update the listeners
				e = listeners.elements();
				while (e.hasMoreElements()) {
	    			e.nextElement().progressComplete("Parsing annotation file " + file.getName() + " (100%)\n" +
	    											 "Processed features: "+groupedFeatures.size() + "\n" +
	    											 "Parsed annotation file " + file.getName(), null);
				}
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

    public void compact() {
    	if (subLocations.size() > 100000) {          
    		Location compactedLocation = new SplitLocation(subLocations.toArray(new Location[0]));
    		subLocations.clear();
    		subLocations.add(compactedLocation);
    	}
    }
		

   }
	

}


