/**
 * Copyright 2010-14 Simon Andrews
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

package uk.ac.babraham.BamQC.AnnotationParsers;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.ProgressListener;
import uk.ac.babraham.BamQC.DataTypes.Genome.Genome;
import uk.ac.babraham.BamQC.Dialogs.Cancellable;


/**
 * The Class AnnotationParser provides the core methods which must be
 * implemented by a class wanting to be able to import features into
 * a BamQC genome.
 */
public abstract class AnnotationParser implements Cancellable, Runnable {
	
	/** The listeners. */
	private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
	
	/** The cancel. */
	protected boolean cancel = false;
	
	// TODO
	// In the future, genome and annotationSet should be merged in an unified annotationSet.
	// GenomeParser will extend this class.
	
	/** The genome. */
	private Genome genome;
	
	/** The annotation Set */
	protected AnnotationSet annotationSet;
	
	/** The file. */
	private File file = null;

	/*
	 * These are the methods any implementing class must provide
	 */
	
	/**
	 * Requires file.
	 * 
	 * @return true, if successful
	 */
	abstract public boolean requiresFile ();
	
	
	public abstract void parseAnnotation(AnnotationSet annotationSet, File file) throws Exception;
	
	/**
	 * Parses the annotation.
	 * 
	 * @param file the file
	 * @param genome the genome
	 * @return the annotation set
	 * @throws Exception the exception
	 */
	public abstract AnnotationSet parseAnnotation (File file, Genome genome) throws Exception;
	
	/**
	 * Name.
	 * 
	 * @return the string
	 */
	abstract public String name ();
	
	
	public AnnotationParser() {	}
	
	/**
	 * Instantiates a new annotation parser.
	 * 
	 * @param genome the genome
	 */
	public AnnotationParser (Genome genome) {
		this.genome = genome;
	}
	
	/**
	 * Genome.
	 * 
	 * @return the genome
	 */
	protected Genome genome () {
		return genome;
	}
	
	/**
	 * Adds the progress listener.
	 * 
	 * @param l the l
	 */
	public void addProgressListener (ProgressListener l) {
		if (l != null && !listeners.contains(l)) {
			listeners.add(l);
		}
	}
	
	/**
	 * Removes the progress listener.
	 * 
	 * @param l the l
	 */
	public void removeProgressListener (ProgressListener l) {
		if (l != null && listeners.contains(l)) {
			listeners.remove(l);
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.Dialogs.Cancellable#cancel()
	 */
	@Override
	public void cancel () {
		cancel = true;
	}
	
	/**
	 * Parses the file.
	 * 
	 * @param file the file
	 */
	public void parseFiles (File file) {
		if (requiresFile() && file == null) {
			progressExceptionReceived(new NullPointerException("File to parse cannot be null"));
			return;
		}
		this.file = file;
		Thread t = new Thread(this);
		t.start();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run () {
		AnnotationSet parsedSet = new AnnotationSet();
		
		try {
			if (requiresFile()) {
				parsedSet = parseAnnotation(file, genome);
				if (parsedSet == null) {
					// They cancelled or had an error which will be reported through the other methods here
					return;
				}
			}
			else {
				parsedSet = parseAnnotation(null, genome);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			progressExceptionReceived(e);
			return;
		}
		
		if (!cancel) {
			genome.setAnnotationSet(parsedSet);
			progressComplete("load_annotation", parsedSet);
		}
	}
	
	
	/*
	 * These are the methods we use to communicate with out listeners.
	 * Some of these can be accessed by the implementing class directly
	 * but the big ones need to go back through this class.
	 */
	
	/**
	 * Progress exception received.
	 * 
	 * @param e the e
	 */
	private void progressExceptionReceived (Exception e) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressExceptionReceived(e);
		}
	}
	
	/**
	 * Progress warning received.
	 * 
	 * @param e the e
	 */
	protected void progressWarningReceived (Exception e) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressWarningReceived(e);
		}
	}
	
	/**
	 * Progress updated.
	 * 
	 * @param message the message
	 * @param current the current
	 * @param max the max
	 */
	protected void progressUpdated (String message, int current, int max) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressUpdated(message, current, max);
		}
	}

	/**
	 * Progress cancelled.
	 */
	protected void progressCancelled () {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressCancelled();
		}
	}
	
	/**
	 * Progress complete.
	 * 
	 * @param command the command
	 * @param result the result
	 */
	private void progressComplete (String command, Object result) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressComplete(command, result);
		}

	}
	
	
}
