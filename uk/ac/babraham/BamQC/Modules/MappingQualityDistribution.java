/**
 * Copyright Copyright 2014 Bart Ailey Eagle Genomics Ltd
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

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.BarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class MappingQualityDistribution extends AbstractQCModule {

	private static Logger log = Logger.getLogger(MappingQualityDistribution.class);

	private final static int QUALITY_MAP_SIZE = 256;

	private int maxCount = 0;

	private int[] distribution = new int[QUALITY_MAP_SIZE];
	private String[] label = new String[QUALITY_MAP_SIZE];

	public MappingQualityDistribution() {
		for (int i = 0; i < QUALITY_MAP_SIZE; i++) {
			label[i] = Integer.toString(i);
		}
	}

	@Override
	public void processSequence(SAMRecord read) {
		int quality = read.getMappingQuality();

		log.debug("quality = " + quality);

		distribution[quality]++;

		log.debug("quality count = " + distribution[quality]);
		
		if (distribution[quality] > maxCount) maxCount = distribution[quality];
	}

	@Override
	public void processFile(SequenceFile file) {}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}

	@Override
	public JPanel getResultsPanel() {
		double[] distributionFloat = getDistributionFolat();
		String[] xTitles = new String[] { "Log of Reads" };
		double maxCountLog = Math.log10(maxCount); //Math.log10(maxCount);

		return new BarGraph(distributionFloat, 0.0D, maxCountLog, "Distribution", xTitles, label, "Quality Mapping Distribution");
	}
	
	public double[] getDistributionFolat() {
		double[] distributionFloat = new double[QUALITY_MAP_SIZE];
		
		for (int i = 0; i < QUALITY_MAP_SIZE; i++) {
			if (distribution[i] != 0) {
				distributionFloat[i] =  Math.log10(distribution[i]); 
			}
		}
		return distributionFloat;
	}


	@Override
	public String name() {
		return "Mapping Quality Distribution";
	}

	@Override
	public String description() {
		return "Mapping Quality Distribution";
	}

	@Override
	public void reset() {
		distribution = new int[QUALITY_MAP_SIZE];
		maxCount = 0;
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
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub

	}

	public int[] getDistribution() {
		return distribution;
	}

	public int getMaxCount() {
		return maxCount;
	}
	
	

}
