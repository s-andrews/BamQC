/**
 * Copyright Copyright 2014 Bart Ailey Eagle Genomics Ltd
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
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.BamQC.Modules;

import java.util.ArrayList;
import java.util.List;

import uk.ac.babraham.BamQC.Statistics.NormalDistribution;

public class NormalDistributionModeler {

	private List<Double> distribution = new ArrayList<Double>();
	//private List<Double> theoreticalDistribution = new ArrayList<Double>();
	private List<Integer> xCategories = new ArrayList<Integer>();
	private double max = 0;
	private double deviationPercent;

	public NormalDistributionModeler() { }

	public synchronized double calculateDistribution() {
		max = 0;
		xCategories = new ArrayList<Integer>();
		// We use the mode to calculate the theoretical distribution
		// so that we cope better with skewed distributions.
		int firstMode = 0;
		double modeCount = 0;
		double totalCount = 0;
		int index = 0;
		
		for (double vaule : distribution) {
			xCategories.add(index);
			
			totalCount += vaule;

			if (vaule > modeCount) {
				modeCount = vaule;
				firstMode = index;
			}
			if (vaule > max) max = vaule;
			index++;
		}
		// The mode might not be a very good measure of the centre
		// of the distribution either due to duplicated vales or
		// several very similar values next to each other. We therefore
		// average over adjacent points which stay above 95% of the modal
		// value

		double mode = 0;
		int modeDuplicates = 0;

		boolean fellOffTop = true;

		for (int i = firstMode; i < distribution.size(); i++) {
			if (distribution.get(i) > distribution.get(firstMode) - (distribution.get(firstMode) / 10)) {
				mode += i;
				modeDuplicates++;
			}
			else {
				fellOffTop = false;
				break;
			}
		}
		boolean fellOffBottom = true;

		for (int i = firstMode - 1; i >= 0; i--) {
			if (distribution.get(i) > distribution.get(firstMode) - (distribution.get(firstMode) / 10)) {
				mode += i;
				modeDuplicates++;
			}
			else {
				fellOffBottom = false;
				break;
			}
		}

		if (fellOffBottom || fellOffTop) {
			// If the distribution is so skewed that 95% of the mode
			// is off the 0-100% scale then we keep the mode as the
			// centre of the model
			mode = firstMode;
		}
		else {
			mode /= modeDuplicates;
		}
		// We can now work out a theoretical distribution
		double stdev = 0;

		for (int i = 0; i < distribution.size(); i++) {
			stdev += Math.pow((i - mode), 2) * distribution.get(i);
		}

		stdev /= totalCount - 1;
		stdev = Math.sqrt(stdev);

		NormalDistribution nd = new NormalDistribution(mode, stdev);
		
		deviationPercent = 0;
		
		for (int i = 0; i < distribution.size(); i++) {
			double probability = nd.getZScoreForValue(i);
			double theoretical = probability * totalCount;
			
			//theoreticalDistribution.add(theoretical);
			
			if (theoretical > max) {
				max = theoretical;
			}
			deviationPercent += Math.abs(theoretical - distribution.get(i));
		}
		deviationPercent /= totalCount;
		deviationPercent *= 100;
		
		return deviationPercent;
	}

	public List<Double> getDistribution() {
		return distribution;
	}

	public void setDistribution(List<Double> distribution) {
		this.distribution = distribution;
	}

	public double getDeviationPercent() {
		return deviationPercent;
	}
	
	
	
}
