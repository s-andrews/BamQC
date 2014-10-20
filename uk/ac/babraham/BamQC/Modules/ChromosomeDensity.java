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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import sun.util.BuddhistCalendar;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Annotation.Chromosome;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class ChromosomeDensity extends AbstractQCModule {

	private String [] chromosomeNames;
	private float [] readDensities;
	
	public void processSequence(SAMRecord read) {}

	public void processFile(SequenceFile file) {}

	public void processAnnotationSet(AnnotationSet annotation) {

		Chromosome [] chromosomes = annotation.chromosomeFactory().getAllChromosomes();
		
		Vector<Chromosome> keptChromosomes = new Vector<Chromosome>();
		
		for (int c=0;c<chromosomes.length;c++) {
			if (chromosomes[c].seqCount() > 0) {
				keptChromosomes.add(chromosomes[c]);
			}
		}
		
		chromosomes = keptChromosomes.toArray(new Chromosome[0]);
		
		Arrays.sort(chromosomes);
		
		chromosomeNames = new String [chromosomes.length];
		readDensities = new float[chromosomes.length];
		
		for (int c=0;c<chromosomes.length;c++) {
			chromosomeNames[c] = chromosomes[c].name();
			readDensities[c] = chromosomes[c].seqCount()/(chromosomes[c].length()/1000f);
			
//			System.err.println("Density of "+chromosomeNames[c]+" is "+readDensities[c]);
		}
	}

	public JPanel getResultsPanel() {
		return new HorizontalBarGraph(chromosomeNames, readDensities, "Per-chromosome read density");
	}

	public String name() {
		return "Chromosome Read Density";
	}

	public String description() {
		return "Tells if the read density varies between chromosomes";
	}

	public void reset() {
		// TODO Auto-generated method stub

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
		return false;
	}

	public boolean needsToSeeAnnotation() {
		return true;
	}

	public boolean ignoreInReport() {
		return false;
	}

	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		ZipOutputStream zip = report.zipFile();
		zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/chromsome_density.png"));
		BufferedImage b = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics g = b.getGraphics();
		
		HorizontalBarGraph graph = new HorizontalBarGraph(chromosomeNames, readDensities, "Per-chromosome read density");
		graph.paint(g, b.getWidth(), b.getHeight());
		
		ImageIO.write(b, "PNG", zip);
		zip.closeEntry();
		
		super.simpleXhtmlReport(report, b, "Chromosome Density Graph");
		
		StringBuffer sb = report.dataDocument();
		
		sb.append("Chromosome\tDensity\n");
		for (int i=0;i<chromosomeNames.length;i++) {
			sb.append(chromosomeNames[i]);
			sb.append("\t");
			sb.append(readDensities[i]);
			sb.append("\n");
		}
		
	}

}
