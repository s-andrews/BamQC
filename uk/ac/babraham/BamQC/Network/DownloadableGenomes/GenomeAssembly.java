/**
 * Copyright 2010-15 Simon Andrews
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
 * - Piero Dalle Pezze: Imported from SeqMonk and adjusted for BamQC
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.Network.DownloadableGenomes;

import java.util.Date;

/**
 * 
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 *
 */
public class GenomeAssembly {

	private GenomeSpecies species;
	private String assembly;
	private int fileSize;
	private Date date;
	
	public GenomeAssembly (GenomeSpecies species, String assmebly, int fileSize, Date date) {
		this.species = species;
		this.assembly = assmebly;
		this.fileSize = fileSize;
		this.date = date;
		species.addAssembly(this);
	}
	
	public GenomeSpecies species () {
		return species;
	}
	
	public String assembly () {
		return assembly;
	}
	
	public int fileSize () {
		return fileSize;
	}
	
	@Override
	public String toString () {
		return assembly();
	}
	
	public Date date () {
		return date;
	}
	
}
