// GeneralPoissonRateMatrix.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.io.*;
import pal.datatype.*;
import pal.mep.*;
import pal.math.OrthogonalHints;

import java.io.*;


/**
 * A general rate matrix class for JC69/F81 style rate matrices (but for all data types)
 * @version $Id: GeneralPoissonRateMatrix.java,v 1.3 2004/08/15 03:00:37 matt Exp $
 *
 * @author Matthew Goode
 * <ul>
 *  <li> 11 May 2004 - Created file </li>
 * </ul>
 */
public class GeneralPoissonRateMatrix implements NeoRateMatrix {
	private final int dimension_;
	public GeneralPoissonRateMatrix(int dimension) {
	  this.dimension_ = dimension;
	}

	public String getUniqueName() { return "General Poisson (dimension "+dimension_+")"; }

	/**
	 * @return true (doesn't really matter)
	 */
	public boolean isReversible() { return true; }

	/**
	 * @return the dimension of this rate matrix. (as for construction)
	 */
	public int getDimension() { return dimension_; }

	/**
	 * Check the compatibility of a data type to be used with the rate matrix
	 * @param dt the data type to test
	 * @return true if data type state count is equal to dimension
	 */
	public boolean isDataTypeCompatible(DataType dt) { return dt.getNumStates()==dimension_; }

	public void createRelativeRates(double[][] rateStore, double[] rateParameters,int startIndex) {
	  for(int i = 0 ; i < dimension_ ; i++) {
		  for(int j = 0 ; j < dimension_ ; j++) {
				rateStore[j][i] = 1;
		  }
		}
	}

	public int getNumberOfRateParameters() { return 0; }

	public double getRateParameterLowerBound(int parameter) { throw new RuntimeException("Assertion error : not expected"); }
	public double getRateParameterUpperBound(int parameter) { throw new RuntimeException("Assertion error : not expected"); }

	public void getDefaultRateParameters(double[] store, int startIndex) {  }
}
