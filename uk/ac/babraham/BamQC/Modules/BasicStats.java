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
				"Total Sequences",
				"Percent primary alignments",
				"Has annotation"
		};		
		
		// Sequence - Count - Percentage
		public int getColumnCount() {
			return 2;
		}
	
		public int getRowCount() {
			return rowNames.length;
		}
	
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0: return rowNames[rowIndex];
				case 1:
					switch (rowIndex) {
					case 0 : return name;
					case 1 : return ""+actualCount;
					case 2 : return (primaryCount*100)/(float)actualCount;
					case 3 : return (hasAnnotation) ? "True" : "False";
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
