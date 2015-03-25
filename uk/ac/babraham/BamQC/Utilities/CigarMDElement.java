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

/**
 * One component of a CigarMD string.
 */
public class CigarMDElement {
	
    private final int length;
    private final CigarMDOperator operator;

    public CigarMDElement(final int length, final CigarMDOperator operator) {
        this.length = length;
        this.operator = operator;
    }

    public int getLength() {
        return length;
    }

    public CigarMDOperator getOperator() {
        return operator;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CigarMDElement)) return false;

        final CigarMDElement that = (CigarMDElement) o;

        if (length != that.length) return false;
        if (operator != that.operator) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = length;
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        return result;
    }
}