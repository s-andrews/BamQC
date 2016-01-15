/**
 * Copyright Copyright 2015 Simon Andrews
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
 * - Piero Dalle Pezze: Added printout.
 * - Bart Ailey: Class creation.
 */
package test.java.uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.NormalDistributionModeler;

/**
 * 
 * @author Bart Ailey
 * @author Piero Dalle Pezze
 *
 */
public class NormalDistributionModelerTest {

	private static Logger log = Logger.getLogger(NormalDistributionModelerTest.class);
	
	private NormalDistributionModeler normalDistributionModeler = null;

	@Before
	public void setUp() throws Exception {
		normalDistributionModeler = new NormalDistributionModeler();
	}

	@After
	public void tearDown() throws Exception {
		normalDistributionModeler = null;
	}

	@Test
	public void testCalculateDistribution() {
		System.out.println("Running test NormalDistributionModelerTest.testCalculateDistribution");
		log.info("Running test NormalDistributionModelerTest.testCalculateDistribution");
		
		List<Double> insertSizes = UtilityTest.readInsertSizesDouble();
		
		for (double insertSize : insertSizes) {
			log.debug("insertSize = " + insertSize);
		}
		normalDistributionModeler.setDistribution(insertSizes);
		normalDistributionModeler.calculateDistribution();
		
		double deviationPercentage = normalDistributionModeler.getDeviationPercent();
		
		assertEquals(16.050, deviationPercentage, 0.001);
	}

}
