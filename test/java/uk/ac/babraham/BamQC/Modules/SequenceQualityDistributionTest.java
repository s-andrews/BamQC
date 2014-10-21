package test.java.uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.babraham.BamQC.Modules.SequenceQualityDistribution;

public class SequenceQualityDistributionTest {

	private static Logger log = Logger.getLogger(SequenceQualityDistributionTest.class);
	
	private SequenceQualityDistribution sequenceQualityDistribution;
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
		sequenceQualityDistribution = new SequenceQualityDistribution();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testProcessSequence() {
		log.info("testDistribution");
		
		for (SAMRecord samRecord : samRecords) {
			sequenceQualityDistribution.processSequence(samRecord);
		}
		List<Integer> distribution = sequenceQualityDistribution.getDistribution();
		
		assertEquals(0, (int) distribution.get(0));
		assertEquals(0, (int) distribution.get(1));
		assertEquals(0, (int) distribution.get(2));
		assertEquals(2, (int) distribution.get(3));
		assertEquals(0, (int) distribution.get(4));
		assertEquals(0, (int) distribution.get(5));
		assertEquals(1, (int) distribution.get(6));
	}

	@Test(expected= IndexOutOfBoundsException.class)
	public void testProcessSequenceException() {
		log.info("testProcessSequenceException");
		
		for (SAMRecord samRecord : samRecords) {
			sequenceQualityDistribution.processSequence(samRecord);
		}
		List<Integer> distribution = sequenceQualityDistribution.getDistribution();
		
		assertEquals(0, (int) distribution.get(7));
	}
	
}