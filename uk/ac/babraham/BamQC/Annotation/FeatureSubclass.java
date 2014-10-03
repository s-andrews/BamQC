/**
 * Copyright Copyright 2014 Simon Andrews
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
package uk.ac.babraham.BamQC.Annotation;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import net.sf.samtools.SAMRecord;

public class FeatureSubclass {

	// The feature objects we store will be split up by chromosome.  They will be further
	// split into sequence level chunks so that we can avoid having to do lengthy linear
	// searches even when we're having random positions thrown at us.
	
	// TODO: Implement splitting
	private static final int SEQUENCE_CHUNK_LENGTH = 100000;

	private AnnotationSet annotationSet;

	private Hashtable<Chromosome, Vector<Feature>> featuresRaw = new Hashtable<Chromosome, Vector<Feature>>();
	
	private Hashtable<Chromosome, Feature[]> features = null;
	private Hashtable<Chromosome, int[]> indices = null;
	
	// These are the collated values being stored
	private int count = 0;
	
	public FeatureSubclass (AnnotationSet a) {
		annotationSet = a;
	}
	
	public void addFeature (Feature f) {
		if (features != null) throw new IllegalStateException("Can't add more features after sending data");
		if (!featuresRaw.containsKey(f.chr())) {
			featuresRaw.put(f.chr(), new Vector<Feature>());
		}
		
		featuresRaw.get(f.chr()).add(f);
	}
	
	public void processSequence (SAMRecord r) {
		
		if (features == null) {
			processFeatures();
		}
		
		Chromosome chr = annotationSet.chromosomeFactory().getChromosome(r.getReferenceName());
		
		if (chr == null) return;
		
		if (!features.containsKey(chr)) {
			return;
		}

		
		int start = r.getAlignmentStart();
		int end = r.getAlignmentEnd();
		
		int binStart = start/SEQUENCE_CHUNK_LENGTH;

		Feature [] thisChrFeatures = features.get(chr);
		
		if (binStart >= indices.get(chr).length) {
			System.err.println("Tried to get bin "+binStart+" from position "+start+" for feature on "+chr.name()+" but found only "+indices.get(chr).length+" bins from a length of "+chr.length());
			return;
		}
		
		boolean foundHit = false;
		for (int i=indices.get(chr)[binStart];i<thisChrFeatures.length;i++) {

			// Check to see if we've gone past where this sequence could
			// possibly hit.
			if (thisChrFeatures[i].location().start() > end) break;

			if (thisChrFeatures[i].location().start() < end && thisChrFeatures[i].location().end() > start) {
				if (!foundHit) {
					++count;
					foundHit = true;
				}
			}			
		}
		
	}
	
	public int count () {
		return count;
	}
	
	private void processFeatures () {
		
		features = new Hashtable<Chromosome, Feature[]>();
		indices = new Hashtable<Chromosome, int[]>();
		
		Chromosome [] chromosomes = featuresRaw.keySet().toArray(new Chromosome[0]);
		
		for (int c=0;c<chromosomes.length;c++) {

			Feature [] featuresForThisChromosome = featuresRaw.get(chromosomes[c]).toArray(new Feature[0]);
			
			Arrays.sort(featuresForThisChromosome);
			
			features.put(chromosomes[c],featuresForThisChromosome);
					
			int numberOfBinsNeeded = (chromosomes[c].length()/SEQUENCE_CHUNK_LENGTH)+1;
			if (!(chromosomes[c].length() % SEQUENCE_CHUNK_LENGTH == 0)) ++numberOfBinsNeeded;
			
			int [] indicesForThisChromsome = new int[numberOfBinsNeeded];
			indicesForThisChromsome[0] = 0;
			indices.put(chromosomes[c],indicesForThisChromsome);
			
			int lastBin = 0;
			
			for (int f=0;f<featuresForThisChromosome.length;f++) {
				int startBin = featuresForThisChromosome[f].location().start()/SEQUENCE_CHUNK_LENGTH;

				if (startBin > lastBin) {
					for (int i=lastBin+1;i<=startBin;i++) {
						indicesForThisChromsome[i] = f;
					}
					lastBin = startBin;
				}
			}
						
		}
		
		featuresRaw = null;
		
	}
	
	
}
