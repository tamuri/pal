// NeoParameterized.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

/**
 * interface for class with (optimizable) parameters. A replacement for the Parameterized interface with
 * it's irritating updating of one parameter at a time
 *
 * @author Korbinian Strimmer, Matthew Goode
 * @version $Id: NeoParameterized.java,v 1.1 2004/08/02 05:22:04 matt Exp $
 */
public interface NeoParameterized {
    /**
     * get number of parameters
     *
     * @return number of parameters
     */
    int getNumberOfParameters();

    /**
     * set model parameter
     *
     * @param parameters the array holding the parameters
     * @param startIndex the index into the array that the related parameters start at
     */
    void setParameters(double[] parameters, int startIndex);

    /**
     * get model parameter
     *
     * @param parameterStore the array holding the parameters
     * @param startIndex the index into the array that the related parameters start at
     */
    void getParameters(double[] parameterStore, int startIndex);

    /**
     * get lower parameter limit
     *
     * @param n parameter number
     * @return lower bound
     */
    double getLowerLimit(int n);

    /**
     * get upper parameter limit
     *
     * @param n parameter number
     * @return upper bound
     */
    double getUpperLimit(int n);


    /**
     * get default value parameter values
     *
     * @return default value
     */
    void getDefaultValues(double[] store, int startIndex);

}
