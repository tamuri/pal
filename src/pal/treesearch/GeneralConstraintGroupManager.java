// GnereralConstraintGroupManager.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: GeneralConstraintGroupManager </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.eval.*;
import pal.math.*;
import pal.misc.*;

public class GeneralConstraintGroupManager {
	private final ConstraintModel.GroupManager relatedGroup_;

	private GroupLeader[] leaders_ = null;

	private final Function allOptimisationFunction_;
	private final Function primaryOptimisationFunction_;
	private final Function secondaryOptimisationFunction_;

	private final MolecularClockLikelihoodModel.External external_;

  public GeneralConstraintGroupManager(ConstraintModel.GroupManager relatedGroup) {
		this.relatedGroup_ = relatedGroup;
		this.allOptimisationFunction_ = createFunction(relatedGroup.getAllGroupRelatedParameterAccess());
		this.primaryOptimisationFunction_ = createFunction(relatedGroup.getPrimaryGroupRelatedParameterAccess());
		this.secondaryOptimisationFunction_ = createFunction(relatedGroup.getSecondaryGroupRelatedParameterAccess());
		System.out.println("All:"+allOptimisationFunction_);
		System.out.println("Primary:"+allOptimisationFunction_);
		System.out.println("Secondary:"+allOptimisationFunction_);

		System.out.println("All:"+allOptimisationFunction_.getNumArguments());
		System.out.println("Primary:"+allOptimisationFunction_.getNumArguments());
		System.out.println("Secondary:"+allOptimisationFunction_.getNumArguments());

		this.external_ = relatedGroup.createNewClockExternal();
	}
	public boolean isOptimisable() {
	  return allOptimisationFunction_!=null&&allOptimisationFunction_.getNumArguments()>0;
	}
	public boolean isPrimaryOptimisable() {
	  return primaryOptimisationFunction_!=null&&primaryOptimisationFunction_.getNumArguments()>0;
	}
	public boolean isSecondarOptimisable() {
	  return secondaryOptimisationFunction_!=null&&secondaryOptimisationFunction_.getNumArguments()>0;
	}


	private static final Function createFunction(NeoParameterized parameterAccess) {
		if(parameterAccess!=null) {
			return new Function(  parameterAccess );
		} else {
			return null;
		}
	}
	public MolecularClockLikelihoodModel.External obtainConstrainedExternalCalculator() {
		return external_;
	}

	public ConstraintModel.GroupManager getRelatedGroup() { return relatedGroup_; }
// - - - - - - - - - - - - - - -- - - - - - - - - -- - - - - - - - - - - - - - - - - - - -



	private final void obtainLeafInformation(HeightInformationUser user) {
		for(int i = 0 ; i < leaders_.length ; i++) {
			leaders_[i].obtainLeafInformation(user);
		}
	}
	private final void postSetupNotify() {
		for(int i = 0 ; i < leaders_.length ; i++) {
			leaders_[i].postSetupNotify(relatedGroup_);
		}
	}


// - - - - - - - - - - - - - - -- - - - - - - - - -- - - - - - - - - - - - - - - - - - - -

	public void setup() {
		HeightInformationUser user = new HeightInformationUser();
		obtainLeafInformation(user);
		relatedGroup_.initialiseParameters(user.getLabels(), user.getHeights() );
		postSetupNotify();
	}



	public boolean isSameGroup(ConstraintModel.GroupManager queryGroup) {
		return queryGroup==relatedGroup_;
	}

	public void addGroupLeader(GroupLeader gl) {
		if(leaders_ == null) {
			leaders_ = new GroupLeader[] { gl };
		} else {
			GroupLeader[] newLeaders = new GroupLeader[leaders_.length+1];
			System.arraycopy(leaders_,0, newLeaders, 0, leaders_.length);
			newLeaders[leaders_.length] = gl;
			this.leaders_ = newLeaders;
		}
	}

	/**
	 * Optimise all the global clock parameters related to this group
	 * @param minimiser The minimiser used for optimisation
	 * @param scoreAccess A means for assessing a set of parameters
	 * @param fxFracDigits Accuracy for the likelihood
	 * @param xFracDigits Accruracy for the parameters
	 * @param rateMonitor A monitor for our progress
	 * @return the optimised log likelihood or >0 if cannot do any optimisation
	 */
	public final double optimiseAllGlobalClockConstraints(MultivariateMinimum minimiser, LikelihoodScoreAccess scoreAccess, int fxFracDigits, int xFracDigits,  MinimiserMonitor rateMonitor) {
		return optimiseGlobalClockConstraintsImpl(allOptimisationFunction_,minimiser,scoreAccess,fxFracDigits,xFracDigits,rateMonitor);
	}
	/**
	 * Optimise the global clock parameters marked as primary related to this group
	 * @param minimiser The minimiser used for optimisation
	 * @param scoreAccess A means for assessing a set of parameters
	 * @param fxFracDigits Accuracy for the likelihood
	 * @param xFracDigits Accruracy for the parameters
	 * @param rateMonitor A monitor for our progress
	 * @return the optimised log likelihood or >0 if cannot do any optimisation
	 */
	public final double optimisePrimaryGlobalClockConstraints(MultivariateMinimum minimiser, LikelihoodScoreAccess scoreAccess, int fxFracDigits, int xFracDigits,  MinimiserMonitor rateMonitor) {
		return optimiseGlobalClockConstraintsImpl(primaryOptimisationFunction_,minimiser,scoreAccess,fxFracDigits,xFracDigits,rateMonitor);
	}
	/**
	 * Optimise the global clock parameters marked as secondary related to this group
	 * @param minimiser The minimiser used for optimisation
	 * @param scoreAccess A means for assessing a set of parameters
	 * @param fxFracDigits Accuracy for the likelihood
	 * @param xFracDigits Accruracy for the parameters
	 * @param rateMonitor A monitor for our progress
	 * @return the optimised log likelihood or >0 if cannot do any optimisation
	 */
	public final double optimiseSecondaryGlobalClockConstraints(MultivariateMinimum minimiser, LikelihoodScoreAccess scoreAccess, int fxFracDigits, int xFracDigits,  MinimiserMonitor rateMonitor) {
		return optimiseGlobalClockConstraintsImpl(primaryOptimisationFunction_,minimiser,scoreAccess,fxFracDigits,xFracDigits,rateMonitor);
	}

	private final double optimiseGlobalClockConstraintsImpl(Function f, MultivariateMinimum minimiser, LikelihoodScoreAccess scoreAccess, int fxFracDigits, int xFracDigits,  MinimiserMonitor rateMonitor) {
		if(f!=null&&f.getNumArguments()>0) {
			f.setup( scoreAccess );
			final double[] arguments = f.getArgumentStore();
			double result = -minimiser.findMinimum( f, arguments, fxFracDigits, xFracDigits, rateMonitor );
//			System.out.println("Updating with:"+pal.misc.Utils.toString(arguments));
			f.updateCurrent(arguments);
			return result;
		}
		return 1;
	}

// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

	private final static class Function implements MultivariateFunction {
		private LikelihoodScoreAccess scoreAccess_;

		private final NeoParameterized parameterAccess_;
		private final double[] argumentStore_;
		public Function( NeoParameterized parameterAccess ) {
			this.parameterAccess_ = parameterAccess;
			this.argumentStore_ = new double[parameterAccess.getNumberOfParameters()];
		}
		public double[] getArgumentStore() {
			parameterAccess_.getParameters(argumentStore_,0);
			return argumentStore_;
		}
		public void updateCurrent(double[] arguments) {
			parameterAccess_.setParameters(arguments,0);
		}

		public void setup(LikelihoodScoreAccess scoreAccess) {
		  this.scoreAccess_ = scoreAccess;
		}
		public double evaluate(double[] argument) {
			if(scoreAccess_==null) {
			  throw new RuntimeException("Assertion error : rootAccess is null, need to call setup at some point!");
			}
			parameterAccess_.setParameters(argument,0);
			double result = -scoreAccess_.calculateLikelihoodScore();
//			System.out.println("Evaluating with :"+pal.misc.Utils.toString(argument)+"  = "+result);
		  return result;
		}
		public int getNumArguments() { return parameterAccess_.getNumberOfParameters(); }

		public double getLowerBound(int n) { return parameterAccess_.getLowerLimit(n); }
		public double getUpperBound(int n) { return parameterAccess_.getUpperLimit(n); }
		public OrthogonalHints getOrthogonalHints() { return null; }
	}

// ========================================================================================

	public static final class Store {

		private GeneralConstraintGroupManager[] constraintGroupManagers_ = new GeneralConstraintGroupManager[0];

		public Store() { }
		public final GeneralConstraintGroupManager[] getConstraintGroupManagers() { return constraintGroupManagers_; }

		public final GeneralConstraintGroupManager getConstraintGroupManager(String[] leafLabelSet, ConstraintModel constraints) {
			return getConstraintGroupManager(constraints.getGlobalClockConstraintGrouping(leafLabelSet));
		}
		public final GeneralConstraintGroupManager getConstraintGroupManager(ConstraintModel.GroupManager grouping) {
			if(constraintGroupManagers_.length==0) {
				GeneralConstraintGroupManager groupManager = new GeneralConstraintGroupManager(grouping);
				this.constraintGroupManagers_ = new GeneralConstraintGroupManager[] { groupManager };
				return groupManager;
			} else {
				for(int i = 0 ; i < constraintGroupManagers_.length ; i++) {
					if(constraintGroupManagers_[i].isSameGroup(grouping)) {
						return constraintGroupManagers_[i];
					}
				}
				GeneralConstraintGroupManager groupManager = new GeneralConstraintGroupManager(grouping);
				GeneralConstraintGroupManager[] newManagers = new GeneralConstraintGroupManager[constraintGroupManagers_.length+1];
				System.arraycopy(constraintGroupManagers_,0,newManagers,0,constraintGroupManagers_.length);
				newManagers[constraintGroupManagers_.length] = groupManager;
				this.constraintGroupManagers_ = newManagers;
				return groupManager;
			}
		}

		public void setupConstraintGroupManagers() {
			for( int i = 0; i<constraintGroupManagers_.length; i++ ) {
				constraintGroupManagers_[i].setup();
			}
		}
	}

	public static interface LikelihoodScoreAccess {
		/**
		 * The likelihood score doesn't exactly have to be the likelihood (well, it should be, but it may be the optimised likelihood)
		 * @return the likelihood score (should be negative)
		 */
	  public double calculateLikelihoodScore();
	}

}