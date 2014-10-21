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

import uk.ac.babraham.BamQC.Modules.InsertDistribution;

public class InsertDistributionTest {

	private static Logger log = Logger.getLogger(InsertDistributionTest.class);
	
	private InsertDistribution insertDistribution;
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
		
		insertDistribution = new InsertDistribution();
	}

	@After
	public void tearDown() throws Exception {}

	@Test(expected= IndexOutOfBoundsException.class)
	public void testProcessSequence() {
		log.info("testProcessSequence");
		
		for (SAMRecord samRecord : samRecords) {
			insertDistribution.processSequence(samRecord);
		}
		List<Integer> distribution = insertDistribution.getDistribution();
		
		assertEquals(0, (int) distribution.get(0));
		assertEquals(0, (int) distribution.get(1));
		assertEquals(0, (int) distribution.get(2));
		assertEquals(2, (int) distribution.get(3));
		
		// throws exception
		assertEquals(0, (int) distribution.get(4));
	}
	
}
