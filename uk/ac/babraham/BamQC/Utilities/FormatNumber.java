/**
 * Copyright 2011-13 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
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
 * - Piero Dalle Pezze: Class creation.
 */
package uk.ac.babraham.BamQC.Utilities;

import java.text.DecimalFormat;


/**
 * A simple class for compacting numbers
 * @author Piero Dalle Pezze
 */
public class FormatNumber {
	private static long p = 1000000000000000L;
	private static long t = 1000000000000L;
	private static long g = 1000000000L;
	private static long m = 1000000L;
	private static long k = 1000L;

	public static String compactInteger(int number) {
		String strNumber = String.valueOf(number);
		if(1.0d*number / p >= 1.0) {
			strNumber = (int)((1.0d*number)/p) + "P";
		} else if(1.0d*number / t >= 1.0) {
			strNumber = (int)((1.0d*number)/t) + "T";	
		} else if(1.0d*number / g >= 1.0) {
			strNumber = (int)((1.0d*number)/g) + "G";
		} else if(1.0d*number / m >= 1.0) {
			strNumber = (int)((1.0d*number)/m) + "M";
		} else if(1.0d*number / k >= 1.0) {
			strNumber = (int)((1.0d*number)/k) + "k";
		}
		return strNumber;
	}
	
	
	public static String compactInteger(String strNumber) {
		return compactInteger(Integer.parseInt(strNumber));
	}


	public static String compactIntegerRange(int number1, int number2) {
		String strNumber = String.valueOf(number1) + "-" + String.valueOf(number2);
		if((number1+number2) / p >= 1.0) {
			strNumber = (int)((number1)/p) + "-" + (int)((number1+number2)/p) + "P";
		} else if((number1+number2) / t >= 1.0) {
			strNumber = (int)((number1)/t) + "-" + (int)((number1+number2)/t) + "T";
		} else if((number1+number2) / g >= 1.0) {
			strNumber = (int)((number1)/g) + "-" + (int)((number1+number2)/g) + "G";
		} else if((number1+number2) / m >= 1.0) {
			strNumber = (int)((number1)/m) + "-" + (int)((number1+number2)/m) + "M";
		} else if((number1+number2) / k >= 1.0) {
			strNumber = (int)((number1)/k) + "-" + (int)((number1+number2)/k) + "k";
		}
		return strNumber;
		
	}
		

	public static String compactIntegerRange(String strNumber1, String strNumber2) {
		return compactIntegerRange(Integer.parseInt(strNumber1),Integer.parseInt(strNumber2));
	}
	
	
	/** Computes the position of the first non-zero decimal digit. 
	 * 
	 * @param number
	 * @return the first non-zero decimal digit for the parameter number.
	 */
	public static int getFirstSignificantNonNullDecimalPosition(double number) {
		//System.out.println(number);
		int significantDecimalPosition = 0;
		
		// We need to convert a scientific notation (e.g. 2.0E-4) to a decimal notation (0.0002)
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(20);
        String extendedNumber = df.format(number);
        
		if(extendedNumber.indexOf(".") != -1) {
			String[] parts = extendedNumber.split("\\.");		
			if(parts[parts.length - 1].length() > 0) {
				int zeros = 0;
				String decimalPart = parts[parts.length - 1];
//				System.out.println("number " + number);
//				System.out.println("String num " + extendedNumber);
//				System.out.println("dec part " + decimalPart);
//				System.out.println("dec part length " + decimalPart.length());
				// count how many zero before a number != 0 
				while(zeros < decimalPart.length() && decimalPart.charAt(zeros) == '0') { 
					zeros++;
				}
				significantDecimalPosition = zeros + 1;
			}
		}
		return significantDecimalPosition;
	}
	
	public static String convertToScientificNotation(String number) {
		return String.valueOf(Double.parseDouble(number));
	}
	
	public static String convertToScientificNotation(double number) {
		return String.valueOf(number);
	}
	
	public static String convertToScientificNotation(int number) {
		return String.valueOf(1.0d*number);
	}
	
	public static String convertToScientificNotation(long number) {
		return String.valueOf(1.0d*number);
	}
	
	public static String convertToScientificNotation(float number) {
		return String.valueOf(1.0d*number);
	}
	
	public static void main (String [] args) {
		double d = 2;
		System.out.println("First Significant Decimal Position for " + d + ": " + getFirstSignificantNonNullDecimalPosition(d));
		d = 1.10;
		System.out.println("First Significant Decimal Position for " + d + ": " + getFirstSignificantNonNullDecimalPosition(d));
		d = 1.01;
		System.out.println("First Significant Decimal Position for " + d + ": " + getFirstSignificantNonNullDecimalPosition(d));		
		d = 1.56300;
		System.out.println("First Significant Decimal Position for " + d + ": " + getFirstSignificantNonNullDecimalPosition(d));
		d = 0.00234;
		System.out.println("First Significant Decimal Position for " + d + ": " + getFirstSignificantNonNullDecimalPosition(d));
		d = 0.00000234;
		System.out.println("First Significant Decimal Position for " + d + ": " + getFirstSignificantNonNullDecimalPosition(d));
		d = 0.0000000000234;
		System.out.println("First Significant Decimal Position for " + d + ": " + getFirstSignificantNonNullDecimalPosition(d));
		
		// TODO not all in here is correct.. Better use the methods to convert to scientific notation instead.
		System.out.println(compactInteger(5000));
		System.out.println(compactInteger(55000));
		System.out.println(compactInteger(555000));		
		System.out.println(compactInteger(5555000));
		System.out.println(compactInteger(55555000));
		System.out.println(compactInteger(10000000));		
		System.out.println(compactInteger(12000000));
		System.out.println(compactInteger(12500000));		
		System.out.println(compactInteger(14000000));
		System.out.println(compactInteger(1000000));		
		System.out.println(compactInteger(1200000));
		System.out.println(compactInteger(1250000));		
		System.out.println(compactInteger(1400000));
		System.out.println(compactInteger(3000000));		
		System.out.println(compactInteger(3200000));
		System.out.println(compactInteger(3400000));		
	}
	
	
}
