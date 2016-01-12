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
 * - Piero Dalle Pezze: Class creation. Taken from Picard library and adapted.
 */
package uk.ac.babraham.BamQC.Utilities.CigarMD;

/**
 * The operators that can appear in a cigar md string, and information about their disk representations.
 * @author Piero Dalle Pezze
 */
public enum CigarMDOperator {
    /** Match */
    m(true, true,   'm'),
    /** Mismatch */
    u(true, true,   'u'),    
    /** Insertion vs. the reference. */
    i(true, false,  'i'),
    /** Deletion vs. the reference. */
    d(false, true,  'd'),
    /** Skipped region from the reference. */
    n(false, true,  'n'),
    /** Soft clip. */
    s(true, false,  's'),
    /** Hard clip. */
    h(false, false, 'h'),
    /** Padding. */
    p(false, false, 'p'),
    /** Matches the reference. */
    eq(true, true,  '='),
    /** Mismatches the reference. */
    x(true, true,   'x')
    ;


    private final boolean consumesReadBases;
    private final boolean consumesReferenceBases;
    //private final byte character;
    private final String string;

    // Readable synonyms of the above enums
    public static final CigarMDOperator MATCH = m;
    public static final CigarMDOperator MISMATCH = u;    
    public static final CigarMDOperator INSERTION = i;
    public static final CigarMDOperator DELETION = d;
    public static final CigarMDOperator SKIPPED_REGION = n;
    public static final CigarMDOperator SOFT_CLIP = s;
    public static final CigarMDOperator HARD_CLIP = h;
    public static final CigarMDOperator PADDING = p;
    // NOTE: 
    // missing EQ and X from the original CigarOperator....is this a bug?
    

    /** Default constructor. */
    CigarMDOperator(boolean consumesReadBases, boolean consumesReferenceBases, char character) {
        this.consumesReadBases = consumesReadBases;
        this.consumesReferenceBases = consumesReferenceBases;
        //this.character = (byte) character;
        this.string = new String(new char[] {character}).intern();
    }

    /** If true, represents that this cigar operator "consumes" bases from the read bases. */
    public boolean consumesReadBases() { return consumesReadBases; }

    /** If true, represents that this cigar operator "consumes" bases from the reference sequence. */
    public boolean consumesReferenceBases() { return consumesReferenceBases; }

    /**
     * @param b CIGARMD operator in character form as appears in a text CIGARMD string
     * @return CigarMDOperator enum value corresponding to the given character.
     */
    public static CigarMDOperator characterToEnum(final int b) {
        switch (b) {
        case 'm':
            return m;
        case 'u':
            return u;            
        case 'i':
            return i;
        case 'd':
            return d;
        case 'n':
            return n;
        case 's':
            return s;
        case 'h':
            return h;
        case 'p':
            return p;
        case '=':
            return eq;
        case 'x':
            return x;
        default:
            throw new IllegalArgumentException("Unrecognized CigarMDOperator: " + b);
        }
    }


    /** Returns the CigarMD operator as it would be seen in a SAM file. */
    @Override 
    public String toString() {
        return this.string;
    }
}