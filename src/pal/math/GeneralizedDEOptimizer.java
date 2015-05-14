// GeneralizedDEOptimizer.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.math;


/**
 * Provides an general interface to the DifferentialEvolution class that is not
 * tied to a certain number of parameters (as DifferentialEvolution is). Works but
 * creating a new DiffentialEvolution engine when presented with a new number of
 * parameters. All the actual optimisation work is handled by DifferentialEvolution.,
 * @author Matthew Goode
 * @version $Id: GeneralizedDEOptimizer.java,v 1.8 2003/05/30 08:51:10 matt Exp $
 */


public class GeneralizedDEOptimizer extends MultivariateMinimum {

	private DifferentialEvolution optimiser_;

	private int currentNumberOfParameters_ = 0;
	/**
	 * A value of <1 means use default for given number of parameters
	 */
	private int populationSize_ = -1;

	public GeneralizedDEOptimizer() {
		this(-1);
	}
	public GeneralizedDEOptimizer(int populationSize) {
		this.populationSize_ = populationSize;
	}



	/**
	 * The actual optimization routine
	 * It finds a minimum close to vector x when the
	 * absolute tolerance for each parameter is specified.
				 *
	 * @param f multivariate function
	 * @param xvec initial guesses for the minimum
	 *         (contains the location of the minimum on return)
	 * @param tolfx absolute tolerance of function value
	 * @param tolx absolute tolerance of each parameter
	 */
	public void optimize(MultivariateFunction f, double[] xvec, double tolfx, double tolx)	{
		optimize(f,xvec,tolfx,tolx,null);
	}

	/**
	 * The actual optimization routine
	 * It finds a minimum close to vector x when the
	 * absolute tolerance for each parameter is specified.
				 *
	 * @param f multivariate function
	 * @param xvec initial guesses for the minimum
	 *         (contains the location of the minimum on return)
	 * @param tolfx absolute tolerance of function value
	 * @param tolx absolute tolerance of each parameter
	 */
	public void optimize(MultivariateFunction f, double[] xvec, double tolfx, double tolx, MinimiserMonitor monitor) {
		if(optimiser_==null||xvec.length!=currentNumberOfParameters_) {
			if(populationSize_>0) {
				optimiser_ = new DifferentialEvolution(xvec.length,populationSize_);
			} else {
				optimiser_ = new DifferentialEvolution(xvec.length);
			}
			this.currentNumberOfParameters_= xvec.length;
		}
		optimiser_.optimize(f,xvec,tolfx, tolx,monitor);
	}
	//============ Static Methods ====================
	/**
	 * Generate a MultivariateMinimum.Factory for an GeneralizedDEOptimiser with a set population size
	 * @param populationSize The set population size
	 */
	public static final Factory generateFactory(int populationSize) {	return new SearchFactory(populationSize);	}


	/**
	 * Generate a MultivariateMinimum.Factory for an GeneralizedDEOptimiser with a population size proportional to the size of the problem
	 */
	public static final Factory generateFactory() {	return new SearchFactory();	}

	// ============ The Factory Class for Orthogonal Searches ===================
	private static final class SearchFactory implements Factory {
		private final int populationSize_;
		private SearchFactory() {	this(-1);	}
		private SearchFactory(int populationSize) {	this.populationSize_ = populationSize;	}
		public MultivariateMinimum generateNewMinimiser() {
			if(populationSize_>0) {
				return new GeneralizedDEOptimizer(populationSize_);
			}
			return new GeneralizedDEOptimizer();
		}
	}

}
