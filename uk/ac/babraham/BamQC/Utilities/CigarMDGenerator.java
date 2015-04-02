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
 * It calculates a string combining information from CIGAR and MD tag. The
 * format is: # (number), operator (m=match, u=unmatch, i=insertion,
 * d=deletion), bases if oper={u,i,d}. For instance: CIGAR: 32M2D5M1I52M MDtag:
 * 7G24^AA7C49 Combined CIGAR+MDTag: 7m1uGT24m2dAA5m1iG2m1uCA49m
 * 
 * Note: G in 1iG is SAM read- dependent.
 */
public class CigarMDGenerator {

	// Data fields used for computing the CigarMD string.

	// The Cigar's elements
	private List<CigarElement> cigarList = null;
	// The current Cigar element
	private String mdString = null;
	// The current Cigar element
	private CigarElement currentCigarElement = null;
	// The length for the current Cigar element
	private int currentCigarElementLength = 0;
	// The operator for the current Cigar element (substring of CIGAR)
	private String currentCigarElementOperator = null;
	// The current MD tag element (substring of MD tag)
	private String currentMDElement = null;
	// The temporary processed length of the processed MD tag element
	private int temporaryMDElementLength = 0;
	// The current processed position of the processed MD tag (for the parser)
	private int currentMDElementPosition = 0;
	// The current base call position of the read
	private int currentBaseCallPosition = 0;

	// Data fields storing the CigarMD object combining Cigar and MD strings.
	private CigarMD cigarMD = new CigarMD();

	// Public interface
	// Constructors
	/**
	 * Default constructor. It generates an empty CigarMD string.
	 */
	public CigarMDGenerator() {	}

	/**
	 * Constructor. It generates a CigarMD string from the SAMRecord read.
	 */
	public CigarMDGenerator(SAMRecord read) {
		computeCigarMDTag(read);
	}

	// getter methods
	/**
	 * It returns a string combining information from CIGAR and MD tag. This
	 * method only returns a CigarMD string computed previously.
	 * 
	 * @return a string containing the computed CigarMD information, or the
	 *         empty string if no CigarMD object was computed.
	 */
	public String getCigarMDString() {
		return cigarMD.toString();
	}

	/**
	 * It returns an object CigarMD containing the combined information from
	 * CIGAR and MD tag. This method only returns a CigarMD object computed
	 * previously.
	 * 
	 * @return a CigarMD object containing the computed Cigar + MD information.
	 */
	public CigarMD getCigarMD() {
		return cigarMD;
	}

	// computing methods
	/**
	 * It generates a string combining information from CIGAR and MD tag.
	 */
	public void generateCigarMD(SAMRecord read) {
		reset();
		computeCigarMDTag(read);
	}

	// Private methods here
	/**
	 * It generates a string combining information from CIGAR and MD tag.
	 */
	private void computeCigarMDTag(SAMRecord read) {

		/*
		 * IMPORTANT NOTE:
		 * The second column of a SAM file contains important hex FLAGS. From the SAM/BAM format specifications: 
		 * (a) "Bit 0x4 is the only reliable place to tell whether the read is unmapped. If 0x4 is set, no assumptions
		 * can be made about RNAME, POS, CIGAR, MAPQ"
		 * (b) "Bit 0x10 indicates whether SEQ has been reverse complemented and QUAL reversed. When bit 0x4 is unset, this 
		 * correspond to the strand to which the segment has been mapped. When 0x4 is set, this indicates whether the unmapped 
		 * read is stored in its original orientation as it came off the sequencing machine. 
		 * 
		 * Since this class strongly depend on the CIGAR string, the bit 0x4 must be unset. 
		 * The bit 0x10 must be evaluated in order to parse and collect statistics correctly. If 0x10 is set, a new read shall 
		 * be computed reversing and complementing SEQ, CIGAR (?) and MD tag (?).
		 * 
		 * Check Simon's test files
		 * Then add these tests.
		 */		

		// if Flag 0x4 is set, then the read is unmapped. Therefore, skip it for the reasons above.
		int mask = read.getFlags();
		// To check the state of a flag bit. Unmapped Flag 0x4 in decimal is: hex2dec(0x4) => 4 
		if ((4 | mask) == mask) {
			// flag 0x4 is set. The read is unmapped. Skip it. 
			// System.out.println("CigarMDGenerator: current SAM read does has flag 0x4 set on.");
			return;			
		}
		
		
		
		
		// Get the MD tag string.
		mdString = read.getStringAttribute("MD");

		if (mdString == null || mdString.equals("")) {
			// System.out.println("CigarMDGenerator: current SAM read does not have MD tag string.");
			return;
		}

		// Get the CIGAR list
		cigarList = read.getCigar().getCigarElements();				
		// Iterate the CigarList
		Iterator<CigarElement> iterCigar = cigarList.iterator();

		// Loop over the CigarElement objects of the read Cigar
		while (iterCigar.hasNext()) {
			currentCigarElement = iterCigar.next();
			currentCigarElementLength = currentCigarElement.getLength();
			currentCigarElementOperator = currentCigarElement.getOperator()
					.toString();

			// debugging
			// System.out.println("Parsing CigarElement: " +
			// String.valueOf(currentCigarElementLength) +
			// currentCigarElementOperator.toString());
			if (currentCigarElementOperator.equals("M")) {
				processMDtagCigarOperatorM(read);
			} else if (currentCigarElementOperator.equals("I")) {
				processMDtagCigarOperatorI(read);
			} else if (currentCigarElementOperator.equals("D")) {
				processMDtagCigarOperatorD();
			} else if (currentCigarElementOperator.equals("N")) {
				// System.out.println("CigarMDGenerator.java: extended CIGAR element N is currently unsupported.");
			} else if (currentCigarElementOperator.equals("S")) {
				// System.out.println("CigarMDGenerator.java: extended CIGAR element S is currently unsupported.");
			} else if (currentCigarElementOperator.equals("H")) {
				// System.out.println("CigarMDGenerator.java: extended CIGAR element H is currently unsupported.");
			} else if (currentCigarElementOperator.equals("P")) {
				// System.out.println("CigarMDGenerator.java: extended CIGAR element P is currently unsupported.");
			} else if (currentCigarElementOperator.equals("=")) {
				// System.out.println("CigarMDGenerator.java: extended CIGAR element = is currently unsupported.");
			} else if (currentCigarElementOperator.equals("X")) {
				// System.out.println("CigarMDGenerator.java: extended CIGAR element X is currently unsupported.");
			} else {
				System.out
						.println("CigarMDGenerator.java: Found unknown operator in the CIGAR string.\n"
								+ "Unknown CigarOperator: "
								+ currentCigarElementOperator
								+ "\n"
								+ "found on read:" + read.toString() + "\n");
				// Possibly, throw an exception.
			}
		}

		// debugging
		// System.out.println("CigarMD string: " + cigarMD.toString());
	}

	/**
	 * It resets the class data fields.
	 */
	private void reset() {
		cigarList = null;
		mdString = null;
		currentCigarElement = null;
		currentCigarElementLength = 0;
		currentCigarElementOperator = null;
		currentMDElement = "";
		temporaryMDElementLength = 0;
		currentMDElementPosition = 0;
		currentBaseCallPosition = 0;
		cigarMD = new CigarMD();
	}

	/**
	 * Test if the current MDtag element is 0. This 0 element is only useful if
	 * the MD tag is processed separately. When the MD tag is processed in
	 * combination with the CIGAR string, the 0 is completely redundant.
	 * 
	 * @return true if the current MD element is 0.
	 */
	private boolean isCurrentMDelementZero() {
		if (currentMDElement.equals("0")) {
			return true;
		}
		return false;
	}

	// These methods process the MD string for each CIGAR operator.
	
	/** Process the MD string once found the CIGAR operator M. */
	private void processMDtagCigarOperatorM(SAMRecord read) { 
		// The temporary length of the current Cigar element
		int temporaryCigarElementLength = currentCigarElementLength;
		String bases;
		
		while(temporaryCigarElementLength > 0) {
			bases = "";
			
			// When you process a new Cigar operator M, temporaryMDElementLength is 0 (you haven't processed it yet!).
			if(temporaryMDElementLength == 0) {
				// Parse and extract the currenMDElement. It is either a number or a char (A,C,G,T)
				// Extract the first character for the MD element.
				// Only parse the next element of MD Tag string if this current has been completed. 
				// This is required as MD tag string does not record insertions, whilst Cigar string does.
				char currentMDChar = mdString.charAt(currentMDElementPosition);
				currentMDElement = String.valueOf(currentMDChar);
				currentMDElementPosition++;
				
		     	// skip if the current MD element is zero. This is redundant information if the CIGAR string is read too..
				if(isCurrentMDelementZero()) {
					continue;
				}
				

				
				if(currentMDChar >= '1' && currentMDChar <= '9') {
					// CASE 1: The first character is a number. Therefore, we are parsing MATCHED bases.
					// Let's continue and see how many numbers we find.
					// This comprehensive number is the temporaryMDElementLength, which tells us 
					// how many matched bases we have.
					boolean fullNumberFound = false;
					while(currentMDElementPosition < mdString.length() && !fullNumberFound) {
						currentMDChar = mdString.charAt(currentMDElementPosition);
						if(currentMDChar >= '0' && currentMDChar <= '9') {
							currentMDElement = currentMDElement + currentMDChar;
							currentMDElementPosition++;
						} else {
							// c is something else. The MD Element has been parsed.
							fullNumberFound = true;
						}
					}
					// currentMDElement is a number.
					temporaryMDElementLength = Integer.parseInt(currentMDElement);
					
				} else {
					// CASE 2: The first character is a char. Therefore, we are parsing MISMATCHED bases or mutations.
					// If there is a mutation on the read bases with respect to the reference, we indicate this as:
					// RefBaseMutBase (base on the reference -> mutated base on the read). 
					// For instance: an element of CigarMD with operator 'u' is 1uCA IF the base C on the reference 
					// is mutated into the base A on the aligned read base. In the case of two contiguous mutation, this 
					// can be coded as 2uCAGT if the reference string CA are mutated into GT.
					
					// Retrieve the mutation and create the first couple of mutated bases.
					char currentBaseCall = read.getReadString().charAt(currentBaseCallPosition);
					// debugging
					// System.out.println("currentBaseCallPosition: " + currentBaseCallPosition);
					if("ACGTN".indexOf("" + currentMDChar) > -1) {
						bases = bases + currentMDChar + currentBaseCall;
					} else {
						// this is an error case
						System.out.println("CigarMDGenerator.java: Found unrecognised mutation " + 
						                   currentMDChar + "->" + currentBaseCallPosition + " inside SAM record " + read.toString());
						bases = bases + '?' + '?';								
					}				
					temporaryMDElementLength++;
					
					// Let's continue and see how many mismatches we find.
					// The number of mismatches will be the temporaryMDElementLength, whereas the variable 
					// bases will store the mismatched couples (ReferenceBase,ReadBaseMutation).
					boolean allContiguousMutationsFound = false;
					while(currentMDElementPosition < mdString.length() && !allContiguousMutationsFound) {
						// debugging
						//System.out.println("currentMDElement: " + currentMDElement + " currentBaseCall: " + currentBaseCall);						
						currentMDChar = mdString.charAt(currentMDElementPosition);
						if("ACGTN".indexOf("" + currentMDChar) > -1) {
							// debugging
							// System.out.println("currentBaseCallPosition: " + (currentBaseCallPosition+temporaryMDElementLength));
							currentBaseCall = read.getReadString().charAt(currentBaseCallPosition+temporaryMDElementLength);
							bases = bases + currentMDChar + currentBaseCall;
							temporaryMDElementLength++;
							currentMDElementPosition++;	
						} else {
							allContiguousMutationsFound = true;
						}			
					}
				}
			}
							
			
			// debugging
			//System.out.println("tempCigElem: " + String.valueOf(temporaryCigarElementLength) + "M ~ " + "parsedMDElem: " + currentMDElement + " ; length: " + String.valueOf(temporaryMDElementLength));
			
			// update the position of the currentBaseCall and the parser.
			if(temporaryMDElementLength <= temporaryCigarElementLength) {
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMD.add(new CigarMDElement(temporaryMDElementLength, CigarMDOperator.MATCH, bases));					
				} else {
					cigarMD.add(new CigarMDElement(temporaryMDElementLength, CigarMDOperator.MISMATCH, bases));
				}			
				currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
				temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
				temporaryMDElementLength = 0;
			} else {
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMD.add(new CigarMDElement(temporaryCigarElementLength, CigarMDOperator.MATCH, bases));
				} else {
					cigarMD.add(new CigarMDElement(temporaryCigarElementLength, CigarMDOperator.MISMATCH, bases));
				}
				currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
				temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
				temporaryCigarElementLength = 0;
			}
			
			// debugging
		    //System.out.println(cigarMD.toString());			
		}
	}
	
	
	
	/** Process the MD string once found the CIGAR operator M. 
	 * This method computed the mutations one per time (1uCA1uAT1uCG)
	 * instead of the method above which computes: 3uCAATCG */
	private void processMDtagCigarOperatorMBACKUP(SAMRecord read) { 
		// The temporary length of the current Cigar element
		int temporaryCigarElementLength = currentCigarElementLength;
		String bases;
		while(temporaryCigarElementLength > 0) {
			bases = "";
			if(temporaryMDElementLength == 0) {
				// Parse and extract the currenMDElement. It is either a number or a char (A,C,G,T)
				// Extract the first character for the MD element.
				// Only parse the next element of MD Tag string if this current has been completed. 
				// This is required as MD tag string does not record insertions, whilst Cigar string does.
				currentMDElement = String.valueOf(mdString.charAt(currentMDElementPosition));
				currentMDElementPosition++;
						
		     	// skip if the current MD element is zero. This is redundant information if the CIGAR string is read too..
				if(isCurrentMDelementZero()) { 
					continue;
				}	

				temporaryMDElementLength = 1;				
				// currentMDElement is either a positive number or a base (A,C,G,T)
					
				// If there is a mutation on the read bases with respect to the reference, we indicate this as:
				// RefBaseMutBase (base on the reference -> mutated base on the read). 
				// For instance: an element of CigarMD with operator 'u' is 1uCA IF the base C on the reference 
				// is mutated into the base A on the aligned read base.
				// We report the bases only if there is a mismatch
				char currentBaseCall = read.getReadString().charAt(currentBaseCallPosition);
				// debugging
				//System.out.println("currentMDElement: " + currentMDElement + " currentBaseCall: " + currentBaseCall);				
				if(currentMDElement.equals("A")) {
					if(currentBaseCall == 'C' || currentBaseCall == 'G' || 
					   currentBaseCall == 'T' || currentBaseCall == 'N') { 
						bases = "A" + currentBaseCall;
					}
				} else if (currentMDElement.equals("C")) {
					if(currentBaseCall == 'A' || currentBaseCall == 'G' || 
					   currentBaseCall == 'T' || currentBaseCall == 'N') { 
						bases = "C" + currentBaseCall;
					}
				} else if (currentMDElement.equals("G")) {
					if(currentBaseCall == 'A' || currentBaseCall == 'C' || 
					   currentBaseCall == 'T' || currentBaseCall == 'N') { 
						bases = "G" + currentBaseCall;
					}
				} else if (currentMDElement.equals("T")) {
					if(currentBaseCall == 'A' || currentBaseCall == 'C' || 
					   currentBaseCall == 'G' || currentBaseCall == 'N') { 
						bases = "T" + currentBaseCall;
					}
				} else if (currentMDElement.equals("N")) {
					bases = "N" + currentBaseCall;
				} else {
					// The first character is a number. Let's continue and see how many numbers 
					// we find
					boolean parsedMDElement = false;
					char c;
					while(currentMDElementPosition < mdString.length() && !parsedMDElement) {
						c = mdString.charAt(currentMDElementPosition);
						if(c >= '0' && c <= '9') {
							currentMDElement = currentMDElement + c;
							currentMDElementPosition++;
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
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMD.add(new CigarMDElement(temporaryMDElementLength, CigarMDOperator.MATCH, bases));					
				} else {
					cigarMD.add(new CigarMDElement(1, CigarMDOperator.MISMATCH, bases));
				}			
				currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
				temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
				temporaryMDElementLength = 0;
			} else {
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMD.add(new CigarMDElement(temporaryCigarElementLength, CigarMDOperator.MATCH, bases));
				} else {
					cigarMD.add(new CigarMDElement(1, CigarMDOperator.MISMATCH, bases));
				}
				currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
				temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
				temporaryCigarElementLength = 0;
			}
			
			// debugging
		    //System.out.println(cigarMD.toString());			
		}
	}

	/** Process the MD string once found the CIGAR operator I. */
	private void processMDtagCigarOperatorI(SAMRecord read) {
		// The MD string does not contain information regarding an insertion.
		String wronglyInsertedBases = read.getReadString().substring(
				currentBaseCallPosition,
				currentBaseCallPosition + currentCigarElementLength);
		currentBaseCallPosition = currentBaseCallPosition
				+ currentCigarElementLength;

//		 System.out.println("tempCigElem: " +
//		 String.valueOf(currentCigarElementLength) + "I ~ " + "parsedMDElem: "
//		 + currentMDElement + " ; length: " +
//		 String.valueOf(temporaryMDElementLength));
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.INSERTION, wronglyInsertedBases));
	}

	/** Process the MD string once found the CIGAR operator D. */
	private void processMDtagCigarOperatorD() {
		// Parse and extract the currenMDElement. It is a string starting with ^
		// and followed by a string of (A,C,G,T)
		// Extract the first character for the MD element.
		currentMDElement = String.valueOf(mdString
				.charAt(currentMDElementPosition));
		currentMDElementPosition++;

		// skip if the current MD element is zero. This is redundant information
		// if the CIGAR string is read too..
		while (isCurrentMDelementZero()) {
			currentMDElement = String.valueOf(mdString
					.charAt(currentMDElementPosition));
			currentMDElementPosition++;
		}

		if (!currentMDElement.equals("^")) {
			// this means an inconsistency between the CIGAR and MD string
			System.out
					.println("CigarMDGenerator: Error, ^ not found in the MD string when processing Cigar Operator D");
			return;
		}

		// The first character is a ^. There are exactly
		// temporaryCigarElementLength chars (A,C,G,T) to parse.
		currentMDElement = mdString.substring(currentMDElementPosition,
				currentMDElementPosition + currentCigarElementLength);
		currentMDElementPosition = currentMDElementPosition
				+ currentCigarElementLength;

		// TODO
		// Is this correct? I don't think we should include it, as we have a
		// deletion..
		// currentBaseCallPosition = currentBaseCallPosition +
		// currentCigarElementLength;

		// System.out.println("tempCigElem: " +
		// String.valueOf(currentCigarElementLength) + "D ~ " + "parsedMDElem: "
		// + currentMDElement + " ; length: " +
		// String.valueOf(currentMDElement.length()));
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.DELETION, currentMDElement));		

	}

	
	// Have to test the following code.	
	
	/** Process the MD string once found the CIGAR operator N. */
	private void processMDtagCigarOperatorN() {
		// As far as I know the MD string contains information regarding as skipped region in the 
		// reference, not in the read.
		// Therefore, there is nothing to retrieve here. Just copy the length of this skipped region 
		// in the read

//		 System.out.println("tempCigElem: " +
//		 String.valueOf(currentCigarElementLength) + "N ~ " + "parsedMDElem: "
//		 + currentMDElement + " ; length: " +
//		 String.valueOf(temporaryMDElementLength));
		currentBaseCallPosition = currentBaseCallPosition + currentCigarElementLength;
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.SKIPPED_REGION, ""));
	}

	/** Process the MD string once found the CIGAR operator S. */
	private void processMDtagCigarOperatorS() {
	}

	/** Process the MD string once found the CIGAR operator H. */
	private void processMDtagCigarOperatorH() {
	}

	/** Process the MD string once found the CIGAR operator P. */
	private void processMDtagCigarOperatorP() {
	}

	/** Process the MD string once found the CIGAR operator =. */
	private void processMDtagCigarOperatorEQ() {
	}

	/** Process the MD string once found the CIGAR operator X. */
	private void processMDtagCigarOperatorNEQ() {
	}

}
