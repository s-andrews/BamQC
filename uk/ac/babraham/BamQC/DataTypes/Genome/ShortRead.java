/**
 * Copyright Copyright 2015 Piero Dalle Pezze
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

package uk.ac.babraham.BamQC.DataTypes.Genome;


/**
 * A simple class for representing a compact read.
 */
class ShortRead implements Comparable<ShortRead> {
	private String referenceName;
	private int alignmentStart;
	private int alignmentEnd;

	public ShortRead(String referenceName, int alignmentStart, int alignmentEnd) {
		this.referenceName = referenceName;
		this.alignmentStart = alignmentStart;
		this.alignmentEnd = alignmentEnd;
	}

	@Override
	public int compareTo(ShortRead sr) {
		int compareTest = referenceName.compareTo(sr.referenceName);
		if(compareTest == 0) {
			return Integer.valueOf(alignmentStart).compareTo(Integer.valueOf(sr.alignmentStart));
		}
		return compareTest;
	}

	public String getReferenceName() { 
		return referenceName;
	}

	public int getAlignmentStart() {
		return alignmentStart;
	}

	public int getAlignmentEnd() {
		return alignmentEnd;
	}

}