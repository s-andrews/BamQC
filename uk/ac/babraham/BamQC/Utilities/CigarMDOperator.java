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
 * The operators that can appear in a cigar md string, and information about their disk representations.
 */
public enum CigarMDOperator {
    /** Match */
    M(true, true,   'm'),
    /** Mismatch */
    U(true, true,   'u'),    
    /** Insertion vs. the reference. */
    I(true, false,  'i'),
    /** Deletion vs. the reference. */
    D(false, true,  'd'),
    /** Skipped region from the reference. */
    N(false, true,  'n'),
    /** Soft clip. */
    S(true, false,  's'),
    /** Hard clip. */
    H(false, false, 'h'),
    /** Padding. */
    P(false, false, 'p'),
    /** Matches the reference. */
    EQ(true, true,  '='),
    /** Mismatches the reference. */
    X(true, true,   'x')
    ;

    // There is no internal representation of CigarMDOperator in BAM file
//    private static final byte OP_M = 0;
//    private static final byte OP_I = 1;
//    private static final byte OP_D = 2;
//    private static final byte OP_N = 3;
//    private static final byte OP_S = 4;
//    private static final byte OP_H = 5;
//    private static final byte OP_P = 6;
//    private static final byte OP_EQ = 7;
//    private static final byte OP_X = 8;

    private final boolean consumesReadBases;
    private final boolean consumesReferenceBases;
    private final byte character;
    private final String string;

    // Readable synonyms of the above enums
    public static final CigarMDOperator MATCH = M;
    public static final CigarMDOperator MISMATCH = U;    
    public static final CigarMDOperator INSERTION = I;
    public static final CigarMDOperator DELETION = D;
    public static final CigarMDOperator SKIPPED_REGION = N;
    public static final CigarMDOperator SOFT_CLIP = S;
    public static final CigarMDOperator HARD_CLIP = H;
    public static final CigarMDOperator PADDING = P;

    /** Default constructor. */
    CigarMDOperator(boolean consumesReadBases, boolean consumesReferenceBases, char character) {
        this.consumesReadBases = consumesReadBases;
        this.consumesReferenceBases = consumesReferenceBases;
        this.character = (byte) character;
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
            return M;
        case 'u':
            return U;            
        case 'i':
            return I;
        case 'd':
            return D;
        case 'n':
            return N;
        case 's':
            return S;
        case 'h':
            return H;
        case 'p':
            return P;
        case '=':
            return EQ;
        case 'x':
            return X;
        default:
            throw new IllegalArgumentException("Unrecognized CigarMDOperator: " + b);
        }
    }

// Bam/SAM files do not have CigarMD strings
//    /**
//     * @param i CIGARMD operator in binary form as appears in a BAMRecord.
//     * @return CigarMDOperator enum value corresponding to the given int value.
//     */
//    public static CigarMDOperator binaryToEnum(final int i) {
//        switch(i) {
//            case OP_M:
//                return M;
//            case OP_I:
//                return I;
//            case OP_D:
//                return D;
//            case OP_N:
//                return N;
//            case OP_S:
//                return S;
//            case OP_H:
//                return H;
//            case OP_P:
//                return P;
//            case OP_EQ:
//                return EQ;
//            case OP_X:
//                return X;
//            default:
//                throw new IllegalArgumentException("Unrecognized CigarOperator: " + i);
//        }
//    }
//
//    /**
//     *
//     * @param e CigarOperator enum value.
//     * @return CIGAR operator corresponding to the enum value in binary form as appears in a BAMRecord.
//     */
//    public static int enumToBinary(final CigarOperator e) {
//        switch(e) {
//            case M:
//                return OP_M;
//            case I:
//                return OP_I;
//            case D:
//                return OP_D;
//            case N:
//                return OP_N;
//            case S:
//                return OP_S;
//            case H:
//                return OP_H;
//            case P:
//                return OP_P;
//            case EQ:
//                return OP_EQ;
//            case X:
//                return OP_X;
//            default:
//                throw new IllegalArgumentException("Unrecognized CigarOperator: " + e);
//        }
//    }
//
//    /** Returns the character that should be used within a SAM file. */
//    public static byte enumToCharacter(final CigarOperator e) {
//        return e.character;
//    }

    /** Returns the CigarMD operator as it would be seen in a SAM file. */
    @Override 
    public String toString() {
        return this.string;
    }
}