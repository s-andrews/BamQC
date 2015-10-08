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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.util.Precision;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.Graphs.BarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class ChromosomeDensity extends AbstractQCModule {

	private String [] chromosomeNames;
	@Deprecated // TODO remove after testing new plot
	private double [] readDensities;
	private double [] logReadNumber;
	private double [] logChromosomeLength;
	
	@Override
	public void processSequence(SAMRecord read) {}

	@Override
	public void processFile(SequenceFile file) {}

	// TODO REMOVE once replaced with new plot
	@Deprecated 
	private void processAnnotationSetDeprecated(AnnotationSet annotation) {

		Chromosome [] chromosomes = annotation.chromosomeFactory().getAllChromosomes();
		
		ArrayList<Chromosome> keptChromosomes = new ArrayList<Chromosome>();
		
		for (int c=0;c<chromosomes.length;c++) {
			if (chromosomes[c].seqCount() > 0) {
				keptChromosomes.add(chromosomes[c]);
			}
		}
		
		chromosomes = keptChromosomes.toArray(new Chromosome[0]);
		
		Arrays.sort(chromosomes);
		
		chromosomeNames = new String [chromosomes.length];
		readDensities = new double[chromosomes.length];
		
		for (int c=0;c<chromosomes.length;c++) {
			chromosomeNames[c] = chromosomes[c].name();
			readDensities[c] = chromosomes[c].seqCount()/(chromosomes[c].length()/1000f);			
		}
	}
	
	
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
		logReadNumber = new double [chromosomes.length];
		logChromosomeLength = new double[chromosomes.length];
		// recorded for the text report only
		chromosomeNames = new String [chromosomes.length];
				
		for (int c=0; c<chromosomes.length; c++) {
			logReadNumber[c] = Precision.round(Math.log(chromosomes[c].seqCount()), 2);
			logChromosomeLength[c] = Precision.round(Math.log(chromosomes[c].length()), 2);
			chromosomeNames[c] = chromosomes[c].name();
		}
		
	}
	
	
	@Override
	public JPanel getResultsPanel() {
		// TODO REMOVE once replaced with new plot
		// return new HorizontalBarGraph(chromosomeNames, readDensities, "Per-chromosome read density");

		String title = "Chromosome Read Density (Log Read Number per Log Chromosome Length)";
		String[] xCategories;
		String xLabel = "Log Chromosome Length";
		double maxY = 0d;
		
		if(logReadNumber.length < 2) {
			xCategories = new String[]{"Null"};
			return new BarGraph(new double[1], 0d, maxY+maxY*0.1, xLabel, xCategories, title);
		}
		
		xCategories = new String[logChromosomeLength.length];
		
		for(int i=0; i<logReadNumber.length; i++) {
			if(maxY < logReadNumber[i]) {
				maxY = logReadNumber[i];
			}
		}
		
		for(int i=0; i<logChromosomeLength.length; i++) {
			xCategories[i] = String.valueOf(logChromosomeLength[i]);
		}
		return new BarGraph(logReadNumber, 0d, maxY+maxY*0.1, xLabel, xCategories, title);
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
		if(logChromosomeLength.length < 2) { 
			return true;
		}
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		
		super.writeDefaultImage(report, "chromosome_density.png", "Chromsome Density Graph", 800, 600);
				
		StringBuffer sb = report.dataDocument();
		
		sb.append("ChromosomeName\tChromosomeLength(log)\tReadNumber(log)\n");
		for (int i=0;i<chromosomeNames.length;i++) {
			sb.append(chromosomeNames[i]);
			sb.append("\t");
			sb.append(logChromosomeLength[i]);
			sb.append("\t");
			sb.append(logReadNumber[i]);
			sb.append("\n");
		}
				
	}

}
