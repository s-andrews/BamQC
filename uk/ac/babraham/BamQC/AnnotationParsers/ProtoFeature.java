/*
 * Changelog: 
 * - Piero Dalle Pezze: Class creation. Code taken from Location.java and adjusted. 
 */
package uk.ac.babraham.BamQC.AnnotationParsers;


import uk.ac.babraham.BamQC.DataTypes.Genome.Feature;
import uk.ac.babraham.BamQC.DataTypes.Genome.Location;






/**
 * The Class ProtoFeature. This class represents a feature with temporary location 
 * parameters which can still be updated. 
 * @author Piero Dalle Pezze
 */
public class ProtoFeature {

	/** The feature. */
	protected Feature feature;
	
	protected long value;
	protected int startValue, endValue, strandValue;
	
	
	public static final int FORWARD = 1;
	public static final int REVERSE = -1;
	public static final int UNKNOWN = 0;
	
	private static final long LAST_31_BIT_MASK = Long.parseLong("0000000000000000000000000000000001111111111111111111111111111111",2);

	// Using the 64th bit is a pain.  We can't use -0 to construct a mask since
	// it gets converted to +0 and loses the 64th bit.  We therefore have to leave
	// the 63rd bit set as well and work around this later.
	private static final long KNOWN_BIT_MASK   =  Long.parseLong("-100000000000000000000000000000000000000000000000000000000000000",2);
	private static final long REVERSE_TEST_MASK = Long.parseLong("0100000000000000000000000000000000000000000000000000000000000000",2);
	private static final long REVERSE_BIT_MASK =  ~REVERSE_TEST_MASK;
	

	/**
	 * Instantiates a new proto feature.
	 */
	public ProtoFeature (Feature feature, int start, int end, int strand) {
		this.feature = feature;
		setPosition (start, end, strand);
	}
	
	public void update(int start, int end, int strand) {
		long newValue = computePosition(start, end, strand);
		int newStartValue = (int)(newValue & LAST_31_BIT_MASK);
		int newEndValue = (int)((newValue>>31) & LAST_31_BIT_MASK);	
		
// From Location.java:		
//		if (start() != o.start()) return start() - o.start();
//		else if (end() != o.end()) return end()- o.end();
//		else if (strand() != o.strand()) return strand() - o.strand();
//		else return hashCode() - o.hashCode();
// From SplitLocation.java
//		Arrays.sort(this.subLocations);
//		setPosition(subLocations[0].start(),subLocations[subLocations.length-1].end(),subLocations[0].strand());

		
// Equivalent code for two intervals (Don't think this is correct though.. )
		if(startValue > newStartValue) {
			setPosition(newStartValue, endValue, strand);
		} else if(endValue > newEndValue) {
			setPosition(newStartValue, endValue, strand);
		} else if(strandValue > strand) {
			setPosition(newStartValue, endValue, strand);
		} else {
			setPosition(startValue, newEndValue, strandValue);
		}
		
// new code (TO TEST)
//		if(startValue <= newStartValue) {
//		  // A1 <= B1  ---> A1
//		  if(endValue < newStartValue) {
//		    // ---> A2		  
//		    // Do nothing
//		  } else if(endValue < newEndValue) {
//        // A2 >= B1
//		    // ---> B2
//	      setPosition(startValue, newEndValue, strandValue);
//		  } else {
//		    // A2 >= B2
//		    // ---> A2
//		    // Do nothing
//		  }
//		} else {
//		  // A1 > B1  ---> B1
//		    if(startValue > newEndValue) {
//		      // --- > B2
//	        setPosition(newStartValue, newEndValue, strand);
//		    } else if(endValue > newEndValue) {
//		      // A1 <= B2
//		      // ---> A2
//          setPosition(newStartValue, endValue, strand);
//		    } else {
//		      // A2 <= B2
//          setPosition(newStartValue, newEndValue, strand);
//		    }
//		}
	
		
	}

	/**
	 * Feature.
	 * 
	 * @return the feature
	 */
	public Feature getFeature() {
		feature.setLocation(new Location(value));
		return feature;
	}
	
	
	
	protected void setPosition (int start, int end, int strand) {
		value = computePosition(start, end, strand);
		// cache the starting and ending values
		setStartEndValues();
	}
	
	private long computePosition(int start, int end, int strand) {
		if (start < 0 || end < 0) throw new IllegalArgumentException("Negative positions are not allowed");
		
		if (end < start) {
			int temp = start;
			start = end;
			end = temp;
		}
		
		// Base is start
		long thisValue = start;
		
		// We need to remove the top sign bit from the end
		// and pack it starting at bit 32
		thisValue += (((end) & LAST_31_BIT_MASK) <<31);
				
		switch (strand) {
			case FORWARD :
				thisValue = thisValue | KNOWN_BIT_MASK; // Sets both forward and known
				break;
			case REVERSE :
				thisValue = thisValue | KNOWN_BIT_MASK; // Sets forward and known
				thisValue = thisValue & REVERSE_BIT_MASK; // Unsets forward
				break;

			case UNKNOWN :
				break; // Leaves known and forward as zero
				
			default :
				throw new IllegalArgumentException("Strand was not FORWARD, REVERSE or UNKNOWN");
				
		}
		return thisValue;
	}
	
	private void setStartEndValues() {
		startValue = (int)(value & LAST_31_BIT_MASK);
		endValue = (int)((value>>31) & LAST_31_BIT_MASK);
		
		if ((value & KNOWN_BIT_MASK) == KNOWN_BIT_MASK) {
			// KNOWN_BIT_MASK actually sets both known and forward.
			strandValue = FORWARD;
		}
		else if (value < 0) {
			// We can't test for the first bit with a bitmask since java
			// doesn't distinguish -0 and +0 so we just look for a negative
			// value to determine a positive position in bit 1.
			strandValue = REVERSE;
		}
		else {
			strandValue =  UNKNOWN;
		}
	}
	
}