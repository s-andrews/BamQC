/**
 * Copyright 2013-15 Simon Andrews
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
package uk.ac.babraham.BamQC.Utilities;

import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;

public class ChromosomeWithOffset {

	/**
	 * This class is used to return a lookup to a chromosome name where we
	 * can optionally add an offset as well.  This allows us to use other
	 * coordinate spaces for our identifiers as long as the linear values
	 * within the region are the same.  This is useful for building 
	 * pseudo chromosomes or for other non-linear mapping in the future
	 */
	
	private Chromosome chromosome;
	private int offset;
	
	public ChromosomeWithOffset (Chromosome chromosome, int offset) {
		this.chromosome = chromosome;
		this.offset = offset;
	}
	
	public Chromosome chromosome () {
		return chromosome;
	}
	
	public int position (int basePosition) {
		return basePosition+offset;
	}
	
}
