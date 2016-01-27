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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Added genome annotation, Statusbar, improved menu, overall class improvement.
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException; 

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
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
import uk.ac.babraham.BamQC.Displays.StatusPanel;


/**
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 *
 */
public class BamQCApplication extends JFrame implements ProgressListener {
	
	private static Logger log = Logger.getLogger(BamQCApplication.class);	
	
	private static BamQCApplication application;
	
	private static final long serialVersionUID = -1761781589885333860L;

	public static final String VERSION = "0.1.0_devel";
	
	private BamQCMenuBar menu;
	
	/** This is the small strip at the bottom of the main display */
	private StatusPanel statusPanel;
	
	private JTabbedPane fileTabs;
	private WelcomePanel welcomePanel;
	private File lastUsedDir = null;
	
	private String title = "BamQC";
		
	/** Flag to check if anything substantial has changed since the file was last loaded/saved. **/
	private boolean changesWereMade = false;
	
	
	public BamQCApplication () {
			setTitle(title);
			setIconImage(new ImageIcon(ClassLoader.getSystemResource("uk/ac/babraham/BamQC/Resources/bamqc_icon.png")).getImage());
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//		setSize(1280, 720);
			setSize(800,600);
	//		setMinimumSize(new Dimension(660, 440));
			setLocationRelativeTo(null);

			menu = new BamQCMenuBar(this);
			setJMenuBar(menu);
			
			
			fileTabs = new JTabbedPane(JTabbedPane.TOP);
			
			getContentPane().setLayout(new BorderLayout());
			
			welcomePanel = new WelcomePanel();
			getContentPane().add(welcomePanel,BorderLayout.CENTER);

			statusPanel = new StatusPanel();
			getContentPane().add(statusPanel,BorderLayout.SOUTH);
			
		}

	public int close () {
		if (fileTabs.getSelectedIndex() >=0) {
			fileTabs.remove(fileTabs.getSelectedIndex());
		}
		if (fileTabs.getTabCount() == 0) {
			closeAll();
		}
		return fileTabs.getTabCount();
	}
	
	public void closeAll () {
		fileTabs.removeAll();
		getContentPane().remove(fileTabs);
		getContentPane().add(welcomePanel,BorderLayout.CENTER);
		validate();
		repaint();
	}
	
	public boolean openFile () {
		statusPanel.progressUpdated(" ", 0, 100);
		
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
			getContentPane().remove(welcomePanel);
			getContentPane().add(fileTabs,BorderLayout.CENTER);
			validate();
			repaint();
		}
		
		File [] files = chooser.getSelectedFiles();		
			
		for (int i=0;i<files.length;i++) {
			lastUsedDir = files[i].getParentFile();
			SequenceFile sequenceFile;
			
			
			try {
				sequenceFile = SequenceFactory.getSequenceFile(files[i]);

				AnalysisRunner runner = new AnalysisRunner(sequenceFile);

				ResultsPanel rp = new ResultsPanel(sequenceFile);
				runner.addProgressListener(rp);
				runner.addAnalysisListener(rp);
				fileTabs.addTab(sequenceFile.name(), rp);
				
				QCModule [] moduleList = ModuleFactory.getStandardModuleList();
		
				runner.startAnalysis(moduleList);
			}
			catch (SequenceFormatException e) {
				JPanel errorPanel = new JPanel();
				errorPanel.setLayout(new BorderLayout());
				errorPanel.add(new JLabel("File format error: "+e.getLocalizedMessage(), JLabel.CENTER),BorderLayout.CENTER);
				fileTabs.addTab(files[i].getName(), errorPanel);
				log.error(e, e);
				continue;
			}
			catch (IOException e) {
				log.error("File "  + files[i].getAbsolutePath() + " broken", e);
				JOptionPane.showMessageDialog(this, "Couldn't read file: "+e.getLocalizedMessage(), "Error reading file", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			catch (Exception e) {
				log.error("Failed to process the file " + files[i].getAbsolutePath(), e);
				JOptionPane.showMessageDialog(this, "Failed to process the file: "+e.getLocalizedMessage(), "Error processing file", JOptionPane.ERROR_MESSAGE);
				continue;
			}
		}
		return true;
	}
	
	
	/**
	 * Launches the genome selector to begin a new project.
	 */
	public boolean openGFFFromNetwork () {
		
		new GenomeSelector(this);
		if(BamQCConfig.getInstance().genome == null) {
			return false;
		}
		// for consistency, let's remove the file annotation if this was set.
		unsetFileAnnotation();
		//setTitle(title + " ~ " + BamQCConfig.getInstance().genome.getAbsolutePath());
		statusPanel.progressUpdated(" ", 0, 100);
		statusPanel.setText("Genome annotation : " + BamQCConfig.getInstance().genome.getAbsolutePath());
		return true;
	}
	
	public boolean openGFF () {
		
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
		// for consistency, let's remove the genome annotation if this was set.
		unsetGenomeAnnotation();
		//setTitle(title + " ~ " + BamQCConfig.getInstance().gff_file.getAbsolutePath());
		statusPanel.progressUpdated(" ", 0, 100);
		statusPanel.setText("Annotation file : " + BamQCConfig.getInstance().gff_file.getAbsolutePath());
		return true;
	}

	/**
	 * Unset the file annotation.
	 */
	public void unsetFileAnnotation () {
		BamQCConfig.getInstance().gff_file = null;
	}
	
	/**
	 * Unset the genome annotation.
	 */
	public void unsetGenomeAnnotation () {
		BamQCConfig.getInstance().genome = null;
		BamQCConfig.getInstance().species = null;
		BamQCConfig.getInstance().assembly = null;
	}
	
	/**
	 * Unset the annotation.
	 */
	public void unsetAnnotation () {
		unsetGenomeAnnotation();
		unsetFileAnnotation();
		//setTitle(title);
		statusPanel.setText(" ");
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
			JOptionPane.showMessageDialog(this, "Failed to create archive: "+e, "Error", JOptionPane.ERROR_MESSAGE);
			log.error("Failed to create archive: " + e, e);
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
        if(GraphicsEnvironment.isHeadless()) {
        	ProgressTextDialog ptd = new ProgressTextDialog("Downloading genome...");
        	d.addProgressListener(ptd);
        }
        
        // TODO 
        //update the status panel with the progress listener. Status bar must implement Progress listener. Copy this from 
        // Result panel
        d.addProgressListener(statusPanel);

        
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

		if (command.equals("Genome downloaded!")) {
			// use the menu controls for consistency instead of calling the method directly.
			// in this way the menu item 'unset_annotation' is automatically enabled if a genome 
			// is set at this stage.
			menu.actionPerformed(new ActionEvent(this, 0, "open_gff_from_network"));
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
	
			
			SwingUtilities.invokeLater(new Runnable() {
		        @Override
				public void run() {
		        	  application = new BamQCApplication();
		        	  application.setVisible(true);
		          }
		    });
			
		}
	}	

}
