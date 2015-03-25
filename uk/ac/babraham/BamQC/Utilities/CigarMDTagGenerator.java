/**
 * Copyright Copyright 2015 Piero Dalle Pezze
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

import java.util.Iterator;
import java.util.List;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;



/** 
 * It calculates a string combining information from CIGAR and MD tag.
 * The format is: # (number), operator (m=match,
 * u=unmatch, i=insertion, d=deletion), bases if oper={u,i,d}. For instance:
 * CIGAR: 32M2D5M1I52M MDtag: 7G24^AA7C49 Combined CIGAR+MDTag:
 * 7m1uG24m2dAA5m1iG2m1uC49m
 * 
 * Note: G in 1iG is SAM read- dependent.
 */
public class CigarMDTagGenerator {

	
	// Data fields used for computing the CigarMD string.
	
	// The Cigar's elements
	private List<CigarElement> cigarList = null;
	// The current Cigar element
	private String mdString = null;
	// The length for the current Cigar element
	private int currentCigarElementLength = 0;
	// The operator for the current Cigar element (substring of CIGAR)
	private String currentCigarElementOperator = null;

	// The current MD tag element (substring of MD tag)		
	private String currentMDElement = null;
	// The temporary processed length of the processed MD tag element
	private int temporaryMDElementLength = 0;
	// The temporary processed position of the processed MD tag (for the parser)
	private int temporaryMDElementPosition = 0;
	
    // The current base call position of the read
	private int currentBaseCallPosition = 0;
	

	// Data fields storing the CigarMD string.
	
	private String cigarMDstring = "";
	
	
	
	
	/**
	 * Default constructor. It generates an empty CigarMD string.
	 */
	public CigarMDTagGenerator() { }
	
	/**
	 * Constructor. It generates a CigarMD string from the SAMRecord read.
	 */
	public CigarMDTagGenerator(SAMRecord read) {
		computeCigarMDTag(read);
	}
	
	
	/**
	 * It resets the class data fields.
	 */
	public void reset() {
		cigarList = null;
		mdString = null;
		currentCigarElementLength = 0;
		currentCigarElementOperator = null;
		currentMDElement = "";
		temporaryMDElementLength = 0;
		temporaryMDElementPosition = 0;
		currentBaseCallPosition = 0;		
		cigarMDstring = "";
	}

	
	/** 
	 * It generates a string combining information from CIGAR and MD tag.
	 */
	public void generateCigarMD(SAMRecord read) {
		computeCigarMDTag(read);
	}	
	
	// getter methods
	
	/** 
	 * It returns a string combining information from CIGAR and MD tag. This method only 
	 * returns a CigarMD string computed previously.
	 * @return a string containing the computed CigarMD information.
	 */
	public String getCigarMDstring() {
		return cigarMDstring;
	}
	
	
	
	
	
	// Private methods here
	
	
	
	/** 
	 * It computes a string combining information from CIGAR and MD tag.
	 */
	private void computeCigarMDTag(SAMRecord read) {

		// Get the CIGAR list and MD tag string.
		cigarList = read.getCigar().getCigarElements();
		mdString = read.getStringAttribute("MD");
		
		if(mdString == null || mdString.equals("")) {
			//System.out.println("SNPFrequencies: current SAM read does not have MD tag string");
			cigarMDstring = "";
			return;
		}

		// The temporary processed position of the processed MD tag (for the parser)
		temporaryMDElementPosition = 0;
	    // The current base call position of the read
		currentBaseCallPosition = 0;		
		
		// Iterate the CigarList
		Iterator<CigarElement> iterCigar = cigarList.iterator();
		CigarElement currentCigarElement = null;
		
		// Used for debugging or future uses
		cigarMDstring = "";
		
		
		while(iterCigar.hasNext()) {
			currentCigarElement = iterCigar.next();
			currentCigarElementLength = currentCigarElement.getLength();
			currentCigarElementOperator = currentCigarElement.getOperator().toString();
			
			// debugging
			//System.out.println("Parsing CigarElement: " + String.valueOf(currentCigarElementLength) + currentCigarElementOperator.toString());
			if(currentCigarElementOperator.equals("M")) {
				processMDtagCigarOperatorM(read);
			} else if(currentCigarElementOperator.equals("I")) {
				processMDtagCigarOperatorI(read);
			} else if(currentCigarElementOperator.equals("D")) {
				processMDtagCigarOperatorD();				
			} else if(currentCigarElementOperator.equals("N")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element N is currently unsupported.");
			} else if(currentCigarElementOperator.equals("S")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element S is currently unsupported.");
			} else if(currentCigarElementOperator.equals("H")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element H is currently unsupported.");
			} else if(currentCigarElementOperator.equals("P")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element P is currently unsupported.");
			} else if(currentCigarElementOperator.equals("=")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element = is currently unsupported.");
			} else if(currentCigarElementOperator.equals("X")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element X is currently unsupported.");
			} else {
				System.out.println("SNPFrequencies.java: Unknown operator in the CIGAR string.");
				// throw an exception possibly.
			}		
		}
		
		// debugging
		// System.out.println("Combined Cigar MDtag: " + cigarMDstring);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Test if the current MDtag element is 0.
	 * This 0 element is only useful if the MD tag is processed separately. 
	 * When the MD tag is processed in combination with the CIGAR string, the 0 
	 * is completely redundant.
	 * @return true if the current MD element is 0.
	 */
	private boolean isCurrentMDelementZero() {
		if(currentMDElement.equals("0")) {
			return true;
		}
		return false;
	}
	
	
	// These methods process the MD string for each CIGAR operator.
	
	/** Process the MD string once found the CIGAR operator M. */
	private void processMDtagCigarOperatorM(SAMRecord read) { 
		// The temporary length of the current Cigar element
		int temporaryCigarElementLength = currentCigarElementLength;

		while(temporaryCigarElementLength > 0) {
			if(temporaryMDElementLength == 0) {
				// Parse and extract the currenMDElement. It is either a number or a char (A,C,G,T)
				// Extract the first character for the MD element.
				// Only parse the next element of MD Tag string if this current has been completed. 
				// This is required as MD tag string does not record insertions, whilst Cigar string does.
				currentMDElement = String.valueOf(mdString.charAt(temporaryMDElementPosition));
				temporaryMDElementPosition++;
						
		     	// skip if the current MD element is zero. This is redundant information if the CIGAR string is read too..
				if(isCurrentMDelementZero()) { 
					continue;
				}	

				temporaryMDElementLength++;				
				// currentMDElement is either a positive number or a base (A,C,G,T)
				if(currentMDElement.equals("A") ||
				   currentMDElement.equals("C") || 
				   currentMDElement.equals("G") || 
				   currentMDElement.equals("T") ||
				   currentMDElement.equals("N")) {
				} else {
					// The first character is a number. Let's continue and see how many numbers 
					// we find
					boolean parsedMDElement = false;
					char c;
					while(temporaryMDElementPosition < mdString.length() && !parsedMDElement) {
						c = mdString.charAt(temporaryMDElementPosition);
						if(c >= '0' && c <= '9') {
							currentMDElement = currentMDElement + c;
							temporaryMDElementPosition++;
						} else {
							// c is something else. The MD Element has been parsed.
							parsedMDElement = true;
						}
					}
					// currentMDElement is a number.
					temporaryMDElementLength = Integer.parseInt(currentMDElement);
				}
			}
			
			// debugging
			//System.out.println("tempCigElem: " + String.valueOf(temporaryCigarElementLength) + "M ~ " + "parsedMDElem: " + currentMDElement + " ; length: " + String.valueOf(temporaryMDElementLength));
		
			// update the position of the currentBaseCall and the parser.
			if(temporaryMDElementLength <= temporaryCigarElementLength) {
				// Used for debugging or future uses
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMDstring = cigarMDstring + temporaryMDElementLength + "m";						
				} else {
					cigarMDstring = cigarMDstring + "1u" + currentMDElement;
				}
				// normal code				
				currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
				temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
				temporaryMDElementLength = 0;
			} else {
				// Used for debugging or future uses
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMDstring = cigarMDstring + temporaryCigarElementLength + "m";						
				} else {
					cigarMDstring = cigarMDstring + "1u" + currentMDElement;
				}
				// normal code
				currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
				temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
				temporaryCigarElementLength = 0;
			}
			
		}
	}

	
	
	/** Process the MD string once found the CIGAR operator I. */	
	private void processMDtagCigarOperatorI(SAMRecord read) {
		// The MD string does not contain information regarding an insertion.
		String wronglyInsertedBases = read.getReadString().substring(currentBaseCallPosition, currentBaseCallPosition + currentCigarElementLength);
		currentBaseCallPosition = currentBaseCallPosition + currentCigarElementLength;		

		// Used for debugging or future uses
		//System.out.println("tempCigElem: " + String.valueOf(currentCigarElementLength) + "I ~ " + "parsedMDElem: " + currentMDElement + " ; length: " + String.valueOf(temporaryMDElementLength));		
		cigarMDstring = cigarMDstring + String.valueOf(currentCigarElementLength) + "i" + wronglyInsertedBases;

	}
	
	

	/** Process the MD string once found the CIGAR operator D. */	
	private void processMDtagCigarOperatorD() {
		// Parse and extract the currenMDElement. It is a string starting with ^ and followed by a string of (A,C,G,T)
		// Extract the first character for the MD element.
		currentMDElement = String.valueOf(mdString.charAt(temporaryMDElementPosition));
		temporaryMDElementPosition++;

     	// skip if the current MD element is zero. This is redundant information if the CIGAR string is read too..
		while(isCurrentMDelementZero()) { 		
			currentMDElement = String.valueOf(mdString.charAt(temporaryMDElementPosition));
			temporaryMDElementPosition++; 
		}
		
		if(!currentMDElement.equals("^")) {
			// this means an inconsistency between the CIGAR and MD string				
			System.out.println("SNPFrequencies: Error, ^ not found in the MD string when processing Cigar Operator D");
			return;
		}
		
		// The first character is a ^. There are exactly temporaryCigarElementLength chars (A,C,G,T) to parse.
		currentMDElement = 
				mdString.substring(temporaryMDElementPosition, temporaryMDElementPosition+currentCigarElementLength);
		temporaryMDElementPosition = temporaryMDElementPosition+currentCigarElementLength;

		// Is this correct? I don't think we should include it, as we have a deletion.. 
		// currentBaseCallPosition = currentBaseCallPosition + currentCigarElementLength;			

		// Used for debugging or future uses
		// System.out.println("tempCigElem: " + String.valueOf(currentCigarElementLength) + "D ~ " + "parsedMDElem: " + currentMDElement + " ; length: " + String.valueOf(currentMDElement.length()));		
		cigarMDstring = cigarMDstring + String.valueOf(currentCigarElementLength) + "d" + currentMDElement;		

	}
	
	
	/** Process the MD string once found the CIGAR operator N. */	
	private void processMDtagCigarOperatorN() {}
	
	/** Process the MD string once found the CIGAR operator S. */	
	private void processMDtagCigarOperatorS() {}
	
	/** Process the MD string once found the CIGAR operator H. */	
	private void processMDtagCigarOperatorH() {}
	
	/** Process the MD string once found the CIGAR operator P. */
	private void processMDtagCigarOperatorP() {}	
	
	/** Process the MD string once found the CIGAR operator =. */	
	private void processMDtagCigarOperatorEQ() {}	
	
	/** Process the MD string once found the CIGAR operator X. */	
	private void processMDtagCigarOperatorNEQ() {}	
		
}
