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
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

/**
 * A generic interface representing a module.
 * @author Simon Andrews
 */
public interface QCModule {

	/**
	 * Process a SAMRecord
	 * @param read to process
	 */
	public void processSequence(SAMRecord read);
	
	/**
	 * Process a sequence file
	 * @param file to process
	 */
	public void processFile(SequenceFile file);
	
	/**
	 * Process an annotation set. 
	 * @param annotation to process
	 */
	public void processAnnotationSet (AnnotationSet annotation);

	/**
	 * Return a the plot following the analysis performed by this module.
	 * @return the module plot.
	 */
	public JPanel getResultsPanel();
	
	/**
	 * Return the module name.
	 * @return the name
	 */
	public String name ();
	
	/**
	 * Return a short description for this module.
	 * @return a description
	 */
	public String description ();
	
	/**
	 * Reset the module.
	 */
	public void reset ();
	
	/**
	 * Return true if this module raises errors
	 * @return true if errors can be raised.
	 */	
	public boolean raisesError();
	
	/**
	 * Return true if this module raises warnings
	 * @return true if warning can be raised.
	 */
	public boolean raisesWarning();
	
	/**
	 * Return true if this module requires the sequences
	 * @return True if sequences are required.
	 */
	public boolean needsToSeeSequences();
	
	/**
	 * Return true if this module requires annotation. 
	 * @return True if annotation is required.
	 */
	public boolean needsToSeeAnnotation();

	/**
	 * Allows you to say that this module shouldn't be included in the final report.
	 * Useful for modules which have a use under some circumstances but not others.
	 * @return
	 */
	public boolean ignoreInReport();

	/** 
	 * Generates a text report.
	 * @param report
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException;
}
