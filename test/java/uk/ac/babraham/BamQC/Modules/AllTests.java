package test.java.uk.ac.babraham.BamQC.Modules;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	QualityDistributionTest.class, 
	ReadFlagStatisticsTest.class 
	})

public class AllTests {

}
