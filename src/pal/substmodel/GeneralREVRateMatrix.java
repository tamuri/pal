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
 * A general rate matrix class for REV style rate matrices (GTR but for all data types)
 * Includes the ability for arbitarily constraints
 *
 * @author Matthew Goode
 * <ul>
 *  <li> 11 May 2004 - Created file - will add parameter decoding and reporting later... </li>
 * </ul>
 */
public class GeneralREVRateMatrix implements NeoRateMatrix {
	private final int dimension_;
	private final int[][] parameterChecks_;
	private final int[] constraints_;
	private final int numberOfParameters_;
	private final int numberOfEffectiveParameters_;
	private final double[] defaultParameters_;
	/**
	 * The general constructor for a fully specified REV model
	 * @param dimension the dimension of the data type
	 */
	public GeneralREVRateMatrix(int dimension) {
		this(dimension,createDefaultConstraints(dimension),null);
	}
	/**
	 * The general constructor for a fully specified REV model
	 * @param dimension the dimension of the data type
	 * @param specifiedDefaultParameters the defaultParameters (potentially used as the starting parameters by a SubstitutionModel)
	 */
	public GeneralREVRateMatrix(int dimension, double[] specifiedDefaultParameters) {
		this(dimension,createDefaultConstraints(dimension),specifiedDefaultParameters);
	}
	/**
	 * The general constructor
	 * <br>Constraint ordering example, for nucleotide data
	 * <code>
	 * -> + A C G T
	 *    A * 0 1 2
	 *    C * * 3 4
	 *    G * * * 5
	 *    T * * * *
	 * </code>
	 * if constraints were {0,1,1,0,0,1} then would be constrained so a-c = c-g = c-t  and a-g = a-t = g-t (and there would be only one parameter)
	 * @param dimension the dimension of the data type
	 * @param constraints the contraints, organised such that if constraints[i]==constraints[j] then transitions i and j will always be the same. The constraints are ordered like usual.
	*
	 */
	public GeneralREVRateMatrix(int dimension, int[] constraints) {
		this(dimension,constraints,null);
	}
	/**
	 * The general constructor
	 * <br>Constraint ordering example, for nucleotide data
	 * <code>
	 * -> + A C G T
	 *    A * 0 1 2
	 *    C * * 3 4
	 *    G * * * 5
	 *    T * * * *
	 * </code>
	 * if constraints were {0,1,1,0,0,1} then would be constrained so a-c = c-g = c-t  and a-g = a-t = g-t (and there would be only one parameter)
	 * @param dimension the dimension of the data type
	 * @param constraints the contraints, organised such that if constraints[i]==constraints[j] then transitions i and j will always be the same. The constraints are ordered like usual. The last constrained item is fixed at 1.
	 * @param specifiedDefaultParameters the defaultParameters (potentially used as the starting parameters by a SubstitutionModel)
	 *
	 */
	public GeneralREVRateMatrix(int dimension, int[] constraints, double[] specifiedDefaultParameters) {
		this(dimension,constraints,specifiedDefaultParameters,-1);
	}
	/**
	 * The general constructor
	 * <br>Constraint ordering example, for nucleotide data
	 * <code>
	 * -> + A C G T
	 *    A * 0 1 2
	 *    C * * 3 4
	 *    G * * * 5
	 *    T * * * *
	 * </code>
	 * if constraints were {0,1,1,0,0,1} then would be constrained so a-c = c-g = c-t  and a-g = a-t = g-t (and there would be only one parameter)
	 * @param dimension the dimension of the data type
	 * @param constraints the contraints, organised such that if constraints[i]==constraints[j] then transitions i and j will always be the same. The constraints are ordered like usual.
	 * @param specifiedDefaultParameters the defaultParameters (potentially used as the starting parameters by a SubstitutionModel)
	 * @param fixedConstraintValue the value of the constraint (in the constraints array) of the fixed constraint (that is, for which all related parts of the rate matrix are set to 1)
	 *
	 */
	 public GeneralREVRateMatrix(int dimension, int[] constraints, double[] specifiedDefaultParameters, int fixedConstraintValue) {
		this.dimension_ = dimension;
		this.constraints_ = pal.misc.Utils.getCopy(constraints);
		int[] parameterContraintMatchup = new int[constraints.length];
		int numberOfParametersFound = 0;

		this.parameterChecks_ = new int[dimension][dimension];
		int constraintIndex = 0;
		int fixedParameter = -1;
		for(int from = 0 ; from < dimension ; from++) {
			for(int to = from+1; to < dimension ; to++) {
				int constraintValue = (constraints==null || constraintIndex>=constraints.length ? constraintIndex++ : constraints[constraintIndex++]);
				int parameter = -1;
				for(int i = 0 ; i < numberOfParametersFound ; i++) {
				  if(parameterContraintMatchup[i]==constraintValue) {
					  parameter = i;
					}
				}
				if(parameter<0) {
				  parameter = numberOfParametersFound++;
					if(fixedConstraintValue==constraintValue) {
					  fixedParameter = parameter;
					}
					parameterContraintMatchup[parameter] = constraintValue;
				}
				parameterChecks_[from][to] = parameter;
			}
		}
		this.numberOfParameters_ = numberOfParametersFound;
		this.numberOfEffectiveParameters_ = numberOfParametersFound-1;
		System.out.println("Fixed parameter:"+fixedParameter);

		if(fixedParameter>=0&&fixedParameter!=numberOfEffectiveParameters_) {
		  for(int from = 0 ; from < dimension ; from++) {
	  		for(int to = from+1; to < dimension ; to++) {
					int p = parameterChecks_[from][to];
					if(p==numberOfEffectiveParameters_) {
					  parameterChecks_[from][to] = fixedParameter;
					} else if(p==fixedParameter) {
					  parameterChecks_[from][to] = numberOfEffectiveParameters_;
					}
	  		}
			}
		}
		System.out.println("Number of effective parameters:"+numberOfEffectiveParameters_);
		this.defaultParameters_ = new double[numberOfEffectiveParameters_];
		if(specifiedDefaultParameters==null) {
		  for(int i = 0 ; i < numberOfEffectiveParameters_ ; i++) {
			  defaultParameters_[i] = 1;
			}
		} else {
		  System.arraycopy(specifiedDefaultParameters,0,defaultParameters_,0,numberOfEffectiveParameters_);
		}

		System.out.println("Number of effective parameters:"+numberOfEffectiveParameters_);
	}
	private static final int[] createDefaultConstraints(int dimension) {
	  int[] result = new int[dimension*(dimension-1)/2];
		for(int i = 0 ; i < result.length ;i++) {
		  result[i] = i;
		}
		return result;
	}
	public String getUniqueName() { return "General REV (dimension "+dimension_+")"; }

	/**
	 * @return true
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

	public void createRelativeRates(double[][] rateStore, double[] rateParameters, int startIndex) {
	  for(int from = 0 ; from < dimension_ ; from++) {
			rateStore[from][from] = 0;
		  for(int to = from+1 ; to < dimension_ ; to++) {
				int parameterIndex = parameterChecks_[from][to];
				final double value;
				if(parameterIndex==numberOfEffectiveParameters_) {
				  value = 1;
				} else {
				  value = rateParameters[parameterIndex+startIndex];
				}
				rateStore[from][to] = rateStore[to][from] = value;
		  }
		}
	}

	public int getNumberOfRateParameters() { return numberOfEffectiveParameters_; }

	public double getRateParameterLowerBound(int parameter) { return 0; }
	public double getRateParameterUpperBound(int parameter) { return 100000; }
	public void getDefaultRateParameters(double[] store, int startIndex) {
		System.arraycopy(defaultParameters_,0,store,startIndex,defaultParameters_.length);
	}
// ============================================================================================
// === Utility Methods
	/**
	 * Create a rate matrix equivalent to the GTR model
	 * @return appropriate rate matrix
	 */
	public static final GeneralREVRateMatrix createGTR() { return new GeneralREVRateMatrix(4); }
	/**
	 * Create a rate matrix equivalent to the GTR model
	 * @param defaultParameters the default parameters of the model
	 * @return appropriate rate matrix
	 */
	public static final GeneralREVRateMatrix createGTR(double[] defaultParameters) { return new GeneralREVRateMatrix(4,defaultParameters); }
	/**
	 * Create a rate matrix equivalent to the GTR model
	 * Parameters laid out
	 *  * <code>
	 * -> + A C G T
	 *    A * a b c
	 *    C * * d e
	 *    G * * * 1
	 *    T * * * *
	 * </code>
	 * @param a the default a parameter of the model
	 * @param b the default a parameter of the model
	 * @param c the default a parameter of the model
	 * @param d the default a parameter of the model
	 * @param e the default a parameter of the model
	 * @return appropriate rate matrix
	 */
	public static final GeneralREVRateMatrix createGTR(double a, double b, double c, double d, double e) {
		return new GeneralREVRateMatrix(4,new double[] { a,b,c,d,e} );
	}
	/**
	 * Create a rate matrix equivalent to the HKY model, the one parameter will be kappa
	 * @return appropriate rate matrix
	 */
	public static final GeneralREVRateMatrix createHKY() {
		return createHKY(2);
	}
	/**
	 * Create a rate matrix equivalent to the HKY model, the one parameter will be kappa
	 * @param defaultKappa the default kappa value
	 * @return appropriate rate matrix
	 */
	public static final GeneralREVRateMatrix createHKY(double defaultKappa) {
		return new GeneralREVRateMatrix(4,new int[] {0, 1, 0, 0, 1, 0},new double[] { defaultKappa },0);
	}
}
