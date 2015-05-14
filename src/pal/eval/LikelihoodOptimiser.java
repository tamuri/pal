// LikelihoodOptimiser.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: LikelihoodOptimiser </p>
 * <p>Description: A strange tool for optimizing the likelihood</p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.misc.Parameterized;
import pal.tree.Tree;
import pal.tree.ParameterizedTree;
import pal.alignment.Alignment;
import pal.substmodel.SubstitutionModel;
import pal.math.*;

public class LikelihoodOptimiser {
	private final Function function_;

	private final Tree tree_;
	private final Alignment alignment_;
	private final SubstitutionModel model_;

	private double[] argumentStore_ = null;
	public LikelihoodOptimiser(Tree tree, Alignment alignment, SubstitutionModel model) {
	  this.function_ = new Function(tree,alignment,model);
		this.tree_ = tree;
		this.alignment_ = alignment;
		this.model_ = model;
	}
	private final double[] setup(Parameterized parameters) {
	  function_.setParameters(parameters);
		argumentStore_ = function_.getCurrentArgumentStore(argumentStore_);
		return argumentStore_;
	}
	public double optimiseLogLikelihood(Parameterized parameters, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor monitor) {
		double result = -minimiser.findMinimum(function_,setup(parameters),fxFracDigits, xFracDigits,monitor);
		return result;
	}
	public double optimiseLogLikelihood(Parameterized parameters, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits) {
		double result = -minimiser.findMinimum(function_,setup(parameters),fxFracDigits, xFracDigits);
		return result;
	}
	/**
	* Optimise parameters to acheive maximum likelihood using a combined stategy. That is, model and tree are optimised concurrently.
	* @param tree The tree to be optimised (will be altered by optimisation)
	* @param alignment The alignment related to tree
	* @param model The substitution model to be optimised (will be altered by optimisation)
	* @param fxFracDigits The number of decimal placess to stabilise to in the log likelihood
	* @param xFracDigits The number of decimal placess to stabilise to in the model/tree parameters
	* @param minimiser The MultivariateMinimum object that is used for minimising
	* @param monitor A minimiser monitor to monitor progress
	* @return The maximal log likelihood found
	*/
	public static final double optimiseCombined(ParameterizedTree tree, Alignment alignment, SubstitutionModel model, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor monitor) {
	  final LikelihoodOptimiser lo = new LikelihoodOptimiser(tree,alignment,model);
		return lo.optimiseLogLikelihood(Parameterized.Utils.combine(tree,model),minimiser,fxFracDigits,xFracDigits,monitor);
	}
	/**
	* Optimise parameters to acheive maximum likelihood using a combined stategy. That is, model and tree are optimised concurrently.
	* @param tree The tree to be optimised (will be altered by optimisation)
	* @param alignment The alignment related to tree
	* @param model The substitution model to be optimised (will be altered by optimisation)
	* @param fxFracDigits The number of decimal placess to stabilise to in the log likelihood
	* @param xFracDigits The number of decimal placess to stabilise to in the model/tree parameters
	* @param minimiser The MultivariateMinimum object that is used for minimising
	* @return The maximal log likelihood found
	*/
	public static final double optimiseCombined(ParameterizedTree tree, Alignment alignment, SubstitutionModel model, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits) {
	  final LikelihoodOptimiser lo = new LikelihoodOptimiser(tree,alignment,model);
		return lo.optimiseLogLikelihood(Parameterized.Utils.combine(tree,model),minimiser,fxFracDigits,xFracDigits);
	}
	/**
	 * Optimise parameters to acheive maximum likelihood using an alternating stategy. That is first the model is optimised, than the tree branch lengths, then the model, then the tree, and so on until convergence.
		* @param tree The tree to be optimised (will be altered by optimisation)
		* @param alignment The alignment related to tree
		* @param model The substitution model to be optimised (will be altered by optimisation)
		* @param fxFracDigits The number of decimal placess to stabilise to in the log likelihood
		* @param xFracDigits The number of decimal placess to stabilise to in the model/tree parameters
		* @param minimiser The MultivariateMinimum object that is used for minimising
		* @return The maximal log likelihood found
		*/
	public static final double optimiseAlternate(ParameterizedTree tree, Alignment alignment, SubstitutionModel model, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits) {
		return optimiseAlternate(tree,alignment,model,minimiser,fxFracDigits,xFracDigits,null);
	}
	/**
	* Optimise parameters to acheive maximum likelihood using an alternating stategy. That is first the model is optimised, than the tree branch lengths, then the model, then the tree, and so on until convergence.
	* @param tree The tree to be optimised (will be altered by optimisation)
	* @param alignment The alignment related to tree
	* @param model The substitution model to be optimised (will be altered by optimisation)
	* @param fxFracDigits The number of decimal placess to stabilise to in the log likelihood
	* @param xFracDigits The number of decimal placess to stabilise to in the model/tree parameters
	* @param minimiser The MultivariateMinimum object that is used for minimising
	* @param monitor A minimiser monitor to monitor progress
	* @return The maximal log likelihood found
	*/
	public static final double optimiseAlternate(ParameterizedTree tree, Alignment alignment, SubstitutionModel model, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor monitor) {
	  final LikelihoodOptimiser lo = new LikelihoodOptimiser(tree,alignment,model);
		double epsilon = generateEpsilon(fxFracDigits);
		double lastResult = 0;
		double result = 0;
		int round = 0;
		while(true) {
		  //Optimise Model
			result = optimise(lo, model,minimiser,fxFracDigits,xFracDigits,monitor);
			//Optimiser tree
			result = optimise(lo, tree,minimiser,fxFracDigits,xFracDigits,monitor);
			if(round>0) {
				if( lastResult>result ) {
					break;
				}
				if( result-lastResult<epsilon ) {
					break;
				}
			}
		  round++;
			lastResult = result;
		}
		return lastResult;
	}
	private static final double optimise(LikelihoodOptimiser lo,Parameterized parameters, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor monitor) {
		if( monitor==null ) {
			return lo.optimiseLogLikelihood(parameters,minimiser,fxFracDigits,xFracDigits);
		} else {
			return lo.optimiseLogLikelihood(parameters,minimiser,fxFracDigits,xFracDigits,monitor);
		}
	}
	/**
	* Optimise tree branchlengths only to acheive maximum likelihood using a combined stategy.
	* @param tree The tree to be optimised (will be altered by optimisation)
	* @param alignment The alignment related to tree
	* @param model The substitution model to be optimised (will *not * be altered by optimisation)
	* @param fxFracDigits The number of decimal placess to stabilise to in the log likelihood
	* @param xFracDigits The number of decimal placess to stabilise to in the model/tree parameters
	* @param minimiser The MultivariateMinimum object that is used for minimising
	* @return The maximal log likelihood found
	*/
	public static final double optimiseTree(ParameterizedTree tree, Alignment alignment, SubstitutionModel model, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits) {
	  final LikelihoodOptimiser lo = new LikelihoodOptimiser(tree,alignment,model);
		return lo.optimiseLogLikelihood(tree, minimiser,fxFracDigits,xFracDigits);
	}
	/**
	* Optimise tree branchlengths only to acheive maximum likelihood using a combined stategy.
	* @param tree The tree to be optimised (will be altered by optimisation)
	* @param alignment The alignment related to tree
	* @param model The substitution model to be optimised (will *not * be altered by optimisation)
	* @param fxFracDigits The number of decimal placess to stabilise to in the log likelihood
	* @param xFracDigits The number of decimal placess to stabilise to in the model/tree parameters
	* @param minimiser The MultivariateMinimum object that is used for minimising
	* @param monitor A minimiser monitor to monitor progress
	* @return The maximal log likelihood found
	*/
	public static final double optimiseTree(ParameterizedTree tree, Alignment alignment, SubstitutionModel model, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor monitor) {
	  final LikelihoodOptimiser lo = new LikelihoodOptimiser(tree,alignment,model);
		return lo.optimiseLogLikelihood(tree, minimiser,fxFracDigits,xFracDigits,monitor);
	}
	/**
	* Optimise model parameters only to acheive maximum likelihood using a combined stategy.
	* @param tree The tree to be optimised (will *not* be  altered by optimisation)
	* @param alignment The alignment related to tree
	* @param model The substitution model to be optimised (will be altered by optimisation)
	* @param fxFracDigits The number of decimal placess to stabilise to in the log likelihood
	* @param xFracDigits The number of decimal placess to stabilise to in the model/tree parameters
	* @param minimiser The MultivariateMinimum object that is used for minimising
	* @param monitor A minimiser monitor to monitor progress
	* @return The maximal log likelihood found
	*/
	public static final double optimiseModel(Tree tree, Alignment alignment, SubstitutionModel model, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor monitor) {
	  final LikelihoodOptimiser lo = new LikelihoodOptimiser(tree,alignment,model);
		return lo.optimiseLogLikelihood(model, minimiser,fxFracDigits,xFracDigits,monitor);
	}

	private static final double generateEpsilon(int fracDigits) {
	  double x = 1;
		for(int i = 0 ; i < fracDigits ; i++) {
		  x/=10.0;
		}
		return x;
	}

// -==================================================================================================-
// Function Class //
	private static final class Function implements MultivariateFunction {

		private final GeneralLikelihoodCalculator likelihoodFunction_;

		private Parameterized parameters_;
		private OrthogonalHints hints_;
		private int numberOfParameters_;
		private double[] lowerLimits_ = null;
		private double[] upperLimits_ = null;

		public Function(Tree tree, Alignment alignment, SubstitutionModel model) {
		  this.likelihoodFunction_ = new GeneralLikelihoodCalculator(alignment,tree,model);
		}
		public final void setParameters(Parameterized p) {
			setParameters(p,null);
		}
		public final void setParameters(Parameterized p, OrthogonalHints hints) {
		  this.parameters_ = p;
			this.hints_ = hints;
			this.numberOfParameters_ = p.getNumParameters();
			lowerLimits_ = check(lowerLimits_,numberOfParameters_);
			upperLimits_ = check(upperLimits_,numberOfParameters_);
			for(int i = 0 ; i < numberOfParameters_ ; i++) {
			  lowerLimits_[i] = p.getLowerLimit(i);
				upperLimits_[i] = p.getUpperLimit(i);
			}
		}
		public final double[] getCurrentArgumentStore(double[] current) {
		  current = check(current,numberOfParameters_);
			for(int i = 0 ; i < numberOfParameters_ ; i++) {
			  current[i] = parameters_.getParameter(i);
			}
			return current;
		}
		public final double[] getDefaultArgumentStore(double[] current) {
		  current = check(current,numberOfParameters_);
			for(int i = 0 ; i < numberOfParameters_ ; i++) {
			  current[i] = parameters_.getDefaultValue(i);
			}
			return current;
		}

		private final double[] check(double[] array, int size) {
		  if(array==null||array.length<size) {
			  return new double[size];
			}
			return array;

		}
		public double evaluate(double[] argument) {
		  for(int i = 0 ; i < numberOfParameters_ ; i++) {
			  parameters_.setParameter(argument[i],i);
			}
			return -likelihoodFunction_.calculateLogLikelihood();
		}
		public final int getNumArguments() { return numberOfParameters_; }

	  public final double getLowerBound(int n) { return lowerLimits_[n]; }

		public final double getUpperBound(int n) { return upperLimits_[n]; }

	  public final OrthogonalHints getOrthogonalHints() { return hints_; }
	}
}