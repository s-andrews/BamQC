package uk.ac.babraham.BamQC.Utilities;

public class QualityCount {

	/*
	 * So I'm on my third go at writing this.  I've now tried an all
	 * primitive version of this class so that we don't have to do 
	 * hash lookps which require a conversion from chr to Character.
	 * We should also be safe with 150 slots which will give us up to
	 * Phred 86 with a 64 offset, which should be plenty.
	 */
	
	private long [] actualCounts = new long[150];
	
	private long totalCounts = 0;

	public void addValue(char c) {
		totalCounts++;
		actualCounts[(int)c]++;
	}
	
	public long getTotalCount () {
		return totalCounts;
	}
	
	public char getMinChar () {
		
		for (int i=0;i<actualCounts.length;i++) {
			if (actualCounts[i]>0) return (char)i;
		}
		
		return (char)1000;
	}
	
	public char getMaxChar () {
		for (int i=actualCounts.length-1;i>=0;i--) {
			if (actualCounts[i]>0) return (char)i;
		}
		
		return (char)1000;

	}
			
	public double getMean (int offset) {
		long total = 0;
		long count = 0;
	
		for (int i=offset;i<actualCounts.length;i++) {
			total += actualCounts[i]*(i-offset);
			count += actualCounts[i];
		}
		
		return ((double)total)/count;
	}
	
	public double getPercentile (int offset, int percentile) {

		long total = totalCounts;
		
		total *= percentile;
		total /= 100;
		
		long count = 0;
		for (int i=offset;i<actualCounts.length;i++) {
			count += actualCounts[i];
			if (count >=total) {
				return((char)(i-offset));
			}
		}
		
		return -1;
		
	}
	
}
