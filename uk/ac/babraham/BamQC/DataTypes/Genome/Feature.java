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
package uk.ac.babraham.BamQC.DataTypes.Genome;

import java.io.Serializable;


/**
 * The Class Feature represents a single annotation feature
 */
public class Feature implements Comparable<Feature>, Serializable {
	
	private static final long serialVersionUID = 5959879654165469490L;
	
	/** The type. */
	private String type;
	
	/** The subclass. */
	private String subclass;

	/** The Chromosome. */
	private Chromosome chr;
	
	/** The location. */
	private Location location = null;
	
	/** The name. */
	private String name = "None";
	
	/** The source. */
	private int source = NONE;
	
		
	//Constants to store source type
	/** The Constant MANUAL. */
//	private static final int MANUAL = 1;
//	private static final int NAME = 2;
//	private static final int DB_XREF = 3;
//	private static final int GENE = 4;
//	private static final int EXON_ID = 5;
//	private static final int STANDARD_NAME = 6;
//	private static final int NOTE = 7;
	private static final int LOCATION = 8;
	private static final int NONE = 1000;
	
	// Make up some constants for storing ids
//	private static final int ID_MANUAL = 1;
//	private static final int ID_ID = 2;
//	private static final int ID_TRANSCRIPT = 3;
//	private static final int ID_GENE_ID = 4;
//	private static final int ID_GENE = 5;
	
	
//	private int idSource = NONE;
//	private String id = null;
	
	public Feature (String type, Chromosome chr) {
		this.type = type;
		this.chr = chr;
	}

	public Feature (String type, String subclass, Chromosome chr) {
		this.type = type;
		this.subclass = subclass;
		this.chr = chr;
	}

	
	public void setLocation (Location l) {
		location = l;
		if (l.end() > chr.length()) 
			chr.setLength(l.end());
		if (source > LOCATION) {
			name = location.start()+".."+location.end();
			source = LOCATION;
		}	
	}
		
	/**
	 * Type.
	 * 
	 * @return the string
	 */
	public String type () {
		return type;
	}
	
	
	public Location location () {
		return location;
	}
	
	public String subclass () {
		if (subclass != null) return subclass;
		return "";
	}
	
	/**
	 * Chromosome.
	 * 
	 * @return the chromosome
	 */
	public Chromosome chr () {
		return chr;
	}
		
	public void processSequence (ShortRead r) {
		//TODO: Placeholder in case we want to record something for every feature.
	}
	
		
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(Feature o) {
		return location.compareTo(o.location);
	}
	
	
}
