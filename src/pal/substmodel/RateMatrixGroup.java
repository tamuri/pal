// RateMatrixGroup.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: RateMatrixGroup </p>
 * <p>Description: A grouping of rate matrices (to provide a model of substitution heteoregeneity)</p>
 * @author Matthew Goode
 * @version 1.0
 * @note There is no reference to parameters here. Paramerization should be managed by an external resource (for example, have a separate object that manages the parameters).
 */
import pal.misc.*;
import pal.datatype.*;
public interface RateMatrixGroup extends java.io.Serializable {

	public DataType getDataType();
	public double[] getEquilibriumFrequencies();

	/**
	 * Update internal representation based on any parameters (that are specified externally) and the given category probablitilies.
	 * The controller (user of this class) should be calling this method after the parameters of an external parameterization object have been set.
	 * @param categoryProbabilities the probabilities (summing to one) of each category (class) of substitution
	 */
	public void updateParameters(double[] categoryProbabilities);
	public int getNumberOfTransitionCategories();
	public void getTransitionProbabilities(double branchLength, double[][][] tableStore);
	public void getTransitionProbabilitiesTranspose(double branchLength, double[][][] tableStore);

	public void getTransitionProbabilities(double branchLength, int category, double[][] tableStore);
	public void getTransitionProbabilitiesTranspose(double branchLength, int category, double[][] tableStore);

	public String getSummary(double[] categoryProbabilities);

}