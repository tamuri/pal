// MathUtils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.math;


/**
 * Handy utility functions which have some Mathematical relavance.
 *
 * @author Matthew Goode
 * @author Alexei Drummond
 *
 * @version $Id: MathUtils.java,v 1.10 2004/08/05 03:00:22 matt Exp $
 */


public class MathUtils {

	public MathUtils() {}
	/**
	 * Calculate the logged difference in two values = x - y, where the log of x and y are given.
	 * That is, given ln(x), and ln(y) calculate ln(x-y)
	 * @param lnOfX the natural log of X
	 * @param lnOfY the natural log of Y
	 * @return X - Y (logged)
	 */
	public static final double getLoggedDifferenceFromLogged(double lnOfX, double lnOfY) {
		return lnOfY+Math.log(Math.exp(lnOfX-lnOfY)-1);
	}

	/**
	 * Ensure a value of x is actaully bounded between two values. Useful when using numerical analysis tools that may, over the limits of the expressed accuracy, return values outside the specified range
	 * @param x the value of interest
	 * @param lower the lower bound
	 * @param upper the upper bound
	 * @return x if between the bounds, or lower bound if lower than lower bound, or upper bound if higher than upper
	 */
	public final static double ensureBounded(double x, double lower, double upper){
	  if(x<lower) {
		  return lower;
		}
		if(x>upper) {
		  return upper;
		}
		return x;
	}


	/**
	 * A random number generator that is initialized with the clock when this
	 * class is loaded into the JVM. Use this for all random numbers.
	 * @note This method or getting random numbers in not thread-safe. Since
	 * MersenneTwisterFast is currently (as of 9/01) not synchronized using
	 * this function may cause concurrency issues. Use the static get methods of the
	 * MersenneTwisterFast class for access to a single instance of the class, that
	 * has synchronization.
	 */
	public static MersenneTwisterFast random = new MersenneTwisterFast();

	/**
	 * @return a new double array where all the values sum to 1.
	 * Relative ratios are preserved.
	 */
	public static final double[] getNormalized(double[] array) {
		double[] newArray = new double[array.length];
		double total = getTotal(array);
		for(int i = 0 ; i < array.length ; i++) {
			newArray[i] = array[i]/total;
		}
		return newArray;
	}

	/**
	 * @param end the index of the element after the last one to be included
	 * @return the total of a the values in a range of an array
	 */
	public static final double getTotal(double[] array, int start, int end) {
		double total = 0.0;
		for(int i = start ; i < array.length ; i++) {
			total+=array[i];
		}
		return total;
	}

	/**
	 * @param array
	 * @param start
	 * @param end the index of the element after the last one to be included
	 * @return the minimum of a the values in a range of an array
	 */
	public static final double getMinimum(double[] array, int start, int end) {
		double minimum = array[start];
		for(int i = start+1 ; i < array.length ; i++) {
			double v = array[i];
			if(v<minimum) {
			  minimum = v;
			}
		}
		return minimum;
	}
	/**
	 * @param array The array of values to examine
	 * @return the minimum of a the values in an array
	 */
	public static final double getMinimum(double[] array) {
	  return getMinimum(array,0,array.length);
	}
	/**
	 * @param array The array of values to examine
	 * @return the maximum of a the values in an array
	 */
	public static final double getMaximum(double[] array) {
	  return getMaximum(array,0,array.length);
	}

	/**
	 * @param array
	 * @param start
	 * @param end the index of the element after the last one to be included
	 * @return the maximum of a the values in a range of an array
	 */
	public static final double getMaximum(double[] array, int start, int end) {
		double maximum = array[start];
		for(int i = start+1 ; i < array.length ; i++) {
			double v = array[i];
			if(v>maximum) {
			  maximum = v;
			}
		}
		return maximum;
	}

	/**
	 * @return the total of the values in an array
	 */
	public static final double getTotal(double[] array) {
		return getTotal(array,0, array.length);

	}
	/**
	 * @return a set of valid, but randomly generated, arguments for a particular MultivariateFunction
	 */
	public static final double[] getRandomArguments(MultivariateFunction mf) {
		double[] values = new double[mf.getNumArguments()];
		for(int i = 0; i < values.length ; i++) {
			double min = mf.getLowerBound(i);
			double max = mf.getUpperBound(i);
			values[i] = (max-min)*MersenneTwisterFast.getNextDouble()+min;
		}
		return values;
	}
}
