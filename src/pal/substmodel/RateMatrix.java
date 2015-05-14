// RateMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
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
 * abstract base class for all rate matrices
 *
 * @version $Id: RateMatrix.java,v 1.34 2003/11/13 04:05:39 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 * @author Matthew Goode
 */
public interface RateMatrix
	extends NamedParameterized, Report, Cloneable, Serializable{

	/**
	 * get numerical code describing the data type
	 *
	 * @return integer code identifying a data type
	 */
	int getTypeID();

	/**
	 * get numerical code describing the model type
	 *
	 * @return integer code identifying a substitution model
	 */
	int getModelID();

	/**
	 * @return a short unique human-readable identifier for this rate matrix.
	 */
	String getUniqueName();

	/**
	 * @return the dimension of this rate matrix.
	 */
	int getDimension();

	/**
	 * @return stationary frequencies (sum = 1.0)
	 */
	double[] getEquilibriumFrequencies();

	/**
	 * @return stationary frequency (sum = 1.0) for ith state
	 * Preferred method for infrequent use.
	 */
	double getEquilibriumFrequency(int i);

	/**
	 * Get the data type of this rate matrix
	 */
	DataType getDataType();

	/**
	 * @return rate matrix (transition: from 1st index to 2nd index)
	 * @deprecated try not to use.
	 */
	double[][] getRelativeRates();

	/**
	 * @return the probability of going from one state to another
	 * given the current distance
	 * @param fromState The state from which we are starting
	 * @param toState The resulting state
	 */
	double getTransitionProbability(int fromState, int toState);

	/** A utility method for speed, transfers trans prob information quickly
	 *	into store
	 */
	void getTransitionProbabilities(double[][] probabilityStore);

	/** Sets the distance (such as time/branch length) used when calculating
	 *	the probabilities. This method may well take the most time!
	 */
	void setDistance(double distance);

	/** Sets the distance (such as time/branch length) used when calculating
	 *	the probabilities.
	 *  @note The resulting transition probabilities will be in reverse
	 *  (that is in the matrix instead of [from][to] it's [to][from])
	 */
	void setDistanceTranspose(double distance);

	/** Add a PalObjectListener to be notified of changes to the model.
	 *  Only the parametersChanged method will generally be called
	 */
	void addPalObjectListener(PalObjectListener pol);
	void removePalObjectListener(PalObjectListener pol);

	/**
	 * @return an orthogonal hints object for orthogonal optimisation (may return null for no hints)
	 */
	OrthogonalHints getOrthogonalHints();

	// interface Report (remains abstract)

	// interface Parameterized (remains abstract)

	Object clone();

	public double setParametersNoScale(double[] parameters);
	public void scale(double scaleValue);

}
