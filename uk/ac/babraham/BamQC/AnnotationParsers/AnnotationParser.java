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

import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.DataTypes.ProgressListener;
import uk.ac.babraham.BamQC.Dialogs.Cancellable;


/**
 * The Class AnnotationParser provides the core methods which must be
 * implemented by a class wanting to be able to import features into
 * a BamQC AnnotationSet.
 */
public abstract class AnnotationParser implements Cancellable, ProgressListener {
	
	/** The listeners. */
	protected Vector<ProgressListener> listeners = new Vector<ProgressListener>();
	
	/** The cancel. */
	protected boolean cancel = false;
	

	/*
	 * These are the methods any implementing class must provide
	 */
	
	/**
	 * Requires file.
	 * 
	 * @return true, if successful
	 */
	public abstract boolean requiresFile ();
	
	/**
	 * Parses a file containing an annotation.
	 * 
	 * @param annotationSet the calculated annotationSet to be returned
	 * @param File the file containing the annotation to parse
	 */
	public abstract void parseAnnotation(AnnotationSet annotationSet, File file) throws Exception;
	
	/**
	 * Parses the genome.
	 * 
	 * @param baseLocation the base location
	 * @throws Exception 
	 */
	public abstract void parseGenome (File baseLocation) throws Exception;
	
	
	/**
	 * Name.
	 * 
	 * @return the string
	 */
	abstract public String name ();
	
	
	public AnnotationParser() {	}
			
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
	
	
	/*
	 * These are the methods we use to communicate with out listeners.
	 * Some of these can be accessed by the implementing class directly
	 * but the big ones need to go back through this class.
	 */
	
	/**
	 * Progress warning received.
	 * 
	 * @param e the e
	 */
	@Override
	public void progressWarningReceived (Exception e) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressWarningReceived(e);
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
	@Override
	public void progressExceptionReceived (Exception e) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressExceptionReceived(e);
		}
	}
	
	
	/**
	 * Progress updated.
	 * 
	 * @param message the message
	 * @param current the current
	 * @param max the max
	 */
	@Override
	public void progressUpdated (String message, int current, int max) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressUpdated(message, current, max);
		}
	}
	
	/**
	 * Progress completed.
	 * 
	 * @param message the message
	 * @param return object
	 */
	@Override
	public void progressComplete (String message, Object result) {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressComplete(message, result);
		}
	}
	

	/**
	 * Progress cancelled.
	 */
	@Override
	public void progressCancelled () {
		Enumeration<ProgressListener>en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().progressCancelled();
		}
	}
	
	
}
