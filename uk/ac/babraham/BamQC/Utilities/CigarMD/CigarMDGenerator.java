/**
 * Copyright Copyright 2015 Simon Andrews
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
 * - Piero Dalle Pezze: Class creation.
 */
package uk.ac.babraham.BamQC.Utilities.CigarMD;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;


/**
 * It calculates a string combining information from CIGAR and MD strings. The
 * format is: # (number), operator (m=match, u=mismatch, i=insertion,
 * d=deletion), bases if oper={u,i,d}. For instance: CIGAR: 32M2D5M1I52M MDtag:
 * 7G24^AA7C49 Combined CIGAR+MDTag: 7m1uGT24m2dAA5m1iG2m1uCA49m
 * 
 * Note: G in 1iG is SAM read- dependent.
 * @author Piero Dalle Pezze
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
	// The operator for the current Cigar element
	private CigarOperator currentCigarElementOperator = null;
	// The temporary processed length of the processed MD tag element
	private int temporaryMDElementLength = 0;
	// The current processed position of the processed MD tag (for the parser)
	private int currentMDElementPosition = 0;
	// The current base call position of the read
	private int currentBaseCallPosition = 0;

	// Data fields storing the CigarMD object combining Cigar and MD strings.
	private CigarMD cigarMD = null;
	
	// If the read is a first or second segment.
	private boolean isFirst = true;
	
	// The read string. This can be quite long and read.getReadString() can be 
	// quite time consuming as it copies the string every time. Here we copy it 
	// one time only.
	private String readString = null;
	// local variables declared here for limiting variable declarations in the method computeCigarMDTag().
	private Cigar cigar = null;
	private int cigarListSize = 0;
	
	// 0: no error, 1: unmapped read, 2: read without MD string, 3: read without Cigar, 4: Cigar/MD/read inconsistencies
	private int errorType = 0;
	

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
	 * It returns the error type. 0: no error, 1: unmapped read, 2: read without MD string, 
	 * 3: read without Cigar, 4: Cigar/MD/read inconsistencies.
	 * @return the error type.
	 */
	public int getErrorType() {
		return errorType;
	}
	
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

	/**
	 * Returns true if the read is a first segment, false if it is a second.
	 * @return
	 */
	public boolean isFirst() {
		return isFirst;
	}
	
	// computing methods
	/**
	 * It generates a string combining information from CIGAR and MD tag. This CigarMD string is null if it could not be created.
	 * In this last case, an error message can be retrieved using the message getErrorType().
	 */
	public void generateCigarMD(SAMRecord read) {
		reset();
		if(!computeCigarMDTag(read)) {
			if(errorType == 0) {
				// if we are here, we detected one of a broad range of errors due to inconsistencies between Cigar/MD/read strings.
				errorType = 4;
				cigarMD = null;
			}
		}
	}

	// Private methods here
	/**
	 * It generates a string combining information from CIGAR and MD tag.
	 * @ return true if the CigarMD string has been computed, false otherwise.
	 */
	private boolean computeCigarMDTag(SAMRecord read) {
	
		// Get the read string
		readString = read.getReadString();
		
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

		// Let's do some preliminary tests
		// if Flag 0x4 is set, then the read is unmapped. Therefore, skip it for the reasons above.
		// Check the state of a flag bit 'READ_UNMAPPED_FLAG'. 
		if(read.getReadUnmappedFlag()) {
			log.debug("Read " + readString + " is unmapped and therefore skipped.");
			errorType = 1;
			return false;	
		}
		
		// Get the MD tag string. It is more likely errors are in the MD rather than the Cigar. Let's put this first.
		mdString = read.getStringAttribute("MD");
		if (mdString == null || mdString.length() == 0) {
			log.debug("Read " + readString + " does not have MD string.");
			errorType = 2;
			// TODO return false;
			mdString = null;
		} else {
			// In some reads the bases in the MD string can be in lower case. The read and cigar strings are already set to upper case 
			// for us by the samtools library. This doesn't happen with the mdString though. Let's set this to upper case once for all now, 
			// so we don't have to worry about it later.
			mdString = mdString.toUpperCase(Locale.ENGLISH);
		}
		
		
		// Get the CIGAR list
		cigar = read.getCigar();
		if (cigar == null || read.getCigarLength() == 0) {
			log.debug("Read " + readString + " does not have Cigar string.");
			errorType = 3;
			return false;
		}
		
		
		cigarList = cigar.getCigarElements();
			
		// setup a new CigarMD string
		cigarMD = new CigarMD();
		
		
		
		// Use the old c-style for loop for memory (garbage collector) and CPU efficiency
		// Iterate the CigarList
//		Iterator<CigarElement> iterCigar = cigarList.iterator();
//		// Loop over the CigarElement objects of the read Cigar
//		while (iterCigar.hasNext()) {
//			currentCigarElement = iterCigar.next();
			
		cigarListSize = cigarList.size();
		for(int i=0; i<cigarListSize; i++) {
			
			currentCigarElement = cigarList.get(i);
			
			currentCigarElementLength = currentCigarElement.getLength();
			currentCigarElementOperator = currentCigarElement.getOperator();
			
			//log.debug("Parsing CigarElement: " + currentCigarElementLength + currentCigarElementOperator.toString());
			
			if (currentCigarElementOperator == CigarOperator.MATCH_OR_MISMATCH) {
				if(!processMDtagCigarOperatorM(read)){
					return false;
				}
					
			} else if (currentCigarElementOperator == CigarOperator.INSERTION) {
				if(!processMDtagCigarOperatorI(read)) {
					return false;
				}
							
			} else if (currentCigarElementOperator == CigarOperator.DELETION) {
				if(!processMDtagCigarOperatorD(read)) {
					return false;
				}
				
			} else if (currentCigarElementOperator == CigarOperator.SKIPPED_REGION) {
				processMDtagCigarOperatorN();
				
			} else if (currentCigarElementOperator == CigarOperator.SOFT_CLIP) {
				processMDtagCigarOperatorS();
				
			} else if (currentCigarElementOperator == CigarOperator.HARD_CLIP) {
				processMDtagCigarOperatorH();
				
			} else if (currentCigarElementOperator == CigarOperator.PADDING) {
				processMDtagCigarOperatorP();
				
			} else if (currentCigarElementOperator == CigarOperator.EQ) {
				log.debug("Extended CIGAR element = is currently unsupported.");
				return false;
				
			} else if (currentCigarElementOperator == CigarOperator.X) {
				log.debug("Extended CIGAR element X is currently unsupported.");
				return false;				
				
			} else {
				log.info("Unknown Cigar operator " +currentCigarElementOperator.toString()+ " in read " + readString + "\n");
				return false;
			}
		}
		
		// Let's do some tests to see whether something is wrong..
		if(currentBaseCallPosition < readString.length()) {
			log.info("Cigar string " + read.getCigarString() + " length " + currentBaseCallPosition + " < read length " + readString.length() 
					+ ". mdString : " + mdString + ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
			return false;
		}

		if(currentBaseCallPosition > readString.length()) {
			log.info("Cigar string " + read.getCigarString() + " length " + currentBaseCallPosition + " > read length " + readString.length() 
					+ ". mdString : " + mdString + ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
			return false;
		}
		
		if(mdString != null && temporaryMDElementLength > 0) {
			log.info("MD string " + mdString + " > Cigar string " + read.getCigarString() + ". CurrentCigarElement : " 
					 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
			return false;
		}
		
		
		// Check whether the read is paired in sequencing or not. 
		// Flag: 0x1 'READ_PAIRED_FLAG'
		if(read.getReadPairedFlag()) {
			// A read can be first/second (0x40/0x80) and forward/backward (0x10). 
			// If the read is first/backward(0x40+0x10) or second/forward(0x80), the CigarMD string must be reversed and complemented.
			// Check the state of a flag bits 'FIRST_OF_PAIR_FLAG' and 'READ_STRAND_FLAG'.
			if(read.getFirstOfPairFlag()) {
				isFirst = true;
				// it is a first segment.
				if(read.getSecondOfPairFlag()) {
					// .. but it is also a second segment
					log.debug("Read " + readString + " is part of a linear template, but it is neither the first nor the last read.");
				} else if(read.getReadNegativeStrandFlag()) {
					// it is reversed and complemented
					//log.debug("Current SAM read is FIRST(0x40) and parsed BACKWARD(0x10).");
					reverseComplementCigarMD();
				} 
				// else it is a FIRST FORWARD. We don't do anything.
			} 
			else  // it is NOT a first segment
				if(!read.getSecondOfPairFlag()) {
					// .. but it is NOT a second segment either
					isFirst = true; // let's leave it as first.
					log.debug("The index for the read " + readString + " in the template is unknown. Non-linear template or index lost in data processing.");
				} else {
					// it is a second segment.
					isFirst = false;
					if(!read.getReadNegativeStrandFlag()) {
						// it is reversed and complemented
						//log.debug("Current SAM read is SECOND(0x80) and parsed FORWARD.");
						reverseComplementCigarMD();				
					}
					// else it is a SECOND BACKWARD. We don't do anything.
				}
			// otherwise: 
			// the read is FIRST AND FORWARD or SECOND AND BACKWARD. In these two cases, no reverse complement is needed.			
		} else {
			// the read is unpaired. Treat as a first segment because it is the only one.
			isFirst = true;			
			if(read.getReadNegativeStrandFlag()) {
				// it is reversed and complemented
				//log.debug("CigarMDGenerator: current SAM read is parsed BACKWARD(0x10).");
				reverseComplementCigarMD();				
			}
		}	
		//log.debug("CigarMD string: " + cigarMD.toString());
		return true;
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
	 * It reverse and complement the CigarMD string if the Flag 0x10 or 0x80 are set on. 
	 */
	private void reverseComplementCigarMD() {
		List<CigarMDElement> oldCigarMDElements = cigarMD.getCigarMDElements();
		int listLength = oldCigarMDElements.size();
		List<CigarMDElement> newCigarMDElements = new ArrayList<CigarMDElement>(listLength);
		CigarMDElement oldElement = null;
		StringBuilder newBases = null;
		String oldBases = null;
		int mutations = 0;
		
		for(int i = listLength - 1; i >= 0; i--) {
			oldElement = oldCigarMDElements.get(i);
			if(oldElement.getOperator() == CigarMDOperator.MATCH) {
				// As we do not record the bases, don't do anything
				newCigarMDElements.add(oldElement);
				
			} else if(oldElement.getOperator() == CigarMDOperator.MISMATCH) {
				// reverse and complement the bases couples
				oldBases = oldElement.getBases();
				mutations = oldElement.getLength();
				newBases = new StringBuilder(oldBases.length());
				for(int j = 0; j < mutations; j++) {
					newBases.insert(0, oldBases.substring(j*2, j*2+2));					
				}
				baseComplement(newBases);			
				newCigarMDElements.add(new CigarMDElement(mutations, CigarMDOperator.MISMATCH, newBases.toString()));
				
			} else if(oldElement.getOperator() == CigarMDOperator.INSERTION) {
				// reverse and complement the bases
				newBases = new StringBuilder(oldElement.getBases());
				baseComplement(newBases.reverse());
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.INSERTION, newBases.toString()));

			} else if(oldElement.getOperator() == CigarMDOperator.DELETION) {
				// reverse and complement the bases
				newBases = new StringBuilder(oldElement.getBases());
				baseComplement(newBases.reverse());
				newCigarMDElements.add(new CigarMDElement(oldElement.getLength(), CigarMDOperator.DELETION, newBases.toString()));

			} else if(oldElement.getOperator() == CigarMDOperator.SKIPPED_REGION) {
				// As we do not record the bases, don't do anything		
				newCigarMDElements.add(oldElement);

			} else if(oldElement.getOperator() == CigarMDOperator.SOFT_CLIP) {
				// As we do not record the bases, don't do anything		
				newCigarMDElements.add(oldElement);

			} else if(oldElement.getOperator() == CigarMDOperator.HARD_CLIP) {
				// As we do not record the bases, don't do anything			
				newCigarMDElements.add(oldElement);

			} else if(oldElement.getOperator() == CigarMDOperator.PADDING) {
				// As we do not record the bases, don't do anything			
				newCigarMDElements.add(oldElement);

			} else if(oldElement.getOperator() == CigarMDOperator.eq) {
				// TODO: THIS CASE DOES NOT EVER HAPPEN AS IT IS NOT IMPLEMENTED. IN THE FUTURE IT NEEDS REVISION 				
				newCigarMDElements.add(oldElement);

			} else if(oldElement.getOperator() == CigarMDOperator.x) {
				// TODO: THIS CASE DOES NOT EVER HAPPEN AS IT IS NOT IMPLEMENTED. IN THE FUTURE IT NEEDS REVISION 
				newCigarMDElements.add(oldElement);
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
		temporaryMDElementLength = 0;
		currentMDElementPosition = 0;
		currentBaseCallPosition = 0;
		readString = null;
		cigarMD = null;
		errorType = 0;
	}

		
	

	// These methods process the MD string for each CIGAR operator.
	
	/** Add a new Match element to the CigarMD Object. */
	private int addMatchToCigarMD(int temporaryCigarElementLength) {
		// update the position of the currentBaseCall and the parser.
		if(mdString != null && temporaryMDElementLength <= temporaryCigarElementLength) {
			cigarMD.add(new CigarMDElement(temporaryMDElementLength, CigarMDOperator.MATCH, ""));					
			currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
			temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
			temporaryMDElementLength = 0;
		} else {
			cigarMD.add(new CigarMDElement(temporaryCigarElementLength, CigarMDOperator.MATCH, ""));
			currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
			temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
			temporaryCigarElementLength = 0;
		}
		return temporaryCigarElementLength;
	}
	
	/** Add a new Mismatch element to the CigarMD Object. */
	private int addMismatchToCigarMD(int temporaryCigarElementLength, StringBuilder bases) {
		// update the position of the currentBaseCall and the parser.
		if(temporaryMDElementLength <= temporaryCigarElementLength) {
			cigarMD.add(new CigarMDElement(temporaryMDElementLength, CigarMDOperator.MISMATCH, bases.toString()));
			currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
			temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
			temporaryMDElementLength = 0;
		} else {
			cigarMD.add(new CigarMDElement(temporaryCigarElementLength, CigarMDOperator.MISMATCH, bases.toString()));
			currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
			temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
			temporaryCigarElementLength = 0;
		}
		return temporaryCigarElementLength;
	}
	
	
	/** Process the MD string once found the CIGAR operator M. */
	private boolean processMDtagCigarOperatorM(SAMRecord read) { 
		// The temporary length of the current Cigar element
		int temporaryCigarElementLength = currentCigarElementLength;

		while(temporaryCigarElementLength > 0) {
						
			if(mdString != null && temporaryMDElementLength == 0) {
				// PARSE A NEW MD ELEMENT
				
				// It is either a number [=>MATCH] or a char (A,C,G,T) [=>MISMATCH]
				// Extract the first character for the MD element.
				// Only parse the next element of MD Tag string if this current has been completed. 
				// This is required as MD string does not record insertions, whilst Cigar string does.
				
				if(mdString.length() <= currentMDElementPosition) {
					if(currentBaseCallPosition + temporaryMDElementLength > readString.length()) {
						log.info("Cigar string " + read.getCigarString() + " length " + (currentBaseCallPosition + temporaryMDElementLength) + " > read length " + readString.length() 
								+ ". mdString : " + mdString + ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
						return false;
					}
					log.info("MD string " + mdString + " < Cigar string " + read.getCigarString() + ". CurrentCigarElement : " 
							 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
					return false;
				}
				
				// extract new MD element char by char
				char currentMDChar = mdString.charAt(currentMDElementPosition);
				currentMDElementPosition++;
				
		
		     	// skip if the retrieved MD element char is zero. This is redundant information if the CIGAR string is read too..
				if(currentMDChar == '0') {
					continue;
				}
				
				// the MD character is not 0. Therefore, we need to process it!
				// Either we have a number (MATCH: 1 or more digits) or a string of bases (MISMATCH: 1 or more chars)

				
				if(currentMDChar >= '1' && currentMDChar <= '9') {
					// CASE 1: The first character is a number. Therefore, we are parsing MATCHED bases.
					// Let's continue and see how many numbers we find.
					// This comprehensive number is the temporaryMDElementLength, which tells us 
					// how many matched bases we have.
					// Capacity initially set to 3: 3 digits of matches (up to 999!)
					StringBuilder mdNumber = new StringBuilder(3);
					mdNumber.append(currentMDChar);
					while(currentMDElementPosition < mdString.length()) {
						currentMDChar = mdString.charAt(currentMDElementPosition);
						if(currentMDChar >= '0' && currentMDChar <= '9') {
							mdNumber.append(currentMDChar);
							currentMDElementPosition++;
						} else {
							// c is something else. The MD Element has been parsed.
							break;
						}
					}

					temporaryMDElementLength = Integer.parseInt(mdNumber.toString());
					
					// add the new MATCH element to the CigarMD string and update the temporaryCigarElementLength
					temporaryCigarElementLength = addMatchToCigarMD(temporaryCigarElementLength);	
					
				} else {
					// CASE 2: The first character is a char. Therefore, we are parsing MISMATCHED bases or mutations.
					// If there is a mutation on the read bases with respect to the reference, we indicate this as:
					// RefBaseMutBase (base on the reference -> mutated base on the read). 
					// For instance: an element of CigarMD with operator 'u' is 1uCA IF the base C on the reference 
					// is mutated into the base A on the aligned read base. In the case of two contiguous mutation, this 
					// can be coded as 2uCAGT if the reference string CA are mutated into GT.
					
					
					if(currentBaseCallPosition >= readString.length()) {
						log.info("MD string " + mdString + " length "+currentBaseCallPosition+" > read " + readString + " length "+readString.length()+". CurrentCigarElement : " 
								 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
						return false;
					}
					
					// The bases for this mismatch
					// Capacity initially set to 8: 8 contiguous SNPs!
					StringBuilder bases = new StringBuilder(8);
					// Retrieve the mutation and create the first couple of mutated bases.
					char currentBaseCall = readString.charAt(currentBaseCallPosition);
					//log.debug("currentBaseCallPosition: " + currentBaseCallPosition);
					if(currentMDChar == 'A' || currentMDChar == 'C' || currentMDChar == 'G' || currentMDChar == 'T' || currentMDChar == 'N') {
						if(currentMDChar == currentBaseCall) {
							//error case : FALSE POSITIVE
							log.info("Expected mutation " + currentMDChar + " at position " + (currentMDElementPosition-1) + " in MD string " + mdString + " but found same base " 
							+ currentBaseCall + " in read position " + currentBaseCallPosition + ". Cigar : " + read.getCigarString()
									+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
							return false;
						}
						bases.append(currentMDChar).append(currentBaseCall);			
					} else {
						log.info("Expected mutation but found " + currentMDChar + " at position " + (currentMDElementPosition-1) + " in MD string " + mdString + ". Cigar : " + read.getCigarString()
										+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
						//bases.append('?').append('?');
						return false;
					}				
					temporaryMDElementLength++;
					
					// Let's continue and see how many mismatches we find.
					// The number of mismatches will be the temporaryMDElementLength, whereas the variable 
					// bases will store the mismatched couples (ReferenceBase,ReadBaseMutation).
					while(currentMDElementPosition < mdString.length()) {		
						currentMDChar = mdString.charAt(currentMDElementPosition);
						if(currentMDChar == 'A' || currentMDChar == 'C' || currentMDChar == 'G' || currentMDChar == 'T' || currentMDChar == 'N') {
							if(currentBaseCallPosition+temporaryMDElementLength >= readString.length()) {
								log.info("MD string " + mdString + " length "+currentBaseCallPosition+temporaryMDElementLength+" > read " + readString + " length "+readString.length()+". CurrentCigarElement : " 
										 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
								return false;
							}
							currentBaseCall = readString.charAt(currentBaseCallPosition+temporaryMDElementLength);
							if(currentMDChar == currentBaseCall) {
								//error case : FALSE POSITIVE
								log.info("Expected mutation " + currentMDChar + " at position " + currentMDElementPosition + " in MD string " + mdString + " but found base " 
								+ currentBaseCall + " in read position " + (currentBaseCallPosition+temporaryMDElementLength) + ". Cigar : " + read.getCigarString()
										+ ", CurrentCigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
								return false;
							}
							bases.append(currentMDChar).append(currentBaseCall);							
							temporaryMDElementLength++;
							currentMDElementPosition++;
						} else if(currentMDChar == '0' && temporaryMDElementLength < temporaryCigarElementLength) {
							// temporaryMDElementLength < temporaryCigarElementLength is needed to assess that we 
							// are still parsing the Cigar operator M, and not something else. 
							currentMDElementPosition++;
						} else {
							break;
						}			
					}
					
					// update the position of the currentBaseCall and the parser.
					if(bases.length() == 0) {
						//error case
						log.info("MD string " + mdString + " < Cigar string " + read.getCigarString() + ". CurrentCigarElement : " 
								 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
						return false;
					}
					// add the new MISMATCH element to the CigarMD string and update the temporaryCigarElementLength
					temporaryCigarElementLength = addMismatchToCigarMD(temporaryCigarElementLength, bases);

				}
			} else {
				// DO NOT PARSE A NEW MD ELEMENT
				
				// IF WE HAVE the mdString, the MD Element is a MATCH. This because temporaryMDElementLength > 0 and the MD string only reports numbers for matches. 
				// Therefore, no mismatched bases to compute for this case.
				
				// IF WE DO NOT HAVE the mdString, this case is appropriate anyway.
				
				// add the new MATCH element to the CigarMD string and update the temporaryCigarElementLength
				temporaryCigarElementLength = addMatchToCigarMD(temporaryCigarElementLength);
			}
			
			//log.debug(cigarMD.toString());
			
		}
		return true;
	}
	
	
	/** Process the MD string once found the CIGAR operator I. */
	private boolean processMDtagCigarOperatorI(SAMRecord read) {
		// The MD string does not contain information regarding an insertion.
		// NOTE: readString is already in upper case by samtools library, even if in the file, the read was in lowercase.
		// therefore, we do not need to worry about this.
		String insertedBases = readString.substring(
				currentBaseCallPosition,
				currentBaseCallPosition + currentCigarElementLength);
		currentBaseCallPosition = currentBaseCallPosition
				+ currentCigarElementLength;
		
		for(int i=0; i<insertedBases.length(); i++) {
			char c = insertedBases.charAt(i);
			if(c != 'A' && c != 'C' && c != 'G' && c != 'T' && c != 'N') {
				log.info("Read " + readString + " contains unknown inserted bases ("+insertedBases+"). Cigar string " + read.getCigarString() + ". CurrentCigarElement : " 
						 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
				return false;
			}
		}

		//log.debug("tempCigElem: " + currentCigarElementLength + "I ~ " + " ; length: " + temporaryMDElementLength);
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.INSERTION, insertedBases));
		return true;
	}

	
	/** Process the MD string once found the CIGAR operator D. */
	private boolean processMDtagCigarOperatorD(SAMRecord read) {
		if(mdString != null) {
			if(temporaryMDElementLength != 0) {
				// There is an inconsistency between Cigar and MD strings. 
				// If the currentCigarElement is D, temporaryMDElementLength should be 0.
				log.info("MD string " + mdString + " contains more matches/mismatches than Cigar string " + read.getCigarString() 
						+ ". CigarElement : " + currentCigarElement.getLength() + currentCigarElement.getOperator().toString()
						+ ", MD string position : " + currentMDElementPosition + ". Base call position : " + currentBaseCallPosition); 
				return false;
			}
			// Parse and extract the current MD Element. It is a string starting with ^
			// and followed by a string of (A,C,G,T)
			// Extract the first character for the MD element.
			char currentMDChar = mdString.charAt(currentMDElementPosition);
			currentMDElementPosition++;
	
			// skip if the current MD element is zero. This is redundant information
			// if the CIGAR string is read too..
			while (currentMDChar == '0') {
				if(mdString.length() <= currentMDElementPosition) {
					log.info("MD string " + mdString + " < Cigar string " + read.getCigarString() + ". CurrentCigarElement : " 
							 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
					return false;
				}
				currentMDChar = mdString.charAt(currentMDElementPosition);
				currentMDElementPosition++;
			}
	
			if (currentMDChar != '^') {
				// this means an inconsistency between the CIGAR and MD string
				log.info("^ not found in the MD string " + mdString + " when processing the CigarElement : " 
				        + currentCigarElement.getLength() + currentCigarElement.getOperator().toString() 
				        + " in the Cigar String " + read.getCigarString());
				return false;
			}
			if(mdString.length() < currentMDElementPosition + currentCigarElementLength) {
				log.info("MD string " + mdString + " < Cigar string " + read.getCigarString() + ". CurrentCigarElement : " 
						 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
				return false;
			}
			
			// The first character is a ^. There are exactly
			// temporaryCigarElementLength chars (A,C,G,T) to parse.
			// Let's be nice with programs setting the mdString bases in lower case. 
			String deletedBases = mdString.substring(currentMDElementPosition,
					currentMDElementPosition + currentCigarElementLength);
			for(int i=0; i<deletedBases.length(); i++) {
				char c = deletedBases.charAt(i);
				if(c != 'A' && c != 'C' && c != 'G' && c != 'T' && c != 'N') {
					log.info("MD string " + mdString + " contains unknown deleted bases ("+deletedBases+"). Cigar string " + read.getCigarString() + ". CurrentCigarElement : " 
							 + currentCigarElement.getLength() + currentCigarElement.getOperator().toString());
					return false;
				}
			}
			
			currentMDElementPosition = currentMDElementPosition
					+ currentCigarElementLength;
			
	        // log.debug("tempCigElem: " + currentCigarElementLength + "D ~ " + "DeletedBases: " + deletedBases + " ; length: " +
	        // currentMDElement.length());
			cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.DELETION, deletedBases));		
			return true;
		}
		// if we do not have the mdString
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.DELETION, ""));
		return true;
	}
	
	
	/** Process the MD string once found the CIGAR operator N. */
	private void processMDtagCigarOperatorN() {
		// As far as I see the MD string contains information about skipped regions in the read.
		// Skipped regions are not reported in the read, so don't update currentBaseCallPosition.
		// We do not record the bases.
//		log.debug("tempCigElem: " +
//		 currentCigarElementLength + "N ~ " + " ; length: " +
//		 temporaryMDElementLength);
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.SKIPPED_REGION, ""));
	}

	
	/** Process the MD string once found the CIGAR operator S. */
	private void processMDtagCigarOperatorS() {
		// MD string does not report any information about soft clips. They are just skipped.
		// Soft clips are reported in the read though, so the currentBaseCallPosition must be updated
		// We do not record the bases.
//		log.debug("tempCigElem: " +
//		 currentCigarElementLength + "S ~ " + " ; length: " +
//		 temporaryMDElementLength);
		currentBaseCallPosition = currentBaseCallPosition + currentCigarElementLength;
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.SOFT_CLIP, ""));
	}

	
	/** Process the MD string once found the CIGAR operator H. */
	private void processMDtagCigarOperatorH() {
		// As far as I see the MD string contains information about hard clips.
		// Hard clips are not reported in the read, so don't update currentBaseCallPosition.
		// We do not record the bases.
//		log.debug("tempCigElem: " +
//				 currentCigarElementLength + "H ~ " + " ; length: " +
//				 temporaryMDElementLength);
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.HARD_CLIP, ""));
	}

	
	/** Process the MD string once found the CIGAR operator P. */
	private void processMDtagCigarOperatorP() {
		// As far as I see the MD string contains information about padding.
		// Paddings are not reported in the read, so don't update currentBaseCallPosition.
		// We do not record the bases.
//		log.debug("tempCigElem: " +
//				 currentCigarElementLength + "P ~ " + " ; length: " +
//				 temporaryMDElementLength);
		cigarMD.add(new CigarMDElement(currentCigarElementLength, CigarMDOperator.PADDING, ""));
	}

	
	/** Process the MD string once found the CIGAR operator =. */
	private void processMDtagCigarOperatorEQ() {
		// is this operator used?
	}
	

	/** Process the MD string once found the CIGAR operator X. */
	private void processMDtagCigarOperatorNEQ() {
		// is this operator used?
	}

}
