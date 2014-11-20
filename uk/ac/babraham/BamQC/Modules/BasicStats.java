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

	private String name = null;
	private long actualCount = 0;
	private long primaryCount = 0;
	private long pairedCount = 0;
	private long properPairCount = 0;
	private long mappedCount = 0;
	private long duplicateCount = 0;
	private long qcFailCount = 0;
	private long singletonCount = 0;
	private boolean hasAnnotation = false;
	
	public String description() {
		return "Calculates some basic statistics about the file";
	}
	
	public void processAnnotationSet(AnnotationSet annotation) {
		if (annotation.hasFeatures()) {
			hasAnnotation = true;
		}		
	}

	public boolean needsToSeeSequences() {
		return true;
	}

	public boolean needsToSeeAnnotation() {
		return true;
	}

	public JPanel getResultsPanel() {
		JPanel returnPanel = new JPanel();
		returnPanel.setLayout(new BorderLayout());
		returnPanel.add(new JLabel("Basic sequence stats",JLabel.CENTER),BorderLayout.NORTH);
		
		TableModel model = new ResultsTable();
		returnPanel.add(new JScrollPane(new JTable(model)),BorderLayout.CENTER);
		
		return returnPanel;
	
	}
	
	public void reset () {
	}

	public String name() {
		return "Basic Statistics";
	}

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
	
	public void processFile (SequenceFile file) {
		this.name = file.name();
	}
	
	public boolean raisesError() {
		return false;
	}

	public boolean raisesWarning() {
		return false;
	}
	
	public boolean ignoreInReport () {
		return false;
	}

	public void makeReport(HTMLReportArchive report) throws XMLStreamException,IOException {
		super.writeTable(report, new ResultsTable());
	}

	@SuppressWarnings("serial")
	private class ResultsTable extends AbstractTableModel {
				
		private String [] rowNames = new String [] {
				"Filename",
				"Total sequences",
				"Percent primary alignments",
				"Has annotation",
				"Percent sequences failed vendor QC",
				"Percent marked duplicate",
				"Percent sequences mapped",
				"Percent sequences paired",
				"Percent sequences properly paired",
				"Percent singletons"
				
		};		
		
		// Sequence - Count - Percentage
		public int getColumnCount() {
			return 2;
		}
	
		public int getRowCount() {
			return pairedCount > 0 ? rowNames.length : 7; // Is there a nicer way to skip paired stats for single end?
		}
	
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0: return rowNames[rowIndex];
				case 1:
					switch (rowIndex) {
					case 0 : return name;
					case 1 : return ""+actualCount;
					case 2 : return formatPercentage(primaryCount, actualCount);
					case 3 : return (hasAnnotation) ? "Yes" : "No"; // I prefer Y/N but feel free to change back
					case 4 : return formatPercentage(qcFailCount, actualCount);
					case 5 : return formatPercentage(duplicateCount, actualCount);
					case 6 : return formatPercentage(mappedCount, actualCount);
					case 7 : return formatPercentage(pairedCount, actualCount);
					case 8 : return formatPercentage(properPairCount, actualCount);
					case 9 : return formatPercentage(singletonCount, actualCount);
					
					}
			}
			return null;
		}
		
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
