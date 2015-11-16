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

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Analysis.AnalysisRunner;
import uk.ac.babraham.BamQC.Analysis.OfflineRunner;
import uk.ac.babraham.BamQC.Dialogs.ProgressTextDialog;
import uk.ac.babraham.BamQC.Dialogs.WelcomePanel;
import uk.ac.babraham.BamQC.Menu.BamQCMenuBar;
import uk.ac.babraham.BamQC.Modules.ModuleFactory;
import uk.ac.babraham.BamQC.Modules.QCModule;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Results.ResultsPanel;
import uk.ac.babraham.BamQC.Sequence.SequenceFactory;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Sequence.SequenceFormatException;
import uk.ac.babraham.BamQC.Utilities.FileFilters.BAMFileFilter;
import uk.ac.babraham.BamQC.Utilities.FileFilters.GFFFileFilter;
import uk.ac.babraham.BamQC.Dialogs.GenomeSelector;
import uk.ac.babraham.BamQC.Network.GenomeDownloader;
import uk.ac.babraham.BamQC.DataTypes.ProgressListener;



public class BamQCApplication extends JFrame implements ProgressListener {
	
	private static Logger log = Logger.getLogger(BamQCApplication.class);	
	
	private static BamQCApplication application;
	
	private static final long serialVersionUID = -1761781589885333860L;

	public static final String VERSION = "0.1.0_devel";
	
	private BamQCMenuBar menu;
	
	private JTabbedPane fileTabs;
	private WelcomePanel welcomePanel;
	private File lastUsedDir = null;
		
	/** Flag to check if anything substantial has changed since the file was last loaded/saved. **/
	private boolean changesWereMade = false;
	
	
	public BamQCApplication () {
			setTitle("BamQC");
			setIconImage(new ImageIcon(ClassLoader.getSystemResource("uk/ac/babraham/BamQC/Resources/bamqc_icon.png")).getImage());
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//		setSize(1280, 720);
			setSize(800,600);
			setLocationRelativeTo(null);
			
			welcomePanel = new WelcomePanel();
			
			fileTabs = new JTabbedPane(JTabbedPane.TOP);
			setContentPane(welcomePanel);
			
			menu = new BamQCMenuBar(this);
			setJMenuBar(menu);
			
		}

	public int close () {
		if (fileTabs.getSelectedIndex() >=0) {
			fileTabs.remove(fileTabs.getSelectedIndex());
		}
		if (fileTabs.getTabCount() == 0) {
			setContentPane(welcomePanel);
			validate();
			repaint();
		}
		return fileTabs.getTabCount();
	}
	
	public void closeAll () {
		unsetAnnotation();
		fileTabs.removeAll();
		setContentPane(welcomePanel);
		validate();
		repaint();
	}
	
	public boolean openFile () {
		JFileChooser chooser;
		
		if (lastUsedDir == null) {
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(lastUsedDir);
		}
		// Open the view by details
//		Action details = chooser.getActionMap().get("viewTypeDetails");
//		details.actionPerformed(null);
		chooser.setMultiSelectionEnabled(true);
		BAMFileFilter bff = new BAMFileFilter();
		// remove default "All Files" filter
		chooser.removeChoosableFileFilter(chooser.getFileFilter());
		chooser.addChoosableFileFilter(bff);
		chooser.setFileFilter(bff);
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.CANCEL_OPTION) return false;
	
		// See if they forced a file format
		FileFilter chosenFilter = chooser.getFileFilter();
		if (chosenFilter instanceof BAMFileFilter) {
			System.setProperty("bamqc.sequence_format", "bam");
		}
		
		// If we're still showing the welcome panel switch this out for
		// the file tabs panel
		if (fileTabs.getTabCount() == 0) {
			setContentPane(fileTabs);
			validate();
			repaint();
		}
		
		File [] files = chooser.getSelectedFiles();		
			
		for (int i=0;i<files.length;i++) {
			lastUsedDir = files[i].getParentFile();
			SequenceFile sequenceFile;
			
			
			try {
				sequenceFile = SequenceFactory.getSequenceFile(files[i]);
			}
			catch (SequenceFormatException e) {
				JPanel errorPanel = new JPanel();
				errorPanel.setLayout(new BorderLayout());
				errorPanel.add(new JLabel("File format error: "+e.getLocalizedMessage(), JLabel.CENTER),BorderLayout.CENTER);
				fileTabs.addTab(files[i].getName(), errorPanel);
				e.printStackTrace();
				continue;
			}
			catch (IOException e) {
				System.err.println("File broken");
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Couldn't read file:"+e.getLocalizedMessage(), "Error reading file", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			AnalysisRunner runner = new AnalysisRunner(sequenceFile);

			ResultsPanel rp = new ResultsPanel(sequenceFile);
			runner.addAnalysisListener(rp);
			fileTabs.addTab(sequenceFile.name(), rp);
			

			QCModule [] module_list = ModuleFactory.getStandardModuleList();
	
			runner.startAnalysis(module_list);
		}
		return true;
	}
	
	
	/**
	 * Launches the genome selector to begin a new project.
	 */
	public boolean openGFFFromNetwork () {
		unsetAnnotation();
		new GenomeSelector(this);
		if(BamQCConfig.getInstance().genome == null) 
			return false;
		return true;
	}
	
	public boolean openGFF () {
		unsetAnnotation();
		
		JFileChooser chooser;
		
		if (lastUsedDir == null) {
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(lastUsedDir);
		}
//		Action details = chooser.getActionMap().get("viewTypeDetails");
//		details.actionPerformed(null);
		chooser.setMultiSelectionEnabled(false);
		GFFFileFilter gff = new GFFFileFilter();
		// remove default "All Files" filter
		chooser.removeChoosableFileFilter(chooser.getFileFilter());
		chooser.addChoosableFileFilter(gff);
		chooser.setFileFilter(gff);
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.CANCEL_OPTION) return false;
	
		
		File gff_file = chooser.getSelectedFile();

		if (!(gff_file.exists() && gff_file.canRead())) {
			JOptionPane.showMessageDialog(this, "GFF file "+gff_file+" doesn't exist or can't be read", "Invalid GFF file", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		BamQCConfig.getInstance().gff_file = gff_file;
		return true;
	}

	/**
	 * Unset the current annotation.
	 */
	public void unsetAnnotation () {
		BamQCConfig.getInstance().genome = null;
		BamQCConfig.getInstance().species = null;
		BamQCConfig.getInstance().assembly = null;
		BamQCConfig.getInstance().gff_file = null;
	}

	public void saveReport () {
		JFileChooser chooser;
		
		if (lastUsedDir == null) {
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(lastUsedDir);
		}
		
		if (fileTabs.getSelectedComponent() == null) {
			JOptionPane.showMessageDialog(this, "No SAM/BAM files are open yet", "Can't save report", JOptionPane.ERROR_MESSAGE);
			return;
		}
		chooser.setSelectedFile(new File(((ResultsPanel)fileTabs.getSelectedComponent()).sequenceFile().getFile().getName().replaceAll(".gz$","").replaceAll(".bz2$","").replaceAll(".txt$","").replaceAll(".fastq$", "").replaceAll(".fq$", "").replaceAll(".sam$", "").replaceAll(".bam$", "")+"_bamqc.html"));
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileFilter() {
		
			@Override
			public String getDescription() {
				return "HTML files";
			}
		
			@Override
			public boolean accept(File f) {
				if (f.isDirectory() || f.getName().toLowerCase().endsWith(".html")) {
					return true;
				}
				return false;
			}
		
		});
	
		File reportFile;
		while (true) {
			int result = chooser.showSaveDialog(this);
			if (result == JFileChooser.CANCEL_OPTION) return;
			
			reportFile = chooser.getSelectedFile();
			if (! reportFile.getName().toLowerCase().endsWith(".html")) {
				reportFile = new File(reportFile.getAbsoluteFile()+".html");
			}
			
			// Check if we're overwriting something
			if (reportFile.exists()) {
				int reply = JOptionPane.showConfirmDialog(this, reportFile.getName()+" already exists.  Overwrite?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.NO_OPTION) {
					continue;
				}
				break;
			}
			break;
		}
		
		
		ResultsPanel selectedPanel = (ResultsPanel)fileTabs.getSelectedComponent();
		
		try {
			new HTMLReportArchive(selectedPanel.sequenceFile(), selectedPanel.modules(), reportFile);
		} 
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Failed to create archive: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			log.warn("Failed to create archive: " + e.getMessage());
		}
	}
	
	
	/**
	 * Unsets the changesWereMade flag so that the user will not be prompted
	 * to save even if the data has changed.
	 */
	public void resetChangesWereMade () {
		changesWereMade = false;
		if (getTitle().endsWith("*")) {
			setTitle(getTitle().replaceAll("\\*$", ""));
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose () {
		// We're overriding this so we can catch the application being
		// closed by the X in the corner.  We need to offer the opportunity
		// to save if they've changed anything.

		// We'll already have been made invisible by this stage, so make
		// us visible again in case we're hanging around.
		setVisible(true);
		
		// Check to see if the user has made any changes they might
		// want to save
		if (changesWereMade) {
			int answer = JOptionPane.showOptionDialog(this,"You have made changes which were not saved.  Do you want to save before exiting?","Save before exit?",0,JOptionPane.QUESTION_MESSAGE,null,new String [] {"Save and Exit","Exit without Saving","Cancel"},"Save");

			switch (answer){
			case 0: 
				return;
			case 1:
				break;
			case 2:
				return;
			}
		}

		setVisible(false);
		super.dispose();
		System.exit(0);
		
	}
		

	
	
	
	/**
	 * This method is usually called from data gathered by the genome selector
	 * which will provide the required values for the assembly name.  This does
	 * not actually load the specified genome, but just downloads it from the
	 * online genome repository.
	 * 
	 * @param species Species name
	 * @param assembly Assembly name
	 * @param size The size of the compressed genome file in bytes
	 */
	public void downloadGenome (String species, String assembly, int size) {
		GenomeDownloader d = new GenomeDownloader();
		d.addProgressListener(this);

//		// using a text ProgressTextDialog
		ProgressTextDialog ptd = new ProgressTextDialog("Downloading genome...");
		d.addProgressListener(ptd);

		d.downloadGenome(species,assembly,size,true);
	}
	
	
	

	/**
	 * Select a genome.
	 * 
	 * @param baseLocation The folder containing the requested genome.
	 */
	public void selectGenome (File baseLocation) {
		BamQCConfig.getInstance().genome = baseLocation;
	}
	


	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressComplete(java.lang.String, java.lang.Object)
	 */
	@Override
	public void progressComplete(String command, Object result) {

		// Many different operations can call this method and our actions
		// depend on who called us and what they sent.
		
		if (command == null) return;

		if (command.equals("genome_downloaded")) {
			// No result is returned
			openGFFFromNetwork();
		}
		
		else {
			throw new IllegalArgumentException("Don't know how to handle progress command '"+command+"'");
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressExceptionReceived(java.lang.Exception)
	 */
	@Override
	public void progressExceptionReceived(Exception e) {
		// Should be handled by specialised widgets
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressUpdated(java.lang.String, int, int)
	 */
	@Override
	public void progressUpdated(String message, int current, int max) {
		// Should be handled by specialised widgets		
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressCancelled()
	 */
	@Override
	public void progressCancelled () {
		// Should be handled by specialised widgets
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.ProgressListener#progressWarningReceived(java.lang.Exception)
	 */
	@Override
	public void progressWarningReceived(Exception e) {
		// Should be handled by specialised widgets
	}
	
	
	
	
	
	/**
	 * Provides a static way to access the main instance of the BamQC
	 * Application so we don't need to keep passing references around
	 * through obscure paths.
	 * 
	 * @return The currently running application instance.
	 */
	
	public static BamQCApplication getInstance () {
		return application;
	}
	
	

	public static void main(String[] args) {
		
		// See if we just have to print out the version
		if (System.getProperty("bamqc.show_version") != null && System.getProperty("bamqc.show_version").equals("true")) {
			System.out.println("BamQC v"+VERSION);
			System.exit(0);
		}
		
		if (args.length > 0) {
			// Set headless to true so we don't get problems
			// with people working without an X display.
			System.setProperty("java.awt.headless", "true");
			
			// We used to default to unzipping the zip file in 
			// non-interactive runs.  As we now save an HTML
			// report at the top level we no longer do this
			// so unzip is false unless explicitly set to be true.
						
			if (BamQCConfig.getInstance().do_unzip == false) {
				BamQCConfig.getInstance().do_unzip = false;
			}
			
//			// uncomment this if you need to run a profiler
//			try { Thread.sleep(10000); } 
//			catch (InterruptedException e) {}
			
			new OfflineRunner(args);
		
		} else {
			
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {}
			
	
			// The interactive default is to not uncompress the
			// reports after they have been generated
			if (BamQCConfig.getInstance().do_unzip == false) {
				BamQCConfig.getInstance().do_unzip = false;
			}
	
			application = new BamQCApplication();
	
			application.setVisible(true);
		}
	}	

}
