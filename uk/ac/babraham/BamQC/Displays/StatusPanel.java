/**
 * Copyright Copyright 2010-15 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.BamQC.Displays;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import uk.ac.babraham.BamQC.DataTypes.ProgressListener;


/**
 * The Class StatusPanel shows the interactive bar at the bottom
 * of the main application screen.
 */
public class StatusPanel extends JPanel implements ProgressListener {

	private static final long serialVersionUID = -7979299860162515406L;
	/** The textLabel. */
	private JLabel textLabel = new JLabel(" ",JLabel.LEFT);
	
	private JLabel progressLabel = new JLabel(" ", JLabel.RIGHT);
	
	/**
	 * Instantiates a new status panel.
	 */
	public StatusPanel() {
		setLayout(new BorderLayout());
		add(textLabel,BorderLayout.WEST);
		add(progressLabel,BorderLayout.EAST);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}
	
	/**
	 * Sets the file name.
	 * 
	 * @param text the new file name
	 */
	public void setText(String text) {
		textLabel.setText(text);
	}
	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressUpdated(java.lang.String, int, int)
	 */
	@Override
	public void progressUpdated(String message, int currentPos, int totalPos) {
		progressLabel.setText(message);
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressExceptionReceived(java.lang.Exception)
	 */
	@Override
	public void progressExceptionReceived(Exception e) {
		progressLabel.setText("Failed to process file: "+e.getLocalizedMessage());
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
		progressLabel.setText(command);
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressWarningReceived(java.lang.Exception)
	 */
	@Override
	public void progressWarningReceived(Exception e) { }	
	
}
