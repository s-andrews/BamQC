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
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Utilities.CigarMD.CigarMD;
import uk.ac.babraham.BamQC.Utilities.CigarMD.CigarMDElement;
import uk.ac.babraham.BamQC.Utilities.CigarMD.CigarMDGenerator;
import uk.ac.babraham.BamQC.Utilities.CigarMD.CigarMDOperator;






public class VariantCallDetection extends AbstractQCModule {

	// logger
	private static Logger log = Logger.getLogger(VariantCallDetection.class);
	
	
	// data fields for statistics
    // first or second indicate whether the read is the first or second segment. If the read is not paired, 
    // it is treated as a first.
	
	private HashMap<String, Long> firstSNPs = new HashMap<String, Long>();
	private HashMap<String, Long> secondSNPs = new HashMap<String, Long>();
	private HashMap<String, Long> insertions = new HashMap<String, Long>();
	private HashMap<String, Long> deletions = new HashMap<String, Long>();
	
	private long totalMutations = 0;	
	private long totalInsertions = 0;
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
	private long readWithoutMDString = 0;
	private long readWithoutCigarString = 0;
	private long inconsistentCigarMDStrings = 0;
    private long totalReads = 0;
    
    private long splicedReads = 0;
    
    // These arrays are used to store the density of SNP and Indels at each read position.
    private long[] firstSNPPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
    private long[] firstInsertionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
    private long[] firstDeletionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
    private long[] secondSNPPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];    
    private long[] secondInsertionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
    private long[] secondDeletionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];    
    private long[] matchPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
    private long[] totalPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
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
    // temporary variable created here to limit variable declarations. 
    // these are initialised inside the method processSequence()
    private boolean isReadSpliced = false;
    private int cigarMDElementsSize = 0;
    
	// Used for computing the statistics 
	private CigarMDGenerator cigarMDGenerator = new CigarMDGenerator();

	private CigarMD cigarMD = new CigarMD();
	private CigarMDElement currentCigarMDElement = null;
	
	
	private boolean existPairedReads = false;

	

	// Constructors
	/**
	 * Default constructor
	 */
	public VariantCallDetection() { 
		firstSNPs.put("AC", 0L);
		firstSNPs.put("AG", 0L);
		firstSNPs.put("AT", 0L);
		firstSNPs.put("CA", 0L);
		firstSNPs.put("CG", 0L);
		firstSNPs.put("CT", 0L);
		firstSNPs.put("GA", 0L);
		firstSNPs.put("GC", 0L);
		firstSNPs.put("GT", 0L);
		firstSNPs.put("TA", 0L);
		firstSNPs.put("TC", 0L);
		firstSNPs.put("TG", 0L);
		
		secondSNPs.put("AC", 0L);
		secondSNPs.put("AG", 0L);
		secondSNPs.put("AT", 0L);
		secondSNPs.put("CA", 0L);
		secondSNPs.put("CG", 0L);
		secondSNPs.put("CT", 0L);
		secondSNPs.put("GA", 0L);
		secondSNPs.put("GC", 0L);
		secondSNPs.put("GT", 0L);
		secondSNPs.put("TA", 0L);
		secondSNPs.put("TC", 0L);
		secondSNPs.put("TG", 0L);
		
		insertions.put("A", 0L);
		insertions.put("C", 0L);
		insertions.put("G", 0L);
		insertions.put("T", 0L);
		insertions.put("N", 0L);
		
		deletions.put("A", 0L);
		deletions.put("C", 0L);
		deletions.put("G", 0L);
		deletions.put("T", 0L);
		deletions.put("N", 0L);		
		
	}

	
	/** 
	 * Compute the totals. For improving efficiency, this method is not invoked 
	 * inside void processSequence(SAMRecord read), but must be invoked 
	 * later.
	 */
	private void computeTotals() {
//		if(totalMutations != 0 || totalInsertions != 0 || totalDeletions != 0) {
//			return;
//		}
//		// NOTE: nInsertions and nDeletions are not counted in the totals. 
		
		totalMutations = 0;
		totalInsertions = 0;
		totalDeletions = 0;
		
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
		total = totalMatches + totalMutations + totalInsertions + totalDeletions + totalSoftClips;
	}
	
	
	// @Override methods
	
	@Override
	public void processSequence(SAMRecord read) {

		isReadSpliced = false;
		totalReads++;
		
		// Compute and get the CigarMD object combining the strings Cigar and MD tag
		cigarMDGenerator.generateCigarMD(read);
		cigarMD = cigarMDGenerator.getCigarMD();
		if(cigarMD == null || cigarMD.isEmpty()) {
			int errorType = cigarMDGenerator.getErrorType();
			switch(errorType) {
				//case 0: // no error
				//case 1: // unmapped read. This is already calculated in the BasicStatistics module
				case 2: readWithoutMDString++;
				case 3: readWithoutCigarString++;
				case 4: inconsistentCigarMDStrings++;
			}
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
		cigarMDElementsSize = cigarMDElements.size();
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
				if(!isReadSpliced) {
					isReadSpliced = true;
					splicedReads++;
				}
				
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
		computeTotals();
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
				
		firstSNPs.put("AC", 0L);
		firstSNPs.put("AG", 0L);
		firstSNPs.put("AT", 0L);
		firstSNPs.put("CA", 0L);
		firstSNPs.put("CG", 0L);
		firstSNPs.put("CT", 0L);
		firstSNPs.put("GA", 0L);
		firstSNPs.put("GC", 0L);
		firstSNPs.put("GT", 0L);
		firstSNPs.put("TA", 0L);
		firstSNPs.put("TC", 0L);
		firstSNPs.put("TG", 0L);
		
		secondSNPs.put("AC", 0L);
		secondSNPs.put("AG", 0L);
		secondSNPs.put("AT", 0L);
		secondSNPs.put("CA", 0L);
		secondSNPs.put("CG", 0L);
		secondSNPs.put("CT", 0L);
		secondSNPs.put("GA", 0L);
		secondSNPs.put("GC", 0L);
		secondSNPs.put("GT", 0L);
		secondSNPs.put("TA", 0L);
		secondSNPs.put("TC", 0L);
		secondSNPs.put("TG", 0L);
	
		totalMutations = 0;
		insertions.put("A", 0L);
		insertions.put("C", 0L);
		insertions.put("G", 0L);
		insertions.put("T", 0L);
		insertions.put("N", 0L);

		totalInsertions = 0;
		deletions.put("A", 0L);
		deletions.put("C", 0L);
		deletions.put("G", 0L);
		deletions.put("T", 0L);
		deletions.put("N", 0L);	
		
		totalDeletions = 0;
		totalMatches = 0;
		totalSkippedRegions = 0;
		totalSoftClips = 0;
		totalHardClips = 0;
		totalPaddings = 0;
		total = 0;
		skippedReads = 0;
		totalReads = 0;
		splicedReads = 0;

		readUnknownBases = 0;
		referenceUnknownBases = 0;
		
		
	    firstSNPPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
	    firstInsertionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
	    firstDeletionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
	    secondSNPPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
	    secondInsertionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
	    secondDeletionPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];	    
	    matchPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];
	    totalPos = new long[ModuleConfig.getParam("VariantCallPosition_array_length", "ignore").intValue()];	  	    
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
		String basePair;

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
				if(basePair.charAt(0) == 'N') { 
					referenceUnknownBases++; currentPosition++; 
					if(basePair.charAt(1) == 'N') { readUnknownBases++;  }
				}
				else if(basePair.charAt(1) == 'N') { readUnknownBases++; currentPosition++;  }
				else {
					firstSNPs.put(basePair, firstSNPs.get(basePair) + 1L);
					firstSNPPos[currentPosition]++; 
					currentPosition++;
				}
			}			
		} else {
			for(int i = 0; i < numMutations; i++) {
				basePair = mutatedBases.substring(i*2, i*2+2);
				if(basePair.charAt(0) == 'N') { 
					referenceUnknownBases++; currentPosition++; 
					if(basePair.charAt(1) == 'N') { readUnknownBases++;  }
				}
				else if(basePair.charAt(1) == 'N') { readUnknownBases++; currentPosition++;  }
				else {
					secondSNPs.put(basePair, secondSNPs.get(basePair) + 1L);
					secondSNPPos[currentPosition]++; 
					currentPosition++;
				}
			}			
		}
	}	
	
	/* Process the MD string once found the CigarMD operator i (insertion). */	
	private void processMDtagCigarOperatorI() {
		int numInsertions = currentCigarMDElement.getLength();
		String insertedBases = currentCigarMDElement.getBases();
		String base;
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays..
		if(currentPosition+numInsertions >= firstInsertionPos.length) {
			extendDensityArrays();
	    }
		if(cigarMDGenerator.isFirst()) {
			for(int i = 0; i < numInsertions; i++) {
				base = insertedBases.substring(i, i+1);
				insertions.put(base, insertions.get(base) + 1L);
				if(insertedBases.charAt(i) != 'N') { 
					firstInsertionPos[currentPosition]++; 
				}
				currentPosition++;
			}
		} else {
			for(int i = 0; i < numInsertions; i++) {
				base = insertedBases.substring(i, i+1);
				insertions.put(base, insertions.get(base) + 1L);
				if(insertedBases.charAt(i) != 'N') { 
					secondInsertionPos[currentPosition]++; 
				}
				currentPosition++;	
			}			
		}
	}
	
	/* Process the MD string once found the CigarMD operator d (deletion). */	
	private void processMDtagCigarOperatorD() {
		int numDeletions = currentCigarMDElement.getLength();
		String deletedBases = currentCigarMDElement.getBases();
		String base;
		// if the read.length is longer than what we supposed to be, here we increase the length of our *Pos arrays..
		if(currentPosition+numDeletions >= firstDeletionPos.length) {
			extendDensityArrays();		
	    }
		if(cigarMDGenerator.isFirst()) {
			for(int i = 0; i < numDeletions; i++) {
				base = deletedBases.substring(i, i+1);
				deletions.put(base, deletions.get(base) + 1L);
				if(deletedBases.charAt(i) != 'N') { 
					firstDeletionPos[currentPosition]++; 
				}
				currentPosition++;		
			}
		} else {
			for(int i = 0; i < numDeletions; i++) {
				base = deletedBases.substring(i, i+1);
				deletions.put(base, deletions.get(base) + 1L);
				if(deletedBases.charAt(i) != 'N') { 
					secondDeletionPos[currentPosition]++; 
				}
				currentPosition++;		
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
	
	/** Returns the calculated CigarMD or null if this is empty 
	 * @return CigarMD or null
	 */
	public CigarMD getCigarMD() {
		return cigarMD;
	}
	
	public boolean existPairedReads() {
		return existPairedReads;
	}
	
	public HashMap<String, Long> getFirstSNPs() {
		return firstSNPs;
	}
	
	public HashMap<String, Long> getSecondSNPs() {
		return secondSNPs;
	}
	
	public HashMap<String, Long> getInsertions() {
		return insertions;
	}
	
	public HashMap<String, Long> getDeletions() {
		return deletions;
	}

	public long getTotalMutations() {
		return totalMutations;
	}

	public long getTotalInsertions() {
		return totalInsertions;
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
	
	public long getTotalSplicedReads() {
		return splicedReads;
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
	
	public long getReadWithoutMDString() {
		return readWithoutMDString;
	}

	public long getReadWithoutCigarString() {
		return readWithoutCigarString;
	}
	
	public long getInconsistentCigarMDStrings() {
		return inconsistentCigarMDStrings;
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
