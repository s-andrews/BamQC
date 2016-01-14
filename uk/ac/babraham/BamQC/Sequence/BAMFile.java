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
package uk.ac.babraham.BamQC.Sequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

/**
 * 
 * @author Simon Andrews
 *
 */
public class BAMFile implements SequenceFile {

	private static Logger log = Logger.getLogger(SequenceFile.class);
	
	private File file;
	private boolean onlyMapped;
	private SAMFileHeader header;
	private long fileSize = 0;
	private long recordSize = 0;
	private AnnotationSet annotationSet = new AnnotationSet();
	
	// We keep the file stream around just so we can see how far through
	// the file we've got.  We don't read from this directly, but it's the
	// only way to access the file pointer.
	private FileInputStream fis;

	private SAMFileReader br;
	private String name;
	private SAMRecord nextSequence = null;
	Iterator<SAMRecord> it;
	
	protected BAMFile (File file, boolean onlyMapped) throws SequenceFormatException, IOException {
		this.file = file;
		fileSize = file.length();
		name = file.getName();
		this.onlyMapped = onlyMapped;

		SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);

		fis = new FileInputStream(file);
		
		br = new SAMFileReader(fis);
		
		header = br.getFileHeader();
		
		it = br.iterator();
		readNext();
	}
	
	@Override
	public String name () {
		return name;
	}
		
	@Override
	public boolean canListChromosomes() {
		return !header.getSequenceDictionary().isEmpty();
	}

	@Override
	public Chromosome[] listChromosomes() {
		SAMSequenceDictionary dict = header.getSequenceDictionary();
		List<SAMSequenceRecord> records = dict.getSequences();
		int recordsSize = records.size(); 
		Chromosome[] chrs = new Chromosome[recordsSize];
		
		for(int i=0; i<recordsSize; i++) {
			SAMSequenceRecord record = records.get(i);
			Chromosome chr = annotationSet.chromosomeFactory().getChromosome(record.getSequenceName());
			chr.setLength(record.getSequenceLength());
			chrs[i] = chr;
		}
		
		return chrs;
		
	}

	@Override
	public int getPercentComplete() {
		if (!hasNext()) return 100;
		
		try {
			int percent = (int) (((double)fis.getChannel().position()/ fileSize)*100);
			return percent;
		} 
		catch (IOException e) {
			log.error(e, e);
		}
		return 0;
	}

	@Override
	public boolean isColorspace () {
		return false;
	}
		
	@Override
	public boolean hasNext() {
		return nextSequence != null;
	}

	@Override
	public SAMRecord next () throws SequenceFormatException {
		SAMRecord returnSeq = nextSequence;
		readNext();
		return returnSeq;
	}
	
	private void readNext() throws SequenceFormatException {
		
		SAMRecord record;
		
		while (true) {
			
			if (!it.hasNext()) {
				nextSequence = null;
				try {
					br.close();
					fis.close();
				}
				catch (IOException ioe) {
					log.error(ioe, ioe);
				}
				return;
			}
		
			try {
				record = it.next();
			}
			catch (SAMFormatException sfe) {
				throw new SequenceFormatException(sfe.getMessage());
			}
		
			// We skip over entries with no mapping if that's what the user asked for
			if (onlyMapped && record.getReadUnmappedFlag()) {
				continue;
			}
			break;
			
		}
		
		if (recordSize == 0) {
			recordSize = (record.getReadLength()*2)+150;
			if (br.isBinary()) {
				recordSize /= 4;
			}
		}

		nextSequence = record;

	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public AnnotationSet annotationSet() {
		return annotationSet;
	}
	
}
