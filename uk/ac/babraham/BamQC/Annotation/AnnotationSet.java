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

import java.util.Hashtable;

import net.sf.samtools.SAMRecord;

public class AnnotationSet {
	
	private ChromosomeFactory factory = new ChromosomeFactory();
	
	private Hashtable<String, FeatureClass> features = new Hashtable<String, FeatureClass>();
	
	private FeatureClass [] featureArray = null;
	
	public ChromosomeFactory chromosomeFactory () {
		return factory;
	}
	
	public void addFeature (Feature f) {
		
		if (!features.containsKey(f.type())) {
			features.put(f.type(), new FeatureClass(this));
		}
		
		features.get(f.type()).addFeature(f);
		
	}
	
	public boolean hasFeatures () {
		return !features.isEmpty();
	}
	
	public String [] listFeatureTypes () {
		return features.keySet().toArray(new String [0]);
	}
	
	public FeatureClass getFeatureClassForType (String type) {
		return features.get(type);
	}
	
	public void processSequence (SAMRecord r) {
		
		if (!r.getReferenceName().equals("*")) {
			Chromosome c = factory.getChromosome(r.getReferenceName());
			c.processSequence(r);
		}
		
		if (featureArray == null) {
			featureArray = features.values().toArray(new FeatureClass[0]);
		}
		
		for (int i=0;i<featureArray.length;i++) {
			featureArray[i].processSequence(r);
		}
	}
	
	
	
	
	
}
