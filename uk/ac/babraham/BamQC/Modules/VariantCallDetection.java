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
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
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
    // first or second indicate whether the read is the first or second segment. If the read is not paired, 
    // it is treated as a first.
	
	private long firstAC = 0;
	private long firstAG = 0;
	private long firstAT = 0;
	private long firstCA = 0;
	private long firstCG = 0;
	private long firstCT = 0;
	private long firstGA = 0;
	private long firstGC = 0;
	private long firstGT = 0;
	private long firstTA = 0;
	private long firstTC = 0;
	private long firstTG = 0;
	private long secondAC = 0;
	private long secondAG = 0;
	private long secondAT = 0;
	private long secondCA = 0;
	private long secondCG = 0;
	private long secondCT = 0;
	private long secondGA = 0;
	private long secondGC = 0;
	private long secondGT = 0;
	private long secondTA = 0;
	private long secondTC = 0;
	private long secondTG = 0;	
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
	private long totalSkippedRegions = 0;
	private long totalSoftClips = 0;
	private long totalHardClips = 0;
	private long totalPaddings = 0;
	private long total = 0;
	
	private long readUnknownBases = 0;
	private long referenceUnknownBases = 0;
	
    private long skippedReads = 0;
    private long totalReads = 0;
    
    // These arrays are used to store the density of SNP and Indels at each read position.
    private long[] firstSNPPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] firstInsertionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] firstDeletionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] secondSNPPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];    
    private long[] secondInsertionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] secondDeletionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];    
    private long[] matchPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    private long[] totalPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
    public HashMap<Integer, Long> getContributingReadsPerPos() {
		return contributingReadsPerPos;
	}

    // currentPosition is the current position used to record changes in the arrays above. This class processes 
    // the CigarMD string, not the read, which is instead processed by class CigarMDGenerator.
	private int currentPosition = 0;
    // This array reports how many reads are included for computing the statistics for each position. It is used for filtering 
    // statistics for positions having less then a defined percentage of reads.
    // key: the read lengths, value: the number of reads with that length.
    private HashMap<Integer, Long> contributingReadsPerPos = new HashMap<Integer, Long>();	
    
    
    private int readLength = 0;
    
	// Used for computing the statistics 
	private CigarMDGenerator cigarMDGenerator = new CigarMDGenerator();

	private CigarMD cigarMD = new CigarMD();
	private CigarMDElement currentCigarMDElement = null;
	
	
	private boolean existPairedReads = false;

	

	// Constructors
	/**
	 * Default constructor
	 */
	public VariantCallDetection() { }

	
	/** 
	 * Compute the totals. For improving efficiency, this method is not invoked 
	 * inside void processSequence(SAMRecord read), but must be invoked 
	 * later.
	 */
	public void computeTotals() {
		if(totalMutations != 0 || totalInsertions != 0 || totalDeletions != 0) {
			return;
		}
//		// NOTE: nInsertions and nDeletions are not counted in the totals. 
		
		if(existPairedReads) {
			for(int i=0; i< firstSNPPos.length; i++) {
				totalMutations = totalMutations + firstSNPPos[i] + secondSNPPos[i];
				totalInsertions = totalInsertions + firstInsertionPos[i] + secondInsertionPos[i];
				totalDeletions = totalDeletions + firstDeletionPos[i] + secondDeletionPos[i];
				totalPos[i] = firstSNPPos[i] + firstInsertionPos[i] + firstDeletionPos[i] + 
							  secondSNPPos[i] + secondInsertionPos[i] + secondDeletionPos[i] + 
					          matchPos[i];
			}
		} else {
			for(int i=0; i< firstSNPPos.length; i++) {
				totalMutations = totalMutations + firstSNPPos[i];
				totalInsertions = totalInsertions + firstInsertionPos[i];
				totalDeletions = totalDeletions + firstDeletionPos[i];
				totalPos[i] = firstSNPPos[i] + firstInsertionPos[i] + firstDeletionPos[i] + 
							  matchPos[i];
			}
		}
		// we do not consider totalSkippedRegions, totalHardClips and totalPaddings because they are not 
		// recorded in the read.
		total = totalMutations + totalInsertions + totalDeletions + totalMatches + totalSoftClips;
	}
	
	
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

		readLength = read.getReadLength();
		
		// Iterate the CigarMDElements list to collect statistics
		List<CigarMDElement> cigarMDElements = cigarMD.getCigarMDElements();

		CigarMDOperator currentCigarMDElementOperator;
		
		// restart the counter for computing SNP/Indels per read position.
		currentPosition = 0;

		// Use the old c-style for loop for memory (garbage collector) and CPU efficiency
//		Iterator<CigarMDElement> cigarMDIter = cigarMDElements.iterator();	
//		while(cigarMDIter.hasNext()) {
//			currentCigarMDElement = cigarMDIter.next();
		int cigarMDElementsSize = cigarMDElements.size();
		for(int i=0; i<cigarMDElementsSize; i++) {
			
			currentCigarMDElement = cigarMDElements.get(i);

			currentCigarMDElementOperator = currentCigarMDElement.getOperator();
			
			//log.debug("Parsing CigarMDElement: " + currentCigarMDElement.toString());

			if(currentCigarMDElementOperator == CigarMDOperator.MATCH) {
				processMDtagCigarOperatorM();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.MISMATCH) {
				processMDtagCigarOperatorU();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.INSERTION) {
				processMDtagCigarOperatorI();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.DELETION) {
				processMDtagCigarOperatorD();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.SKIPPED_REGION) {
				processMDtagCigarOperatorN();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.SOFT_CLIP) {
				processMDtagCigarOperatorS();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.HARD_CLIP) {
				processMDtagCigarOperatorH();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.PADDING) {
				processMDtagCigarOperatorP();
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.eq) {
				log.debug("Extended CIGAR element = is not currently supported.");
				skippedReads++;
				return;	
				
			} else if(currentCigarMDElementOperator == CigarMDOperator.x) {
				log.debug("Extended CIGAR element X is not currently supported.");
				skippedReads++;
				return;
				
			} else {
				log.debug("Unknown operator in the CIGAR string.");
				skippedReads++;
				return;	
			}		
		}
		
		if(contributingReadsPerPos.containsKey(readLength)) {
			contributingReadsPerPos.put(readLength, contributingReadsPerPos.get(readLength) + 1L);
		} else {
			contributingReadsPerPos.put(readLength, 1L);
		}
		//log.debug("key, value:" + readLength + ", " + contributingReadsPerPos.get(readLength));
		//log.debug("Combined Cigar MDtag: " + cigarMD.toString());

		
		// Are there better way to skip this test?
		if(!cigarMDGenerator.isFirst()) { 
			existPairedReads=true;
		}
	}
	
	
	@Override	
	public void processFile(SequenceFile file) {}
	
	

	@Override	
	public void processAnnotationSet(AnnotationSet annotation) { }	

	@Override	
	public JPanel getResultsPanel() {
		return new JPanel();
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
				
		firstAC = 0;
		firstAG = 0;
		firstAT = 0;
		firstCA = 0;
		firstCG = 0;
		firstCT = 0;
		firstGA = 0;
		firstGC = 0;
		firstGT = 0;
		firstTA = 0;
		firstTC = 0;
		firstTG = 0;
		secondAC = 0;
		secondAG = 0;
		secondAT = 0;
		secondCA = 0;
		secondCG = 0;
		secondCT = 0;
		secondGA = 0;
		secondGC = 0;
		secondGT = 0;
		secondTA = 0;
		secondTC = 0;
		secondTG = 0;	
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
		totalSkippedRegions = 0;
		totalSoftClips = 0;
		totalHardClips = 0;
		totalPaddings = 0;
		total = 0;
		skippedReads = 0;
		totalReads = 0;

		readUnknownBases = 0;
		referenceUnknownBases = 0;
		
		
	    firstSNPPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    firstInsertionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    firstDeletionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    secondSNPPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    secondInsertionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    secondDeletionPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];	    
	    matchPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];
	    totalPos = new long[ModuleConfig.getParam("variant_call_position_length", "ignore").intValue()];	  	    
	    currentPosition = 0;
	    contributingReadsPerPos = new HashMap<Integer, Long>();

	    readLength = 0;
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
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException { }	 

	
	
	
	// Private methods here
	
	
	private void extendDensityArrays() {
		long[] oldFirstSNPPos = firstSNPPos;
		long[] oldFirstInsertionPos = firstInsertionPos;
		long[] oldFirstDeletionPos = firstDeletionPos;
		long[] oldMatchPos = matchPos;
		long[] oldTotalPos = totalPos;		
		// We do not want to call this method too often, that's why it is better to extend it 
		// 2 times the current length. 
		firstSNPPos = new long[firstSNPPos.length*2];
		firstInsertionPos = new long[firstSNPPos.length*2];
		firstDeletionPos = new long[firstSNPPos.length*2];
		matchPos = new long[firstSNPPos.length*2];
		totalPos = new long[firstSNPPos.length*2];				
		
		System.arraycopy(oldFirstSNPPos, 0, firstSNPPos, 0, oldFirstSNPPos.length);
		System.arraycopy(oldFirstInsertionPos, 0, firstInsertionPos, 0, oldFirstSNPPos.length);
		System.arraycopy(oldFirstDeletionPos, 0, firstDeletionPos, 0, oldFirstSNPPos.length);	
		System.arraycopy(oldMatchPos, 0, matchPos, 0, oldFirstSNPPos.length);
		System.arraycopy(oldTotalPos, 0, totalPos, 0, oldFirstSNPPos.length);		
		
		if(existPairedReads) {
			long[] oldSecondSNPPos = secondSNPPos;
			long[] oldSecondInsertionPos = secondInsertionPos;
			long[] oldSecondDeletionPos = secondDeletionPos;		
			secondSNPPos = new long[firstSNPPos.length*2];			
			secondInsertionPos = new long[firstSNPPos.length*2];
			secondDeletionPos = new long[firstSNPPos.length*2];
			System.arraycopy(oldSecondSNPPos, 0, secondSNPPos, 0, oldFirstSNPPos.length);
			System.arraycopy(oldSecondInsertionPos, 0, secondInsertionPos, 0, oldFirstSNPPos.length);
			System.arraycopy(oldSecondDeletionPos, 0, secondDeletionPos, 0, oldFirstSNPPos.length);			
		}
		
	}
	
	
	// These methods process the combined CigarMD object.
	

	
	/* Process the MD string once found the CigarMD operator m (match). */
	private void processMDtagCigarOperatorM() {
		int numMatches = currentCigarMDElement.getLength();
		totalMatches = totalMatches + numMatches;
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
		if(currentPosition+numMatches >= matchPos.length) {
			extendDensityArrays();			
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

		if(mutatedBases.length() == 0) {
			log.error("Mutated bases not reported. currentCigarMDElement: " + currentCigarMDElement + ", cigarMD: " + cigarMD.toString() + 
					 ", mutatedBases: " + mutatedBases);
			// if we are in this case, the following for loop will cause a java.lang.StringIndexOutOfBoundsException . 
			// This would be a bug in the computation of the CigarMD string. mutatedBases should never be empty.
			// For now, leave this test as it is useful.
		}
		
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
		if(currentPosition+numMutations >= firstSNPPos.length) {
			extendDensityArrays();			
	    }
		if(cigarMDGenerator.isFirst()) {
			for(int i = 0; i < numMutations; i++) {
				basePair = mutatedBases.substring(i*2, i*2+2);
				if(basePair.equals("AC"))      { firstAC++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("AG")) { firstAG++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("AT")) { firstAT++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("CA")) { firstCA++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("CG")) { firstCG++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("CT")) { firstCT++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("GA")) { firstGA++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("GC")) { firstGC++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("GT")) { firstGT++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("TA")) { firstTA++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("TC")) { firstTC++; firstSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("TG")) { firstTG++; firstSNPPos[currentPosition]++; currentPosition++; }	
				else if(basePair.charAt(0) == 'N') { 
					referenceUnknownBases++; currentPosition++; 
					if(basePair.charAt(1) == 'N') { readUnknownBases++; currentPosition++; }
				}
				else if(basePair.charAt(1) == 'N') { readUnknownBases++; currentPosition++; }
			}			
		} else {
			for(int i = 0; i < numMutations; i++) {
				basePair = mutatedBases.substring(i*2, i*2+2);
				if(basePair.equals("AC"))      { secondAC++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("AG")) { secondAG++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("AT")) { secondAT++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("CA")) { secondCA++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("CG")) { secondCG++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("CT")) { secondCT++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("GA")) { secondGA++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("GC")) { secondGC++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("GT")) { secondGT++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("TA")) { secondTA++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("TC")) { secondTC++; secondSNPPos[currentPosition]++; currentPosition++; }
				else if(basePair.equals("TG")) { secondTG++; secondSNPPos[currentPosition]++; currentPosition++; }	
				else if(basePair.charAt(0) == 'N') { 
					referenceUnknownBases++; currentPosition++; 
					if(basePair.charAt(1) == 'N') { readUnknownBases++; currentPosition++; }
				}
				else if(basePair.charAt(1) == 'N') { readUnknownBases++; currentPosition++; }			
			}			
		}
	}	
	
	/* Process the MD string once found the CigarMD operator i (insertion). */	
	private void processMDtagCigarOperatorI() {
		int numInsertions = currentCigarMDElement.getLength();
		String insertedBases = currentCigarMDElement.getBases();
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays..
		if(currentPosition+numInsertions >= firstInsertionPos.length) {
			extendDensityArrays();
	    }
		if(cigarMDGenerator.isFirst()) {
			for(int i = 0; i < numInsertions; i++) {
				if(insertedBases.charAt(i) == 'A')      { aInsertions++; firstInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'C') { cInsertions++; firstInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'G') { gInsertions++; firstInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'T') { tInsertions++; firstInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'N') { nInsertions++; currentPosition++; }			
			}
		} else {
			for(int i = 0; i < numInsertions; i++) {
				if(insertedBases.charAt(i) == 'A')      { aInsertions++; secondInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'C') { cInsertions++; secondInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'G') { gInsertions++; secondInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'T') { tInsertions++; secondInsertionPos[currentPosition]++; currentPosition++; }
				else if(insertedBases.charAt(i) == 'N') { nInsertions++; currentPosition++; }			
			}			
		}
	}
	
	/* Process the MD string once found the CigarMD operator d (deletion). */	
	private void processMDtagCigarOperatorD() {
		int numDeletions = currentCigarMDElement.getLength();
		String deletedBases = currentCigarMDElement.getBases();
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays..
		if(currentPosition+numDeletions >= firstDeletionPos.length) {
			extendDensityArrays();		
	    }
		if(cigarMDGenerator.isFirst()) {
			for(int i = 0; i < numDeletions; i++) {
				if(deletedBases.charAt(i) == 'A')      { aDeletions++; firstDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'C') { cDeletions++; firstDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'G') { gDeletions++; firstDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'T') { tDeletions++; firstDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'N') { nDeletions++; currentPosition++; }			
			}
		} else {
			for(int i = 0; i < numDeletions; i++) {
				if(deletedBases.charAt(i) == 'A')      { aDeletions++; secondDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'C') { cDeletions++; secondDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'G') { gDeletions++; secondDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'T') { tDeletions++; secondDeletionPos[currentPosition]++; currentPosition++; }
				else if(deletedBases.charAt(i) == 'N') { nDeletions++; currentPosition++; }			
			}			
		}
	}
	
	
	// Have to test the following code.
	
	/* Process the MD string once found the CigarMD operator n. */	
	private void processMDtagCigarOperatorN() {
		int numSkipped = currentCigarMDElement.getLength();		
		totalSkippedRegions = totalSkippedRegions + numSkipped;
//		currentPosition = currentPosition + numSkipped;
//		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
//		if(currentPosition >= matchPos.length) {
//			extendDensityArrays(currentPosition);			
//	    }
	}
	
	/* Process the MD string once found the CigarMD operator s. */	
	private void processMDtagCigarOperatorS() {
		int numSoftClips = currentCigarMDElement.getLength();
		totalSoftClips = totalSoftClips + numSoftClips;
//		currentPosition = currentPosition + numSoftClips;
//		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
//		if(currentPosition >= matchPos.length) {
//			extendDensityArrays(currentPosition);			
//	    }
	}
	
	/* Process the MD string once found the CigarMD operator h. */	
	private void processMDtagCigarOperatorH() {
		int numHardClips = currentCigarMDElement.getLength();		
		totalHardClips = totalHardClips + numHardClips;
//		currentPosition = currentPosition + numHardClips;
//		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
//		if(currentPosition >= matchPos.length) {
//			extendDensityArrays(currentPosition);			
//	    }
	}
	
	/* Process the MD string once found the CigarMD operator p. */
	private void processMDtagCigarOperatorP() {
		int numPaddings = currentCigarMDElement.getLength();		
		totalPaddings = totalPaddings + numPaddings;
//		currentPosition = currentPosition + numPaddings;
//		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays.
//		if(currentPosition >= matchPos.length) {
//			extendDensityArrays(currentPosition);			
//	    }
	}	
	
	/* Process the MD string once found the CigarMD operator =. */	
	private void processMDtagCigarOperatorEQ() {
		// is this operator used?
	}	
	
	/* Process the MD string once found the CigarMD operator X. */	
	private void processMDtagCigarOperatorNEQ() {
		// is this operator used?
	}

	
	
	
	
	
	
	
	
	// Getter methods
	
	public CigarMD getCigarMD() {
		return cigarMD;
	}
	
	public boolean existPairedReads() {
		return existPairedReads;
	}
	
	public long getFirstA2C() {
		return firstAC;
	}

	public long getFirstA2G() {
		return firstAG;
	}

	public long getFirstA2T() {
		return firstAT;
	}

	public long getFirstC2A() {
		return firstCA;
	}

	public long getFirstC2G() {
		return firstCG;
	}

	public long getFirstC2T() {
		return firstCT;
	}

	public long getFirstG2A() {
		return firstGA;
	}

	public long getFirstG2C() {
		return firstGC;
	}

	public long getFirstG2T() {
		return firstGT;
	}

	public long getFirstT2A() {
		return firstTA;
	}

	public long getFirstT2C() {
		return firstTC;
	}

	public long getFirstT2G() {
		return firstTG;
	}
	
	public long getSecondA2C() {
		return secondAC;
	}

	public long getSecondA2G() {
		return secondAG;
	}

	public long getSecondA2T() {
		return secondAT;
	}

	public long getSecondC2A() {
		return secondCA;
	}

	public long getSecondC2G() {
		return secondCG;
	}

	public long getSecondC2T() {
		return secondCT;
	}

	public long getSecondG2A() {
		return secondGA;
	}

	public long getSecondG2C() {
		return secondGC;
	}

	public long getSecondG2T() {
		return secondGT;
	}

	public long getSecondT2A() {
		return secondTA;
	}

	public long getSecondT2C() {
		return secondTC;
	}

	public long getSecondT2G() {
		return secondTG;
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
	
	public long getTotalSkippedRegions() {
		return totalSkippedRegions;
	}

	public long getTotalSoftClips() {
		return totalSoftClips;
	}

	public long getTotalHardClips() {
		return totalHardClips;
	}

	public long getTotalPaddings() {
		return totalPaddings;
	}




	public long getTotal() {
		return total;
	}

	public long getReadUnknownBases() {
		return readUnknownBases;
	}	
	
	public long getReferenceUnknownBases() {
		return referenceUnknownBases;
	}	
	
	public long getSkippedReads() {
		return skippedReads;
	}	
	
	public long getTotalReads() {
		return totalReads;
	}

	public long[] getFirstSNPPos() {
		return firstSNPPos;
	}
	
	public long[] getSecondSNPPos() {
		return secondSNPPos;
	}

	public long[] getFirstInsertionPos() {
		return firstInsertionPos;
	}

	public long[] getFirstDeletionPos() {
		return firstDeletionPos;
	}
	
	public long[] getSecondInsertionPos() {
		return secondInsertionPos;
	}

	public long[] getSecondDeletionPos() {
		return secondDeletionPos;
	}	
	
	public long[] getMatchPos() {
		return matchPos;
	}	
	
	public long[] getTotalPos() {
		return totalPos;
	}
	

	
}
