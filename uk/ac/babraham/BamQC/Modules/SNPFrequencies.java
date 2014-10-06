/**
 * Copyright Copyright 2014 Simon Andrews
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Annotation.Chromosome;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class SNPFrequencies extends AbstractQCModule {

	private long ga = 0;
	private long gt = 0;
	private long gc = 0;
	private long gd = 0;
	private long ag = 0;
	private long at = 0;
	private long ac = 0;
	private long ad = 0;
	private long tg = 0;
	private long ta = 0;
	private long tc = 0;
	private long td = 0;
	private long cg = 0;
	private long ca = 0;
	private long ct = 0;
	private long cd = 0;
	private long insertion = 0;
	private long nonMut = 0;
	private long total = 0;

	
	public void processSequence(SAMRecord read) {

		// SAM format sucks for reading deletions/insertions.  To get this
		// information you need to use a combination of cigar operations
		// which say what the pattern of matches, insertions and deletions
		// is, and the MD tag which contains the bases from the reference which
		// are different to the read.
		
		String md = read.getStringAttribute("MD");
//		System.err.println("MD = "+md+" Cigar="+read.getCigarString());
		
	}

	public void processFile(SequenceFile file) {}

	public void processAnnotationSet(AnnotationSet annotation) {}

	public JPanel getResultsPanel() {
		return null;
	}

	public String name() {
		return "SNP Frequency";
	}

	public String description() {
		return "Looks at the overall SNP frequencies in the data";
	}

	public void reset() {
		ga = 0;
		gt = 0;
		gc = 0;
		gd = 0;
		ag = 0;
		at = 0;
		ac = 0;
		ad = 0;
		tg = 0;
		ta = 0;
		tc = 0;
		td = 0;
		cg = 0;
		ca = 0;
		ct = 0;
		cd = 0;
		nonMut = 0;
		total = 0;
		insertion = 0;
	}

	public boolean raisesError() {
		//TODO: Set this
		return false;
	}

	public boolean raisesWarning() {
		//TODO: Set this
		return false;
	}

	public boolean needsToSeeSequences() {
		return true;
	}

	public boolean needsToSeeAnnotation() {
		return false;
	}

	public boolean ignoreInReport() {
		return total == 0;
	}

	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub

	}

}
