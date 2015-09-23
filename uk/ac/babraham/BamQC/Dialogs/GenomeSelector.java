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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import uk.ac.babraham.BamQC.BamQCApplication;
import uk.ac.babraham.BamQC.Displays.ManualGenomeBuilder.ManualGenomeBuilderDialog;
import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;

/**
 * The Class GenomeSelector shows a tree of the currently available genomes
 */
public class GenomeSelector extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1228085093041611779L;

	/** The application. */
	private BamQCApplication application;
	
	/** The tree. */
	private JTree tree;
	
	/** The ok button. */
	private JButton okButton;
	
	/**
	 * Instantiates a new genome selector.
	 * 
	 * @param application the application
	 */
	public GenomeSelector (BamQCApplication application) {
		super(application,"Select Genome...");
		this.application = application;
		setSize(600,350);
		setLocationRelativeTo(application);
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout());

		// Create the tree of available genomes
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Genomes");
		
		File[] genomes;
		try {
			genomes = BamQCPreferences.getInstance().getGenomeBase().listFiles();
			if (genomes == null) {
				throw new FileNotFoundException();
			}
		} 
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(application, "Couldn't find the folder containing your genomes.  Please check your file preferences", "Error getting genomes", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		for (int i=0;i<genomes.length;i++) {
			if (genomes[i].isDirectory()) {
				DefaultMutableTreeNode genomeNode = new DefaultMutableTreeNode(genomes[i].getName());
				File [] assemblies = genomes[i].listFiles();
				
				// Skip folders which don't contain any assemblies, or for
				// which assemblies can't be listed
				if (assemblies == null) {
					System.err.println("Skipping genomes folder "+genomes[i].getAbsolutePath()+" since I can't list files");
					continue;
				}
				
				boolean foundAssembly = false;
				for (int j=0;j<assemblies.length;j++) {
					if (assemblies[j].isDirectory()) {
						genomeNode.add(new AssemblyNode(assemblies[j]));
						foundAssembly = true;
					}
				}
				if (foundAssembly) {
					root.add(genomeNode);
				}
				else {
					System.err.println("Skipping genomes folder "+genomes[i].getAbsolutePath()+" which didn't contain any assemblies");
				}
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.addTreeSelectionListener(new TreeListener());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);


		getContentPane().add(new JScrollPane(tree),BorderLayout.CENTER);
		
		
		
		// Create the buttons at the bottom.
		ButtonListener l = new ButtonListener();
		
		JPanel buttonPanel = new JPanel();
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(l);
		buttonPanel.add(cancelButton);

		JButton importButton = new JButton("Import");
		importButton.setActionCommand("import");
		importButton.addActionListener(l);
		buttonPanel.add(importButton);

		JButton customButton = new JButton("Build Custom Genome");
		customButton.setActionCommand("custom");
		customButton.addActionListener(l);
		buttonPanel.add(customButton);

		
		okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.setEnabled(false);
		okButton.addActionListener(l);
		getRootPane().setDefaultButton(okButton);
		buttonPanel.add(okButton);
		
		getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		
		setVisible(true);
	}

	/**
	 * The listener interface for receiving button events.
	 * The class that is interested in processing a button
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addButtonListener<code> method. When
	 * the button event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see ButtonEvent
	 */
	private class ButtonListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ae.getActionCommand().equals("ok")) {
				setVisible(false);			
				application.loadGenome(((AssemblyNode)tree.getSelectionPath().getLastPathComponent()).file());
				dispose();
			}
			else if (ae.getActionCommand().equals("import")) {
				try {
					new GenomeDownloadSelector(application);
				} catch (Exception e) {
					e.printStackTrace();
				}
				setVisible(false);
				dispose();
			}
			else if (ae.getActionCommand().equals("custom")) {
				new ManualGenomeBuilderDialog();
				setVisible(false);
				dispose();
			}
			else if (ae.getActionCommand().equals("cancel")) {
				setVisible(false);
				dispose();
			}
			
		}
	}
	
	/**
	 * The listener interface for receiving tree events.
	 * The class that is interested in processing a tree
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTreeListener<code> method. When
	 * the tree event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see TreeEvent
	 */
	private class TreeListener implements TreeSelectionListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent tse) {
			if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() instanceof AssemblyNode) {
				okButton.setEnabled(true);
			}
			else {
				okButton.setEnabled(false);
			}
		}
		
	}
	
	/**
	 * The Class AssemblyNode.
	 */
	private class AssemblyNode extends DefaultMutableTreeNode {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7834551810794251101L;
		/** The f. */
		private File f;
		
		/**
		 * Instantiates a new assembly node.
		 * 
		 * @param f the f
		 */
		public AssemblyNode (File f) {
			super(f.getName());
			this.f = f;
		}
		
		/**
		 * File.
		 * 
		 * @return the file
		 */
		public File file () {
			return f;
		}
		
	}
	
}
