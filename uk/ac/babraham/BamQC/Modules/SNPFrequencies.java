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

public class SNPFrequencies extends AbstractQCModule {

	// data fields for statistics
	private long ga = 0;
	private long gt = 0;
	private long gc = 0;
	private long gd = 0;
	private long ag = 0;
	private long at = 0;
	private long ac = 0;
	private long ad = 0;
	private long tg = 0;
	private long ta = 0;
	private long tc = 0;
	private long td = 0;
	private long cg = 0;
	private long ca = 0;
	private long ct = 0;
	private long cd = 0;
	private long insertion = 0;
	private long nonMut = 0;
	private long total = 0;
	

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
	
	
	

	// Private methods here
	
	// These methods process the MD string for each CIGAR operator.
	
	/* Process the MD string once found the CIGAR operator M. */
	private void processMDtagCigarOperatorM() { 
		// The temporary length of the current Cigar element
		int temporaryCigarElementLength = currentCigarElementLength;

		while(temporaryCigarElementLength > 0) {
			if(temporaryMDElementLength == 0) {
				// Parse and extract the currenMDElement. It is either a number or a char (A,C,G,T)
				// Extract the first character for the MD element.
				currentMDElement = String.valueOf(mdString.charAt(temporaryMDElementPosition));
				temporaryMDElementPosition++;
				temporaryMDElementLength++;
				
				if(currentMDElement.equals("A")) {
					// update SNP freq and position for A.
				} else if (currentMDElement.equals("C")) {
					// update SNP freq and position for C.
				} else if (currentMDElement.equals("G")) {
					// update SNP freq and position for G.
				} else if (currentMDElement.equals("T")) {
					// update SNP freq and position for T.
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
			// update the position of the currentBaseCall and the parser.
			if(temporaryMDElementLength <= temporaryCigarElementLength) {
				currentBaseCallPosition = currentBaseCallPosition + temporaryMDElementLength;
				temporaryCigarElementLength = temporaryCigarElementLength - temporaryMDElementLength;
				temporaryMDElementLength = 0;
			} else {
				currentBaseCallPosition = currentBaseCallPosition + temporaryCigarElementLength;
				temporaryMDElementLength = temporaryMDElementLength - temporaryCigarElementLength;
				temporaryMDElementLength = 0;
			}
			
			System.out.println("CigarOper M - corresponding MDElement: " + currentMDElement);
		}
	}

	
	/* Process the MD string once found the CIGAR operator I. */	
	private void processMDtagCigarOperatorI() {
		// The MD string does not contain information regarding an insertion.
		// Update SNIP freq/position for this insertion
		currentBaseCallPosition = currentBaseCallPosition + currentCigarElementLength;
		
		System.out.println("CigarOper I - corresponding MDElement: " + currentMDElement);		
	}
	
	/* Process the MD string once found the CIGAR operator D. */	
	private void processMDtagCigarOperatorD() {

		if(temporaryMDElementLength == 0) {
			// The previous MD element must have been processed
			
			// Parse and extract the currenMDElement. It is a string starting with ^ and followed by a string of (A,C,G,T)
			// Extract the first character for the MD element.
			currentMDElement = String.valueOf(mdString.charAt(temporaryMDElementPosition));
			if(!currentMDElement.equals("^")) {
				// this means an inconsistency between the CIGAR and MD string				
				System.out.println("SNPFrequencies: Error, ^ not found in the MD string when processing Cigar Operator D");
				return;
			}
			
			temporaryMDElementPosition++;

			// The first character is a ^. There are exactly temporaryCigarElementLength chars (A,C,G,T) to parse.
			currentMDElement = 
					mdString.substring(temporaryMDElementPosition, temporaryMDElementPosition+currentCigarElementLength);
			temporaryMDElementPosition = temporaryMDElementPosition+currentCigarElementLength;
			
			// Is this correct? I don't think we should include it, as we have a deletion.. 
			currentBaseCallPosition = currentBaseCallPosition + currentCigarElementLength;			
			
			// Parse the deletions encountered.
			for(int i = 0; i < currentMDElement.length(); i++) {
				if(currentMDElement.charAt(i) == 'A') {
					// update SNP freq and position for A.
				} else if (currentMDElement.charAt(i) == 'C') {
					// update SNP freq and position for C.
				} else if (currentMDElement.charAt(i) == 'G') {
					// update SNP freq and position for G.
				} else if (currentMDElement.charAt(i) == 'T') {
					// update SNP freq and position for T.
				} 
			}
		}
		System.out.println("CigarOper D - corresponding MDElement: " + currentMDElement);
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
	

	// These methods process the MD string for each CIGAR operator.
	private void parseMDtag() {}	
	
	
	
	

	public void processSequence(SAMRecord read) {

		// SAM format sucks for reading deletions/insertions.  To get this
		// information you need to use a combination of cigar operations
		// which say what the pattern of matches, insertions and deletions
		// is, and the MD tag which contains the bases from the reference which
		// are different to the read.
		// System.err.println("MD = "+md+" Cigar="+read.getCigarString());
		
		//String md = read.getStringAttribute("MD");
		//Cigar cigar = read.getCigar();
		
		/* you will have to implement the pseudo code that you wrote here. 
		 * You will need to define some extra private functions though! :)
		 * WRITE THE PSEUDO CODE FOR THE MD PARSER FIRST. Possibly one parser per case.
		 */
		
		// Get the CIGAR list and MD tag string.
		cigarList = read.getCigar().getCigarElements();
		mdString = read.getStringAttribute("MD");
		
		if(mdString == null || mdString.equals("")) {
			System.out.println("SNPFrequencies: current SAM read does not have MD tag string");
			// possibly throw an exception too.
			return;
		}

		// The temporary processed position of the processed MD tag (for the parser)
		temporaryMDElementPosition = 0;
	    // The current base call position of the read
		currentBaseCallPosition = 0;		
		
		// Iterate the CigarList
		Iterator<CigarElement> iterCigar = cigarList.iterator();
		CigarElement currentCigarElement = null;
		
		while(iterCigar.hasNext()) {
			currentCigarElement = iterCigar.next();
			currentCigarElementLength = currentCigarElement.getLength();
			currentCigarElementOperator = currentCigarElement.getOperator().toString();
			
			System.out.println("Parsing CigarElement: " + currentCigarElement.toString());
			if(currentCigarElementOperator.equals("M")) {
				processMDtagCigarOperatorM();		
				
			} else if(currentCigarElementOperator.equals("I")) {
				processMDtagCigarOperatorI();
				
			} else if(currentCigarElementOperator.equals("D")) {
				processMDtagCigarOperatorD();				
				
			} else if(currentCigarElementOperator.equals("N")) {
				
			} else if(currentCigarElementOperator.equals("S")) {
				
			} else if(currentCigarElementOperator.equals("H")) {
				
			} else if(currentCigarElementOperator.equals("P")) {
				
			} else if(currentCigarElementOperator.equals("=")) {
				
			} else if(currentCigarElementOperator.equals("X")) {
				
			} else {
				System.out.println("SNPFrequencies.java: Unknown operator in the CIGAR string.");
				// throw an exception possibly.
			} 
			
		}
	}
	
	
	
	public void processFile(SequenceFile file) {}

	public void processAnnotationSet(AnnotationSet annotation) {}

	public JPanel getResultsPanel() {
		return null;
	}

	public String name() {
		return "SNP Frequencies";
	}

	public String description() {
		return "Looks at the overall SNP frequencies in the data";
	}

	public void reset() {
		ga = 0;
		gt = 0;
		gc = 0;
		gd = 0;
		ag = 0;
		at = 0;
		ac = 0;
		ad = 0;
		tg = 0;
		ta = 0;
		tc = 0;
		td = 0;
		cg = 0;
		ca = 0;
		ct = 0;
		cd = 0;
		nonMut = 0;
		total = 0;
		insertion = 0;
		
		cigarList = null;
		mdString = null;
		currentCigarElementLength = 0;
		currentCigarElementOperator = null;
		currentMDElement = "";
		temporaryMDElementLength = 0;
		temporaryMDElementPosition = 0;
		currentBaseCallPosition = 0;
		
		
	}

	public boolean raisesError() {
		//TODO: Set this
		return false;
	}

	public boolean raisesWarning() {
		//TODO: Set this
		return false;
	}

	public boolean needsToSeeSequences() {
		return true;
	}

	public boolean needsToSeeAnnotation() {
		return false;
	}

	public boolean ignoreInReport() {
		return total == 0;
	}

	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub

	}	 

	
	
}
