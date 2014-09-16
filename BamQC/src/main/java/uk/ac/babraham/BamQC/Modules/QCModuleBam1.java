/**
 * Copyright Copyright 2014 Bartlett Ailey Eagle Genomics Ltd
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

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class QCModuleBam1 extends AbstractQCModule {

	private static Logger log = Logger.getLogger(QCModuleBam1.class);

	private long readCount = 0L;

	@Override
	public void processSequence(SAMRecord read) {
		log.info("read  = " + read);
		readCount++;
	}

	@Override
	public void processFile(SequenceFile file) {}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		// TODO Auto-generated method stub
	}

	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new JPanel();
		returnPanel.setLayout(new BorderLayout());
		returnPanel.add(new JLabel("Basic sequence stats", JLabel.CENTER), BorderLayout.NORTH);

		TableModel model = new ResultsTable();

		returnPanel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

		return returnPanel;
	}
	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeTable(report, new ResultsTable());
	}

	private class ResultsTable extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return "Read Count";
			}
			else {
				return readCount;
			}
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "Measure";
			case 1:
				return "Value";
			}
			return null;
		}

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
				return Long.class;
			}
			return null;
		}
	}

	@Override
	public String name() {
		return "BAM 1";
	}

	@Override
	public String description() {
		return "BAM 1 Description";
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean raisesError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean raisesWarning() {
		// TODO Auto-generated method stub
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
		return false;
	}
	
}
