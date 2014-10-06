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

import uk.ac.babraham.BamQC.Modules.ReadFlagStatistics;

public class ReadFlagStatisticsTest {
	
	private static Logger log = Logger.getLogger(ReadFlagStatisticsTest.class);
	
	private ReadFlagStatistics readFlagStatistics;
	private TestObjectFactory testObjectFactory;
	private List<SAMRecord> samRecords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		readFlagStatistics = new ReadFlagStatistics();
		
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testProcessSequence() {
		log.info("testProcessSequence");
		
		for (SAMRecord samRecord : samRecords) {
			readFlagStatistics.processSequence(samRecord);
		}
		
		assertEquals(3, readFlagStatistics.getReadNumber());
		assertEquals(2, readFlagStatistics.getPairNumber());
		assertEquals(2, readFlagStatistics.getMappedPairNumber());
		assertEquals(1, readFlagStatistics.getFailedQualityControlNumber());
		assertEquals(2, readFlagStatistics.getDuplicateNumber());
	}

	
	
}
