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

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public interface QCModule {

	public void processSequence(SAMRecord read);
	
	public void processFile(SequenceFile file);
	
	public void processAnnotationSet (AnnotationSet annotation);

	public JPanel getResultsPanel();
	
	public String name ();
	
	public String description ();
	
	public void reset ();
	
	public boolean raisesError();
	
	public boolean raisesWarning();
	
	public boolean needsToSeeSequences();
	
	public boolean needsToSeeAnnotation();
	/**
	 * Allows you to say that this module shouldn't be included in the final report.
	 * Useful for modules which have a use under some circumstances but not others.
	 * @return
	 */
	public boolean ignoreInReport();

	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException;
	
	
}
