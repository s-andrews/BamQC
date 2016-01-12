/**
 * Copyright Copyright 2015 Simon Andrews
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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Class creation. Code take from Picard Library and adapted.
 */
package uk.ac.babraham.BamQC.Utilities.CigarMD;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A list of CigarMDElement. The format is: # (number), operator (m=match,
 * u=mismatch, i=insertion, d=deletion), bases if oper={u,i,d}. For instance:
 * CIGAR: 32M2D5M1I52M MDtag: 7G24^AA7C49 Combined CIGAR+MDTag:
 * 7m1uGT24m2dAA5m1iG2m1uCA49m
 * 
 * Note: G in 1iG is SAM read- dependent.
 * @author Piero Dalle Pezze
 */
public class CigarMD {

	private final List<CigarMDElement> cigarMDElements = new ArrayList<CigarMDElement>();
	
	// Store this as StringBuilder for maximising efficiency in the concatenation process.
	private StringBuilder cigarMDString;

	/**
	 * Constructor.
	 */
	public CigarMD() { 
		cigarMDString = new StringBuilder(); 
	}

	/**
	 * Constructor from a list of CigarMD elements.
	 * @param cigarMDElements
	 */
	public CigarMD(final List<CigarMDElement> cigarMDElements) {
		this.cigarMDElements.addAll(cigarMDElements);
		
		cigarMDString = new StringBuilder();
		for(CigarMDElement element : cigarMDElements) {
			cigarMDString.append(element.toString());
		}
	}

	/**
	 * Return the list of CigarMD elements.
	 * @return the CigarMD elements.
	 */
	public List<CigarMDElement> getCigarMDElements() {
		return Collections.unmodifiableList(cigarMDElements);
	}

	/**
	 * Return the i-th CigarMD element.
	 * @param i the i-th element to retrieve.
	 * @return the i-th element
	 */
	public CigarMDElement getCigarMDElement(final int i) {
		return cigarMDElements.get(i);
	}

	/**
	 * Add an element to the CigarMD object.
	 * @param cigarMDElement to add.
	 */
	public void add(final CigarMDElement cigarMDElement) {
		cigarMDElements.add(cigarMDElement);
		cigarMDString.append(cigarMDElement.toString());
	}

	/**
	 * Return the number of CigarMD elements.
	 * @return the size of CigarMD.
	 */
	public int numCigarMDElements() {
		return cigarMDElements.size();
	}

	/**
	 * Return true if the CigarMD object is empty.
	 * @return True if CigarMD is empty.
	 */
	public boolean isEmpty() {
		return cigarMDElements.isEmpty();
	}

	/**
	 * Return the number of reference bases that the read covers, excluding
	 *         padding.
	 * @return The number of reference bases that the read covers, excluding
	 *         padding.
	 */
	public int getReferenceLength() {
		int length = 0;
		for (final CigarMDElement element : cigarMDElements) {
			switch (element.getOperator()) {
			case m:
			case u:
				// case i:
			case d:
				// case s:
				// case h:
				// case p:
			case n:
			case eq:
			case x:
				length += element.getLength(); break;
			default:
			}
		}
		return length;
	}

	/**
	 * Return the number of reference bases that the read covers, including
	 *         padding.
	 * @return The number of reference bases that the read covers, including
	 *         padding.
	 */
	public int getPaddedReferenceLength() {
		int length = 0;
		for (final CigarMDElement element : cigarMDElements) {
			switch (element.getOperator()) {
			case m:
			case u:
				// case i:
			case d:
				// case s:
				// case h:
				// case p:
			case n:
			case eq:
			case x:
				length += element.getLength(); break;
			default:
			}
		}
		return length;
	}

	/**
	 * Return the number of read bases that the read covers.
	 * @return The number of read bases that the read covers.
	 */
	public int getReadLength() {
		return getReadLength(cigarMDElements);
	}

	/**
	 * Return the number of read bases that the read covers.
	 * @return The number of read bases that the read covers.
	 */
	public static int getReadLength(final List<CigarMDElement> cigarMDElements) {
		int length = 0;
		for (final CigarMDElement element : cigarMDElements) {
			if (element.getOperator().consumesReadBases()) {
				length += element.getLength();
			}
		}
		return length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CigarMD))
			return false;

		final CigarMD cigarMD = (CigarMD) o;

		if (cigarMDElements != null ? !cigarMDElements
				.equals(cigarMD.cigarMDElements)
				: cigarMD.cigarMDElements != null)
			return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return cigarMDElements != null ? cigarMDElements.hashCode() : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return cigarMDString.toString();
	}
	
}
