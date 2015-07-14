package test.java.uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.NormalDistributionModeler;

public class NormalDistributionModelerTest {

	private static Logger log = Logger.getLogger(NormalDistributionModelerTest.class);
	
	private NormalDistributionModeler normalDistributionModeler = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set up : NormalDistributionModelerTest");	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down : NormalDistributionModelerTest");	
	}

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
		log.info("testCalculateDistribution");
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
