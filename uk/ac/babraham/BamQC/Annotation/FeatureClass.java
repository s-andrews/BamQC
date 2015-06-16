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

import java.util.Collection;
import java.util.HashMap;

import net.sf.samtools.SAMRecord;


/** 
 * This class stores the map of annotation features to objects of type FeatureSubclass. 
 */
public class FeatureClass {

	private AnnotationSet annotationSet;

	private HashMap<String, FeatureSubclass> subClasses = new HashMap<String, FeatureSubclass>();
	
	
	public FeatureClass (AnnotationSet a) {
			annotationSet = a;
	}
	
	public void addFeature (Feature f) {
		if (! subClasses.containsKey(f.subclass())) {
			subClasses.put(f.subclass(), new FeatureSubclass(annotationSet));
		}
		
		subClasses.get(f.subclass()).addFeature(f);
		
	}
	
	@Deprecated
	public void processSequence (SAMRecord r) {
		FeatureSubclass[] fsc = subClasses.values().toArray(new FeatureSubclass[0]);
		for (int i=0; i<fsc.length; i++) {
			fsc[i].processSequence(r);
		}
	}
	
	public void processSequence (ShortRead r) {
		FeatureSubclass[] fsc = subClasses.values().toArray(new FeatureSubclass[0]);
		for (int i=0; i<fsc.length; i++) {
			fsc[i].processSequence(r);
		}
	}
	
	public String [] getSubclassNames () {
		return subClasses.keySet().toArray(new String[0]);
	}
	
	public FeatureSubclass getSubclassForName(String name) {
		if (subClasses.containsKey(name)) {
			return subClasses.get(name);
		}
		return null;
	}
	
}