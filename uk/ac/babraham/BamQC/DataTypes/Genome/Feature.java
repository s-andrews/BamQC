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
	
	/** The annotation. */
	private AnnotationTagValue [] annotation = new AnnotationTagValue[0];
	
		
	//Constants to store source type
	/** The Constant MANUAL. */
	private static final int MANUAL = 1;
	private static final int NAME = 2;
	private static final int DB_XREF = 3;
	private static final int GENE = 4;
	private static final int EXON_ID = 5;
	private static final int STANDARD_NAME = 6;
	private static final int NOTE = 7;
	private static final int LOCATION = 8;
	private static final int NONE = 1000;
	
	// Make up some constants for storing ids
	private static final int ID_MANUAL = 1;
	private static final int ID_ID = 2;
	private static final int ID_TRANSCRIPT = 3;
	private static final int ID_GENE_ID = 4;
	private static final int ID_GENE = 5;
	
	
	private int idSource = NONE;
	private String id = null;
	
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
		if (l.end() > chr.length()) chr.setLength(l.end());
		if (source > LOCATION) {
			name = location.start()+".."+location.end();
			source = LOCATION;
		}	
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName (String name) {
		this.name = new String(name);
		source = MANUAL; // This always wins!
	}

	
	public void setID (String id) {
		this.id = new String(id);
		idSource = ID_MANUAL; // This always wins!
	}

	
	/**
	 * Gets the annotation tag values.
	 * 
	 * @return the annotation tag values
	 */
	public AnnotationTagValue [] getAnnotationTagValues () {
		return annotation;
	}
	
	/**
	 * Gets the all annotation.
	 * 
	 * @return the all annotation
	 */
	public String getAllAnnotation (){
		StringBuffer buffer = new StringBuffer();
		for (int e=0;e<annotation.length;e++) {
			AnnotationTagValue a = annotation[e];
			buffer.append(a.tag());
			for (int i=0;i<15-a.tag().length();i++) {
				buffer.append(" ");
			}
			buffer.append(a.value());
			buffer.append("\n");
		}
		return buffer.toString();
	}
		
	/**
	 * Adds the attribute.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void addAttribute (String key, String value) {
		
		// We tried storing attributes in a more structured way, but
		// the memory overhead was too big.  We therefore just put them
		// all into one big string buffer.  Before we do that we check
		// the new data to see if it provides a better name for the 
		// feature than the one we already have.
		
		if (value == null) {
			value = "";
		}
		
		if (key.startsWith("/")) {
			key = key.substring(1);
		}
		
		if (key.length()==0) return;
		
		// Strip off quote marks around values - we don't need them.
		if (value.startsWith("\"")) {
			value = value.substring(1);
		}
		if (value.endsWith("\"")) {
			value = value.substring(0,value.length()-1);
		}
		
		if (value.length() == 0) return;
				
		findBestName(key,value);

		AnnotationTagValue [] newAnnotation = new AnnotationTagValue[annotation.length+1];
		for (int i=0;i<annotation.length;i++) {
			newAnnotation[i] = annotation[i];
		}
		newAnnotation[newAnnotation.length-1] = new AnnotationTagValue(key, value);
		annotation = newAnnotation;		
	}
	
	/**
	 * Type.
	 * 
	 * @return the string
	 */
	public String type () {
		return type;
	}
	
	
	/**
	 * This resets the type of this feature.  This should only be used as part of
	 * a controlled renaming if this feature is part of an annotation collection, or
	 * weird stuff will happen.  You have been warned...
	 * 
	 * @param type
	 */
	public void setType (String type) {
		this.type = type;
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
	
	/**
	 * Name.
	 * 
	 * @return the string
	 */
	public String name () {
		if ((source == NONE || source==LOCATION) && idSource != NONE) {
			return id();
		}
		return name;
	}
	
	
	/**
	 * The id is intended to return an unambiguous 
	 * accession number which can uniquely identify
	 * this feature.  It is taken from the annotation
	 * tag with the name of id.  If a tag with this 
	 * name isn't found then the value of the gene tag
	 * is used instead.  If this isn't present either
	 * then the normal name for this feature is used,
	 * whatever this contains.
	 * 
	 * @return
	 */
	public String id () {
		
		if (idSource != NONE) return id;
		
		return name();
	}
	
	/**
	 * Description.
	 * 
	 * @return the string
	 */
	public String description () {
		for (int e=0;e<annotation.length;e++) {
			AnnotationTagValue a = annotation[e];
			if (a.tag().equals("description")) {
				if (a.value().equals("")) {
					return "No description";
				}
				return a.value();
			}
		}
		return "No description";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name();
	}
	
	/**
	 * Find best name.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	private void findBestName (String key, String value) {
		
		// We need to see if a better name than the one we've currently
		// got has been found already.
		
		key = key.toLowerCase();

		int thisSource = NONE;
		String thisValue = null;
				
		if (key.equals("name")) {
			thisSource = NAME;
			thisValue = value;
		}
		
		else if (key.equals("db_xref") && value.indexOf("MarkerSymbol")>=0) {
			thisValue = new String((value.split(":"))[1]);
			thisSource = DB_XREF;
		}
		
		if (key.equals("gene")){
			thisValue = value;
			thisSource = GENE;
		}
		
		if (key.equals("note") && value.indexOf("exon_id")>=0) {
			thisValue = new String((value.split("="))[1]);
			thisSource=EXON_ID;
		}
		
		// STSs have a "standard name"
		if (key.equals("standard_name")){
			thisValue = value;
			thisSource = STANDARD_NAME;
		}
		
		// We can use the first note for misc_features
		if (key.equals("note")){
			thisValue = value;
			thisSource = NOTE;
		}
		
		if (source > thisSource) {
			name = thisValue;
			name = name.replaceAll("\"","");
			source = thisSource;
		}
		
		// Now do the same thing for the IDs
		thisSource = NONE;
		thisValue = null;

		if (key.equals("id")) {
			thisSource = ID_ID;
			thisValue = value;
		}

		else if (key.equals("transcript_id") || (key.equals("note") && value.startsWith("transcript_id="))) {
			thisSource = ID_TRANSCRIPT;
			if (key.equals("transcript_id")) {
				thisValue = value;
			}
			else {
				thisValue = value.replace("transcript_id=", "");
			}
		}
				
		else if (key.equals("gene_id")){
			thisValue = value;
			thisSource = ID_GENE_ID;
		}
		
		else if (key.equals("gene")){
			thisValue = value;
			thisSource = ID_GENE;
		}
		
		if (idSource > thisSource) {
			id = thisValue;
			id = id.replaceAll("\"","");
			idSource = thisSource;
		}
		
		
	}
	
	/**
	 * Flatten tags.
	 * 
	 * @return the string
	 */
	public String flattenTags () {
		return annotation.toString().replaceAll("\\n", "\\\\n");
	}

	@Override
	public int compareTo(Feature o) {
		return location.compareTo(o.location);
	}
	
	
}
