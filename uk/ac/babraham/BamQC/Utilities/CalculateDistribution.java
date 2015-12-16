/**
 * Copyright Copyright 2007-13 Simon Andrews
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
package uk.ac.babraham.BamQC.Utilities;


/**
 * This class calculates a distribution of values for compacting multiple bars in a histogram.
 * @author Simon Andrews
 */
public class CalculateDistribution {
	
	double[] graphCounts = null;
	private double[] distributionDouble = null;
	private String[] xCategories = null;
	private double[] rawCounts = null;
	private double aboveMaxThreshold = 0L;
	private double max = 0.0;
	private int BIN_SIZE = 1;

	
	

	public CalculateDistribution(double[] rawCounts, 
			   				     double aboveMaxThreshold, 
								 final int BIN_SIZE) {
		this.rawCounts = rawCounts;
		this.aboveMaxThreshold = aboveMaxThreshold;
		this.BIN_SIZE = BIN_SIZE;
		
		calculate();
	}
		
	
	
	public double[] getGraphCounts() {
		return graphCounts;
	}



	public double[] getDistributionDouble() {
		return distributionDouble;
	}



	public String[] getXCategories() {
		return xCategories;
	}



	public double getMax() {
		return max;
	}



	private void calculate() {
		int maxLen = 0;
		int minLen = -1;
		
		prepareDistribution();	
		
		// Find the min and max lengths		
		for (int i=0;i<distributionDouble.length;i++) {
			if (distributionDouble[i] > 0.0d) {
				if (minLen < 0) {
					minLen = i;
				}
				maxLen = i;
			}
		}
		
		// We put one extra category either side of the actual size
		if (minLen>0) minLen--;
		maxLen++;
		
		int [] startAndInterval = getSizeDistribution(minLen, maxLen);
				
		// Work out how many categories we need
		int categories = 0;
		int currentValue = startAndInterval[0];
		while (currentValue<= maxLen) {
			++categories;
			currentValue+= startAndInterval[1];
		}
		
		graphCounts = new double[categories];
		xCategories = new String[categories];
		
		for (int i=0;i<graphCounts.length;i++) {
			
			int minValue = startAndInterval[0]+(startAndInterval[1]*i);
			int maxValue = (startAndInterval[0]+(startAndInterval[1]*(i+1)))-1;

			if (maxValue > maxLen) {
				maxValue = maxLen;
			}
			
			for (int bp=minValue;bp<=maxValue;bp++) {
				if (bp < distributionDouble.length) {
					graphCounts[i] += distributionDouble[bp];
				}
			}
			
			if (startAndInterval[1] == 1) {
				xCategories[i] = ""+Integer.toString(minValue * 10);
			}
			else {
				xCategories[i] = Integer.toString(minValue * 10)+"-"+Integer.toString(maxValue * 10);
			}
			if (graphCounts[i] > max) max = graphCounts[i];
		}
	}
	
	
	
	private double percent(double value, double total) {
		return (value / total) * 100.0;
	}
	
	
	
	private void prepareDistribution() {
		// +2 = fraction and exceeding max values N
		int binNumber = (rawCounts.length / BIN_SIZE) + 2;
		distributionDouble = new double[binNumber];
		double total = aboveMaxThreshold;
		
		for (double count : rawCounts) {
			total += count;
		}
		distributionDouble[binNumber - 1] = percent(aboveMaxThreshold, total);

		for (int i = 0; i < rawCounts.length; i++) {
			int index = (i / BIN_SIZE);
			distributionDouble[index] += percent(rawCounts[i], total);
		}
	}
	
	
	private int [] getSizeDistribution(int min, int max) {
		int base = 1;
		while (base > (max-min)) {
			base /= 10;
		}
		int interval;
		int starting;
		int [] divisions = new int [] {1,2,5};
		OUTER: while (true) {
			for (int d=0;d<divisions.length;d++) {
				int tester = base * divisions[d];
				if (((max-min) / tester) <= 50) {
					interval = tester;
					break OUTER;
				}
			}
			base *=10;
		}
		// Now we work out the first value to be plotted
		int basicDivision = min/interval;	
		int testStart = basicDivision * interval;	
		starting = testStart;
		return new int[] {starting,interval};
		
	}	
	
}