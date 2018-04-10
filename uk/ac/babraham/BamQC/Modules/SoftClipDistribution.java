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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Corrected null pointer exception
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.Modules;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.LineGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

/**
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 *
 */
public class SoftClipDistribution extends AbstractQCModule {

	// logger
	private static Logger log = Logger.getLogger(SoftClipDistribution.class);
	
	private static final long ERROR_CLIP_NUMBER = ModuleConfig.getParam("SoftClipDistribution_clip_number_threshold", "error").longValue();
	private static final long WARNING_CLIP_NUMBER = ModuleConfig.getParam("SoftClipDistribution_clip_number_threshold", "warn").longValue();

	private long [] leftClipCounts = new long[1];
	private long [] rightClipCounts = new long[1];
	
	@Override
	public void processSequence(SAMRecord read) {
		
		if (read.getReadUnmappedFlag()) return;
		
		int leftClip = 0;
		int rightClip = 0;
		
		
		// Get the CIGAR list
		Cigar cigar = read.getCigar();
		if (cigar == null || read.getCigarLength() == 0) {
			log.debug("Read " + read.getReadString() + " does not have Cigar string.");
			return;
		}
		
		
		List<CigarElement> elements = cigar.getCigarElements();
		CigarElement first = elements.get(0);
		CigarElement last = elements.get(elements.size()-1);

		if (first.getOperator().equals(CigarOperator.S)) {
			if (read.getReadNegativeStrandFlag()) {
				rightClip = first.getLength();
			}
			else {
				leftClip = first.getLength();				
			}
		}
		
		if (last.getOperator().equals(CigarOperator.S)) {
			if (read.getReadNegativeStrandFlag()) {
				leftClip = last.getLength();
			}
			else {
				rightClip = last.getLength();
			}			
		}

		int max=leftClip;
		if (rightClip>leftClip)max=rightClip;
		
		if (max+1 > leftClipCounts.length) expandCounts(max+1);
		
		leftClipCounts[leftClip]++;
		rightClipCounts[rightClip]++;
		
	}

	private void expandCounts (int newLen) {
		long [] temp = new long[newLen];
		for (int i=0;i<leftClipCounts.length;i++) {
			temp[i] = leftClipCounts[i];
		}
		leftClipCounts = temp;
		
		temp = new long[newLen];
		for (int i=0;i<rightClipCounts.length;i++) {
			temp[i] = rightClipCounts[i];
		}
		rightClipCounts = temp;
	}
	
	@Override
	public void processFile(SequenceFile file) {}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {}

	@Override
	public JPanel getResultsPanel() {

		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new GridLayout(2,1));
		
		String [] labels = new String[leftClipCounts.length];
		double [][] leftData = new double[1][leftClipCounts.length];
		double [][] rightData = new double[1][leftClipCounts.length];
		
		double maxLeft=1;
		double maxRight=1;
		
		for (int i=0;i<leftClipCounts.length;i++) {
			labels[i] = ""+i;
			leftData[0][i] = leftClipCounts[i];
			rightData[0][i] = rightClipCounts[i];
			
			if (leftData[0][i] > maxLeft) maxLeft = leftData[0][i];
			if (rightData[0][i] > maxRight) maxRight = rightData[0][i];
		}
		
		
		resultsPanel.add(new LineGraph(leftData, 0, maxLeft, "Clip Length", "Clip Number", new String[]{"Left (5') clips"}, labels,"Soft Clip Distribution at the Left (5') End"));
		resultsPanel.add(new LineGraph(rightData, 0, maxRight, "Clip Length", "Clip Number", new String[]{"Right (3') clips"},labels, "Soft Clip Distribution at the Right (3') End"));
		
		return (resultsPanel);
	}

	@Override
	public String name() {
		return "Soft Clip Length Distributions";
	}

	@Override
	public String description() {
		return "Looks at how much of your reads have been soft clipped";
	}

	@Override
	public void reset() {
		leftClipCounts = new long[1];
		rightClipCounts = new long[1];
		
	}

	@Override
	public boolean raisesError() {
		for(int i=0; i<leftClipCounts.length; i++) {
			if(leftClipCounts[i] > ERROR_CLIP_NUMBER) {
				return true;
			}
		}
		for(int i=0; i<leftClipCounts.length; i++) {
			if(rightClipCounts[i] > ERROR_CLIP_NUMBER) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean raisesWarning() {
		for(int i=0; i<leftClipCounts.length; i++) {
			if(leftClipCounts[i] > WARNING_CLIP_NUMBER) {
				return true;
			}
		}
		for(int i=0; i<leftClipCounts.length; i++) {
			if(rightClipCounts[i] > WARNING_CLIP_NUMBER) {
				return true;
			}
		}
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
		if(ModuleConfig.getParam("SoftClipDistribution", "ignore") > 0 || (leftClipCounts.length==1 && rightClipCounts.length==1))
			return true;
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		super.writeDefaultImage(report, "soft_clip_distribution_graph.png","Soft Clipping Distribution Graph", 800, 600);
				
		StringBuffer sb = report.dataDocument();
				
		sb.append("Length\t5'_count\t3'_count\n");
		for (int i=0;i<leftClipCounts.length;i++) {
			sb.append(i);
			sb.append("\t");
			sb.append(leftClipCounts[i]);
			sb.append("\t");
			sb.append(rightClipCounts[i]);
			sb.append("\n");
		}
	}

	public long[] getLeftClipCounts() {
		return leftClipCounts;
	}

	public long[] getRightClipCounts() {
		return rightClipCounts;
	}

}
