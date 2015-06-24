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
package uk.ac.babraham.BamQC.Analysis;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import uk.ac.babraham.BamQC.BamQCConfig;
import uk.ac.babraham.BamQC.Modules.QCModule;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class AnalysisQueue implements Runnable, AnalysisListener{

	private static AnalysisQueue instance = new AnalysisQueue();
	
	private LinkedBlockingDeque<AnalysisRunner>queue = new LinkedBlockingDeque<AnalysisRunner>();
	
	private AtomicInteger availableSlots = new AtomicInteger(1);
	private AtomicInteger usedSlots = new AtomicInteger(0);
	
	public static AnalysisQueue getInstance () {
		return instance;
	}
	
	private AnalysisQueue () {
		
		if (BamQCConfig.getInstance().threads != null) {
			availableSlots.set(BamQCConfig.getInstance().threads);			
		}
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public void addToQueue (AnalysisRunner runner) {
		queue.add(runner);
	}

	@Override
	public void run() {

		while (true) {
//			System.err.println("Status available="+availableSlots+" used="+usedSlots+" queue="+queue.size());
			if (availableSlots.intValue() > usedSlots.intValue() && queue.size() > 0) {
				usedSlots.incrementAndGet();
				AnalysisRunner currentRun = queue.getFirst();
				queue.removeFirst();
				currentRun.addAnalysisListener(this);
				Thread t = new Thread(currentRun);
				t.start();
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public void analysisComplete(SequenceFile file, QCModule[] results) {
		usedSlots.decrementAndGet();
	}

	@Override
	public void analysisUpdated(SequenceFile file, int sequencesProcessed, int percentComplete) {}

	@Override
	public void analysisExceptionReceived(SequenceFile file, Exception e) {
		usedSlots.decrementAndGet();
	}

	@Override
	public void analysisStarted(SequenceFile file) {}
	
	
}
