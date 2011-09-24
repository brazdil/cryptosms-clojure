package uk.ac.cam.db538.cryptosms.utils;

public class Numeric {
	
	/**
	 * Round up division
	 *
	 * @param number 
	 * @param divisor
	 * @return 
	 */
	public static int roundUpDivision(int number, int divisor) {
		return (number + divisor - 1) / divisor;
	}

	/**
	 * Least greater multiple of a given number
	 *
	 * @param number 
	 * @param multipleOf
	 * @return 
	 */
	public static int leastGreaterMultiple(int number, int multipleOf) {
		return roundUpDivision(number, multipleOf) * multipleOf;
	}
}