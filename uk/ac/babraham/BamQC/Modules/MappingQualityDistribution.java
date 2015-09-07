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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Graphs.BarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class MappingQualityDistribution extends AbstractQCModule {

	private static Logger log = Logger.getLogger(MappingQualityDistribution.class);

	private final static int QUALITY_MAP_SIZE = 256;
	private static final double ERROR_FRACTION = ModuleConfig.getParam("MappingQualityDistribution_fraction", "error");
	private static final double WARNING_FRACTION = ModuleConfig.getParam("MappingQualityDistribution_fraction", "warn");
	
	private int maxCount = 0;
	private int readNumber = 0; 

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
		readNumber++;
		
		log.debug("quality count = " + distribution[quality]);

		if (distribution[quality] > maxCount) {
			maxCount = distribution[quality];
		}
	}

	@Override
	public void processFile(SequenceFile file) {}

	@Override
	public void processAnnotationSet(AnnotationSet annotation) {
		throw new UnsupportedOperationException("processAnnotationSet called");
	}

	@Override
	public JPanel getResultsPanel() {
		double[] distributionDouble = getDistributionDouble();
		double maxCountPercent = (maxCount / (double) readNumber) * 100.0; //Math.log10(maxCount);
		
		return new BarGraph(distributionDouble, 0.0D, maxCountPercent, "MAPQ Value", label, "Quality Mapping Distribution");
	}


	public double[] getDistributionDouble() {
		double total = readNumber;
		List<Integer> distributionList = new ArrayList<Integer>();
		
		for (int count : distribution) {
			distributionList.add(count);
		}
		for (int i = (distributionList.size() -1); i >= 0; i--) {
			double value = distributionList.get(i);
			
			if (value != 0) break;
			
			distributionList.remove(i);
		}
		double[] distributionDouble = new double[distributionList.size()];
		
		int i = 0;
		for (int value : distributionList) {
			distributionDouble[i++] = (value / total) * 100.0;
		}
		return distributionDouble;
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
	
	public double getFraction() {
		double fraction = 0.0;
		
		for (int i = (distribution.length -1); i >= 0; i--) {
			if (distribution[i] != 0) {
				fraction =  (double) distribution[i] / (double) readNumber;
				break;
			}
		}
		return fraction;
	}

	@Override
	public boolean raisesError() {
		return getFraction() < ERROR_FRACTION;
	}

	@Override
	public boolean raisesWarning() {
		return getFraction() < WARNING_FRACTION;
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

		super.writeDefaultImage(report, "mapq_distribtion.png", "Mapping quality value distribtion", 800, 600);
			
		StringBuffer sb = report.dataDocument();
		
		sb.append("MAPQ\tCount\n");
		
		for (int i=0;i<distribution.length;i++) {
			sb.append(i);
			sb.append("\t");
			sb.append(distribution[i]);
			sb.append("\n");
		}
			
	}

	public int[] getDistribution() {
		return distribution;
	}

	public int getMaxCount() {
		return maxCount;
	}
	
}
