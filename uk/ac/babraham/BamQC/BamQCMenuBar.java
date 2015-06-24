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
package uk.ac.babraham.BamQC;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import uk.ac.babraham.BamQC.Dialogs.AboutDialog;
import uk.ac.babraham.BamQC.Help.HelpDialog;

public class BamQCMenuBar extends JMenuBar implements ActionListener {

	private static final long serialVersionUID = -1301056504996459340L;
	private BamQCApplication application;
	
	public BamQCMenuBar (BamQCApplication application) {
		this.application = application;
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem fileGFFOpen = new JMenuItem("Set GFF...");
		fileGFFOpen.setMnemonic(KeyEvent.VK_G);
		fileGFFOpen.setAccelerator(KeyStroke.getKeyStroke('G', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileGFFOpen.setActionCommand("open_gff");
		fileGFFOpen.addActionListener(this);
		fileMenu.add(fileGFFOpen);

		
		JMenuItem fileBAMOpen = new JMenuItem("Open BAM...");
		fileBAMOpen.setMnemonic(KeyEvent.VK_O);
		fileBAMOpen.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileBAMOpen.setActionCommand("open");
		fileBAMOpen.addActionListener(this);
		fileMenu.add(fileBAMOpen);
		
		fileMenu.addSeparator();
		
		JMenuItem fileSave = new JMenuItem("Save report...");
		fileSave.setMnemonic(KeyEvent.VK_S);
		fileSave.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileSave.setActionCommand("save");
		fileSave.addActionListener(this);
		fileMenu.add(fileSave);
		
		fileMenu.addSeparator();
		
		JMenuItem fileClose = new JMenuItem("Close");
		fileClose.setMnemonic(KeyEvent.VK_C);
		fileClose.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileClose.setActionCommand("close");
		fileClose.addActionListener(this);
		fileMenu.add(fileClose);
		

		JMenuItem fileCloseAll = new JMenuItem("Close All");
		fileCloseAll.setMnemonic(KeyEvent.VK_A);
		fileCloseAll.setActionCommand("close_all");
		fileCloseAll.addActionListener(this);
		fileMenu.add(fileCloseAll);

		
		fileMenu.addSeparator();
		
		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic(KeyEvent.VK_X);
		fileExit.setActionCommand("exit");
		fileExit.addActionListener(this);
		fileMenu.add(fileExit);
		
		add(fileMenu);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem helpContents = new JMenuItem("Contents...");
		helpContents.setMnemonic(KeyEvent.VK_C);
		helpContents.setActionCommand("help_contents");
		helpContents.addActionListener(this);
		helpMenu.add(helpContents);
		
		helpMenu.addSeparator();
		
		JMenuItem helpAbout = new JMenuItem("About BamQC");
		helpAbout.setMnemonic(KeyEvent.VK_A);
		helpAbout.setActionCommand("about");
		helpAbout.addActionListener(this);
		
		helpMenu.add(helpAbout);
		
		add(helpMenu);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if (command.equals("exit")) {
			System.exit(0);
		}
		else if (command.equals("open")) {
			application.openFile();
		}
		else if (command.equals("open_gff")) {
			application.openGFF();
		}

		else if (command.equals("save")) {
			application.saveReport();
		}
		else if (command.equals("close")) {
			application.close();
		}
		else if (command.equals("close_all")) {
			application.closeAll();
		}
		else if (command.equals("help_contents")) {
			try {
				new HelpDialog(application,new File(URLDecoder.decode(ClassLoader.getSystemResource("Help").getFile(),"UTF-8")));
			} 
			catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		else if (command.equals("about")) {
			new AboutDialog(application);
		}
		else {
			JOptionPane.showMessageDialog(application, "Unknown menu command "+command, "Unknown command", JOptionPane.ERROR_MESSAGE);
		}
	}
	
}
