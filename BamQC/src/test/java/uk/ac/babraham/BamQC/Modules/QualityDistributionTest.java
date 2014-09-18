package uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QualityDistributionTest {

	private static Logger log = Logger.getLogger(QualityDistributionTest.class);
	
	private QualityDistribution qualityDistribution;
	private TestObjectFactory testObjectFactory;
	private List<SAMRecord> samRecords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
		
		qualityDistribution = new QualityDistribution();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testDistribution() {
		log.info("testDistribution");
		for (SAMRecord samRecord : samRecords) {
			qualityDistribution.processSequence(samRecord);
		}
		int[] distribution = qualityDistribution.getDistribution();
		
		assertEquals(1, distribution[0]);
		assertEquals(1, distribution[255]);
		assertEquals(1, distribution[10]);
	
		for (int i = 1; i < 256; i++) {
			if (i != 10 && i != 255) {
				assertEquals(0, distribution[i]);
			}
		}
		for (SAMRecord samRecord : samRecords) {
			qualityDistribution.processSequence(samRecord);
		}
		assertEquals(2, distribution[0]);
		assertEquals(2, distribution[255]);
		assertEquals(2, distribution[10]);
	}
}
