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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Utilities.CigarMDGenerator;
import uk.ac.babraham.BamQC.Utilities.CigarMDElement;
import uk.ac.babraham.BamQC.Utilities.CigarMDOperator;
import uk.ac.babraham.BamQC.Utilities.CigarMD;






public class VariantCallDetection extends AbstractQCModule {

	// logger
	private static Logger log = Logger.getLogger(VariantCallDetection.class);
	
	
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
	private long nInsertions = 0;		
	private long totalInsertions = 0;
	private long aDeletions = 0;
	private long cDeletions = 0;
	private long gDeletions = 0;
	private long tDeletions = 0;
	private long nDeletions = 0;	
	private long totalDeletions = 0;	
	private long totalMatches = 0;
	private long total = 0;
	
	private long readSkippedRegions = 0;
	private long referenceSkippedRegions = 0;
	
    private long skippedReads = 0;
    private long totalReads = 0;
    
    // These arrays are used to store the density of SNP and Indels at each read position.
    private long[] snpPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] insertionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] deletionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] matchPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] totalPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    public HashMap<Integer, Long> getContributingReadsPerPos() {
		return contributingReadsPerPos;
	}

	private int currentPosition = 0;
    // This array reports how many reads are included for computing the statistics for each position. It is used for filtering 
    // statistics for positions having less then a defined percentage of reads.
    // key: the read lengths, value: the number of reads with that length.
    private HashMap<Integer, Long> contributingReadsPerPos = new HashMap<Integer, Long>();	
    
    
	// Used for computing the statistics 
	private CigarMDGenerator cigarMDGenerator = new CigarMDGenerator();

	private CigarMD cigarMD = new CigarMD();
	private CigarMDElement currentCigarMDElement = null;
	
	
	
	// Constructors
	/**
	 * Default constructor
	 */
	public VariantCallDetection() { }

	
	
	
	// @Override methods
	
	@Override
	public void processSequence(SAMRecord read) {

		totalReads++;
		
		// Compute and get the CigarMD object combining the strings Cigar and MD tag
		cigarMDGenerator.generateCigarMD(read);
		cigarMD = cigarMDGenerator.getCigarMD();
				
		if(cigarMD.isEmpty()) {
			skippedReads++;
			return;			
		}

		// Iterate the CigarMDElements list to collect statistics
		List<CigarMDElement> cigarMDElements = cigarMD.getCigarMDElements();
		Iterator<CigarMDElement> cigarMDIter = cigarMDElements.iterator();
		CigarMDOperator currentCigarMDElementOperator;
		
		// restart the counter for computing SNP/Indels per read position.
		currentPosition = 0;

	
		while(cigarMDIter.hasNext()) {
			currentCigarMDElement = cigarMDIter.next();

			currentCigarMDElementOperator = currentCigarMDElement.getOperator();
			
			log.debug("Parsing CigarMDElement: " + currentCigarMDElement.toString());

			if(currentCigarMDElementOperator.equals(CigarMDOperator.MATCH)) {
				processMDtagCigarOperatorM();
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.MISMATCH)) {
				processMDtagCigarOperatorU();
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.INSERTION)) {
				processMDtagCigarOperatorI();
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.DELETION)) {
				processMDtagCigarOperatorD();				
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.SKIPPED_REGION)) {
				//processMDtagCigarOperatorN();
				log.debug("VariantCallDetection.java: extended CIGAR element N is currently unsupported.");
				skippedReads++;
				break;
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.SOFT_CLIP)) {
				log.debug("VariantCallDetection.java: extended CIGAR element S is currently unsupported.");
				skippedReads++;
				break;
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.HARD_CLIP)) {
				log.debug("VariantCallDetection.java: extended CIGAR element H is currently unsupported.");
				skippedReads++;
				break;
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.PADDING)) {
				log.debug("VariantCallDetection.java: extended CIGAR element P is currently unsupported.");
				skippedReads++;
				break;
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.eq)) {
				log.debug("VariantCallDetection.java: extended CIGAR element = is currently unsupported.");
				skippedReads++;
				break;
			} else if(currentCigarMDElementOperator.equals(CigarMDOperator.x)) {
				log.debug("VariantCallDetection.java: extended CIGAR element X is currently unsupported.");
				skippedReads++;
				break;
			} else {
				log.debug("VariantCallDetection.java: Unknown operator in the CIGAR string.");
				skippedReads++;
				break;
			}		
		}
		computeTotals();
		if(contributingReadsPerPos.containsKey(read.getReadLength())) {
			contributingReadsPerPos.put(read.getReadLength(), contributingReadsPerPos.get(read.getReadLength()) + 1L);
		} else {
			contributingReadsPerPos.put(read.getReadLength(), 1L);
		}
		//System.out.println("key, value:" + read.getReadLength() + ", " + contributingReadsPerPos.get(read.getReadLength()));
		log.debug("Combined Cigar MDtag: " + cigarMD.toString());
	}
	
	
	@Override	
	public void processFile(SequenceFile file) {}
	
	

	@Override	
	public void processAnnotationSet(AnnotationSet annotation) { }	

	@Override	
	public JPanel getResultsPanel() {
		String[] names = new String[0];
		double[] vcd = new double[0];
		String title = String.format("Variant call detection");		
		return new HorizontalBarGraph(names, vcd, title, 0d,1d);
	}

	@Override	
	public String name() {
		return "Variant Call Detection";
	}

	@Override	
	public String description() {
		return "Looks at the variant calls in the data";
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
		nInsertions = 0;
		totalInsertions = 0;
		aDeletions = 0;
		cDeletions = 0;
		gDeletions = 0;
		tDeletions = 0;
		nDeletions = 0;
		totalDeletions = 0;
		totalMatches = 0;
		total = 0;
		skippedReads = 0;
		totalReads = 0;

		readSkippedRegions = 0;
		referenceSkippedRegions = 0;
		
		
	    snpPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    insertionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    deletionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    matchPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    totalPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];	  	    
	    currentPosition = 0;
	    contributingReadsPerPos = new HashMap<Integer, Long>();

		cigarMD = new CigarMD();
	}

	@Override	
	public boolean raisesError() {
		return false;
	}

	@Override	
	public boolean raisesWarning() {
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
		return true;
	}

	@Override	
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "variant_call_detection.png", "Variant Call Detection", 800, 600);
	}	 

	
	
	
	// Private methods here
	
	
	private void extendDensityArrays(int newBound) {
		long[] oldSNPPos = snpPos;
		long[] oldInsertionPos = insertionPos;
		long[] oldDeletionPos = deletionPos;
		long[] oldMatchPos = matchPos;
		long[] oldTotalPos = totalPos;		
		// We do not want to call this method often, that's why it is better to extend it 
		// 2 times the current length. However, if the newBound is larger than this, it is 
		// better to be conservative and just increase for that new size.
		if(snpPos.length*2 < newBound) {
			snpPos = new long[newBound+1];
			insertionPos = new long[newBound+1];
			deletionPos = new long[newBound+1];
			matchPos = new long[newBound+1];
			totalPos = new long[newBound+1];			
		} else {
			snpPos = new long[snpPos.length*2];
			insertionPos = new long[snpPos.length*2];
			deletionPos = new long[snpPos.length*2];
			matchPos = new long[snpPos.length*2];
			totalPos = new long[snpPos.length*2];				
		}
		System.arraycopy(oldSNPPos, 0, snpPos, 0, oldSNPPos.length);
		System.arraycopy(oldInsertionPos, 0, insertionPos, 0, oldInsertionPos.length);
		System.arraycopy(oldDeletionPos, 0, deletionPos, 0, oldDeletionPos.length);	
		System.arraycopy(oldMatchPos, 0, matchPos, 0, oldMatchPos.length);
		System.arraycopy(oldTotalPos, 0, totalPos, 0, oldTotalPos.length);		
	}
	
	/* Compute the totals */
	private void computeTotals() {
		totalMutations = ac+ag+at+
						  ca+cg+ct+
						  ga+gc+gt+
						  ta+tc+tg;
		// NOTE: nInsertions and nDeletions are not counted in the totals. 
		totalInsertions = aInsertions + cInsertions + gInsertions + tInsertions;
		totalDeletions = aDeletions + cDeletions + gDeletions + tDeletions;
		total = totalMutations + totalInsertions + totalDeletions + totalMatches;
		for(int i=0; i< snpPos.length; i++) {
			totalPos[i] = snpPos[i] + insertionPos[i] + deletionPos[i] + matchPos[i];
		}
	}
	
	

	// These methods process the combined CigarMD object.
	
	/* Process the MD string once found the CigarMD operator m (match). */
	private void processMDtagCigarOperatorM() {
		int numMatches = currentCigarMDElement.getLength();
		totalMatches = totalMatches + numMatches;
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
		if(currentPosition+numMatches >= matchPos.length) {
			extendDensityArrays(currentPosition+numMatches);			
	    }
		for(int i=0; i<numMatches; i++) {
			matchPos[currentPosition+i]++;
		}
		currentPosition = currentPosition + numMatches;
	}
	
	/* Process the MD string once found the CigarMD operator u (mismatch). 
	 * So far this element is indicated as 1u{ACGT}ref{ACGT}read
	 * to indicate a mutation from reference to read.
	 * In the future the length will correspond to the number of adjacent mutations.
	 * e.g. 3uACGTAT will indicate that the substring AGA on the reference has been 
	 * mutated in CTT.
	 */
	private void processMDtagCigarOperatorU() {
		int numMutations = currentCigarMDElement.getLength();
		String mutatedBases = currentCigarMDElement.getBases();
		String basePair = "";
		
		if(mutatedBases.equals("")) {
			log.error("Mutated bases not reported. currentCigarMDElement: " + currentCigarMDElement + ", cigarMD: " + cigarMD.toString() + 
					 ", mutatedBases: " + mutatedBases);
			// if we are in this case, the following for loop will cause a java.lang.StringIndexOutOfBoundsException . 
			// This would be a bug in the computation of the CigarMD string. mutatedBases should never be empty.
			// For now, leave this test as it is useful.
		}
		
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
		if(currentPosition+numMutations >= snpPos.length) {
			extendDensityArrays(currentPosition+numMutations);			
	    }
		for(int i = 0; i < numMutations; i++) {
			basePair = mutatedBases.substring(i*2, i*2+2);
			if(basePair.equals("AC"))      { ac++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("AG")) { ag++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("AT")) { at++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("CA")) { ca++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("CG")) { cg++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("CT")) { ct++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("GA")) { ga++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("GC")) { gc++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("GT")) { gt++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("TA")) { ta++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("TC")) { tc++; snpPos[currentPosition]++; currentPosition++; }
			else if(basePair.equals("TG")) { tg++; snpPos[currentPosition]++; currentPosition++; }	
			else if(basePair.charAt(0) == 'N') { referenceSkippedRegions++; currentPosition++; }
			else if(basePair.charAt(1) == 'N') { readSkippedRegions++; currentPosition++; }			
		}
	}	
	
	/* Process the MD string once found the CigarMD operator i (insertion). */	
	private void processMDtagCigarOperatorI() {
		int numInsertions = currentCigarMDElement.getLength();
		String insertedBases = currentCigarMDElement.getBases();
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays..
		if(currentPosition+numInsertions >= insertionPos.length) {
			extendDensityArrays(currentPosition+numInsertions);
	    }
		for(int i = 0; i < numInsertions; i++) {
			if(insertedBases.charAt(i) == 'A')      { aInsertions++; insertionPos[currentPosition]++; currentPosition++; }
			else if(insertedBases.charAt(i) == 'C') { cInsertions++; insertionPos[currentPosition]++; currentPosition++; }
			else if(insertedBases.charAt(i) == 'G') { gInsertions++; insertionPos[currentPosition]++; currentPosition++; }
			else if(insertedBases.charAt(i) == 'T') { tInsertions++; insertionPos[currentPosition]++; currentPosition++; }
			else if(insertedBases.charAt(i) == 'N') { nInsertions++; currentPosition++; }			
		}
	}
	
	/* Process the MD string once found the CigarMD operator d (deletion). */	
	private void processMDtagCigarOperatorD() {
		int numDeletions = currentCigarMDElement.getLength();
		String deletedBases = currentCigarMDElement.getBases();
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays..
		if(currentPosition+numDeletions >= deletionPos.length) {
			extendDensityArrays(currentPosition+numDeletions);		
	    }		
		for(int i = 0; i < numDeletions; i++) {
			if(deletedBases.charAt(i) == 'A')      { aDeletions++; deletionPos[currentPosition]++; currentPosition++; }
			else if(deletedBases.charAt(i) == 'C') { cDeletions++; deletionPos[currentPosition]++; currentPosition++; }
			else if(deletedBases.charAt(i) == 'G') { gDeletions++; deletionPos[currentPosition]++; currentPosition++; }
			else if(deletedBases.charAt(i) == 'T') { tDeletions++; deletionPos[currentPosition]++; currentPosition++; }
			else if(deletedBases.charAt(i) == 'N') { nDeletions++; currentPosition++; }			
		}
	}
	
	
	// Have to test the following code.
	
	/* Process the MD string once found the CigarMD operator n. */	
	private void processMDtagCigarOperatorN() {
		int numSkipped = currentCigarMDElement.getLength();		
		readSkippedRegions = readSkippedRegions + numSkipped;
		currentPosition = currentPosition + numSkipped;
	}
	
	/* Process the MD string once found the CigarMD operator s. */	
	private void processMDtagCigarOperatorS() {}
	
	/* Process the MD string once found the CigarMD operator h. */	
	private void processMDtagCigarOperatorH() {}
	
	/* Process the MD string once found the CigarMD operator p. */
	private void processMDtagCigarOperatorP() {}	
	
	/* Process the MD string once found the CigarMD operator =. */	
	private void processMDtagCigarOperatorEQ() {}	
	
	/* Process the MD string once found the CigarMD operator X. */	
	private void processMDtagCigarOperatorNEQ() {}

	
	
	
	
	
	
	
	
	// Getter methods
	
	public CigarMD getCigarMD() {
		return cigarMD;
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

	public long getNInsertions() {
		return nInsertions;
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

	public long getReadSkippedRegions() {
		return readSkippedRegions;
	}	
	
	public long getReferenceSkippedRegions() {
		return referenceSkippedRegions;
	}	
	
	public long getSkippedReads() {
		return skippedReads;
	}	
	
	public long getTotalReads() {
		return totalReads;
	}

	public long[] getSNPPos() {
		return snpPos;
	}

	public long[] getInsertionPos() {
		return insertionPos;
	}

	public long[] getDeletionPos() {
		return deletionPos;
	}	
	
	public long[] getMatchPos() {
		return matchPos;
	}	
	
	public long[] getTotalPos() {
		return totalPos;
	}		
	
}
