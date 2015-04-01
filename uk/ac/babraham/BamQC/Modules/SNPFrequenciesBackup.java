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

package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.lang.annotation.Inherited;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Annotation.Chromosome;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class SNPFrequenciesBackup extends AbstractQCModule {

	// data fields for statistics
	private long ac = 0;
	private long ag = 0;
	private long at = 0;
	private long ca = 0;
	private long cg = 0;
	private long ct = 0;
	private long ga = 0;
	private long gc = 0;
	private long gt = 0;
	private long ta = 0;
	private long tc = 0;
	private long tg = 0;
	private long totalMutations = 0;	
	private long aInsertions = 0;
	private long cInsertions = 0;
	private long gInsertions = 0;
	private long tInsertions = 0;	
	private long totalInsertions = 0;
	private long aDeletions = 0;
	private long cDeletions = 0;
	private long gDeletions = 0;
	private long tDeletions = 0;
	private long nDeletions = 0;	
	private long totalDeletions = 0;	
	private long totalMatches = 0;
	private long total = 0;
	
	private long referenceSkippedRegion = 0;
	private long processedReads = 0;	
	private long unprocessedReads = 0;
	
	

	// data fields for computation
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
	
	// Used for debugging or future uses.
	private String combinedCigarMDtag = "";
	
	

	
	// Private methods here
	
	/*
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
	
	/* Process the MD string once found the CIGAR operator M. */
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

				// currentMDElement is either a positive number or a base (A,C,G,T)
				temporaryMDElementLength++;				

				// update SNP statistics for mutations.				
				char mutatedBase = read.getReadString().charAt(currentBaseCallPosition);
				if(currentMDElement.equals("A")) {
					if(mutatedBase == 'C') { 
						ac++; totalMutations++; 
					} else if(mutatedBase == 'G') { 
						ag++; totalMutations++; 
					} else if(mutatedBase == 'T') { 
						at++; totalMutations++; 
					}
				} else if (currentMDElement.equals("C")) {
					if(mutatedBase == 'A') { 
						ca++; totalMutations++; 
					} else if(mutatedBase == 'G') { 
						cg++; totalMutations++; 
					} else if(mutatedBase == 'T') { 
						ct++; totalMutations++; 
					}
				} else if (currentMDElement.equals("G")) {
					if(mutatedBase == 'A') { 
						ga++; totalMutations++; 
					} else if(mutatedBase == 'C') { 
						gc++; totalMutations++; 
					} else if(mutatedBase == 'T') { 
						gt++; totalMutations++; 
					}
				} else if (currentMDElement.equals("T")) {
					if(mutatedBase == 'A') { 
						ta++; totalMutations++; 
					} else if(mutatedBase == 'C') { 
						tc++; totalMutations++; 
					} else if(mutatedBase == 'G') { 
						tg++; totalMutations++; 
					}
				} else if (currentMDElement.equals("N")) {
					referenceSkippedRegion++;
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
				// statistics: count the matches
				totalMatches = totalMatches + temporaryMDElementLength;				
				// Used for debugging or future uses
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					combinedCigarMDtag = combinedCigarMDtag + temporaryMDElementLength + "m";						
				} else {
					combinedCigarMDtag = combinedCigarMDtag + "1u" + currentMDElement;
				}
				// normal code				
				currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
				temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
				temporaryMDElementLength = 0;
			} else {
				// statistics: count the matches
				totalMatches = totalMatches + temporaryCigarElementLength;								
				// Used for debugging or future uses
				if(currentMDElement.charAt(0) >= '0' && currentMDElement.charAt(0) <= '9') {
					combinedCigarMDtag = combinedCigarMDtag + temporaryCigarElementLength + "m";						
				} else {
					combinedCigarMDtag = combinedCigarMDtag + "1u" + currentMDElement;
				}
				// normal code
				currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
				temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
				temporaryCigarElementLength = 0;
			}
			
		}
	}

	
	
	/* Process the MD string once found the CIGAR operator I. */	
	private void processMDtagCigarOperatorI(SAMRecord read) {
		// The MD string does not contain information regarding an insertion.
		String wronglyInsertedBases = read.getReadString().substring(currentBaseCallPosition, currentBaseCallPosition + currentCigarElementLength);
		currentBaseCallPosition = currentBaseCallPosition + currentCigarElementLength;		

		// Update statistics for this insertion
		totalInsertions = totalInsertions + currentCigarElementLength;
		for(int i = 0; i < wronglyInsertedBases.length(); i++) {
			if(wronglyInsertedBases.charAt(i) == 'A') { aInsertions++; }
			else if(wronglyInsertedBases.charAt(i) == 'C') { cInsertions++; }
			else if(wronglyInsertedBases.charAt(i) == 'G') { gInsertions++; }
			else if(wronglyInsertedBases.charAt(i) == 'T') { tInsertions++; }
		}
		
		// Used for debugging or future uses
		//System.out.println("tempCigElem: " + String.valueOf(currentCigarElementLength) + "I ~ " + "parsedMDElem: " + currentMDElement + " ; length: " + String.valueOf(temporaryMDElementLength));		
		combinedCigarMDtag = combinedCigarMDtag + String.valueOf(currentCigarElementLength) + "i" + wronglyInsertedBases;

	}
	
	

	/* Process the MD string once found the CIGAR operator D. */	
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
			unprocessedReads++;
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
		combinedCigarMDtag = combinedCigarMDtag + String.valueOf(currentCigarElementLength) + "d" + currentMDElement;		

		// Parse the deletions encountered and update statistics
		totalDeletions = totalDeletions + currentCigarElementLength;		
		for(int i = 0; i < currentMDElement.length(); i++) {
			if(currentMDElement.charAt(i) == 'A') { aDeletions++; }
			else if (currentMDElement.charAt(i) == 'C') { cDeletions++; }
			else if (currentMDElement.charAt(i) == 'G') { gDeletions++; }
			else if (currentMDElement.charAt(i) == 'T') { tDeletions++; }
			else if (currentMDElement.charAt(i) == 'N') { nDeletions++; } 
		}
	}
	
	
	/* Process the MD string once found the CIGAR operator N. */	
	private void processMDtagCigarOperatorN() {}
	
	/* Process the MD string once found the CIGAR operator S. */	
	private void processMDtagCigarOperatorS() {}
	
	/* Process the MD string once found the CIGAR operator H. */	
	private void processMDtagCigarOperatorH() {}
	
	/* Process the MD string once found the CIGAR operator P. */
	private void processMDtagCigarOperatorP() {}	
	
	/* Process the MD string once found the CIGAR operator =. */	
	private void processMDtagCigarOperatorEQ() {}	
	
	/* Process the MD string once found the CIGAR operator X. */	
	private void processMDtagCigarOperatorNEQ() {}	
	
	
	
	
	// @Override methods
	
	@Override
	public void processSequence(SAMRecord read) {

		// Get the CIGAR list and MD tag string.
		cigarList = read.getCigar().getCigarElements();
		mdString = read.getStringAttribute("MD");
		
		if(mdString == null || mdString.equals("")) {
			//System.out.println("SNPFrequencies: current SAM read does not have MD tag string");
			unprocessedReads++;
			combinedCigarMDtag = "";
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
		combinedCigarMDtag = "";
		
		
		while(iterCigar.hasNext()) {
			currentCigarElement = iterCigar.next();
			currentCigarElementLength = currentCigarElement.getLength();
			currentCigarElementOperator = currentCigarElement.getOperator().toString();
			
			// debugging
			//System.out.println("Parsing CigarElement: " + String.valueOf(currentCigarElementLength) + currentCigarElementOperator.toString());
			if(currentCigarElementOperator.equals("M")) {
				processMDtagCigarOperatorM(read);
				// Increase the number of processed reads.
				processedReads++;				
			} else if(currentCigarElementOperator.equals("I")) {
				processMDtagCigarOperatorI(read);
				// Increase the number of processed reads.
				processedReads++;
			} else if(currentCigarElementOperator.equals("D")) {
				processMDtagCigarOperatorD();				
				// Increase the number of processed reads.
				processedReads++;
			} else if(currentCigarElementOperator.equals("N")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element N is currently unsupported.");
				unprocessedReads++;
			} else if(currentCigarElementOperator.equals("S")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element S is currently unsupported.");
				unprocessedReads++;
			} else if(currentCigarElementOperator.equals("H")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element H is currently unsupported.");
				unprocessedReads++;
			} else if(currentCigarElementOperator.equals("P")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element P is currently unsupported.");
				unprocessedReads++;
			} else if(currentCigarElementOperator.equals("=")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element = is currently unsupported.");
				unprocessedReads++;
			} else if(currentCigarElementOperator.equals("X")) {
				//System.out.println("SNPFrequencies.java: extended CIGAR element X is currently unsupported.");
				unprocessedReads++;
			} else {
				System.out.println("SNPFrequencies.java: Unknown operator in the CIGAR string.");
				unprocessedReads++;
				// throw an exception possibly.
			}		
		}
		
		total = totalMutations + totalInsertions + totalDeletions;
		// debugging
		// System.out.println("Combined Cigar MDtag: " + combinedCigarMDtag);
	}
	
	
	@Override	
	public void processFile(SequenceFile file) {}

	@Override	
	public void processAnnotationSet(AnnotationSet annotation) {}

	@Override	
	public JPanel getResultsPanel() {
		return null;
	}

	@Override	
	public String name() {
		return "SNP Frequencies";
	}

	@Override	
	public String description() {
		return "Looks at the overall SNP frequencies in the data";
	}

	@Override	
	public void reset() {
		ac = 0;
		ag = 0;
		at = 0;
		ca = 0;
		cg = 0;
		ct = 0;
		ga = 0;
		gc = 0;
		gt = 0;
		ta = 0;
		tc = 0;
		tg = 0;
		totalMutations = 0;
		aInsertions = 0;
		cInsertions = 0;
		gInsertions = 0;
		tInsertions = 0;	
		totalInsertions = 0;
		aDeletions = 0;
		cDeletions = 0;
		gDeletions = 0;
		tDeletions = 0;
		nDeletions = 0;
		totalDeletions = 0;
		totalMatches = 0;
		total = 0;
		
		referenceSkippedRegion = 0;
		unprocessedReads = 0;
		
		cigarList = null;
		mdString = null;
		currentCigarElementLength = 0;
		currentCigarElementOperator = null;
		currentMDElement = "";
		temporaryMDElementLength = 0;
		temporaryMDElementPosition = 0;
		currentBaseCallPosition = 0;
		
		combinedCigarMDtag = "";
		
	}

	@Override	
	public boolean raisesError() {
		//TODO: Set this
		return false;
	}

	@Override	
	public boolean raisesWarning() {
		//TODO: Set this
		return false;
	}

	@Override	
	public boolean needsToSeeSequences() {
		return true;
	}

	@Override	
	public boolean needsToSeeAnnotation() {
		return false;
	}

	@Override	
	public boolean ignoreInReport() {
		return total == 0;
	}

	@Override	
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub

	}	 

	
	
	
	
	// getter methods
	
	/** 
	 * It returns a string combining information from CIGAR and MD tag. The format is: 
	 * # (number), snip (m=match, u=unmatch, i=insertion, d=deletion), bases if snip={u,i,d}. 
	 * For instance: 
	 * CIGAR: 32M2D5M1I52M
	 * MDtag: 7G24^AA7C49
	 * Combined CIGAR+MDTag: 7m1uG24m2dAA5m1iG2m1uC49m
	 * 
	 * Note: G in 1iG is SAM read- dependent. 
	 * @return a string combining information from CIGAR and MD tag.
	 */
	public String getCombinedCigarMDtag() {
		return combinedCigarMDtag;
	}



	public long getA2C() {
		return ac;
	}



	public long getA2G() {
		return ag;
	}



	public long getA2T() {
		return at;
	}



	public long getC2A() {
		return ca;
	}



	public long getC2G() {
		return cg;
	}



	public long getC2T() {
		return ct;
	}



	public long getG2A() {
		return ga;
	}



	public long getG2C() {
		return gc;
	}



	public long getG2T() {
		return gt;
	}



	public long getT2A() {
		return ta;
	}



	public long getT2C() {
		return tc;
	}



	public long getT2G() {
		return tg;
	}



	public long getTotalMutations() {
		return totalMutations;
	}



	public long getAInsertions() {
		return aInsertions;
	}



	public long getCInsertions() {
		return cInsertions;
	}



	public long getGInsertions() {
		return gInsertions;
	}



	public long getTInsertions() {
		return tInsertions;
	}



	public long getTotalInsertions() {
		return totalInsertions;
	}



	public long getADeletions() {
		return aDeletions;
	}



	public long getCDeletions() {
		return cDeletions;
	}



	public long getGDeletions() {
		return gDeletions;
	}



	public long getTDeletions() {
		return tDeletions;
	}
	
	public long getNDeletions() {
		return nDeletions;
	}

	public long getTotalDeletions() {
		return totalDeletions;
	}



	public long getTotalMatches() {
		return totalMatches;
	}



	public long getTotal() {
		return total;
	}


	public long getProcessedReads() {
		return processedReads;
	}
	
	
	public long getUnprocessedReads() {
		return unprocessedReads;
	}


	public long getReferenceSkippedRegion() {
		return referenceSkippedRegion;
	}	
	
	
}
