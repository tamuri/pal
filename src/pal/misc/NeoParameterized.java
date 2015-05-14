// NeoParameterized.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

import java.io.*;


/**
 * interface for class with (optimizable) parameters. A replacement for the Parameterized interface with
 * it's irritating updating of one parameter at a time
 *
 * @version $Id: NeoParameterized.java,v 1.1 2004/08/02 05:22:04 matt Exp $
 *
 * @author Korbinian Strimmer, Matthew Goode
 */
public interface NeoParameterized {
	/**
	 * get number of parameters
	 *
	 * @return number of parameters
	 */
	public int getNumberOfParameters();

	/**
	 * set model parameter
	 *
	 * @param parameters the array holding the parameters
	 * @param startIndex the index into the array that the related parameters start at
	 */
	public void setParameters(double[] parameters, int startIndex);

	/**
	 * get model parameter
	 *
	 * @param parameters the array holding the parameters
	 * @param startIndex the index into the array that the related parameters start at
	 */
	public void getParameters(double[] parameterStore, int startIndex);

	/**
	 * get lower parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return lower bound
	 */
	public double getLowerLimit(int n);

	/**
	 * get upper parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return upper bound
	 */
	public double getUpperLimit(int n);


	/**
	 * get default value parameter values
	 *
	 * @return default value
	 */
	public void getDefaultValues(double[] store, int startIndex);

}
