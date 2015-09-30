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
package uk.ac.babraham.BamQC.DataTypes.Genome;

import java.io.File;

import uk.ac.babraham.BamQC.BamQCException;

/**
 * The Class Genome represents a simple annotated genome assembly
 */
public class Genome {

	/** The species. */
	private String species;
	
	/** The assembly. */
	private String assembly;
	
	/** The annotation set. */
	private AnnotationSet annotationSet;
	
	/**
	 * Instantiates a new genome.
	 * 
	 * @param baseLocation the base location
	 * @throws BamQCException the seq monk exception
	 */
	public Genome (File baseLocation) throws BamQCException {
		if (!baseLocation.exists()) {
			throw new BamQCException("Couldn't find the "+baseLocation.getName()+" genome at "+baseLocation.getAbsolutePath()+".  You may need to add it to your Genome Base location");
		}
		if (!baseLocation.isDirectory()) {
			throw new BamQCException("Base location for new genome must be a directory: "+baseLocation.getAbsolutePath());
		}
		
		String [] sections = baseLocation.getAbsolutePath().split("[\\\\\\/]");
		if (sections.length < 2) {
			throw new BamQCException("Couldn't identify the species and assembly from "+baseLocation.getAbsolutePath());
		}
		assembly = sections[sections.length-1];
		species = sections[sections.length-2];
		
		annotationSet = new AnnotationSet();
	}
	
	/**
	 * Annotation set.
	 * 
	 * @return the annotation set
	 */
	public AnnotationSet annotationSet() {
		return annotationSet;
	}
	
	/**
	 * Set an Annotation set.
	 * 
	 * @param annotationSet the new annotation set
	 */
	public void setAnnotationSet(AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
	}
	
	/**
	 * Species.
	 * 
	 * @return the string
	 */
	public String species () {
		return species;
	}
	
	/**
	 * Assembly.
	 * 
	 * @return the string
	 */
	public String assembly () {
		return assembly;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString () {
		return species+" "+assembly;
	}

}

