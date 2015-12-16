/**
 * Copyright Copyright 2010-15 Simon Andrews
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
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
/*
 * Changelog: 
 * - Piero Dalle Pezze: Imported from SeqMonk and adjusted for BamQC
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC;

/**
 * A generic exception class representing logic exceptions generated within BamQC
 * @author Simon Andrews
 */
public class BamQCException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2638136960061734517L;

	/**
	 * Instantiates a new bamqc exception.
	 * 
	 * @param error Error text
	 */
	public BamQCException (String error) {
		super(error);
	}
	
}
