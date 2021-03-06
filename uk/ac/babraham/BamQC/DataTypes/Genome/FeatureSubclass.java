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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Optimised data structures (removed unneeded concurrency), optimised algorithm.
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.DataTypes.Genome;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.apache.log4j.Logger;




/** 
 * This class stores the sub-features of a feature. 
 * The feature objects we store will be split up by chromosome. They will be further
 * split into sequence level chunks so that we can avoid having to do lengthy linear
 * searches even when we're having random positions thrown at us.
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 */
public class FeatureSubclass {

	private static Logger log = Logger.getLogger(FeatureSubclass.class);
	
	// TODO: Implement splitting
	private static final int SEQUENCE_CHUNK_LENGTH = 100000;

	private AnnotationSet annotationSet;

	private HashMap<Chromosome, ArrayList<Feature>> features = new HashMap<Chromosome, ArrayList<Feature>>();
	
	private HashMap<Chromosome, int[]> indices = null;
	
	
	// cache these values
	private String currReferenceName = "";
	private int currRecordAlignmentStart = 0;
	private int currRecordAlignmentEnd = 0;
	private Chromosome currChromosome = null;
	private ArrayList<Feature> currChromosomeFeatures = null;
	private int[] currChromosomeIndices = null;
	
	
	// These are the collated values being stored
	private int count = 0;
	
	public FeatureSubclass (AnnotationSet a) {
		annotationSet = a;
	}
	
	public int count () {
		return count;
	}
	
	public void addFeature (Feature f) {
		if (indices != null) throw new IllegalStateException("Can't add more features after sending data");
		if (!features.containsKey(f.chr())) {
			features.put(f.chr(), new ArrayList<Feature>());
		}
		
		features.get(f.chr()).add(f);
	}
	
	
	public void processSequence (ShortRead r) {
		
		if (indices == null) {
			processFeatures();
		}
	
		currRecordAlignmentStart = r.getAlignmentStart();
		currRecordAlignmentEnd = r.getAlignmentEnd();
		int binStart = currRecordAlignmentStart/SEQUENCE_CHUNK_LENGTH;		
		
		if(!currReferenceName.equals(r.getReferenceName())) {
			// NEW CHROMOSOME
			// update chromosome info
			currReferenceName = r.getReferenceName();
			currChromosome = annotationSet.chromosomeFactory().getChromosome(currReferenceName);
			currChromosomeFeatures = features.get(currChromosome);
			currChromosomeIndices = indices.get(currChromosome);
		} 
		
		if (currChromosome == null || currChromosomeFeatures == null) return;

		if (binStart >= currChromosomeIndices.length) {
			// TODO 
			// This print can be very demanding in terms of performance in this place. It is better to 
			// leave it as log.debug enabling on request.
			log.debug("Tried to get bin " + binStart + " from position " + currRecordAlignmentStart
					+ " for feature on " + currChromosome.name() + " but found only " + currChromosomeIndices.length
					+ " bins from a length of " + currChromosome.length());
			return;
		}
		
		for (int i = currChromosomeIndices[binStart]; i < currChromosomeFeatures.size() && 
      		currChromosomeFeatures.get(i).location().start() < currRecordAlignmentEnd; i++) {
			if (currChromosomeFeatures.get(i).location().end() > currRecordAlignmentStart) {	
				count++;
				break;		
			}
		}
		
	}
	
	private void processFeatures () {
		
		indices = new HashMap<Chromosome, int[]>();
		
		for(Entry<Chromosome, ArrayList<Feature>> entry : features.entrySet()) {
			Chromosome chromosome = entry.getKey();
			ArrayList<Feature> chromosomeFeatures = entry.getValue();

			// Sort features by their location
			Collections.sort(chromosomeFeatures);
						
			int numberOfBinsNeeded = (chromosome.length()/SEQUENCE_CHUNK_LENGTH)+1;
			if (!(chromosome.length() % SEQUENCE_CHUNK_LENGTH == 0)) numberOfBinsNeeded++;
			
			int [] indicesForThisChromsome = new int[numberOfBinsNeeded];
			indicesForThisChromsome[0] = 0;
			indices.put(chromosome,indicesForThisChromsome);
			
			int lastBin = 0;
			
			for (int f=0;f<chromosomeFeatures.size();f++) {
				int startBin = chromosomeFeatures.get(f).location().start()/SEQUENCE_CHUNK_LENGTH;

				if (startBin > lastBin) {
					for (int i=lastBin+1;i<=startBin;i++) {
						indicesForThisChromsome[i] = f;
					}
					lastBin = startBin;
				}
			}				
		}
	}
	
	
}
