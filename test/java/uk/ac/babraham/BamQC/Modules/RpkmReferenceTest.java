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

import uk.ac.babraham.BamQC.Modules.RpkmReference;

public class RpkmReferenceTest {

	private static Logger log = Logger.getLogger(RpkmReferenceTest.class);
	
	private RpkmReference rpkmReference;
	private TestObjectFactory testObjectFactory;
	private List<SAMRecord> samRecords;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		rpkmReference = new RpkmReference();
		
		testObjectFactory = new TestObjectFactory();
		samRecords = testObjectFactory.getSamRecords();
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testProcessSequence() {
		log.info("testProcessSequence");
		
		int count = 0;
		for (SAMRecord samRecord : samRecords) {
			rpkmReference.processSequence(samRecord);
			
			double[] coverageReference = rpkmReference.getCoverage();
			
			if (count == 0) {
				assertEquals(9.0E-4, coverageReference[0], 0.0000001);
			}
			count++;
		}
		double[] coverageReference = rpkmReference.getCoverage();
		
		assertEquals(3.5E-3, coverageReference[0], 0.000001);
	}

}
