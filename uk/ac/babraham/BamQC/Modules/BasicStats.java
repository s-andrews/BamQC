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
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class BasicStats extends AbstractQCModule {

	private String filename = "";
	private String command = "";
	private boolean hasAnnotation = false;
	private String annotationFile = "";
	private long actualCount = 0;
	private long primaryCount = 0;
	private long pairedCount = 0;
	private long properPairCount = 0;
	private long mappedCount = 0;
	private long duplicateCount = 0;
	private long qcFailCount = 0;
	private long singletonCount = 0;

	VariantCallDetection vcd = null;

	/**
	 * Constructor. Reuse of the computation provided by VariantCallDetection analysis.
	 */
	public BasicStats(VariantCallDetection vcd) {
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
			annotationFile = annotation.getFile().getName();
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
		returnPanel.add(new JLabel("Basic sequence stats",JLabel.CENTER),BorderLayout.NORTH);
		
		TableModel model = new ResultsTable();
		returnPanel.add(new JScrollPane(new JTable(model)),BorderLayout.CENTER);
		
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
		actualCount++;
		if (!sequence.isSecondaryOrSupplementary()) {
			++primaryCount;
		}
		
		if (sequence.getReadPairedFlag()) {
			pairedCount++;
			if (sequence.getProperPairFlag()) properPairCount++;
			if (sequence.getMateUnmappedFlag() && ! sequence.getReadUnmappedFlag()) singletonCount++;
		}
		
		if (! sequence.getReadUnmappedFlag()) mappedCount++;
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
		super.writeTable(report, new ResultsTable());
	}

	@SuppressWarnings("serial")
	private class ResultsTable extends AbstractTableModel {
		
		private long totalSplicedReads = vcd.getTotalSplicedReads();
		private long totalSkippedReads = vcd.getSkippedReads();
		private long variantCallDetectionTotalReads = vcd.getTotalReads();
		private long totalInsertions = vcd.getTotalInsertions();
		private long totalDeletions = vcd.getTotalDeletions();
		private long totalMutations = vcd.getTotalMutations();
		private long totalBases = vcd.getTotal();
		
		private ArrayList<String> rowNames = new ArrayList<String>();
		private ArrayList<String> rowValues = new ArrayList<String>();
		
		public ResultsTable() {
			super();
			
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
 			} 
 			else { 
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
 					
 			rowNames.add("Percent sequences mapped");
 			rowValues.add(formatPercentage(mappedCount, actualCount));
 			
 			rowNames.add("Percent sequences paired");
 			rowValues.add(formatPercentage(pairedCount, actualCount));
 			if(pairedCount > 0) {
 				rowNames.add("Percent sequences properly paired");
 				rowValues.add(formatPercentage(properPairCount, actualCount));
 			
 				rowNames.add("Percent singletons");
 				rowValues.add(formatPercentage(singletonCount, actualCount));
 			}
 			
 			rowNames.add("Percent spliced reads");
 			rowValues.add(formatPercentage(totalSplicedReads, variantCallDetectionTotalReads));
 			
 			rowNames.add("Percent reads without MD tag string");
 			rowValues.add(formatPercentage(totalSkippedReads, variantCallDetectionTotalReads));
 			
 			rowNames.add("Percent indels");
 			if(totalBases > 0) { 
 				rowValues.add(formatPercentage(totalInsertions+totalDeletions, totalBases)); 
 			} else {
 				rowValues.add("NaN");
 			}
 			
 			rowNames.add("Percent SNPs");	
 			if(totalBases > 0) { 
 				rowValues.add(formatPercentage(totalMutations, totalBases)); 
 			} else {
 				rowValues.add("NaN");
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
		
		private String formatPercentage(long a, long b) {
	        return String.format("%6.3f", 100 * a / (double) b);
	    }
		
		@Override
		public Class<?> getColumnClass (int columnIndex) {
			switch (columnIndex) {
			case 0: return String.class;
			case 1: return String.class;
			case 2: return Float.class;
			case 3: return String.class;
		}
		return null;
			
		}
	}

	

}
