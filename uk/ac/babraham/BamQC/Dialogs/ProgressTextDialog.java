/**
 * Copyright Copyright 2010-15 Simon Andrews
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
package uk.ac.babraham.BamQC.Dialogs;

import java.util.Vector;


import uk.ac.babraham.BamQC.DataTypes.ProgressListener;



/**
 * The Class ProgressDialog is a generic progress message showing the progress. 
 * We print all the output on the command line rather than a JDialog.
 */
public class ProgressTextDialog implements Runnable, ProgressListener {

	
	/** The warning count. */
	private int warningCount = 0;
	
	private boolean ignoreExceptions = false;
	
	/** The warnings. */
	private Vector<Exception>warnings = new Vector<Exception>();
	
	/** A record of any exception we've received */
	private Exception reportedException = null;

	/**
	 * Instantiates a new progress dialog.
	 */
	public ProgressTextDialog (String title) {
		if(!title.isEmpty())
			System.out.println(title);
	}
		
	public void setIgnoreExceptions (boolean ignore) {
		this.ignoreExceptions = ignore;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressUpdated(java.lang.String, int, int)
	 */
	@Override
	public void progressUpdated(String message, int currentPos, int totalPos) {
		System.out.println(message);
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressExceptionReceived(java.lang.Exception)
	 */
	@Override
	public void progressExceptionReceived(Exception e) {
		if (reportedException != null && reportedException == e) return;
		reportedException = e;
		if (! ignoreExceptions) {
			e.printStackTrace();
		}
	}

	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressCancelled()
	 */
	@Override
	public void progressCancelled() { }

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressComplete(java.lang.String, java.lang.Object)
	 */
	@Override
	public void progressComplete(String command, Object result) {
		System.out.println(command);
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressWarningReceived(java.lang.Exception)
	 */
	@Override
	public void progressWarningReceived(Exception e) {
		warningCount++;
		// We just store this warning so we can display all
		// of them at the end.  We only keep the first 5000
		// so that things don't get too out of hand
		if (warningCount<=5000){
			warnings.add(e);
		}		
	}

}

