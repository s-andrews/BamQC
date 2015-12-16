/**
 * Copyright Copyright 2010-14 Simon Andrews
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
 * - Piero Dalle Pezze: annotation, command, variant calls, splices, creation of new table having multilines.
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.Modules;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.BamQCConfig;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.DataTypes.Genome.ChromosomeFactory;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Utilities.MultiLineTableCellRenderer;

/**
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 *
 */
public class BasicStatistics extends AbstractQCModule {

	private String filename = "";
	private boolean headerParsed = false;
	private String command = "";
	private boolean hasAnnotation = false;
	private String annotationFile = "";
	private long featureTypeCount = 0;
	private int chromosomeCount = 0;
	private long actualCount = 0;
	private long primaryCount = 0;
	private long pairedCount = 0;
	private long properPairCount = 0;
	private long unmappedCount = 0;
	private long duplicateCount = 0;
	private long qcFailCount = 0;
	private long singletonCount = 0;
	
	private long totalSplicedReads = 0;
	private long totalSkippedReads = 0;
	private long totalReadsWithoutMDString = 0;
	private long totalReadsWithoutCigarString = 0;
	private long totalInconsistenReadsCigarMDString = 0;
	private long variantCallDetectionTotalReads = 0;
	private long totalSoftClips = 0;
	private long totalInsertions = 0;
	private long totalDeletions = 0;
	private long totalMutations = 0;
	private long totalBases = 0;
	

	VariantCallDetection vcd = null;
	private boolean genomeCoverage = false;


	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public BasicStatistics(VariantCallDetection vcd) {
		super();
		this.vcd = vcd;
	}
	
	@Override
	public String description() {
		return "Calculates some basic statistics about the file";
	}
	
	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		if (annotation.hasFeatures()) {
			hasAnnotation = true;
			if(annotation.getFile() != null) {
				annotationFile = annotation.getFile().getName();
			} else {
				annotationFile = "Genome: " + BamQCConfig.getInstance().genome;
			}
			featureTypeCount = annotation.getAllFeatures().length;
		}
		chromosomeCount = annotation.chromosomeFactory().getAllChromosomes().length;

		Chromosome[] chromosomes = annotation.chromosomeFactory().getAllChromosomes();
		if(chromosomes != null && chromosomes.length > 0) {
			for(int c = 0; c < chromosomes.length && !genomeCoverage; c++)
				for(int i = 0; i< chromosomes[c].getBinCountData().length && !genomeCoverage; i++)
					if(chromosomes[c].getBinCountData().length > 1)
						genomeCoverage = true;
		}
	}

	@Override
	public boolean needsToSeeSequences() {
		return true;
	}

	@Override
	public boolean needsToSeeAnnotation() {
		return true;
	}

	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new JPanel();
		returnPanel.setLayout(new BorderLayout());
		returnPanel.add(new JLabel("Basic Statistics",JLabel.CENTER),BorderLayout.NORTH);
				
		TableModel model = new ResultsTable();
		JTable table = new JTable(model);
		// add multi line per cell renderer to this table.
		table.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());
		returnPanel.add(new JScrollPane(table),BorderLayout.CENTER);
		
		return returnPanel;
	
	}
	
	@Override
	public void reset () {
	}

	@Override
	public String name() {
		return "Basic Statistics";
	}

	@Override
	public void processSequence(SAMRecord sequence) {
		
		// extract the method used for generating the SAM/BAM file if present in the header file.
		if(!headerParsed) {
			String fullHeader = sequence.getHeader().getTextHeader();
			if(fullHeader != null) {
				String[] headerLines = fullHeader.split("@");
				for(int i=0; i<headerLines.length; i++) {
					if(headerLines[i].startsWith("PG")) {
						command += headerLines[i].replace("PG\t", "").replace('\t', ' ');
					}
				}
			}
			headerParsed = true;
		}
		
		
		actualCount++;
		if (!sequence.isSecondaryOrSupplementary()) {
			++primaryCount;
		}
		
		if (sequence.getReadPairedFlag()) {
			pairedCount++;
			if (sequence.getProperPairFlag()) properPairCount++;
			if (sequence.getMateUnmappedFlag() && ! sequence.getReadUnmappedFlag()) singletonCount++;
		}
		
		if (sequence.getReadUnmappedFlag()) unmappedCount++;
		if (sequence.getReadFailsVendorQualityCheckFlag()) qcFailCount++;
		if (sequence.getDuplicateReadFlag()) duplicateCount++;
		
	}
	
	@Override
	public void processFile (SequenceFile file) {
		this.filename = file.name();
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
	public boolean ignoreInReport () {
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException,IOException {
		// note: the following method generates both the HTML code and the text report. 
		// Therefore no text report code is required here.
		super.writeTable(report, new ResultsTable());
	}
	
	private String formatPercentage(long a, long b) {
        return String.format("%6.3f", 100 * a / (double) b);
    }
	

	/**
	 * The Table containing the statistics and additional information about the SAM/Bam file.
	 * @author Piero Dalle Pezze
	 */
	private class ResultsTable extends AbstractTableModel {
		private static final long serialVersionUID = 4444508216021418468L;
			
		private ArrayList<String> rowNames = new ArrayList<String>();
		private ArrayList<String> rowValues = new ArrayList<String>();
		
		public ResultsTable() {
			super();
			
			extractVariantCallStatistics();
			
			rowNames.add("File name");
			rowValues.add(filename);
			
			if(!command.equals("")) {
				rowNames.add("Command generating Sam/Bam file");
				rowValues.add(command);
			}
			
			rowNames.add("Has annotation");
 			if(hasAnnotation) { 
 				rowValues.add("Yes");
 				
 				rowNames.add("Annotation file name");
 				rowValues.add(annotationFile);
 				rowNames.add("Total feature types");
 				rowValues.add("" + featureTypeCount);
 			} 
 			else { 
 				rowValues.add("No");
 			}

 			rowNames.add("Total chromosomes");
 			rowValues.add("" + chromosomeCount);
 			
 			// Genome Coverage
 			rowNames.add("Sufficient genome coverage");
 			if(genomeCoverage) {
 	 			rowValues.add("Yes");
 			} else {
 	 			rowValues.add("No");
 			}
 			
 			rowNames.add("Total sequences");
 			rowValues.add("" + actualCount);
 			
 			rowNames.add("Percent primary alignments");
 			rowValues.add(formatPercentage(primaryCount, actualCount));
 			
 			rowNames.add("Percent sequences failed vendor QC");
 			rowValues.add(formatPercentage(qcFailCount, actualCount));
 			
 			rowNames.add("Percent marked duplicate");
 			rowValues.add(formatPercentage(duplicateCount, actualCount));
 			
			rowNames.add("Percent sequences spliced");
 			rowValues.add(formatPercentage(totalSplicedReads, variantCallDetectionTotalReads));
 			
 			rowNames.add("Percent sequences paired");
 			rowValues.add(formatPercentage(pairedCount, actualCount));
 			if(pairedCount > 0) {
 				rowNames.add("Percent sequences properly paired");
 				rowValues.add(formatPercentage(properPairCount, actualCount));
 			
 				rowNames.add("Percent singletons");
 				rowValues.add(formatPercentage(singletonCount, actualCount));
 			}
 			
 			rowNames.add("Percent sequences unmapped");
 			rowValues.add(formatPercentage(unmappedCount, actualCount));
 			
 			// we do not consider here the unmapped sequences which are naturally discarded.
 			rowNames.add("Percent sequences without MD Tag String");
 			rowValues.add(formatPercentage(totalReadsWithoutMDString, variantCallDetectionTotalReads));
 			
 			rowNames.add("Percent sequences without Cigar String");
 			rowValues.add(formatPercentage(totalReadsWithoutCigarString, variantCallDetectionTotalReads));
 			
 			rowNames.add("Percent sequences with inconsistent Cigar or MD Tag Strings");
 			rowValues.add(formatPercentage(totalInconsistenReadsCigarMDString, variantCallDetectionTotalReads));

 			rowNames.add("Percent sequences discarded for variant call detection");
 			rowValues.add(formatPercentage(totalSkippedReads, variantCallDetectionTotalReads));
 			
 			if(totalSkippedReads < variantCallDetectionTotalReads) {
	 			rowNames.add("Percent indels");
	 			if(totalBases > 0) { 
	 				rowValues.add(formatPercentage(totalInsertions+totalDeletions, totalBases) + 
	 							  " (Ins:" + formatPercentage(totalInsertions, totalBases) +
	   							  "; Del:" + formatPercentage(totalDeletions, totalBases) + ")"); 
	 			} else {
	 				rowValues.add("NaN");
	 			}
	 			
	 			rowNames.add("Percent SNPs");	
	 			if(totalBases > 0) { 
	 				rowValues.add(formatPercentage(totalMutations, totalBases)); 
	 			} else {
	 				rowValues.add("NaN");
	 			}
	 			
	 			rowNames.add("Percent soft clips");
	 			if(totalBases > 0) { 
	 				rowValues.add(formatPercentage(totalSoftClips, totalBases)); 
	 			} else {
	 				rowValues.add("NaN");
	 			}
	 			
 			}
 			
		}
		
		// Sequence - Count - Percentage
		@Override
		public int getColumnCount() {
			return 2;
		}
	
		@Override
		public int getRowCount() {
			return rowNames.size();
		}
	
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 0)
				return rowNames.get(rowIndex);
			else if(columnIndex == 1) 
				return rowValues.get(rowIndex);
			else 
				return null;
		}
			
		@Override
		public String getColumnName (int columnIndex) {
			switch (columnIndex) {
				case 0: return "Measure";
				case 1: return "Value";
			}
			return null;
		}
				
		@Override
		public Class<?> getColumnClass (int columnIndex) {
			return String.class;
		}
		
		@Override
	    public boolean isCellEditable(int row, int column) {
	       return false;
	    }
		
	}

	private void extractVariantCallStatistics() {
		// extract these results
		// Variant Call Detection Summaries
		if(vcd != null) {
			totalSplicedReads = vcd.getTotalSplicedReads();
			totalSkippedReads = vcd.getSkippedReads();
			totalReadsWithoutMDString = vcd.getReadWithoutMDString();
			totalReadsWithoutCigarString = vcd.getReadWithoutCigarString();
			totalInconsistenReadsCigarMDString = vcd.getInconsistentCigarMDStrings();
			variantCallDetectionTotalReads = vcd.getTotalReads();
			totalSoftClips = vcd.getTotalSoftClips();
			totalInsertions = vcd.getTotalInsertions();
			totalDeletions = vcd.getTotalDeletions();
			totalMutations = vcd.getTotalMutations();
			totalBases = vcd.getTotal();
		}
	}
	
	public String getFilename() {
		return filename;
	}

	public boolean isHeaderParsed() {
		return headerParsed;
	}

	public String getCommand() {
		return command;
	}

	public boolean isHasAnnotation() {
		return hasAnnotation;
	}

	public String getAnnotationFile() {
		return annotationFile;
	}
	
	public long getFeatureTypeCount() {
		return featureTypeCount;
	}
	
	public int getChromosomeCount() {
		return chromosomeCount;
	}

	public long getActualCount() {
		return actualCount;
	}

	public long getPrimaryCount() {
		return primaryCount;
	}

	public long getPairedCount() {
		return pairedCount;
	}

	public long getProperPairCount() {
		return properPairCount;
	}

	public long getUnmappedCount() {
		return unmappedCount;
	}

	public long getDuplicateCount() {
		return duplicateCount;
	}

	public long getQcFailCount() {
		return qcFailCount;
	}

	public long getSingletonCount() {
		return singletonCount;
	}

	public long getTotalSplicedReads() {
		return totalSplicedReads;
	}

	public long getTotalSkippedReads() {
		return totalSkippedReads;
	}

	public long getVariantCallDetectionTotalReads() {
		return variantCallDetectionTotalReads;
	}

	public long getTotalInsertions() {
		return totalInsertions;
	}

	public long getTotalDeletions() {
		return totalDeletions;
	}

	public long getTotalMutations() {
		return totalMutations;
	}

	public long getTotalBases() {
		return totalBases;
	}


}
