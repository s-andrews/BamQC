/**
 * Copyright Copyright 2014 Simon Andrews
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
 * - Piero Dalle Pezze: added BasicStatistics, ChromosomeDensity, FeatureCoverage, 
 * SoftVariantDistribution, VariantCallDetection
 * - Bart Ailey: Class creation.
 */
package test.java.uk.ac.babraham.BamQC.Modules;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runner.JUnitCore;
import org.junit.internal.TextListener;

@RunWith(Suite.class)
@SuiteClasses({
	BasicStatisticsTest.class,
	ChromosomeDensityTest.class,
	FeatureCoverageTest.class,
	GenomeCoverageTest.class,
	InsertLengthDistributionTest.class,
	MappingQualityDistributionTest.class,
	NormalDistributionModelerTest.class,
	RpkmReferenceTest.class,
	SequenceQualityDistributionTest.class,	
	SoftClipDistributionTest.class,
	VariantCallDetectionTest.class
	})


public class AllTests { 
    /* Main method used to run the tests programmatically */
    public static void main(String args[]) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        junit.run(AllTests.class);
    }
}
