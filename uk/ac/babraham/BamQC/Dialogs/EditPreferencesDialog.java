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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Code taken from SeqMonk. Simplified for the needs of BamQC.
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.Dialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import uk.ac.babraham.BamQC.BamQCApplication;
import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;

/**
 * A Dialog to allow the viewing and editing of all BamQC preferences.
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 */
public class EditPreferencesDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9120924960274548107L;

	/** The genome base. */
	private JTextField genomeBase;
	
	/** The save location. */
	private JTextField saveLocation;
	
	/** The proxy host. */
	private JTextField proxyHost;
	
	/** The proxy port. */
	private JTextField proxyPort;
	
	/** The download location. */
	private JTextField downloadLocation;
		
	/**
	 * Instantiates a new edits the preferences dialog.
	 * 
	 * @param application the application
	 */
	public EditPreferencesDialog () {
		super(BamQCApplication.getInstance(),"Edit Preferences...");
		setSize(600,280);
		setLocationRelativeTo(BamQCApplication.getInstance());
		setModal(true);
		BamQCPreferences p = BamQCPreferences.getInstance();
		
		JTabbedPane tabs = new JTabbedPane();

		JPanel filePanel = new JPanel();
		filePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		filePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0.1;
		c.weighty=0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		filePanel.add(new JLabel("Genome Base Location"),c);
		c.gridx=1;
		c.weightx=0.5;
		genomeBase = new JTextField();
		try {
			genomeBase.setText(p.getGenomeBase().getAbsolutePath());
		}
		catch (FileNotFoundException e){
			JOptionPane.showMessageDialog(this, "Couldn't find the folder which was supposed to hold the genomes", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		genomeBase.setEditable(false);
		filePanel.add(genomeBase,c);
		c.gridx=2;
		c.weightx=0.1;
		JButton genomeButton = new JButton("Browse");
		genomeButton.setActionCommand("genomeBase");
		genomeButton.addActionListener(this);
		filePanel.add(genomeButton,c);
		
		c.gridx=0;
		c.gridy++;
		c.weightx=0.1;
		filePanel.add(new JLabel("Default Save Location"),c);
		c.gridx=1;
		c.weightx=0.5;
		saveLocation = new JTextField();
		if(p.getSaveLocation() != null) { 
			saveLocation.setText(p.getSaveLocationPreference().getAbsolutePath());
		}
		saveLocation.setEditable(false);
		filePanel.add(saveLocation,c);
		c.gridx=2;
		c.weightx=0.1;
		JButton saveLocationButton = new JButton("Browse");
		saveLocationButton.setActionCommand("saveLocation");
		saveLocationButton.addActionListener(this);
		filePanel.add(saveLocationButton,c);

		tabs.addTab("Files",filePanel);

		
		
		
		JPanel networkPanel = new JPanel();
		networkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		
		networkPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0.1;
		c.weighty=0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		networkPanel.add(new JLabel("Genome Download URL"),c);
		c.gridx=1;
		c.weightx=0.5;
		downloadLocation = new JTextField(p.getGenomeDownloadLocation());
		networkPanel.add(downloadLocation,c);

		c.gridx=0;
		c.gridy++;
		c.weightx=0.1;
		networkPanel.add(new JLabel("HTTP Proxy server"),c);
		c.gridx=1;
		c.weightx=0.5;
		proxyHost = new JTextField(p.proxyHost());
		networkPanel.add(proxyHost,c);
		
		c.gridx=0;
		c.gridy++;
		c.weightx=0.1;
		networkPanel.add(new JLabel("HTTP Proxy port"),c);
		c.gridx=1;
		c.weightx=0.5;
		proxyPort = new JTextField(""+p.proxyPort());
		networkPanel.add(proxyPort,c);
		tabs.addTab("Network",networkPanel);
		
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabs, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		
		JButton okButton = new JButton("Save");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		
		getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		setVisible(true);
	}

	/**
	 * Launches a file browser to select a directory
	 * 
	 * @param f the TextFild from which to take the starting directory
	 * @return the selected directory
	 */
	private void getDir (JTextField f) {
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new File(f.getText()));
	    chooser.setDialogTitle("Select Directory");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	f.setText(chooser.getSelectedFile().getAbsolutePath());
	    }
	}

	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		String c = ae.getActionCommand();
		
		if (c.equals("genomeBase")) {
			getDir(genomeBase);
		}
		else if (c.equals("saveLocation")) {
			getDir(saveLocation);
		}
		else if (c.equals("cancel")) {
			setVisible(false);
			dispose();
		}
		
		else if (c.equals("ok")) {
			File genomeBaseFile = new File(genomeBase.getText());
			if (!genomeBaseFile.exists()) {
				JOptionPane.showMessageDialog(this,"Invalid genome base location","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			File saveLocationFile = new File(saveLocation.getText());
			if (!saveLocationFile.exists()) {
				JOptionPane.showMessageDialog(this,"Invalid save location","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String proxyHostValue = proxyHost.getText();
			int proxyPortValue = 0;
			if (proxyPort.getText().length()>0) {
				try {
					proxyPortValue = Integer.parseInt(proxyPort.getText());
				}
				catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(this,"Proxy port number was not an integer","Error",JOptionPane.ERROR_MESSAGE);
					return;				
				}
			}
			
			if (proxyHostValue.length()>0 && proxyPort.getText().length() == 0){
				JOptionPane.showMessageDialog(this,"You specified a proxy server address, but did not provide the port number (default is usually 80 or 8080)","Error",JOptionPane.ERROR_MESSAGE);
				return;								
			}
			
			
			// OK that's everything which could have gone wrong.  Let's save it
			// to the preferences file
			
			BamQCPreferences p = BamQCPreferences.getInstance();
			
			p.setSaveLocation(saveLocationFile);
			p.setGenomeBase(genomeBaseFile);
			p.setProxy(proxyHostValue,proxyPortValue);
			p.setGenomeDownloadLocation(downloadLocation.getText());
			
			try {
				p.savePreferences();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setVisible(false);
		}
	}
	
}

