// Utils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

import pal.math.*;

/**
 * Provides some miscellaneous methods.
 *
 * @version $Id: Utils.java,v 1.25 2004/08/02 05:22:04 matt Exp $
 *
 * @author Matthew Goode
 */
public class Utils {
	/**
	 * Test if a string occurs within a set
	 * @param set the set of strings
	 * @param query the query string
	 * @return true if the query string is in the set (as determined by object equality)
	 */
	public static final boolean isContains(String[] set, String query) {
	  for(int i = 0 ; i < set.length ; i++) {
		  if(query.equals(set[i])) { return true; }
		}
		return false;
	}
	/** Clones an array of doubles
		* @return null if input is null, otherwise return complete copy.
		*/
	public static final double[] getCopy(double[] array) {
		if(array == null) { return null; }
		double[] copy = new double[array.length];
		System.arraycopy(array,0,copy,0,array.length);
		return copy;
	}

	/**
	 * Calculate the total of an array
	 * @param array The array to sum up
	 * @return the sum of all the elements
	 */
	public static final double getSum(double[] array) {
		double total = 0;
		for(int i = 0 ; i < array.length ; i++) {
		  total+=array[i];
		}
		return total;
	}
	/**
	 * Calculate the max of an array
	 * @param array The array to check
	 * @return the max of all the elements
	 */
	public static final double getMax(double[] array) {
	  return getMax(array, 0,array.length);
	}
	/**
	 * Calculate the max of an array
	 * @param array The array to check
	 * @param start the first index to check
	 * @param end the index after the last index to check
	 * @return the max of all the elements
	 */
	public static final double getMax(double[] array, int start, int end) {
		double max = Double.NEGATIVE_INFINITY;
		for(int i = start ; i < end ; i++) {
		  final double v = array[i+start];
			if(v>max) { max = v; }
		}
		return max;
	}
	/**
	 * Calculate the min of an array
	 * @param array The array to check
	 * @return the min of all the elements
	 */
	public static final double getMin(double[] array) {
		double min = Double.POSITIVE_INFINITY;
		for(int i = 0 ; i < array.length ; i++) {
		  final double v = array[i];
			if(v<min) { min = v; }
		}
		return min;
	}
	/**
	 * Calculate the mean value of an array
	 * @param array the values
	 * @return the average
	 * @note can also use DiscreteStatistics.mean()
	 */
	public static final double getMean(double[] array) {
	  return getSum(array)/array.length;
	}

	/** Clones an array of doubles from index start (inclusive) to index end (exclusive)
		* @return null if input is null
		*/
	public static final double[] getCopy(double[] array, int start, int end) {
		if(array == null) { return null; }
		double[] copy = new double[end-start];
		System.arraycopy(array,start,copy,0,copy.length);
		return copy;
	}
	/** Clones an array of doubles from index start (inclusive) to end
		* @return null if input is null
		*/
	public static final double[] getCopy(double[] array, int start) {
		return getCopy(array,start,array.length);
	}

	/** Clones an array of bytes
	 * @param array the bytes to copy
		* @return null if input is null, otherwise return complete copy.
		*/
	public static final byte[] getCopy(byte[] array) {
		if(array == null) {	return null; }
		byte[] copy = new byte[array.length];
		System.arraycopy(array,0,copy,0,array.length);
		return copy;
	}

	/** Clones an array of Strings
	  * @param array the strings to copy
		* @return null if input is null, otherwise return complete copy.
		*/
	public static final String[] getCopy(String[] array) {
		if(array == null) {	return null; }
		String[] copy = new String[array.length];
		System.arraycopy(array,0,copy,0,array.length);
		return copy;
	}

	/**
	 * Clones an array of doubles
	 * @return null if input is null, otherwise return complete copy.
	 */
	public static final double[][] getCopy(double[][] array) {
		if(array == null) { return null; }
		double[][] copy = new double[array.length][];
		for(int i = 0 ; i < copy.length ; i++) {
			copy[i] = new double[array[i].length];
			System.arraycopy(array[i],0,copy[i],0,array[i].length);
		}
		return copy;
	}
	/**
	 * Clones a matrix of ints
	 * @param matrix the matrix to clone
	 * @return null if input is null, otherwise return complete copy.
	 */
	public static final int[][] getCopy(int[][] matrix) {
		if(matrix == null) { return null; }
		int[][] copy = new int[matrix.length][];
		for(int i = 0 ; i < copy.length ; i++) {
			copy[i] = new int[matrix[i].length];
			System.arraycopy(matrix[i],0,copy[i],0,matrix[i].length);
		}
		return copy;
	}
	/**
	 * Clones an array of doubles
	 * @return null if input is null, otherwise return complete copy.
	 */
	public static final double[][][] getCopy(double[][][] array) {
		if(array == null) { return null; }
		double[][][] copy = new double[array.length][][];
		for(int i = 0 ; i < copy.length ; i++) {
			copy[i] = getCopy(array[i]);
		}
		return copy;
	}
	/**
	 * Clones an array of bytes
	 * @return null if input is null, otherwise return complete copy.
	 */
	public static final byte[][] getCopy(byte[][] array) {
		if(array == null) { return null; }
		byte[][] copy = new byte[array.length][];
		for(int i = 0 ; i < copy.length ; i++) {
			copy[i] = new byte[array[i].length];
			System.arraycopy(array[i],0,copy[i],0,array[i].length);
		}
		return copy;
	}
	/**
	 * Clones an array of booleans
	 * @return null if input is null, otherwise return complete copy.
	 */
	public static final boolean[][] getCopy(boolean[][] array) {
		if(array == null) { return null; }
		boolean[][] copy = new boolean[array.length][];
		for(int i = 0 ; i < copy.length ; i++) {
			copy[i] = new boolean[array[i].length];
			System.arraycopy(array[i],0,copy[i],0,array[i].length);
		}
		return copy;
	}
	/**
	 * Clones an array of ints
	 * @return null if input is null, otherwise return complete copy.
	 */
	public static final int[] getCopy(int[] array) {
		if(array == null) { return null; }
		int[] copy = new int[array.length];
		System.arraycopy(array,0,copy,0,array.length);
		return copy;
	}
	/**
	 * Clones an array of ints
	 * @param startingIndex, starts copying from this index
	 * @return null if input is null, otherwise return complete copy.
	 */
	public static final int[] getCopy(int[] array, int startingIndex) {
		if(array == null) { return null; }
		int[] copy = new int[array.length-startingIndex];
		System.arraycopy(array,startingIndex,copy,0,array.length-startingIndex);
		return copy;
	}

	/** Copies all of source into dest - assumes dest to be large enough */
	public static final void copy(double[][] source, double[][] dest) {
		for(int i = 0 ; i < source.length ; i++) {
			System.arraycopy(source[i],0,dest[i],0,source[i].length);
		}
	}
	/**
	 * A simple toString method for an array of doubles.
	 * No fancy formating.
	 * Puts spaces between each value
	 * @param number number of elements to process starting from first element
	 */
	public static final String toString(double[] array, int number) {
		StringBuffer sb = new StringBuffer(array.length*7);
		for(int i = 0 ; i < number ; i++) {
			sb.append(array[i]);
			sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * A simple toString method for an array of objects.
	 * No fancy formating.
	 * Puts spaces between each value
	 * @param number number of elements to process starting from first element
	 */
	public static final String toString(Object[] array, int number) {
		StringBuffer sb = new StringBuffer(array.length*7);
		for(int i = 0 ; i < number ; i++) {
			sb.append(array[i]);
			sb.append(' ');
		}
		return sb.toString();
	}
	/**
	 * A simple toString method for an array of objects.
	 * No fancy formating.
	 * Puts user defined string between each value
	 * @param number number of elements to process starting from first element
	 */
	public static final String toString(Object[] array, String divider) {
		return toString(array,divider,array.length);
	}
	/**
	 * A simple toString method for an array of objects.
	 * No fancy formating.
	 * Puts user defined string between each value
	 * @param number number of elements to process starting from first element
	 */
	public static final String toString(Object[] array, String divider, int number) {
		StringBuffer sb = new StringBuffer(array.length*7);
		for(int i = 0 ; i < number ; i++) {
			sb.append(array[i]);
			if(i!=number-1) {
				sb.append(divider);
			}
		}
		return sb.toString();
	}
	/**
	 * A simple toString method for an array of doubles.
	 * No fancy formating.
	 * Puts spaces between each value
	 */
	public static final String toString(Object[] array) {
		return toString(array,array.length);
	}
	/**
	 * A simple toString method for an array of doubles.
	 * No fancy formating.
	 * Puts spaces between each value
	 */
	public static final String toString(double[] array) {
		return toString(array,array.length);
	}
	/**
	 * A simple toString method for an array of ints.
	 * No fancy formating.
	 * Puts spaces between each value
	 */
	public static final String toString(int[] array) {
		return toString(array,array.length);
	}
	public static final String toString(int[] array, int number) {
		StringBuffer sb = new StringBuffer(array.length*7);
		for(int i = 0 ; i < number ; i++) {
			sb.append(array[i]);
			sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * A simple toString method for an array of doubles.
	 * No fancy formating.
	 * Puts spaces between each value
	 */
	public static final String toString(double[][] array) {
		String ss = "";
		for(int i = 0 ; i < array.length ; i++) {
			ss+= i+":"+toString(array[i])+'\n';
		}
		return ss;
	}
	/**
	 * A simple toString method for an array of ints.
	 * No fancy formating.
	 * Puts spaces between each value
	 */
	public static final String toString(int[][] array) {
		String ss = "";
		for(int i = 0 ; i < array.length ; i++) {
			ss+= i+":"+toString(array[i])+'\n';
		}
		return ss;
	}
	/**
	 * @deprecated
	 * @see getArgmax()
   */
	public static final int argmax(int[] array) {
	  return getArgmax(array);
	}
	/**
	 * Find the maximum "argument"
	 * @param array The array to examine
	 * @return the element of the array with the maximum value
	 * @note if array is zero length returns -1
	 */
	public static final int getArgmax(int[] array) {
		if(array.length==0) {
			return -1;
		}
		int maxValue = array[0];
		int maxIndex = 0;
		for(int i = 1 ; i < array.length ;i++) {
		  final int v = array[i];
			if(v>maxValue) {
			  maxValue = v;
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	/**
	 * @deprecated
	 * @see getArgmax()
   */
	public static final int argmax(double[] array) {
	  return getArgmax(array);
	}
	/**
	 * Find the maximum "argument" (of a double array)
	 * @param array The array to examine
	 * @return the element of the array with the maximum value
	 * @note if array is zero length returns -1
	 */
	public static final int getArgmax(double[] array) {
		if(array.length==0) {
			return -1;
		}
		double maxValue = array[0];
		int maxIndex = 0;
		for(int i = 1 ; i < array.length ;i++) {
		  final double v = array[i];
			if(v>maxValue) {
			  maxValue = v;
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	/**
	 * Creates an interface between a parameterised object to allow it to act as
	 * a multivariate minimum.
	 */
	public static final MultivariateFunction combineMultivariateFunction(MultivariateFunction base, Parameterized[] additionalParameters) {
		return new CombineMultiParam(base,additionalParameters);
	}

	private static class CombineMultiParam implements MultivariateFunction {
		Parameterized[] additionalParameters_;
		MultivariateFunction base_;

		double[] baseArgumentStorage_;
		double[] lowerBounds_;
		double[] upperBounds_;

		public CombineMultiParam(MultivariateFunction base, Parameterized[] additionalParameters) {
			this.additionalParameters_ = additionalParameters;
			this.base_ = base;
			int numberOfArguments = base_.getNumArguments();
			baseArgumentStorage_ = new double[numberOfArguments];

			for(int i = 0 ; i < additionalParameters.length ; i++) {
				numberOfArguments+=additionalParameters[i].getNumParameters();
			}
			lowerBounds_ = new double[numberOfArguments];
			upperBounds_ = new double[numberOfArguments];
			int argumentNumber = 0;
			for(int i = 0 ; i < base_.getNumArguments() ; i++) {
				lowerBounds_[argumentNumber] = base_.getLowerBound(i);
				upperBounds_[argumentNumber] = base_.getUpperBound(i);
				argumentNumber++;
			}
			for(int i = 0 ; i < additionalParameters.length ; i++) {
				for(int j = 0 ; j < additionalParameters[i].getNumParameters() ; j++) {
					lowerBounds_[argumentNumber] = additionalParameters[i].getLowerLimit(j);
					upperBounds_[argumentNumber] = additionalParameters[i].getUpperLimit(j);
					argumentNumber++;
				}
			}

		}


		public double evaluate(double[] argument) {
			int argumentNumber = 0;
			for(int i = 0 ; i < baseArgumentStorage_.length ; i++) {
				baseArgumentStorage_[i] = argument[argumentNumber];
				argumentNumber++;
			}
			for(int i = 0 ; i < additionalParameters_.length ; i++) {
				int numParam = additionalParameters_[i].getNumParameters();
				for(int j = 0 ; j < numParam ;	j++) {
					additionalParameters_[i].setParameter(argument[argumentNumber], j);
					argumentNumber++;
				}
			}
			return base_.evaluate(baseArgumentStorage_);
		}

		public int getNumArguments() {
			return lowerBounds_.length;
		}
		public double getLowerBound(int n) {
			return lowerBounds_[n];
		}

		public double getUpperBound(int n) {
			return upperBounds_[n];
		}

		/**
		 * @note NEEDS TO BE IMPLEMENTED CORRECTLY
		 * @return null
		 */
		public OrthogonalHints getOrthogonalHints() { return null; }
	}

}

