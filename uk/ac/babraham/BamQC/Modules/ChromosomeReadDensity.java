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
 * - Piero Dalle Pezze: Changed plot, changed data representation, added report, added y axis label, antialiasing, axes numbers resizing to avoid overlapping.
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.util.Precision;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.Graphs.CompactScatterGraph;
import uk.ac.babraham.BamQC.Graphs.ScatterGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

/**
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 *
 */
public class ChromosomeReadDensity extends AbstractQCModule {

	private String [] chromosomeNames;
	private double [] readNumber;
	private double [] chromosomeLength;
	
	@Override
	public void processSequence(SAMRecord read) {}

	@Override
	public void processFile(SequenceFile file) {}
	
	@Override
	public void processAnnotationSet(AnnotationSet annotation) {

		//processAnnotationSetDeprecated(annotation);
		
		Chromosome [] chromosomes = annotation.chromosomeFactory().getAllChromosomes();
		
		ArrayList<Chromosome> keptChromosomes = new ArrayList<Chromosome>();
		
		for (int c=0;c<chromosomes.length;c++) {
			if (chromosomes[c].seqCount() > 0) {
				keptChromosomes.add(chromosomes[c]);
			}
		}
		
		chromosomes = keptChromosomes.toArray(new Chromosome[0]);
		

		// Sort by chromosome length, replacing the Chromosome implementation of compare.
		Arrays.sort(chromosomes, new Comparator<Chromosome>() {
			@Override	
			public int compare(Chromosome c1, Chromosome c2) {
				if(c1.length() < c2.length()) { 
					return -1;
				} else if(c1.length() == c2.length()) {
					return 0;
				} 
				return 1;
			}
			});
		
		// recorded for the plot and text report
		readNumber = new double [chromosomes.length];
		chromosomeLength = new double[chromosomes.length];
		// recorded for the text report only
		chromosomeNames = new String [chromosomes.length];
				
		for (int c=0; c<chromosomes.length; c++) {
//			readNumber[c] = chromosomes[c].seqCount();
//			chromosomeLength[c] = chromosomes[c].length();
			chromosomeNames[c] = chromosomes[c].name();
			readNumber[c] = Precision.round(Math.log(chromosomes[c].seqCount()), 2);
			chromosomeLength[c] = Precision.round(Math.log(chromosomes[c].length()), 2);
		}
		
	}
	
	
	@Override
	public JPanel getResultsPanel() {
		String title = "Chromosome Read Density ( hover the mouse on the blue dots for names )";
		String xLabel = "Log Chromosome Length";
		String yLabel = "Log Read Number";
		if(readNumber.length < 1) {
			return new ScatterGraph(new double[1], new double[1], new String[1], xLabel, yLabel, title);
		}		
		return new ScatterGraph(readNumber, chromosomeLength, chromosomeNames, xLabel, yLabel, title);
	}

	
	/* This simply plots the points without including empty spaces in between if these are found. */
	@Deprecated
	public JPanel getOldResultsPanel() {

		String title = "Chromosome Read Density";
		String[] xCategories;
		String xLabel = "Log Chromosome Length";
		String yLabel = "Log Read Number";
		double maxY = Double.MIN_VALUE, minY=Double.MAX_VALUE;
		
		if(readNumber.length < 1) {
			xCategories = new String[]{"Null"};
//			 Previously this was a bar graph
//			return new BarGraph(new double[1], 0d, maxY, xLabel, yLabel, xCategories, title);
			return new CompactScatterGraph(new double[1], 0d, maxY, xLabel, yLabel, xCategories, title);
		}
		
		xCategories = new String[chromosomeLength.length];
		
		for(int i=0; i<readNumber.length; i++) {
			if(maxY < readNumber[i]) {
				maxY = readNumber[i];
			} else if(minY > readNumber[i]) {
					minY = readNumber[i];
			}
			//System.out.println(chromosomeLength[i] + " " + readNumber[i]);
		}

		// temporarily replaced with 0
		minY = 0;
		
		for(int i=0; i<chromosomeLength.length; i++) {
			xCategories[i] = String.valueOf(chromosomeLength[i]);
		}
		// Previously this was a bar graph
		//return new BarGraph(readNumber, minY, maxY, xLabel, yLabel, xCategories, title);
		// This just plots the data as it is, without empty non-represented points.
		return new CompactScatterGraph(readNumber, minY, maxY+maxY*0.1, xLabel, yLabel, xCategories, title);
	}
	
	
	@Override
	public String name() {
		return "Chromosome Read Density";
	}

	@Override
	public String description() {
		return "Tells if the read density varies between chromosomes";
	}

	@Override
	public void reset() { }

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
		return false;
	}

	@Override
	public boolean needsToSeeAnnotation() {
		return true;
	}

	@Override
	public boolean ignoreInReport() {
		if(ModuleConfig.getParam("ChromosomeReadDensity", "ignore") > 0 || chromosomeLength.length < 1) { 
			return true;
		}
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		
		super.writeDefaultImage(report, "chromosome_density.png", "Chromsome Density Graph", 800, 600);
				
		StringBuffer sb = report.dataDocument();
		
		sb.append("Chromosome_name\tChromosome_length_(log)\tRead_number_(log)\n");
		for (int i=0;i<chromosomeNames.length;i++) {
			sb.append(chromosomeNames[i]);
			sb.append("\t");
			sb.append(chromosomeLength[i]);
			sb.append("\t");
			sb.append(readNumber[i]);
			sb.append("\n");
		}
				
	}

	public String[] getChromosomeNames() {
		return chromosomeNames;
	}

	public double[] getLogReadNumber() {
		return readNumber;
	}

	public double[] getLogChromosomeLength() {
		return chromosomeLength;
	}

}
