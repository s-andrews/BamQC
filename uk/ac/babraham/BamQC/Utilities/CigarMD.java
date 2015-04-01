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

package uk.ac.babraham.BamQC.Utilities;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

/**
 * A list of CigarMDElement. The format is: # (number), operator (m=match,
 * u=unmatch, i=insertion, d=deletion), bases if oper={u,i,d}. For instance:
 * CIGAR: 32M2D5M1I52M MDtag: 7G24^AA7C49 Combined CIGAR+MDTag:
 * 7m1uGT24m2dAA5m1iG2m1uCA49m
 * 
 * Note: G in 1iG is SAM read- dependent.
 */
public class CigarMD {

	private final List<CigarMDElement> cigarMDElements = new ArrayList<CigarMDElement>();

	public CigarMD() { }

	public CigarMD(final List<CigarMDElement> cigarMDElements) {
		this.cigarMDElements.addAll(cigarMDElements);
	}

	public List<CigarMDElement> getCigarMDElements() {
		return Collections.unmodifiableList(cigarMDElements);
	}

	public CigarMDElement getCigarMDElement(final int i) {
		return cigarMDElements.get(i);
	}

	public void add(final CigarMDElement cigarMDElement) {
		cigarMDElements.add(cigarMDElement);
	}

	public int numCigarMDElements() {
		return cigarMDElements.size();
	}

	public boolean isEmpty() {
		return cigarMDElements.isEmpty();
	}

	/**
	 * @return The number of reference bases that the read covers, excluding
	 *         padding.
	 */
	public int getReferenceLength() {
		int length = 0;
		for (final CigarMDElement element : cigarMDElements) {
			switch (element.getOperator()) {
			case M:
			case U:
				// case I:
			case D:
				// case S:
				// case H:
				// case P:
			case N:
			case EQ:
			case X:
				length += element.getLength(); break;
			default:
			}
		}
		return length;
	}

	/**
	 * @return The number of reference bases that the read covers, including
	 *         padding.
	 */
	public int getPaddedReferenceLength() {
		int length = 0;
		for (final CigarMDElement element : cigarMDElements) {
			switch (element.getOperator()) {
			case M:
			case U:
				// case I:
			case D:
				// case S:
				// case H:
				// case P:
			case N:
			case EQ:
			case X:
				length += element.getLength(); break;
			default:
			}
		}
		return length;
	}

	/**
	 * @return The number of read bases that the read covers.
	 */
	public int getReadLength() {
		return getReadLength(cigarMDElements);
	}

	/**
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

	// THIS NEEDS SOME THINKING
	// /**
	// * Exhaustive validation of CIGAR.
	// * Note that this method deliberately returns null rather than
	// Collections.emptyList() if there
	// * are no validation errors, because callers tend to assume that if a
	// non-null list is returned, it is modifiable.
	// * @param readName For error reporting only. May be null if not known.
	// * @param recordNumber For error reporting only. May be -1 if not known.
	// * @return List of validation errors, or null if no errors.
	// */
	// public List<SAMValidationError> isValid(final String readName, final long
	// recordNumber) {
	// if (this.isEmpty()) {
	// return null;
	// }
	// List<SAMValidationError> ret = null;
	// boolean seenRealOperator = false;
	// for (int i = 0; i < cigarElements.size(); ++i) {
	// final CigarElement element = cigarElements.get(i);
	// // clipping operator can only be at start or end of CIGAR
	// final CigarOperator op = element.getOperator();
	// if (isClippingOperator(op)) {
	// if (op == CigarOperator.H) {
	// if (i != 0 && i != cigarElements.size() - 1) {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "Hard clipping operator not at start or end of CIGAR", readName,
	// recordNumber));
	// }
	// } else {
	// if (op != CigarOperator.S) throw new
	// IllegalStateException("Should never happen: " + op.name());
	// if (i == 0 || i == cigarElements.size() - 1) {
	// // Soft clip at either end is fine
	// } else if (i == 1) {
	// if (cigarElements.size() == 3 && cigarElements.get(2).getOperator() ==
	// CigarOperator.H) {
	// // Handle funky special case in which S operator is both one from the
	// beginning and one
	// // from the end.
	// } else if (cigarElements.get(0).getOperator() != CigarOperator.H) {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "Soft clipping CIGAR operator can only be inside of hard clipping operator",
	// readName, recordNumber));
	// }
	// } else if (i == cigarElements.size() - 2) {
	// if (cigarElements.get(cigarElements.size() - 1).getOperator() !=
	// CigarOperator.H) {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "Soft clipping CIGAR operator can only be inside of hard clipping operator",
	// readName, recordNumber));
	// }
	// } else {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "Soft clipping CIGAR operator can at start or end of read, or be inside of hard clipping operator",
	// readName, recordNumber));
	// }
	//
	// }
	// } else if (isRealOperator(op)) {
	// // Must be at least one real operator (MIDN)
	// seenRealOperator = true;
	// // There should be an M or P operator between any pair of IDN operators
	// if (isInDelOperator(op)) {
	// for (int j = i+1; j < cigarElements.size(); ++j) {
	// final CigarOperator nextOperator = cigarElements.get(j).getOperator();
	// // Allow
	// if ((isRealOperator(nextOperator) && !isInDelOperator(nextOperator)) ||
	// isPaddingOperator(nextOperator)) {
	// break;
	// }
	// if (isInDelOperator(nextOperator) && op == nextOperator) {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "No M or N operator between pair of " + op.name() +
	// " operators in CIGAR", readName, recordNumber));
	// }
	// }
	// }
	// } else if (isPaddingOperator(op)) {
	// if (i == 0) {
	// /*
	// * Removed restriction that padding not be the first operator because if a
	// read starts in the middle of a pad
	// * in a padded reference, it is necessary to precede the read with padding
	// so that alignment start refers to a
	// * position on the unpadded reference.
	// */
	// } else if (i == cigarElements.size() - 1) {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "Padding operator not valid at end of CIGAR", readName, recordNumber));
	// } else if (!isRealOperator(cigarElements.get(i-1).getOperator()) ||
	// !isRealOperator(cigarElements.get(i+1).getOperator())) {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "Padding operator not between real operators in CIGAR", readName,
	// recordNumber));
	// }
	// }
	// }
	// if (!seenRealOperator) {
	// if (ret == null) ret = new ArrayList<SAMValidationError>();
	// ret.add(new SAMValidationError(SAMValidationError.Type.INVALID_CIGAR,
	// "No real operator (M|I|D|N) in CIGAR", readName, recordNumber));
	// }
	// return ret;
	// }

	private static boolean isRealOperator(final CigarMDOperator op) {
		return op == CigarMDOperator.M || op == CigarMDOperator.U
				|| op == CigarMDOperator.EQ || op == CigarMDOperator.X
				|| op == CigarMDOperator.I || op == CigarMDOperator.D
				|| op == CigarMDOperator.N;
	}

	private static boolean isInDelOperator(final CigarMDOperator op) {
		return op == CigarMDOperator.I || op == CigarMDOperator.D;
	}

	private static boolean isClippingOperator(final CigarMDOperator op) {
		return op == CigarMDOperator.S || op == CigarMDOperator.H;
	}

	private static boolean isPaddingOperator(final CigarMDOperator op) {
		return op == CigarMDOperator.P;
	}

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

	@Override
	public int hashCode() {
		return cigarMDElements != null ? cigarMDElements.hashCode() : 0;
	}

	@Override
	public String toString() {
		String cigarMDString = "";
		for(CigarMDElement element : cigarMDElements) {
			cigarMDString = cigarMDString + element.toString();
		}
		return cigarMDString;
	}
}
