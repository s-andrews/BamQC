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
package uk.ac.babraham.BamQC.Menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import uk.ac.babraham.BamQC.BamQCApplication;
import uk.ac.babraham.BamQC.BamQCException;
import uk.ac.babraham.BamQC.Dialogs.AboutDialog;
import uk.ac.babraham.BamQC.Dialogs.EditPreferencesDialog;
import uk.ac.babraham.BamQC.Dialogs.LicenseDialog;
import uk.ac.babraham.BamQC.Help.HelpDialog;

public class BamQCMenuBar extends JMenuBar implements ActionListener {

	private static final long serialVersionUID = -1301056504996459340L;
	/** The main application */
	private BamQCApplication application;
	

	private JMenu fileMenu;
	private JMenuItem fileGFFOpen;
	private JMenuItem fileGFFOpenNetwork;
	private JMenuItem fileUnsetAnnotation;	
	private JMenuItem fileBAMOpen;
	private JMenuItem fileSave;
	private JMenuItem fileClose;
	private JMenuItem fileCloseAll;
	private JMenuItem fileExit;
	private JMenu editMenu;
	private JMenuItem editPreferences;
	
	private JMenu helpMenu;
	private JMenuItem helpContents;
	private JMenuItem helpLicense;
	private JMenuItem helpAbout;
	
	
	
	
	public BamQCMenuBar (BamQCApplication application) {
		this.application = application;
		
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		fileGFFOpenNetwork = new JMenuItem("Set Annotation from Network");
		fileGFFOpenNetwork.setMnemonic(KeyEvent.VK_N);
		fileGFFOpenNetwork.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileGFFOpenNetwork.setActionCommand("open_gff_from_network");
		fileGFFOpenNetwork.addActionListener(this);
		fileMenu.add(fileGFFOpenNetwork);
		
		fileGFFOpen = new JMenuItem("Set GFF/GTF Annotation File");
		fileGFFOpen.setMnemonic(KeyEvent.VK_A);
		fileGFFOpen.setAccelerator(KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileGFFOpen.setActionCommand("open_gff");
		fileGFFOpen.addActionListener(this);
		fileMenu.add(fileGFFOpen);
		
		fileUnsetAnnotation = new JMenuItem("Unset Annotation");
		fileUnsetAnnotation.setMnemonic(KeyEvent.VK_U);
		fileUnsetAnnotation.setAccelerator(KeyStroke.getKeyStroke('U', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileUnsetAnnotation.setActionCommand("unset_annotation");
		fileUnsetAnnotation.addActionListener(this);
		fileUnsetAnnotation.setEnabled(false);
		fileMenu.add(fileUnsetAnnotation);

		fileMenu.addSeparator();
		
		fileBAMOpen = new JMenuItem("Open SAM/BAM File...");
		fileBAMOpen.setMnemonic(KeyEvent.VK_O);
		fileBAMOpen.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileBAMOpen.setActionCommand("open");
		fileBAMOpen.addActionListener(this);
		fileMenu.add(fileBAMOpen);
		
		fileSave = new JMenuItem("Save Report...");
		fileSave.setMnemonic(KeyEvent.VK_R);
		fileSave.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileSave.setActionCommand("save");
		fileSave.addActionListener(this);
		fileSave.setEnabled(false);
		fileMenu.add(fileSave);
		
		fileMenu.addSeparator();
		
		fileClose = new JMenuItem("Close");
		fileClose.setMnemonic(KeyEvent.VK_C);
		fileClose.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileClose.setActionCommand("close");
		fileClose.addActionListener(this);
		fileClose.setEnabled(false);
		fileMenu.add(fileClose);
		
		fileCloseAll = new JMenuItem("Close All");
		fileCloseAll.setMnemonic(KeyEvent.VK_A);
		fileCloseAll.setAccelerator(KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileCloseAll.setActionCommand("close_all");
		fileCloseAll.addActionListener(this);
		fileCloseAll.setEnabled(false);
		fileMenu.add(fileCloseAll);
		
		fileMenu.addSeparator();
		
		fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic(KeyEvent.VK_Q);
		fileExit.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileExit.setActionCommand("exit");
		fileExit.addActionListener(this);
		fileMenu.add(fileExit);
		
		add(fileMenu);
		
		
		editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		editPreferences = new JMenuItem("Preferences...");
		editPreferences.setActionCommand("edit_preferences");
		editPreferences.setMnemonic(KeyEvent.VK_P);
		editPreferences.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		editPreferences.addActionListener(this);
		editMenu.add(editPreferences);
		
		add(editMenu);		
		
		
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		helpContents = new JMenuItem("Contents...");
		helpContents.setMnemonic(KeyEvent.VK_T);
		helpContents.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		helpContents.setActionCommand("help_contents");
		helpContents.addActionListener(this);
		helpMenu.add(helpContents);
		
		helpMenu.addSeparator();
		
		helpLicense = new JMenuItem("License");
		helpLicense.setActionCommand("help_license");
		helpLicense.setMnemonic(KeyEvent.VK_L);
		helpLicense.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		helpLicense.addActionListener(this);
		helpMenu.add(helpLicense);
		
		helpAbout = new JMenuItem("About BamQC");
		helpAbout.setMnemonic(KeyEvent.VK_A);
		helpAbout.setAccelerator(KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		helpAbout.setActionCommand("about");
		helpAbout.addActionListener(this);
		helpMenu.add(helpAbout);
		
		add(helpMenu);
		
	}

	@Override
	public void actionPerformed(ActionEvent ae) {

		String action = ae.getActionCommand();
		
		if (action.equals("exit")) {
			application.dispose();
		}
		else if (action.equals("open")) {
			if(application.openFile()) {
				fileClose.setEnabled(true);
				fileCloseAll.setEnabled(true);
				fileSave.setEnabled(true);
			}
		}
		else if (action.equals("open_gff")) {
			if(application.openGFF())
				fileUnsetAnnotation.setEnabled(true);
		}
		else if (action.equals("open_gff_from_network")) {
			if(application.openGFFFromNetwork())
				fileUnsetAnnotation.setEnabled(true);
		}
		else if (action.equals("unset_annotation")) {
			application.unsetAnnotation();
			fileUnsetAnnotation.setEnabled(false);
		}
		else if (action.equals("save")) {
			application.saveReport();
		}
		else if (action.equals("close")) {
			if(application.close() == 0) {
				application.closeAll();
				fileSave.setEnabled(false);
				fileClose.setEnabled(false);
				fileCloseAll.setEnabled(false);				
			}
		}
		else if (action.equals("close_all")) {
			application.closeAll();
			fileSave.setEnabled(false);
			fileClose.setEnabled(false);
			fileCloseAll.setEnabled(false);
		}
		else if (action.equals("edit_preferences")) {
			new EditPreferencesDialog();
		}
		else if (action.equals("help_contents")) {
			try {
				new HelpDialog(application,new File(URLDecoder.decode(ClassLoader.getSystemResource("Help").getFile(),"UTF-8")));
			} 
			catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		else if (action.equals("help_license")) {
			try {
				new LicenseDialog(application);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (action.equals("about")) {
			new AboutDialog(application);
		}
		else {
			//JOptionPane.showMessageDialog(application, "Unknown menu command "+action, "Unknown command", JOptionPane.ERROR_MESSAGE);
			new BamQCException("Unknown command '"+action+"' from main menu");
		}
	}
	
}
