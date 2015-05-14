// GeneralLikelihoodSearcher.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: GeneralLikelihoodSearcher</p>
 * <p>Description: A new improved likelihood framework that is very powerful, allowing complex constraints on the tree and complex models of evolution.</p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.algorithmics.*;
import pal.alignment.*;
import pal.math.*;
import pal.misc.*;
import pal.tree.*;
import pal.util.*;

public class GeneralLikelihoodSearcher {
	public static final int OPTIMISE_ALL = 0;
	public static final int OPTIMISE_PRIMARY = 1;
	public static final int OPTIMISE_SECONDARY = 2;
	public static final int NO_OPTIMISE = 3;

	private final ConstraintModel constraintModel_;
	private final RootAccess rootAccess_;
	private final GeneralConstraintGroupManager[] constraintGroupManagers_;
	private final GeneralConstructionTool tool_;
	private final ModelFunction modelFunction_;
	private final RootAccessScorer rootAccessScorer_;

	private final GeneralOptimiser generalOptimiser_;

	private final static boolean FREQUENT_SEARCH_UPDATES = true;

  public GeneralLikelihoodSearcher(Node baseTopology, Alignment baseAlignment, ConstraintModel constraintModel) {
		this.constraintModel_ = constraintModel;
		this.tool_ = new GeneralConstructionTool(constraintModel,baseAlignment);
		GeneralConstraintGroupManager.Store store = new GeneralConstraintGroupManager.Store();
		this.rootAccess_ = tool_.createRootAccess(baseTopology,store);
		store.setupConstraintGroupManagers();
		this.constraintGroupManagers_ = store.getConstraintGroupManagers();
		NeoParameterized np = constraintModel.getGlobalParameterAccess();
		if(np!=null&&np.getNumberOfParameters()>0) {
		  this.modelFunction_ = new ModelFunction(np,rootAccess_,tool_);
		} else {
		  this.modelFunction_ = null;
		}
		this.generalOptimiser_ = new GeneralOptimiser(tool_,rootAccess_);
		this.rootAccessScorer_ = new RootAccessScorer(rootAccess_,tool_);
//		System.out.println("*****************************************************");
//		testLikelihood();
//		System.out.println("*****************************************************");
	}
	public double optimiseGeneral(StoppingCriteria stopper, int fracDigits, AlgorithmCallback callback) {
		return optimiseGeneral(stopper,fracDigits, callback, null);
	}

	public double optimiseGeneral(StoppingCriteria stopper, int fracDigits, AlgorithmCallback callback, SearchMonitor monitor) {
		stopper.reset();
		double logLikelihood = 0;
		do {
		  logLikelihood = generalOptimiser_.generalOptimiseRound(fracDigits, (FREQUENT_SEARCH_UPDATES ? monitor : null));
			if(monitor!=null) {
			  monitor.searchStepComplete(logLikelihood);
			}
			callback.updateStatus("Round likelihood:"+logLikelihood);
//			System.out.println("Round likelihood:"+logLikelihood+"  check:"+calculatedLogLikelihood());

			stopper.newIteration(logLikelihood,logLikelihood,true,true,callback);
		} while (!stopper.isTimeToStop()&&!callback.isPleaseStop());
		return logLikelihood;
	}
	public double optimiseConstraintRateModels(MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor rateMonitor) {
		return optimiseConstraintRateModels(minimiser,fxFracDigits,xFracDigits,rootAccessScorer_,rateMonitor);
	}
	private double optimiseConstraintRateModels(MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, GeneralConstraintGroupManager.LikelihoodScoreAccess scoreAccess, MinimiserMonitor rateMonitor) {
	  return optimiseConstraintRateModels(minimiser, fxFracDigits, xFracDigits, scoreAccess, rateMonitor, OPTIMISE_ALL);
	}
	private double optimiseConstraintRateModels(MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, GeneralConstraintGroupManager.LikelihoodScoreAccess scoreAccess, MinimiserMonitor rateMonitor, int optimisationType) {
		double logLikelihood = 1;
		for(int i = 0 ; i < constraintGroupManagers_.length ; i++) {

		  final double l;
		  switch(optimisationType) {
				case OPTIMISE_ALL: {
					l = constraintGroupManagers_[i].optimiseAllGlobalClockConstraints( minimiser, scoreAccess, fxFracDigits, xFracDigits, rateMonitor );
					break;
				}
				case OPTIMISE_PRIMARY: {
					l = constraintGroupManagers_[i].optimisePrimaryGlobalClockConstraints( minimiser, scoreAccess, fxFracDigits, xFracDigits, rateMonitor );
					break;
				}
				case OPTIMISE_SECONDARY: {
					l = constraintGroupManagers_[i].optimiseSecondaryGlobalClockConstraints( minimiser, scoreAccess, fxFracDigits, xFracDigits, rateMonitor );
					break;
				}
				case NO_OPTIMISE : {	  l = 1;		break;			}
				default : {		  throw new IllegalArgumentException("Unknown optimisation type:"+optimisationType);		}
			}
		 if(l<0) {  logLikelihood = l;	}
		}
		return logLikelihood;
	}

	private final boolean isAnyConstraingGroupOptimisable() {
	  for(int i = 0 ; i < constraintGroupManagers_.length ; i++) {
			if(constraintGroupManagers_[i].isOptimisable()) {
				return true;
			}
	  }
		return false;
	}
	public double optimiseSubstitutionModels(MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits, MinimiserMonitor monitor) {
		double logLikelihood = 1;
		if(modelFunction_!=null) {
		  logLikelihood = -minimiser.findMinimum(modelFunction_,modelFunction_.getArgumentStore(),fxFracDigits,xFracDigits,monitor);
		  modelFunction_.synchronizeModel();
		}

		return logLikelihood;
	}
	public double optimiseAllSimple(StoppingCriteria stopper, MultivariateMinimum rateMinimiser, int fxFracDigits, int xFracDigits, AlgorithmCallback callback) {
	  return optimiseAllSimple(stopper,rateMinimiser,fxFracDigits,xFracDigits,callback,null,null);
	}
	public double optimiseAllSimple(StoppingCriteria stopper, MultivariateMinimum rateMinimiser, int fxFracDigits, int xFracDigits, AlgorithmCallback callback, SearchMonitor monitor, MinimiserMonitor rateMonitor) {
		return optimiseAllSimple(stopper, rateMinimiser, fxFracDigits, xFracDigits, callback, monitor, rateMonitor, OPTIMISE_ALL);
	}
	public double optimiseAllSimple(StoppingCriteria stopper, MultivariateMinimum rateMinimiser, int fxFracDigits, int xFracDigits, AlgorithmCallback callback, SearchMonitor monitor, MinimiserMonitor rateMonitor, int groupOptimistionType) {
		stopper.reset();
		double logLikelihood = 0;
		boolean foundResult;
		do {
			foundResult = false;
		  double roundValue;
			roundValue = generalOptimiser_.generalOptimiseRound(fxFracDigits, (FREQUENT_SEARCH_UPDATES ? monitor : null));
			callback.updateStatus("Round likelihood:"+roundValue);
			if(roundValue<0) {
				logLikelihood = roundValue; foundResult = true;
				if(monitor!=null) { monitor.searchStepComplete(logLikelihood);  }
			}
//			System.out.println("Round likelihood(1):"+logLikelihood);
//			System.out.println("Round likelihood (1):"+roundValue+"  check:"+calculatedLogLikelihood());
		  if(constraintGroupManagers_.length > 0) {
				roundValue = optimiseConstraintRateModels( rateMinimiser, fxFracDigits, xFracDigits, rootAccessScorer_, rateMonitor,groupOptimistionType );
			}
//			System.out.println("Round likelihood (2):"+roundValue+"  check:"+calculatedLogLikelihood());
			if(roundValue<0) {
				logLikelihood = roundValue;	foundResult = true;
				if(monitor!=null) { monitor.searchStepComplete(logLikelihood);  }
			}
			stopper.newIteration(logLikelihood,logLikelihood,true,true,callback);
		} while (foundResult&&!stopper.isTimeToStop());
		return logLikelihood;
	}
	public double optimiseAllSimpleHeirarchy(StoppingCriteria stopper, MultivariateMinimum rateMinimiser, int fxFracDigits, int xFracDigits, AlgorithmCallback callback, SearchMonitor monitor, MinimiserMonitor rateMonitor) {
		if(!isAnyConstraingGroupOptimisable()) {
			return optimiseGeneral(stopper,fxFracDigits,callback,monitor);
		}
		stopper.reset();
		double logLikelihood = 0;
		boolean foundResult;
		GeneralRoundScorer grs = new GeneralRoundScorer(generalOptimiser_, fxFracDigits, monitor);
		do {
			foundResult = false;
		  double roundValue = optimiseConstraintRateModels( rateMinimiser, fxFracDigits, xFracDigits, grs, rateMonitor );

			if(roundValue<0) {
				logLikelihood = roundValue;	foundResult = true;
				stopper.newIteration(logLikelihood,logLikelihood,true,true,callback);
				if(monitor!=null) { monitor.searchStepComplete(logLikelihood);  }
			}
		} while (foundResult&&!stopper.isTimeToStop());
		return logLikelihood;
	}
	public double optimiseAllFullHeirarchy(StoppingCriteria mainStopper, StoppingCriteria subStopper, MultivariateMinimum rateMinimiser, int fxFracDigits, int xFracDigits, AlgorithmCallback callback, SearchMonitor monitor, MinimiserMonitor rateMonitor) {

		if(!isAnyConstraingGroupOptimisable()) {	return optimiseGeneral(mainStopper,fxFracDigits,callback,monitor);	}
		mainStopper.reset();
		double logLikelihood = 0;
		boolean foundResult;
		System.out.println("Optimising heirachy");
		FullGeneralRoundScorer grs = new FullGeneralRoundScorer(rateMinimiser, fxFracDigits, xFracDigits, monitor,subStopper, callback,rateMonitor,OPTIMISE_SECONDARY);
		do {
			if(callback.isPleaseStop()) { return 0; }
			foundResult = false;
		  double roundValue = optimiseConstraintRateModels( rateMinimiser, fxFracDigits, xFracDigits, grs, rateMonitor,OPTIMISE_PRIMARY );

			if(roundValue<0) {
				logLikelihood = roundValue;	foundResult = true;
				mainStopper.newIteration(logLikelihood,logLikelihood,true,true,callback);
				if(monitor!=null) { monitor.searchStepComplete(logLikelihood);  }
			}

		} while (foundResult&&!mainStopper.isTimeToStop()&&!callback.isPleaseStop());
		return logLikelihood;
	}
	public double optimiseAllPlusSubstitutionModel(StoppingCriteria stopper, MultivariateMinimum rateMinimiser, MultivariateMinimum substitutionModelMinimiser, int fxFracDigits, int xFracDigits, AlgorithmCallback callback, SearchMonitor monitor, int substitutionModelOptimiseFrequency, MinimiserMonitor substitutionModelMonitor, MinimiserMonitor rateMonitor) {
		stopper.reset();
		double logLikelihood = 0;
		boolean foundResult;
		int substitutionModelCount = substitutionModelOptimiseFrequency;
		boolean justOptimisedModel = false;
		do {
			foundResult = false;
		  double roundValue;
			roundValue = generalOptimiser_.generalOptimiseRound(fxFracDigits, (FREQUENT_SEARCH_UPDATES ? monitor : null));
			callback.updateStatus("Round likelihood:"+roundValue);
			if(roundValue<0) {
				logLikelihood = roundValue; foundResult = true;
				if(monitor!=null) { monitor.searchStepComplete(logLikelihood);  }
			}
			roundValue = optimiseConstraintRateModels(rateMinimiser,fxFracDigits,xFracDigits,rateMonitor);
			if(roundValue<0) {
				logLikelihood = roundValue;	foundResult = true;
				if(monitor!=null) { monitor.searchStepComplete(logLikelihood);  }
			}
			if(substitutionModelCount==0) {
			  roundValue = optimiseSubstitutionModels(substitutionModelMinimiser,fxFracDigits,xFracDigits,substitutionModelMonitor);
				if(roundValue<0) {
				  logLikelihood = roundValue;	foundResult = true;
				}
				justOptimisedModel = true;
				substitutionModelCount = substitutionModelOptimiseFrequency;
			} else {
				if(stopper.isTimeToStop()) {
				  substitutionModelCount=0;
					//Make sure next round we optimise the model!
				} else {
					substitutionModelCount--;
				}
				justOptimisedModel = false;
			}
			stopper.newIteration(logLikelihood,logLikelihood,true,true,callback);

		} while (foundResult&&!callback.isPleaseStop()&&(!stopper.isTimeToStop()||!justOptimisedModel));
		return logLikelihood;
	}
	public Node buildPALNodeBase() { return rootAccess_.buildPALNodeBase(); }
	public Tree buildPALTreeBase() {
		SimpleTree st = new SimpleTree(rootAccess_.buildPALNodeBase());
		st.setUnits(Units.EXPECTED_SUBSTITUTIONS);
		return st;
	}
	public Node buildPALNodeES() { return rootAccess_.buildPALNodeES(); }
	public Tree buildPALTreeES() {
		SimpleTree st = new SimpleTree(rootAccess_.buildPALNodeES());
		st.setUnits(Units.EXPECTED_SUBSTITUTIONS);
		return st;
	}


	public double calculatedLogLikelihood() {
		return rootAccess_.calculateLogLikelihood(tool_);
	}
	public void testLikelihood() {
		rootAccess_.testLikelihood(tool_);
	}
// =============================================================================================
// ====================== Model Function =======================================================
	private final static class ModelFunction implements MultivariateFunction {
	  private final NeoParameterized parameters_;
		private final RootAccess rootAccess_;
		private final GeneralConstructionTool tool_;
		private final double[] argumentStore_;
		public ModelFunction(NeoParameterized parameters, RootAccess rootAccess, GeneralConstructionTool tool) {
		  this.parameters_ = parameters;
			this.rootAccess_ = rootAccess;
			this.tool_ = tool;
			this.argumentStore_ = new double[parameters.getNumberOfParameters()];
		}
		public double[] getArgumentStore() {
			parameters_.getParameters(argumentStore_,0);
		  return argumentStore_;
		}
		public void synchronizeModel() {
			parameters_.setParameters(argumentStore_,0);
		}
	  public double evaluate(double[] argument) {
		  parameters_.setParameters(argument,0);
			return -rootAccess_.calculateLogLikelihood(tool_);
		}
		public int getNumArguments() { return parameters_.getNumberOfParameters(); }
		public double getLowerBound(int n) {  return parameters_.getLowerLimit(n); }
	  public double getUpperBound(int n) {  return parameters_.getUpperLimit(n);  }
		public OrthogonalHints getOrthogonalHints() { return null; }
	}
	private final class GeneralOptimiser {

		private final OptimisationHandler[] optimisations_;
		private final MersenneTwisterFast random_;
		private final UnivariateMinimum minimiser_;
		private final GeneralConstructionTool tool_;

		public GeneralOptimiser(GeneralConstructionTool tool, RootAccess rootAccess) {
	  	ArrayList al = new ArrayList();
		  rootAccess.getAllComponents(al,GeneralOptimisable.class);
		  GeneralOptimisable[] generalOptimisables = new GeneralOptimisable[al.size()];
		  al.toArray(generalOptimisables);
			al.clear();
			for(int i = 0 ; i < generalOptimisables.length ; i++) {
		    int numberOfTypes = generalOptimisables[i].getNumberOfOptimisationTypes();
//				int numberOfTypes =1 ;

				for(int j = 0 ; j < numberOfTypes ; j++) {
					al.add(new OptimisationHandler(generalOptimisables[i],j));
				}
			}
			optimisations_ = new OptimisationHandler[al.size()];
		  al.toArray(optimisations_);

			this.random_ = new MersenneTwisterFast();
			this.tool_ = tool;

			this.minimiser_ = new UnivariateMinimum();
		}
		public boolean isHasOptimisations() { return optimisations_.length>0; }

		public double generalOptimiseRound(int fracDigits, SearchMonitor searchMonitor) {
		  random_.shuffle(optimisations_);
			double logLikelihood = 1;
//			System.out.println("Optimisations:"+optimisations_.length);
			for(int i = 0 ; i < optimisations_.length ; i++) {
				double l = optimisations_[i].optimise(minimiser_,tool_,fracDigits);
				if(l<0) {
//					if(l<logLikelihood) {
//				    System.out.println("OptimisationHandler:"+optimisations_[i]+":"+l+"  ("+calculatedLogLikelihood()+")");
//					}
//				  testLikelihood();
					logLikelihood = l;
					if(searchMonitor!=null) {
					  searchMonitor.searchStepComplete(logLikelihood);
//						System.out.println("Root:"+rootAccess_);
//						testLikelihood();
//						System.out.println("***************************************");
					}
//					System.out.println("Optimised General:"+logLikelihood);
				}

			}
			return logLikelihood;
	  }
		// ====================
		private final class OptimisationHandler {
			private final GeneralOptimisable baseOptimisation_;
			private final int optimisationType_;
			public OptimisationHandler(GeneralOptimisable baseOptimisation, int optimisationType) {
				this.baseOptimisation_ = baseOptimisation;
				this.optimisationType_ = optimisationType;
			}
			public double optimise(UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
				return baseOptimisation_.optimise(optimisationType_, minimiser,tool,fracDigits);
			}
			public String toString() {
			  return "("+optimisationType_+", "+baseOptimisation_+")";
			}
		}

	}
	private static final class RootAccessScorer implements GeneralConstraintGroupManager.LikelihoodScoreAccess {
	  private final RootAccess rootAccess_;
		private final GeneralConstructionTool tool_;
		public RootAccessScorer(RootAccess rootAccess, GeneralConstructionTool tool) {
		  this.rootAccess_ = rootAccess;
			this.tool_ = tool;
		}
		public double calculateLikelihoodScore() {
		  return rootAccess_.calculateLogLikelihood(tool_);
		}
	}
	private static final class GeneralRoundScorer implements GeneralConstraintGroupManager.LikelihoodScoreAccess {
	  private final GeneralOptimiser optimisation_;
		private final int fracDigits_;
		private final SearchMonitor monitor_;
//		private final GeneralConstructionTool tool_;
		public GeneralRoundScorer(GeneralOptimiser optimisation, int fracDigits, SearchMonitor monitor) {
		  this.optimisation_ = optimisation;
			this.fracDigits_ = fracDigits;
			this.monitor_ = monitor;
//			this.tool_ = tool;
		}
		public double calculateLikelihoodScore() {
		  return optimisation_.generalOptimiseRound(fracDigits_,monitor_);
		}
	}
	private final class FullGeneralRoundScorer implements GeneralConstraintGroupManager.LikelihoodScoreAccess {
	  private final int fxFracDigits_;
		private final int xFracDigits_;
		private final SearchMonitor monitor_;
		private final StoppingCriteria stopper_;
		private final AlgorithmCallback callback_;
		private final int groupOptimisationType_;
		private final MultivariateMinimum rateMinimiser_;
		private final MinimiserMonitor rateMonitor_;
//		private final GeneralConstructionTool tool_;

		public FullGeneralRoundScorer(MultivariateMinimum rateMinimiser, int fxFracDigits, int xFracDigits, SearchMonitor monitor, StoppingCriteria stopper, AlgorithmCallback callback, MinimiserMonitor rateMonitor, int groupOptimisationType) {
		  this.stopper_ = stopper;
			this.callback_ = callback;
			this.xFracDigits_ = xFracDigits;
			this.fxFracDigits_ = fxFracDigits;
			this.rateMonitor_ = rateMonitor;
			this.rateMinimiser_ = rateMinimiser;

			this.monitor_ = monitor;
			this.groupOptimisationType_ = groupOptimisationType;
//			this.tool_ = tool;
		}
		public double calculateLikelihoodScore() {
			return optimiseAllSimple(stopper_,rateMinimiser_,fxFracDigits_,xFracDigits_, callback_,monitor_, rateMonitor_,groupOptimisationType_);
		}
	}
}