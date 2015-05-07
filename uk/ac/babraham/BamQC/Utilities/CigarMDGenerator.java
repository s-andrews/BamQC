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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import test.java.uk.ac.babraham.BamQC.Modules.VariantCallDetectionTest;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;


/**
 * It calculates a string combining information from CIGAR and MD tag. The
 * format is: # (number), operator (m=match, u=mismatch, i=insertion,
 * d=deletion), bases if oper={u,i,d}. For instance: CIGAR: 32M2D5M1I52M MDtag:
 * 7G24^AA7C49 Combined CIGAR+MDTag: 7m1uGT24m2dAA5m1iG2m1uCA49m
 * 
 * Note: G in 1iG is SAM read- dependent.
 */
public class CigarMDGenerator {

	
	private static Logger log = Logger.getLogger(CigarMDGenerator.class);	
	
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
		 * The bit 0x10 must be evaluated in order to parse and collect statistics correctly. If so, 
		 * the correct positions for SNP/Indels must be calculated. 
		 */		

		// if Flag 0x4 is set, then the read is unmapped. Therefore, skip it for the reasons above.
		// Check the state of a flag bit 'READ_UNMAPPED_FLAG'. 
		if(read.getReadUnmappedFlag()) {
			log.debug("CigarMDGenerator: current SAM read does has flag 0x4 set on.");
			return;	
		}
		
		
		
		
		// Get the MD tag string.
		mdString = read.getStringAttribute("MD");
		if (mdString == null || mdString.equals("")) {
			log.debug("CigarMDGenerator: current SAM read " + read.getReadName() + " does not have MD tag string.");
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

			log.debug("Parsing CigarElement: " + String.valueOf(currentCigarElementLength) + currentCigarElementOperator.toString());
			if (currentCigarElementOperator.equals("M")) {
				if(!processMDtagCigarOperatorM(read)){
					cigarMD = new CigarMD();
					break;
				}
			} else if (currentCigarElementOperator.equals("I")) {
				processMDtagCigarOperatorI(read);
			} else if (currentCigarElementOperator.equals("D")) {
				if(!processMDtagCigarOperatorD(read)) {
					cigarMD = new CigarMD();
					break;
				}
			} else if (currentCigarElementOperator.equals("N")) {
				log.debug("CigarMDGenerator.java: extended CIGAR element N is currently unsupported.");
			} else if (currentCigarElementOperator.equals("S")) {
				log.debug("CigarMDGenerator.java: extended CIGAR element S is currently unsupported.");
			} else if (currentCigarElementOperator.equals("H")) {
				log.debug("CigarMDGenerator.java: extended CIGAR element H is currently unsupported.");
			} else if (currentCigarElementOperator.equals("P")) {
				log.debug("CigarMDGenerator.java: extended CIGAR element P is currently unsupported.");
			} else if (currentCigarElementOperator.equals("=")) {
				log.debug("CigarMDGenerator.java: extended CIGAR element = is currently unsupported.");
			} else if (currentCigarElementOperator.equals("X")) {
				log.debug("CigarMDGenerator.java: extended CIGAR element X is currently unsupported.");
			} else {
				log.warn("CigarMDGenerator.java: Found unknown operator in the CIGAR string.\n"
								+ "Unknown CigarOperator: "
								+ currentCigarElementOperator
								+ "\n"
								+ "found on read:" + read.toString() + "\n");
				// Possibly, throw an exception.
			}
		}
		
		
		// Check whether the read is paired in sequencing or not. 
		// Flag: 0x1 'READ_PAIRED_FLAG'
		if(read.getReadPairedFlag()) {
			// warning cases
			if(read.getFirstOfPairFlag() && read.getSecondOfPairFlag()) {
				log.warn("The read is part of a linear template, but it is neither the first nor the last read.");
			} 
			else if(!read.getFirstOfPairFlag() && !read.getSecondOfPairFlag()) {
				log.warn("The index of the read in the template is unknown. Non-linear template or index lost in data processing.");
			}
			// A read can be first/second (0x40/0x80) and forward/backward (0x10). 
			// If the read is first/backward(0x40+0x10) or second/forward(0x80), the CigarMD string must be reversed and complemented.
			// Check the state of a flag bits 'FIRST_OF_PAIR_FLAG' and 'READ_STRAND_FLAG'.
			else if(read.getFirstOfPairFlag() && read.getReadNegativeStrandFlag()) {
				log.debug("Current SAM read is FIRST(0x40) and parsed BACKWARD(0x10).");
				reverseComplementCigarMD();
			}
			else if(read.getSecondOfPairFlag() && !read.getReadNegativeStrandFlag()) {
				log.debug("Current SAM read is SECOND(0x80) and parsed FORWARD.");
				reverseComplementCigarMD();
			}
			// otherwise: 
			// the read is FIRST AND FORWARD or SECOND AND BACKWARD. In these two cases, no reverse complement is needed.	        
		} else {
			if(read.getReadNegativeStrandFlag()) {
				log.debug("CigarMDGenerator: current SAM read is parsed BACKWARD(0x10).");
				reverseComplementCigarMD();				
			}
		}

		log.debug("CigarMD string: " + cigarMD.toString());
	}

	/**
	 * Complements a base (e.g. "ACGT" => "TGCA")
	 * @param bases
	 * @return the complement of bases
	 */
	private void baseComplement(StringBuilder bases) {
		for(int i = 0; i < bases.length(); i++) {
			if(bases.charAt(i) == 'A') bases.replace(i, i+1, "T");
			else if(bases.charAt(i) == 'C') bases.replace(i, i+1, "G");
			else if(bases.charAt(i) == 'G') bases.replace(i, i+1, "C");
			else if(bases.charAt(i) == 'T') bases.replace(i, i+1, "A");	
		}
	}	
	
	
	/**
	 * It reverse and complement the CigarMD string if the Flag 0x10 is set on.
	 */
	private void reverseComplementCigarMD() {
		List<CigarMDElement> oldCigarMDElements = cigarMD.getCigarMDElements();
		int listLength = oldCigarMDElements.size();
		List<CigarMDElement> newCigarMDElements = new ArrayList<CigarMDElement>(listLength);
		CigarMDElement oldElement;
		
		for(int i = listLength - 1; i >= 0; i--) {
			oldElement = oldCigarMDElements.get(i);
			if(oldElement.getOperator() == CigarMDOperator.MATCH) {
				// don't do anything
				newCigarMDElements.add(oldElement);
				
			} else if(oldElement.getOperator() == CigarMDOperator.MISMATCH) {
				// reverse and complement the bases couples
				String oldBases = oldElement.getBases();
				StringBuilder newBases = new StringBuilder();
				int mutations = oldElement.getLength();
				for(int j = 0; j < mutations; j++) {
					//newBases = baseComplement(oldBases.substring(j*2, j*2+2)) + newBases;
					newBases.insert(0, oldBases.substring(j*2, j*2+2));					
				}
				baseComplement(newBases);			
				newCigarMDElements.add(new CigarMDElement(mutations, CigarMDOperator.MISMATCH, newBases.toString()));
				
			} else if(oldElement.getOperator() == CigarMDOperator.INSERTION) {
				// reverse and complement the bases
				StringBuilder newBases = new StringBuilder(new StringBuffer(oldElement.getBases()).reverse().toString());
				baseComplement(newBases);
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.INSERTION, newBases.toString()));

			} else if(oldElement.getOperator() == CigarMDOperator.DELETION) {
				// reverse and complement the bases
				StringBuilder newBases = new StringBuilder(new StringBuffer(oldElement.getBases()).reverse().toString());
				baseComplement(newBases);
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.DELETION, newBases.toString()));

			} else if(oldElement.getOperator() == CigarMDOperator.SKIPPED_REGION) {
				// don't do anything				
				newCigarMDElements.add(oldElement);

			} else if(oldElement.getOperator() == CigarMDOperator.SOFT_CLIP) {
				// TODO: THIS CASE DOES NOT EVER HAPPEN AS IT IS NOT IMPLEMENTED. IN THE FUTURE IT NEEDS REVISION 				
				// reverse the bases
				String newBases = new StringBuffer(oldElement.getBases()).reverse().toString();
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.SOFT_CLIP, newBases));

			} else if(oldElement.getOperator() == CigarMDOperator.HARD_CLIP) {
				// TODO: THIS CASE DOES NOT EVER HAPPEN AS IT IS NOT IMPLEMENTED. IN THE FUTURE IT NEEDS REVISION 				
				// reverse the bases
				String newBases = new StringBuffer(oldElement.getBases()).reverse().toString();
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.HARD_CLIP, newBases));

			} else if(oldElement.getOperator() == CigarMDOperator.PADDING) {
				// TODO: THIS CASE DOES NOT EVER HAPPEN AS IT IS NOT IMPLEMENTED. IN THE FUTURE IT NEEDS REVISION 				
				// reverse the bases
				String newBases = new StringBuffer(oldElement.getBases()).reverse().toString();
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.PADDING, newBases));

			} else if(oldElement.getOperator() == CigarMDOperator.eq) {
				// TODO: THIS CASE DOES NOT EVER HAPPEN AS IT IS NOT IMPLEMENTED. IN THE FUTURE IT NEEDS REVISION 				
				// reverse the bases
				String newBases = new StringBuffer(oldElement.getBases()).reverse().toString();
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.eq, newBases));

			} else if(oldElement.getOperator() == CigarMDOperator.x) {
				// TODO: THIS CASE DOES NOT EVER HAPPEN AS IT IS NOT IMPLEMENTED. IN THE FUTURE IT NEEDS REVISION 
				// reverse the bases
				String newBases = new StringBuffer(oldElement.getBases()).reverse().toString();
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.x, newBases));
			} 

		}
		cigarMD = new CigarMD(newCigarMDElements);	
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
	private boolean processMDtagCigarOperatorM(SAMRecord read) { 
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
				
				if(mdString.length() <= currentMDElementPosition) {
					log.warn("MD tag string is shorter than expected. Cigar : " + read.getCigarString() + ", mdString : " + mdString.toString()
							+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
					return false;
				}
				
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
					log.debug("currentBaseCallPosition: " + currentBaseCallPosition);
					if("ACGTN".indexOf("" + currentMDChar) > -1) {
						bases = bases + currentMDChar + currentBaseCall;			
					} else {
						log.warn("Found unrecognised mutation " + 
						                   currentMDChar + "->" + currentBaseCallPosition + " inside SAM record " + read.toString());
						//bases = bases + '?' + '?';
						return false;
					}				
					temporaryMDElementLength++;
					
					// Let's continue and see how many mismatches we find.
					// The number of mismatches will be the temporaryMDElementLength, whereas the variable 
					// bases will store the mismatched couples (ReferenceBase,ReadBaseMutation).
					boolean allContiguousMutationsFound = false;
					while(currentMDElementPosition < mdString.length() && !allContiguousMutationsFound) {		
						currentMDChar = mdString.charAt(currentMDElementPosition);
						if("ACGTN".indexOf("" + currentMDChar) > -1) {
							log.debug("currentMDElement: " + currentMDElement + " currentBaseCall: " + currentBaseCall);								
							log.debug("currentBaseCallPosition: " + (currentBaseCallPosition+temporaryMDElementLength));
							currentBaseCall = read.getReadString().charAt(currentBaseCallPosition+temporaryMDElementLength);
							bases = bases + currentMDChar + currentBaseCall;
							temporaryMDElementLength++;
							currentMDElementPosition++;
						} else if(currentMDChar == '0' && temporaryMDElementLength < temporaryCigarElementLength) {
							// temporaryMDElementLength < temporaryCigarElementLength is needed to assess that we 
							// are still parsing the Cigar operator M, and not something else. 
							currentMDElementPosition++;
						} else {
							allContiguousMutationsFound = true;
						}			
					}
				}
			}
							
			
			log.debug("tempCigElem: " + String.valueOf(temporaryCigarElementLength) + "M ~ " + "parsedMDElem: " + currentMDElement + " ; length: " + String.valueOf(temporaryMDElementLength));
			
			// update the position of the currentBaseCall and the parser.
			if(temporaryMDElementLength <= temporaryCigarElementLength) {
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMD.add(new CigarMDElement(temporaryMDElementLength, CigarMDOperator.MATCH, bases));					
				} else {
					cigarMD.add(new CigarMDElement(temporaryMDElementLength, CigarMDOperator.MISMATCH, bases));
					
					if(bases.equals("")) {
						log.warn("MD tag string is shorter than expected. Cigar : " + read.getCigarString() + ", mdString : " + mdString.toString()
								+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
					}
					
				}			
				currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
				temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
				temporaryMDElementLength = 0;
			} else {
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					cigarMD.add(new CigarMDElement(temporaryCigarElementLength, CigarMDOperator.MATCH, bases));
				} else {
					cigarMD.add(new CigarMDElement(temporaryCigarElementLength, CigarMDOperator.MISMATCH, bases));
					
					
					if(bases.equals("")) {
						log.warn("MD tag string is shorter than expected. Cigar : " + read.getCigarString() + ", mdString : " + mdString.toString()
								+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
					}
					
					
				}
				currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
				temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
				temporaryCigarElementLength = 0;
			}
			
			log.debug(cigarMD.toString());	
		}
		return true;
	}
	
	
	/** Process the MD string once found the CIGAR operator I. */
	private void processMDtagCigarOperatorI(SAMRecord read) {
		// The MD string does not contain information regarding an insertion.
		String wronglyInsertedBases = read.getReadString().substring(
				currentBaseCallPosition,
				currentBaseCallPosition + currentCigarElementLength);
		currentBaseCallPosition = currentBaseCallPosition
				+ currentCigarElementLength;

		log.debug("tempCigElem: " + String.valueOf(currentCigarElementLength) + "I ~ " + "parsedMDElem: "
		 + currentMDElement + " ; length: " + String.valueOf(temporaryMDElementLength));
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.INSERTION, wronglyInsertedBases));
	}

	
	/** Process the MD string once found the CIGAR operator D. */
	private boolean processMDtagCigarOperatorD(SAMRecord read) {
		if(temporaryMDElementLength != 0) {
			// There is an inconsistency between Cigar and MD strings. 
			// If the currentCigarElement is D, temporaryMDElementLength should be 0.
			log.warn("Previous MD element not processed completely. ^ not found in the MD string while processing Cigar-Oper D. Cigar : " + read.getCigarString() + ", mdString : " + mdString.toString()
					+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
			return false;
		}
		// Parse and extract the currenMDElement. It is a string starting with ^
		// and followed by a string of (A,C,G,T)
		// Extract the first character for the MD element.
		currentMDElement = String.valueOf(mdString
				.charAt(currentMDElementPosition));
		currentMDElementPosition++;

		// skip if the current MD element is zero. This is redundant information
		// if the CIGAR string is read too..
		while (isCurrentMDelementZero()) {
			if(mdString.length() <= currentMDElementPosition) {
				log.warn("MD tag string is shorter than expected (1). Cigar : " + read.getCigarString() + ", mdString : " + mdString.toString()
						+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
				return false;
			}
			currentMDElement = String.valueOf(mdString
					.charAt(currentMDElementPosition));
			currentMDElementPosition++;
		}

		if (!currentMDElement.equals("^")) {
			// this means an inconsistency between the CIGAR and MD string
			log.warn("^ not found in the MD string when processing Cigar-Oper D. Cigar : " + read.getCigarString() + ", mdString : " + mdString.toString()
					+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
			return false;
		}
		if(mdString.length() < currentMDElementPosition + currentCigarElementLength) {
			log.warn("MD tag string is shorter than expected (2). Cigar : " + read.getCigarString() + ", mdString : " + mdString.toString()
					+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
			return false;
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

		log.debug("tempCigElem: " + String.valueOf(currentCigarElementLength) + "D ~ " + "parsedMDElem: "
		+ currentMDElement + " ; length: " +
		String.valueOf(currentMDElement.length()));
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.DELETION, currentMDElement));		
		return true;
	}

	
	// Have to test the following code.	
	
	/** Process the MD string once found the CIGAR operator N. */
	private void processMDtagCigarOperatorN() {
		// As far as I know the MD string contains information regarding as skipped region in the 
		// reference, not in the read.
		// Therefore, there is nothing to retrieve here. Just copy the length of this skipped region 
		// in the read

		log.debug("tempCigElem: " +
		 String.valueOf(currentCigarElementLength) + "N ~ " + "parsedMDElem: "
		 + currentMDElement + " ; length: " +
		 String.valueOf(temporaryMDElementLength));
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
