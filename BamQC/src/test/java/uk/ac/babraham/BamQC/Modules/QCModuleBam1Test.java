package uk.ac.babraham.BamQC.Modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.samtools.SAMRecord;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QCModuleBam1Test {

	private QCModuleBam1 qcModuleBam1;
	private SAMRecord samRecord1, samRecord2, samRecord3;
	private TestObjectFactory testObjectFactory;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		testObjectFactory = new TestObjectFactory();
		List<SAMRecord> samRecords = testObjectFactory.getSamRecords();
		
		qcModuleBam1 = new QCModuleBam1();
		
		samRecord1 = samRecords.get(0);
		samRecord2 = samRecords.get(1);
		samRecord3 = samRecords.get(2);
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testNeedsToSee() {
		assertTrue(qcModuleBam1.needsToSeeAnnotation());
		assertTrue(qcModuleBam1.needsToSeeSequences());
	}
	
	@Test
	public void testReadCount() {
		assertEquals(0, qcModuleBam1.getReadCount());
		
		qcModuleBam1.processSequence(samRecord1);
		qcModuleBam1.processSequence(samRecord2);
		qcModuleBam1.processSequence(samRecord3);
		
		assertEquals(3, qcModuleBam1.getReadCount());
		
		qcModuleBam1.reset();
		
		assertEquals(0, qcModuleBam1.getReadCount());
		
		qcModuleBam1.processSequence(samRecord2);
		qcModuleBam1.processSequence(samRecord3);
		
		assertEquals(2, qcModuleBam1.getReadCount());
	}

}
