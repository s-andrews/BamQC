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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import uk.ac.babraham.BamQC.BamQCException;
import uk.ac.babraham.BamQC.Utilities.ChromosomeNameTranslator;
import uk.ac.babraham.BamQC.Utilities.ChromosomeWithOffset;

/**
 * The Class Genome represents an entire annotated genome assembly
 */
public class Genome {

	/** The species. */
	private String species;
	
	/** The assembly. */
	private String assembly;
	
	/** The chromosomes. */
	private HashSet<Chromosome> chromosomes = new HashSet<Chromosome>();	
	
	/** The un loaded feature types. */
	private Vector<String> unLoadedFeatureTypes = new Vector<String>();
	
	/** The translator. */
	private ChromosomeNameTranslator translator;
	
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
		
		translator = new ChromosomeNameTranslator(this);
		annotationSet = new AnnotationSet(this);
	}
	
	/**
	 * Gets the exact chromosome name match.
	 * 
	 * @param name the name
	 * @return the exact chromosome name match
	 */
	public Chromosome getExactChromsomeNameMatch (String name) {
		/**
		 * This method should not normally be used to retrieve chromosomes
		 * since it is relatively slow.  Normally you'd use getChromosome(name)
		 * instead.  This method is only normally used by the name translator
		 * to tell when it's got a match.
		 */
		
		Iterator<Chromosome> i = chromosomes.iterator();
		while (i.hasNext()) {
			Chromosome c = i.next();
			if (c.name().equals(name)) {
				return c;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * Adds a new alias to the name translator which may not be able
	 * to be guessed by the standard algorithms.  Allows for unusual
	 * mappings such as IX === 9
	 * 
	 * @param alias The name you want to be able to use for the chromosome
	 * @param chrName An actual name of a current chromosome (will not be translated)
	 * @throws BamQCException If a chromosome can't be found with your reference name
	 */
	public void addAlias (String alias, String chrName, int offset) throws BamQCException {
		
		Chromosome c = getExactChromsomeNameMatch(chrName);
		
		if (c == null) {
			throw new BamQCException("No chromosome called "+chrName+" to match to alias "+alias);
		}
		
		translator.addNameMap(alias, c, offset);
		
//		System.out.println("Added alias "+alias+" for chr "+chrName+" which matched "+c.name());

	}
	
	/**
	 * Adds the chromosome.
	 * 
	 * @param name the name
	 * @return the chromosome
	 */
	public Chromosome addChromosome (String name) {
		try {
			// We add the second argument so we don't cache failed names since
			// we expect to see names which fail here.
			return translator.getChromosomeForName(name,false).chromosome();
		}
		catch (IllegalArgumentException e) {
			Chromosome c = new Chromosome(name);
			chromosomes.add(c);
			return c;
		}
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
	 * Gets the chromosome count.
	 * 
	 * @return the chromosome count
	 */
	public int getChromosomeCount (){
		return chromosomes.size();
	}

	/**
	 * Checks for chromosome.
	 * 
	 * @param c the c
	 * @return true, if successful
	 */
	public boolean hasChromosome (Chromosome c) {
		if (chromosomes.contains(c)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the all chromosomes.
	 * 
	 * @return the all chromosomes
	 */
	public Chromosome [] getAllChromosomes () {
		
		Chromosome [] c = chromosomes.toArray(new Chromosome[0]);
		
		Arrays.sort(c);
		
		return c;
	}
	
	/**
	 * Gets the chromosome.
	 * 
	 * @param name the name
	 * @return the chromosome
	 * @throws BamQCException the seq monk exception
	 */
	public ChromosomeWithOffset getChromosome (String name) {
		
		/**
		 * This is just a shortcut to the chromosome name
		 * translator method to retrieve chromsomes based
		 * on their name.
		 */
		
		return translator.getChromosomeForName(name,true);
	}
	
	/**
	 * List unloaded feature types.
	 * 
	 * @return the string[]
	 */
	public String [] listUnloadedFeatureTypes () {
		return unLoadedFeatureTypes.toArray(new String[0]);		
	}
	
	/**
	 * Adds the unloaded feature type.
	 * 
	 * @param name the name
	 */
	public void addUnloadedFeatureType (String name) {
		if (! unLoadedFeatureTypes.contains(name)) {
			unLoadedFeatureTypes.add(name);
		}
	}
	
	/**
	 * Gets the total genome length.
	 * 
	 * @return the total genome length
	 */
	public long getTotalGenomeLength () {
		Chromosome [] chroms = getAllChromosomes();
		long length = 0;
		for (int c=0;c<chroms.length;c++) {
			length+=chroms[c].length();
		}
		
		return length;
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

	/**
	 * Gets the longest chromosome length.
	 * 
	 * @return the longest chromosome length
	 */
	public int getLongestChromosomeLength () {
		int l = 0;
		Chromosome [] c = getAllChromosomes();
		for (int i=0;i<c.length;i++) {
			if (c[i].length() > l) {
				l=c[i].length();
			}
		}
		return l; 
	}
}
