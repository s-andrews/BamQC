/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.BamQC.Annotation;

import java.util.Arrays;

/**
 * SplitLocation can be used to represent complex genomic
 * positions built up from several sublocations.
 */
public class SplitLocation extends Location {

	private static final long serialVersionUID = 4275718914518130070L;
	private Location [] subLocations;
	
	/**
	 * Instantiates a new split location.
	 * 
	 * @param subLocations The set of sublocations from which the whole feature will be built
	 * @param strand Which strand the feature is on
	 * @throws BamQCException
	 */
	public SplitLocation (Location [] subLocations) {
		super(0,0,UNKNOWN);
		if (subLocations == null || subLocations.length == 0) {
			throw new IllegalArgumentException("There must be at least one sublocation to define a feature");
		}
		this.subLocations = subLocations;
		Arrays.sort(this.subLocations);
		setPosition(subLocations[0].start(),subLocations[subLocations.length-1].end(),subLocations[0].strand());
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.Genome.Location#subLocations()
	 */
	public Location [] subLocations () {
		if (subLocations == null) {
			return new Location[] {this};
		}
		return subLocations;
	}	
	
	
}
