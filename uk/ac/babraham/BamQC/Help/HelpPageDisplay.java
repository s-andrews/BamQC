/**
 * Copyright 2009-14 Simon Andrews
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
package uk.ac.babraham.BamQC.Help;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;


/**
 * The Class HelpPageDisplay.
 * @author Simon Andrews
 */
public class HelpPageDisplay extends JPanel implements HyperlinkListener {

	private static Logger log = Logger.getLogger(HelpPageDisplay.class);
	
	private static final long serialVersionUID = 6274584311171948484L;
	/** The html pane. */
	public JEditorPane htmlPane;

	/**
	 * Instantiates a new help page display.
	 * 
	 * @param page the page
	 */
	public HelpPageDisplay (HelpPage page) {

		if (page != null) {
			try {
				htmlPane = new HelpEditor(page.getFile().toURI().toURL());
			}
			catch (MalformedURLException e) {
				log.error(e, e);
			}
			catch (IOException e) {
				log.error(e, e);
			}
			
			htmlPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
			htmlPane.setEditable(false);
			htmlPane.addHyperlinkListener(this);
			
			setLayout(new BorderLayout());
			add(new JScrollPane(htmlPane),BorderLayout.CENTER);
		}
	}

	    
	/**
	 * The Class HelpEditor.
	 */
	private class HelpEditor extends JEditorPane {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5398497756074674632L;

		/**
		 * Instantiates a new help editor.
		 * 
		 * @param u the u
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public HelpEditor (URL u) throws IOException {
			super(u);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			if (g instanceof Graphics2D) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			}	
			super.paint(g);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent h) {
		if (h.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				htmlPane.setPage(h.getURL());
			} catch (IOException e) {
				log.error(e, e);
			}
		}
	}
	
}
