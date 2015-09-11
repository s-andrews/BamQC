/**
 * Copyright 2009-15-13 Simon Andrews
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
package uk.ac.babraham.BamQC.DataTypes.Sequence;

import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;

/**
 * The Class SequenceReadWithChromosome is used in places where 
 * both the read and chromsome need to passed together.  Sequence
 * Reads do not store their chromosome by default to save memory
 */
public class SequenceReadWithChromosome implements Comparable<SequenceReadWithChromosome>{

	/**
	 * This class is only to be used by data parsers which temporarily
	 * need to associate a sequence read with a chromosome in a single
	 * object.  All of the main classes use the SequenceRead object which
	 * doesn't store the chromosome to save memory.
	 */
	
	public Chromosome chromosome;
	
	/** The read. */
	public long read;
	
	/**
	 * Instantiates a new sequence read with chromosome.
	 * 
	 * @param c the c
	 * @param r the r
	 */
	public SequenceReadWithChromosome (Chromosome c, long r) {
		chromosome = c;
		read = r;
	}

	@Override
	public int compareTo(SequenceReadWithChromosome s2) {

		if (this.chromosome != s2.chromosome) {
			return this.chromosome.compareTo(s2.chromosome);
		}
		return SequenceRead.compare(read,s2.read);
	}
	
	@Override
	public String toString () {
		return chromosome.name()+":"+SequenceRead.start(read)+"-"+SequenceRead.end(read);
	}
}
