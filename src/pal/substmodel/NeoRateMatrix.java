// NeoRateMatrix.java
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
 * The new RateMatrix class. Replaces the old RateMatrix, and will in turn be renamed to RateMatrix at a later point.
 * A NeoRateMatix object should be stateless (with regard to parameters used in likelihood searching). State is given by the parameter values which are administered externally.
 * @note This type of RateMatrix object only creates a relative rate matrix, not a Q matrix (so no reference to equilibrium frequencies!)
 * @version $Id: NeoRateMatrix.java,v 1.3 2004/08/15 03:00:37 matt Exp $
 *
 * @author Matthew Goode
 * <ul>
 *  <li> 3 May 2004 - Created file </li>
 * </ul>
 */
public interface NeoRateMatrix extends  Serializable{

	/**
	 * @return a short unique human-readable identifier for this rate matrix.
	 */
	public String getUniqueName();

	/**
	 * Is the relative rate matrix described by this rate matrix meant to represent a reversible process?
	 * If true only the upper part of the rate matrix needs to be filled in (eg  in matrix[i][j], for all where j > i );
	 * @return true if reversible
	 * @note I don't know how this will go - MG
	 */
	public boolean isReversible();

	/**
	 * @return the dimension of this rate matrix.
	 */
	public int getDimension();

	/**
	 * Check the compatibility of a data type to be used with the rate matrix
	 * @param dt the data type to test
	 * @return true if data type compatible (false otherwise)
	 */
	public boolean isDataTypeCompatible(DataType dt);

	/**
	 * Create the relative rates array
	 * @param rateStore The place where the relative rates are stored (should be assumed to be large enough - based on dimension)
	 * @param rateParameters The parameters to be used to construct the rate store
	 * @param startIndex The index into the rateParameters to start reading parameters
	 */
	public void createRelativeRates(double[][] rateStore, double[] rateParameters, int startIndex);

	public int getNumberOfRateParameters();

	public double getRateParameterLowerBound(int parameter);
	public double getRateParameterUpperBound(int parameter);

	public void getDefaultRateParameters(double[] parameterStore, int startIndex);
}
