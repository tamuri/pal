// UnrootedMLSearcher.java
//
// (c) 1999-2003 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: UnrootedMLSearcher</p>
 * <p>Description:
 * A tool for searching for a maximum likelihood unrooted tree under a general substitution model (which may be optimised).
 * Much of the code is delegated to other classes (such as LHCalculator classes) which in turn manage datatype related optimisations, high accuracy  computation, low memory computation, and cached calculation.
 * Even given the offsourcing of code this class is rather large!
 * Includes the algorithm of [1]
 * </p>
 * [1]  Guindon, Stéphane  Gascuel, Olivier (2003) A Simple, Fast, and Accurate Algorithm to Estimate Large Phylogenies by Maximum Likelihood. Systematic Biology 52:5 pages  696 - 704 / October 2003
 * @author Matthew Goode
 * @version 1.0
 */
import pal.tree.*;
import pal.datatype.*;
import pal.math.*;
import pal.util.*;

import pal.substmodel.*;
import pal.misc.*;
import pal.alignment.*;

import pal.algorithmics.*;
import pal.eval.*;

import java.util.*;

public class UnrootedMLSearcher implements Markable, StateProvider, UnrootedTreeInterface.Instructee {
	//The initial value of the update id (when not updated)
	private static final int STARTING_UPDATE_ID = 0;
	private static final double CONSTRUCTED_BRANCH_LENGTH = 0.01;

	private static final double MINIMUM_BRANCH_LENGTH = 0;
	private static final double MAXIMUM_BRANCH_LENGTH = 10;

	private Connection treeAccess_;
	private final SubstitutionModel model_;
	private final ConstructionTool tool_;
	private final Connection[] allConnections_;
	private final UNode[] orderedNodes_;

	private final MersenneTwisterFast random_ = new MersenneTwisterFast();

	private final OptimisationHandler optimisationHandler_;

	private final LHCalculator.Factory calcFactory_;
	/**
	 * Build an unconstrained optimiser based on a randomly generated tree.
	 * @param alignment the alignment used to represent each OTU
	 * @param model the substitution model that is used for calcuation. If optimisation on the model occurs than
	 * this model will be altered
	 */
	public UnrootedMLSearcher(Alignment alignment, SubstitutionModel model) {
		this(alignment,model,SimpleModelFastFourStateLHCalculator.getFactory());
	}

	public UnrootedMLSearcher(Alignment alignment, SubstitutionModel model, LHCalculator.Factory calcFactory) {
		this.model_ = model;
		int numberOfStates = model_.getDataType().getNumStates();
		this.calcFactory_ = calcFactory;
		tool_ = new ConstructionTool(alignment,numberOfStates,model.getNumberOfTransitionCategories(),calcFactory);
		this.treeAccess_ = new Connection(Identifier.getNames(alignment),tool_,random_);
		ArrayList v = new ArrayList();
		this.treeAccess_.getAllConnections(v);

		this.allConnections_ = new Connection[v.size()];
		v.toArray(allConnections_);
		this.optimisationHandler_ = new OptimisationHandler(model_,tool_);
		this.treeAccess_.setup(tool_,allConnections_);
		optimisationHandler_.setup(treeAccess_,true);

		this.orderedNodes_ = tool_.getOrderedNodes();
	}

	public UnrootedMLSearcher(Tree t, Alignment alignment, SubstitutionModel model) {
		this(t.getRoot(),alignment,model,SimpleModelFastFourStateLHCalculator.getFactory());
	}
	public UnrootedMLSearcher(Node root, Alignment alignment, SubstitutionModel model) {
		this(root,alignment,model,SimpleModelFastFourStateLHCalculator.getFactory());
	}
	/**
	 * Create a searcher based on a given tree, that has no alignment specified (useful as backbone tree for attaching new nodes)
	 * @param root the root of the tree to base things on (doesn't matter if it's rooted)
	 * @param model the substitution model to be used
	 */
	public UnrootedMLSearcher(Node root,  SubstitutionModel model) {
		this(root,null,model,SimpleModelFastFourStateLHCalculator.getFactory());
	}
	/**
	 * Create a searcher based on a given tree, that has no alignment , or model, specified (useful as backbone tree for attaching new nodes)
	 * @param root the root of the tree to base things on (doesn't matter if it's rooted)
	 */
	public UnrootedMLSearcher(Node root) {
		this(root,null,null,SimpleModelFastFourStateLHCalculator.getFactory());
	}
	public UnrootedMLSearcher(Node root, Alignment alignment, SubstitutionModel model, LHCalculator.Factory calcFactory) {
		this.calcFactory_ = calcFactory;
		this.model_ = model;
		if(model_==null) {
		  tool_ = new ConstructionTool(alignment,0,1,calcFactory);
		} else {
		  int numberOfStates = model_.getDataType().getNumStates();
		  tool_ = new ConstructionTool(alignment,numberOfStates,model.getNumberOfTransitionCategories(),calcFactory);
		}
		this.treeAccess_ = new Connection(root,tool_);
		ArrayList v = new ArrayList();
		this.treeAccess_.getAllConnections(v);

		this.allConnections_ = new Connection[v.size()];
		v.toArray(allConnections_);
		this.treeAccess_.setup(tool_,allConnections_);
		if(model_==null) {
		  this.optimisationHandler_ = null;
		} else {
		  this.optimisationHandler_ = new OptimisationHandler(model_,tool_);
		  optimisationHandler_.setup(treeAccess_,true);
		}
		this.orderedNodes_ = tool_.getOrderedNodes();
	}
	private UnrootedMLSearcher(UnrootedMLSearcher base, Connection attachmentPoint, Node newSubtree, Alignment newSequences, SubstitutionModel model) {
		this.model_ = model;
		this.calcFactory_  = base.calcFactory_;
		int numberOfStates = model_.getDataType().getNumStates();
		tool_ = new ConstructionTool(newSequences,numberOfStates,model_.getNumberOfTransitionCategories(),calcFactory_);
		this.treeAccess_ = new Connection(base.treeAccess_, attachmentPoint, newSubtree,tool_);
		ArrayList v = new ArrayList();
		this.treeAccess_.getAllConnections(v);

		this.allConnections_ = new Connection[v.size()];
		v.toArray(allConnections_);
		this.optimisationHandler_ = new OptimisationHandler(model_,tool_);
		this.treeAccess_.setup(tool_,allConnections_);
		optimisationHandler_.setup(treeAccess_,true);

		this.orderedNodes_ = tool_.getOrderedNodes();
	}
	public BranchAccess[] getAccessToBranches() {
	  BranchAccess[] bas = new BranchAccess[allConnections_.length];
		for(int i = 0 ; i < bas.length ; i++) {
		  bas[i] = new BranchAccessImpl(allConnections_[i],this);
		}
		return bas;
	}
	public NodeAccess[] getAccessToNodes() {
	  NodeAccess[] bas = new NodeAccess[orderedNodes_.length];
		for(int i = 0 ; i < bas.length ; i++) {
		  bas[i] = new NodeAccessImpl(orderedNodes_[i],this);
		}
		return bas;
	}
// -=-=-=-=-=-=-==-=--=--==--=-=-=-=-==-=--==-=
// -=-=-=-= State Provider stuff -=-==-=-=-=-=-

// Implement correctly!

	public Object getStateReference() {
		return new StateObject(allConnections_);
	}

	public void restoreState(Object stateReference) {
		StateObject so = (StateObject)stateReference;
		so.rebuildTree(allConnections_,orderedNodes_);
	}

// -=-=-=-=-=-=-==--=-=-=-=-=-=-=-=-=-=-=-=-=-=--==--=-=-=-=-==-=-=-=-=-=--==--

	public void instruct(UnrootedTreeInterface treeInterface) {
	  UnrootedTreeInterface.BaseBranch base = treeInterface.createBase();
		treeAccess_.instructBase(base);
	}

	public UndoableAction getNNIAction(StoppingCriteria.Factory stopper) {
		Assessor a = getSimpleAssessor(stopper);
		return new NNIAction(allConnections_,a,random_,tool_);
	}
	public UndoableAction getBranchLengthOptimiseAction(StoppingCriteria.Factory stopper) {
		return new BranchLengthOptimiseAction(allConnections_,stopper.newInstance(),AlgorithmCallback.Utils.getNullCallback());
	}
		/**
		 *
		 * @param stopper The means for determining when a set of round should be stopped
		 * @return An undoable action that does the Simulataneous NNI/Branch length of Stephan Guindon
		 * @note this action cannot undo (well, it could but it hasn't been implemented). This is okay as it should always find a better or equal valued state
		 */
	public UndoableAction getNNIBranchLengthOptimiseAction(StoppingCriteria.Factory stopper) {
		return new NNIBranchLengthOptimiseAction(allConnections_,model_, stopper.newInstance(),AlgorithmCallback.Utils.getNullCallback(),tool_);
	}
	public UndoableAction getBranchLengthWithModelOptimiseAction(StoppingCriteria.Factory stopper, MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits) {
		return new BranchLengthWithModelOptimiseAction(tool_, allConnections_,stopper.newInstance(),AlgorithmCallback.Utils.getNullCallback(), minimiser, MinimiserMonitor.Utils.createNullMonitor(),model_,fxFracDigits,xFracDigits);
	}
	public UndoableAction getModelOptimiseAction(MultivariateMinimum minimiser, int fxFracDigits, int xFracDigits) {
		return new ModelOptimiseAction(treeAccess_,minimiser, MinimiserMonitor.Utils.createNullMonitor(),model_,fxFracDigits,xFracDigits, tool_);
	}
		public UndoableAction getModelOptimiseAction(MultivariateMinimum minimiser, MinimiserMonitor monitor, int fxFracDigits, int xFracDigits) {
		return new ModelOptimiseAction(treeAccess_,minimiser, monitor,model_,fxFracDigits,xFracDigits, tool_);
	}



	public UndoableAction getSPRAction(StoppingCriteria.Factory stopper) {
		Assessor a = getSimpleAssessor(stopper);
		return new SPRAction(allConnections_,this, a,random_,tool_);
	}
	public UndoableAction getSweepSPRAction(StoppingCriteria.Factory stopper) {
		Assessor a = getSimpleAssessor(stopper);
		SPRAction base = new SPRAction(allConnections_,this, a,random_,tool_);
		return new SweepSPRAction(allConnections_,base,random_);
	}
	public UndoableAction getFullSweepSPRAction(StoppingCriteria.Factory stopper) {
		Assessor a = getSimpleAssessor( stopper );
		SPRAction base = new SPRAction( allConnections_, this, a, random_, tool_ );
		return new FullSweepSPRAction( allConnections_, base );
}



//=--=-=-=-=-=-=-==--==--=
// For Markable interface
	public final void mark() {
		for(int i = 0 ; i < allConnections_.length ; i++) {
			allConnections_[i].mark();
		}
		for(int i = 0 ; i < orderedNodes_.length ; i++) {
			orderedNodes_[i].mark();
		}
	}
	public final void undoToMark() {
		for(int i = 0 ; i < allConnections_.length ; i++) {
			allConnections_[i].undoToMark();
		}
		for(int i = 0 ; i < orderedNodes_.length ; i++) {
			orderedNodes_[i].undoToMark();
		}
		treeAccess_.setup(tool_,allConnections_);
	}

	private final Connection getRandomConnection() {
		return allConnections_[random_.nextInt(allConnections_.length)];
	}

	public void testLiklihood() {
		Workspace workspace = new Workspace(30,tool_.getNumberOfSites(),tool_);
		OptimisationHandler oh = new OptimisationHandler(model_, tool_);
		oh.setup(treeAccess_,true);
		treeAccess_.testLikelihood(model_,tool_);
	}
	 /**
		* Likelihood calculation method (not optimisation)
		* @return the log likelihood, based on current model, branchlengths and topology
		* @note not valid if no alignment/model given in construction
		*/
	public double calculateLogLikelihood() {
		 return treeAccess_.calculateLogLikelihood(model_,true, tool_.allocateNewExternalCalculator(), tool_);
	}
	 /**
		* An alternative likelihood calculation method (should give same results as other method, and in same time)
		* @return the log likelihood, based on current model, branchlengths and topology
		* @note not valid if no alignment/model given in construction
		*/
	public double calculateLogLikelihood2() {
		return treeAccess_.calculateLogLikelihood2(model_,true, tool_.allocateNewExternalCalculator(), tool_);
	}

	public SiteDetails calculateSiteDetails() {
		 return treeAccess_.calculateSiteDetails(model_,true, tool_.allocateNewExternalCalculator(), tool_);
	}

 /**
		* Optimise the branch lengths of the tree to obtain the maximum likelihood. Does not change the model
		* or the topology
		* @param epsilon the tolerance places for convergence (on the likelihood score)
		* @param callback a callback to monitor progress
		* @return the resulting likelihood
		*/
	public double simpleOptimiseLikelihood(double epsilon, AlgorithmCallback callback) {
	  return simpleOptimiseLikelihood(StoppingCriteria.Utils.getNonExactUnchangedScore(2,true,epsilon).newInstance(),callback);
	}

	 /**
		* Optimise the branch lengths of the tree to obtain the maximum likelihood. Does not change the model
		* or the topology
		* @param stopper the stopping criteria (on the likelihood score)
		* @param callback a callback to monitor progress
		* @return the resulting likelihood
		*/
	public double simpleOptimiseLikelihood(StoppingCriteria stopper, AlgorithmCallback callback) {
		stopper.reset();
		double maximum = Double.NEGATIVE_INFINITY;
		boolean firstTime = true;
		while(!stopper.isTimeToStop()) {
			for(int i = 0 ; i < allConnections_.length ; i++) {
				Connection c = allConnections_[i];
				maximum = optimisationHandler_.optimiseBranchLength(c,firstTime);
				firstTime = false;

			}
			stopper.newIteration(maximum,maximum,true,true,callback);
		}
		return maximum;
	}


	public Tree buildPALTree() {
		return new SimpleTree(buildPALNode());
	}
	public Node buildPALNode() {
		Node n = treeAccess_.buildPALNode();
		NodeUtils.lengths2Heights(n);
		return n;
	}
// =--=-=-=-=-==--=-=-=-=-=-=-=-=-=-=-=-=-=-==--==--=-=-=-=-=-==--=-=-=-=-==--=
// === State stuff
// =--=-=-=-=-==--=-=-=-=-=-=-=-=-=-=-=-=-=-==--==--=-=-=-=-=-==--=-=-=-=-==--=
	public static final class StateObject {
		private final double[] branchLengths_;
		private final int[] connectionInfo_;
		public StateObject(Connection[] allConnections) {
			this.branchLengths_ = new double[allConnections.length];
			this.connectionInfo_ = new int[allConnections.length*2];
			for(int i = 0 ; i < allConnections.length ; i++) {
				this.branchLengths_[i] = allConnections[i].getBranchLength();
				allConnections[i].fillInConnectionState(connectionInfo_,i*2);
			}
		}
		private final int fillIn(Connection[] store, int nodeIndex, Connection[] allConnections) {
			int found = 0;

			for(int i = 0 ; i < allConnections.length ; i++ ) {
				if(connectionInfo_[i*2]==nodeIndex||connectionInfo_[i*2+1]==nodeIndex) {
					store[found++]=allConnections[i];
				}
			}
			return found;
		}
		public final void rebuildTree(Connection[] allConnections, UNode[] orderedNodes) {
			for(int i = 0 ; i < allConnections.length ; i++) {
				allConnections[i].setNodes(orderedNodes[connectionInfo_[i*2]],orderedNodes[connectionInfo_[i*2+1]]);
			}
			Connection[] store = new Connection[3];
			for(int nodeIndex = 0 ; nodeIndex < orderedNodes.length ; nodeIndex++) {
				int found = fillIn(store,nodeIndex, allConnections);
				orderedNodes[nodeIndex].setConnections(store,found);

			}
		}
		public boolean equals(Object o) {
			if(o instanceof StateObject) {
				StateObject so = (StateObject)o;
				int[] other = so.connectionInfo_;
				for(int i = 0 ; i < connectionInfo_.length ; i++) {
					if(connectionInfo_[i]!=other[i]) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}
// =--=-=-=-=-==--=-=-=-=-=-=-=-=-=-=-=-=-=-==--==--=-=-=-=-=-==--=-=-=-=-==--=
// === Algorithmics stuff Stuff
// =--=-=-=-=-==--=-=-=-=-=-=-=-=-=-=-=-=-=-==--==--=-=-=-=-=-==--=-=-=-=-==--=
	public final Assessor getSimpleAssessor(StoppingCriteria.Factory stopper) {
		return new SimpleAssessor(stopper.newInstance(),AlgorithmCallback.Utils.getNullCallback());
	}

	//-=-=-==--==-

	private final class SimpleAssessor implements Assessor {
		private final StoppingCriteria stopper_;
		private final AlgorithmCallback callback_;
		public SimpleAssessor(StoppingCriteria stopper, AlgorithmCallback callback) {
			this.stopper_ = stopper;
			stopper_.reset();
			this.callback_ = callback;
		}
		public double getCurrentValue() {
			return simpleOptimiseLikelihood(stopper_, callback_);
		}
	}

// - - - - -- - - -  - - -- - - -  - -- - - - - - - - - - - -  -
		/**
		 *
		 * <p>Title: NNIBranchLengthOptimiseAction</p>
		 * <p>Description: Implements the simultaneous NNI/branch length optimisation of Stephan Guindon et al</p>
		 */
	private static final class NNIBranchLengthOptimiseAction implements UndoableAction {
		private final StoppingCriteria stopper_;
		private final AlgorithmCallback callback_;
//    private final double[] branchLengths_;
		private final Connection[] allConnections_;
		private NNIOptimisationHandler handler_;
		public NNIBranchLengthOptimiseAction(Connection[] allConnections, SubstitutionModel model,  StoppingCriteria stopper, AlgorithmCallback callback, ConstructionTool tool) {
			this.stopper_ = stopper;
			stopper_.reset();
			this.callback_ = callback;
			this.allConnections_ = allConnections;
			this.handler_ = new NNIOptimisationHandler(allConnections, model, tool);
//      this.branchLengths_ = new double[allConnections.length];
		}
		public double doAction(double currentScore, double desparationValue) {
//      for(int i = 0 ; i < allConnections_.length ; i++) {
//        branchLengths_[i] = allConnections_[i].getBranchLength();
//      }
			stopper_.reset();
			double maximum = Double.NEGATIVE_INFINITY;
			boolean firstTime = true;
			while(!stopper_.isTimeToStop()) {
				for(int i = 0 ; i < allConnections_.length ; i++) {
					Connection c = allConnections_[i];
					//Do unchanged way
					maximum = handler_.optimiseSimulataneousNNIBranchLength(c,firstTime);
					firstTime = false;
					}
				stopper_.newIteration(maximum,maximum,true,true,callback_);
			}
			return maximum;
		}
		public boolean isActionSuccessful() {
			return true;
		}
		/**
		 *
		 * @return false
		 */
		public boolean undoAction() {
//      for(int i = 0 ; i < allConnections_.length ; i++) {
//        allConnections_[i].setBranchLength(branchLengths_[i]);
//      }
			return false;
		}
		/**
		 * @return false
		 */
		public boolean isActionDeterministic() {
			return false;
		}

	}

// - - - - -- - - -  - - -- - - -  - -- - - - - - - - - - - -  -
	private final  class BranchLengthOptimiseAction implements UndoableAction {
		private final StoppingCriteria stopper_;
		private final AlgorithmCallback callback_;
		private final double[] branchLengths_;
		private final Connection[] allConnections_;
		public BranchLengthOptimiseAction(Connection[] allConnections, StoppingCriteria stopper, AlgorithmCallback callback) {
			this.stopper_ = stopper;
			stopper_.reset();
			this.callback_ = callback;
			this.allConnections_ = allConnections;
			this.branchLengths_ = new double[allConnections.length];
		}
		public double doAction(double currentScore, double desparationValue) {
			for(int i = 0 ; i < allConnections_.length ; i++) {
				branchLengths_[i] = allConnections_[i].getBranchLength();
			}
			return simpleOptimiseLikelihood(stopper_, callback_);
		}
		public boolean isActionSuccessful() {
			return true;
		}
		/**
		 *
		 * @return true
		 */
		public boolean undoAction() {
			System.out.println("Doing undo action!");
			for(int i = 0 ; i < allConnections_.length ; i++) {
				allConnections_[i].setBranchLength(branchLengths_[i]);
			}
			return true;
		}
		/**
		 * @return false
		 */
		public boolean isActionDeterministic() {
			return false;
		}

	}

// - - - - -- - - -  - - -- - - -  - -- - - - - - - - - - - -  -
	private final static  class BranchLengthWithModelOptimiseAction implements UndoableAction, MultivariateFunction {
		private final StoppingCriteria stopper_;
		private final AlgorithmCallback callback_;
		private final double[] branchLengths_;
		private final Connection[] allConnections_;
		private final MultivariateMinimum minimiser_;
		private final MinimiserMonitor monitor_;
		private final SubstitutionModel model_;
		private final double[] modelParameterStore_;
		private final double[] xvec_;
		private final int fxDigits_;
		private final int xDigits_;
		private final LHCalculator.External calculator_;

		private final UnivariateMinimum um_;
		private final OptimisationHandler optimisationHandler_;
		private final ConstructionTool tool_;

		public BranchLengthWithModelOptimiseAction(ConstructionTool tool, Connection[] allConnections, StoppingCriteria stopper, AlgorithmCallback callback, MultivariateMinimum minimiser, MinimiserMonitor monitor, SubstitutionModel model, int fxDigits, int xDigits) {
			this.stopper_ = stopper;
			stopper_.reset();
			this.tool_ = tool;
			this.callback_ = callback;
			this.allConnections_ = allConnections;
			this.branchLengths_ = new double[allConnections.length];
			this.minimiser_ = minimiser;
			this.monitor_ = monitor;
			this.model_ = model;
			this.calculator_ = tool.allocateNewExternalCalculator();
			this.modelParameterStore_ = new double[model.getNumParameters()];
			this.xvec_ = new double[model.getNumParameters()];
			this.xDigits_ = xDigits;
			this.fxDigits_ = fxDigits;
			this.um_ = new UnivariateMinimum();
			this.optimisationHandler_ = new OptimisationHandler(model_,tool);
		}
		public double evaluate(double[] xvec) {
			for(int i = 0 ; i < xvec.length ; i++) {
				model_.setParameter(xvec[i],i);
			}
			return -allConnections_[0].calculateLogLikelihood(model_,true,calculator_,tool_);
		}
		public int getNumArguments() { return xvec_.length; }
		public double getLowerBound(int n) { return model_.getLowerLimit(n); }
		public double getUpperBound(int n) { return model_.getUpperLimit(n); }
		public OrthogonalHints getOrthogonalHints() { return model_.getOrthogonalHints(); }

		public double doAction(double currentScore, double desparationValue) {
			for(int i = 0 ; i < allConnections_.length ; i++) {
				branchLengths_[i] = allConnections_[i].getBranchLength();
			}
			for(int i = 0 ; i < modelParameterStore_.length ; i++) {
				modelParameterStore_[i] = model_.getParameter(i);
			}
			stopper_.reset();


			System.arraycopy(modelParameterStore_,0,xvec_,0,xvec_.length);


			double minimum = Double.POSITIVE_INFINITY;
			boolean firstTime = true;
			while(!stopper_.isTimeToStop()) {
				for(int i = 0 ; i < allConnections_.length ; i++) {
					Connection c = allConnections_[i];
					optimisationHandler_.setup(c,firstTime);
					firstTime = false;
					um_.findMinimum(c.getBranchLength(),optimisationHandler_);
					c.setBranchLength(um_.minx);
					//minimum = um_.fminx;
					minimum = minimiser_.findMinimum(this,xvec_,fxDigits_,xDigits_,monitor_);
					for(int x = 0 ; x < xvec_.length ; x++) {
						model_.setParameter(xvec_[x],x);
					}
					c.setBranchLength(um_.minx);
			}
			stopper_.newIteration(minimum,minimum,true,true,callback_);
		}
		return -minimum;

		}
		public boolean isActionSuccessful() {
			return true;
		}
		/**
		 *
		 * @return true
		 */
		public boolean undoAction() {
			for(int i = 0 ; i < allConnections_.length ; i++) {
				allConnections_[i].setBranchLength(branchLengths_[i]);
			}
			for(int i = 0 ; i < modelParameterStore_.length ; i++) {
				model_.setParameter(modelParameterStore_[i],i);
			}
			return true;
		}
		/**
		 * @return false
		 */
		public boolean isActionDeterministic() {
			return false;
		}

	}

	// - - - - -- - - -  - - -- - - -  - -- - - - - - - - - - - -  -
	private final static class ModelOptimiseAction implements UndoableAction, MultivariateFunction {
		private final Connection treeAccess_;
		private final MultivariateMinimum minimiser_;
		private final MinimiserMonitor monitor_;
		private final SubstitutionModel model_;
		private final double[] modelParameterStore_;
		private final double[] xvec_;
		private final int fxDigits_;
		private final int xDigits_;
		private final LHCalculator.External calculator_;
		private final ConstructionTool tool_;
		public ModelOptimiseAction(Connection treeAccess, MultivariateMinimum minimiser, MinimiserMonitor monitor, SubstitutionModel model, int fxDigits, int xDigits, ConstructionTool tool) {
			this.treeAccess_ = treeAccess;
			this.minimiser_ = minimiser;
			this.monitor_ = monitor;
			this.model_ = model;
			this.tool_ = tool;
			this.calculator_ = tool_.allocateNewExternalCalculator();

			this.modelParameterStore_ = new double[model.getNumParameters()];
			this.xvec_ = new double[model.getNumParameters()];
			this.xDigits_ = xDigits;
			this.fxDigits_ = fxDigits;
		}
		public double evaluate(double[] xvec) {
			for(int i = 0 ; i < xvec.length ; i++) {
				model_.setParameter(xvec[i],i);
			}
			return -treeAccess_.calculateLogLikelihood(model_,true,calculator_, tool_);
		}
		public int getNumArguments() { return xvec_.length; }
		public double getLowerBound(int n) { return model_.getLowerLimit(n); }
		public double getUpperBound(int n) { return model_.getUpperLimit(n); }
		public OrthogonalHints getOrthogonalHints() { return model_.getOrthogonalHints(); }
		/**
		 * @return true
		 */
		public boolean isActionDeterministic() {
			return true;
		}
		public double doAction(double currentScore, double desparationValue) {
			for(int i = 0 ; i < modelParameterStore_.length ; i++) {
				modelParameterStore_[i] = model_.getParameter(i);
			}
			System.arraycopy(modelParameterStore_,0,xvec_,0,xvec_.length);
			double minimum = -minimiser_.findMinimum(this,xvec_,fxDigits_,xDigits_,monitor_);
			for(int i = 0 ; i < xvec_.length ; i++) {
				model_.setParameter(xvec_[i],i);
			}
			return minimum;
		}
		public boolean isActionSuccessful() {
			return true;
		}
		/**
		 *
		 * @return true
		 */
		public boolean undoAction() {
			for(int i = 0 ; i < modelParameterStore_.length ; i++) {
				model_.setParameter(modelParameterStore_[i],i);
			}
			return true;
		}

	}

// - - - - -- - - -  - - -- - - -  - -- - - - - - - - - - - -  -
	private static final class NNIAction implements UndoableAction {
		private boolean leftSwapLeft_;
		private boolean rightSwapLeft_;
		private Connection connection_;
		private final Connection[] allConnections_;
		private final double[] branchLengths_;
		private final Assessor assessor_;
		private boolean lastActionSuccessful_ = false;
		private final MersenneTwisterFast random_;
		private final ConstructionTool tool_;
		public NNIAction(Connection[] allConnections, Assessor assessor, MersenneTwisterFast r, ConstructionTool tool) {
			this.allConnections_ = allConnections;
			this.assessor_ = assessor;
			this.random_ = r;
			this.tool_ = tool;
			this.branchLengths_ = new double[allConnections.length];
		}
		public double doAction(double currentScore, double desparationValue) {
			connection_ = allConnections_[random_.nextInt(allConnections_.length)];
			leftSwapLeft_ = random_.nextBoolean();
			rightSwapLeft_ = random_.nextBoolean();

			lastActionSuccessful_ = connection_.doNNI(leftSwapLeft_,rightSwapLeft_);
			if(lastActionSuccessful_) {
				for(int i = 0 ; i < allConnections_.length ; i++) {
					branchLengths_[i] = allConnections_[i].getBranchLength();
				}
				connection_.setup(tool_,allConnections_);
				return assessor_.getCurrentValue();
			} else {
				return currentScore;
			}
		}
		/**
		 * @return false
		 */
		public boolean isActionDeterministic() {
			return false;
		}
		public boolean isActionSuccessful() { return lastActionSuccessful_; }
		/**
		 * @return true
		 */
		public boolean undoAction() {
			if(lastActionSuccessful_) {
				connection_.doNNI( leftSwapLeft_, rightSwapLeft_ );
				for( int i = 0; i<allConnections_.length; i++ ) {
					allConnections_[i].setBranchLength( branchLengths_[i] );
				}
				connection_.setup(tool_,allConnections_);
				lastActionSuccessful_=false;
				return true;
			} else {
				throw new RuntimeException("Illegal operation : undoLast() called when last operation invalid (may already have been undone)");
			}
		}
	}
// - - - - -- - - -  - - -- - - -  - -- - - - - - - - - - - -  -
	private static final class SweepSPRAction implements UndoableAction {
		private Connection toMove_ = null ;
		private SPRAction baseAction_;

		private boolean lastActionSuccessful_ = false;

		private final Connection[] shuffledConnections_;
		private final MersenneTwisterFast random_;

		/**
		 * @param solution a reference to the UnconstrainedOptiser
		 * @param assessor a means of assessing the solution (assumes gives true likelihood)
		 * @note I choose to use a static inner class and have funny references because of personal style (I like writing inner classes I can ship out to separate files if I need to)
		 */
		public SweepSPRAction(Connection[] allConnections,  SPRAction baseAction, MersenneTwisterFast random) {
			this.baseAction_ = baseAction;
			this.random_ = random;
			this.shuffledConnections_ = new Connection[allConnections.length];
			System.arraycopy(allConnections,0,shuffledConnections_,0,allConnections.length);
		}
		public SweepSPRAction setRandomTarget() {
			return setTarget(shuffledConnections_[random_.nextInt(shuffledConnections_.length)]);
		}
		public SweepSPRAction setTarget(Connection toMove) {
			this.toMove_ = toMove;
			shuffle(shuffledConnections_);
			return this;
		}
		/**
		 * @return false
		 */
		public boolean isActionDeterministic() {
			return false;
		}
		private final void shuffle(Connection[] cs) {
			for(int i = 0 ; i < cs.length ; i++) {
				int j = random_.nextInt(cs.length-i)+i;
				Connection t = cs[i];
				cs[i] = cs[j];
				cs[j] = t;
			}
		}
		public double doAction(double originalScore, double desparationValue) {
			setRandomTarget();
			return doSetupAction(originalScore);
		}
		public double doSetupAction(double originalScore) {
			Connection best = null;
			double bestScore = originalScore;
			for(int i = 0 ; i < shuffledConnections_.length; i++) {
				Connection c = shuffledConnections_[i];
				if(c!=toMove_) {
					baseAction_.setTarget(toMove_,c);
					double score = baseAction_.doSetupAction(originalScore);
					if(baseAction_.isActionSuccessful()) {
						if(score>bestScore) {
							best = c;
							bestScore = score;
						}
						baseAction_.undoAction();
					}
				}
			}
			if(best!=null) {
				baseAction_.setTarget(toMove_,best);
				lastActionSuccessful_ = true;
				//We assume that if the action worked before it will work now
				return baseAction_.doSetupAction(originalScore);
			}
			lastActionSuccessful_ = false;
			//When we fail the score does not matter
			return originalScore;
		}
		public boolean isActionSuccessful() { return lastActionSuccessful_; }
		public boolean undoAction() {
			if(lastActionSuccessful_) {
				return baseAction_.undoAction();
			} else {
				throw new RuntimeException("Illegal operation : undoLast() called when last operation invalid (may already have been undone)");
			}
		}
	}

// - - - - -- - - -  - - -- - - -  - -- - - - - - - - - - - -  -
	private static final class FullSweepSPRAction implements UndoableAction {
		private SPRAction baseAction_;

		private final Connection[] allConnections_;
		private boolean lastActionSuccessful_ = false;

		/**
		 * @param solution a reference to the UnconstrainedOptiser
		 * @param assessor a means of assessing the solution (assumes gives true likelihood)
		 * @note I choose to use a static inner class and have funny references because of personal style (I like writing inner classes I can ship out to separate files if I need to)
		 */
		public FullSweepSPRAction(Connection[] allConnections,  SPRAction baseAction) {
			this.baseAction_ = baseAction;
			this.allConnections_ = allConnections;
		}

		public double doAction(double originalScore, double desparationValue) {
			Connection bestStart = null;
			Connection bestEnd = null;
			double bestScore = originalScore;
			for(int i = 0 ; i < allConnections_.length; i++) {
				Connection start = allConnections_[i];
				for(int j = i+1 ; j < allConnections_.length; j++) {

					Connection end = allConnections_[i];
					baseAction_.setTarget(start,end);
					double score = baseAction_.doSetupAction(originalScore);
					if(baseAction_.isActionSuccessful()) {
						if(score>bestScore) {
							bestStart = start;
							bestEnd = end;
							bestScore = score;
						}
						baseAction_.undoAction();
					}
				}
			}
			if(bestStart!=null) {
				baseAction_.setTarget(bestStart,bestEnd);
				lastActionSuccessful_ = true;
				//We assume that if the action worked before it will work now
				return baseAction_.doSetupAction(originalScore);
			}
			lastActionSuccessful_ = false;
			//When we fail the score does not matter
			return originalScore;
		}
		/**
		 * @return true
		 */
		public boolean isActionDeterministic() {
			return true;
		}
		public boolean isActionSuccessful() { return lastActionSuccessful_; }
		public boolean undoAction() {
			if(lastActionSuccessful_) {
				return baseAction_.undoAction();
			} else {
				throw new RuntimeException("Illegal operation : undoLast() called when last operation invalid (may already have been undone)");
			}
		}
	}
// - - -- - - - - - -- - - - - - - - - - - - - - - - -- - - - - - - - - - - - -
	private static final class SPRAction implements UndoableAction {
		private Connection toRemove_ = null ;
		private Connection attachmentPoint_ = null;
		private Connection reattachmentPoint_ = null;
		private final Connection[] allConnections_;
		private final Connection[] store_ = new Connection[3];

		private final double[] branchLengths_;

		private boolean lastActionSuccessful_ = false;
		private final Assessor assessor_;
		private final Markable subject_;
		private final MersenneTwisterFast random_;
		private final ConstructionTool tool_;
		public SPRAction(Connection[] allConnections, Markable subject, Assessor assessor, MersenneTwisterFast random, ConstructionTool tool) {
			this.allConnections_ = allConnections;
			this.subject_ = subject;
			this.assessor_ = assessor;
			this.tool_ = tool;
			this.branchLengths_ = new double[allConnections.length];
			this.random_ = random;
		}
		public SPRAction setRandomTargets() {
			int i = random_.nextInt(allConnections_.length);
			int j = random_.nextInt(allConnections_.length-1);
			if(j>=i) { j++; }
			this.toRemove_ = allConnections_[i];
			this.attachmentPoint_ = allConnections_[j];
			return this;
		}
		public SPRAction setTarget(Connection toRemove, Connection attachmentPoint) {
			this.toRemove_ = toRemove;
			this.attachmentPoint_ = attachmentPoint;
			return this;
		}
		public boolean isActionSuccessful() { return lastActionSuccessful_; }
		/**
		 * Peform an action based on setup connections (ie must have called setTarget() already)
		 * @param currentScore
		 * @return the new score if successful
		 */
		public double doSetupAction(double currentScore) {
			subject_.mark();
			reattachmentPoint_ = toRemove_.attachTo(attachmentPoint_,store_);
			lastActionSuccessful_ = (reattachmentPoint_!=null);
			if(lastActionSuccessful_) {
				for(int i = 0 ; i < allConnections_.length ; i++) {
					branchLengths_[i] = allConnections_[i].getBranchLength();
				}
				toRemove_.setup(tool_,allConnections_);
			}

			return(lastActionSuccessful_? assessor_.getCurrentValue() : currentScore);
		}
		public double doAction(double currentScore, double desparationValue) {
			setRandomTargets();
			return doSetupAction(currentScore);
		}
		/**
		 * @return false
		 */
		public boolean isActionDeterministic() {
			return false;
		}
		public boolean undoAction() {
			if(lastActionSuccessful_) {
				subject_.undoToMark();
				return true;
			} else {
				throw new RuntimeException("Undo last called when last operation not successful");
			}
		}
	}
// -=-=-=-=-=-==--==--==--=-=-==-=--=-=-=-==-=-=--==-=-=--==--==-=-=--==--==--=
// == Static Utility Methods ===
// =--=-==-=--=-==--=-==--=-=-=-=-==-=-=--=-==-=-=-=--==-=-=-=-=--==-=-=-=-=-=-
	private static final UNode createUNode(Node n, Connection parentConnection, ConstructionTool tool) {
		if(n.isLeaf()) {
			return new LeafNode(n,parentConnection, tool);
		}
		return new InternalNode(n,parentConnection,tool);
	}

	private static final UNode createUNode(String[] leafNames, Connection parentConnection, ConstructionTool tool, MersenneTwisterFast r) {
			if(leafNames.length==1) {
				return new LeafNode(leafNames[0],parentConnection, tool);
			}
			return new InternalNode(leafNames,parentConnection,tool,r);
		}



// -=-=-=-=-=-==--==--==--=-=-==-=--=-=-=-==-=-=--==-=-=--==--==-=-=--==--==--=
// == Workspace ===
// =--=-==-=---==--=-==--=-=-=-=-==-=-=--=-==-=-=-=--==-=-=-=-=--==-=-=-=-=-=-
	private static final class Workspace {
		private final double[][][][] conditionalLikelihoodTables_;
		private final double[] endStateProbabilityStore_;
		private final double[][][] transitionProbabilityStore_;
		private final int numberOfCategories_;
		private final int numberOfStates_;
		private final boolean[] locks_;
		public Workspace(int capacity, int maxNumberOfPatterns, ConstructionTool tool) {
			this.numberOfCategories_ = tool.getNumberOfTransitionCategories();
			numberOfStates_ = tool.getNumberOfStates();

			this.conditionalLikelihoodTables_ =
					new double
							[capacity]
							[numberOfCategories_]
							[maxNumberOfPatterns]
							[tool.getNumberOfStates()];
		 this.endStateProbabilityStore_ = new double[numberOfStates_];
		 this.transitionProbabilityStore_ = new double[numberOfCategories_][numberOfStates_][numberOfStates_];
		 this.locks_ = new boolean[capacity];
		}
		public final int getNumberOfCategories() { return numberOfCategories_; }
		public final int getNumberOfStates() { return numberOfStates_; }
		public final double[] getEndStateProbabilityStore() { return endStateProbabilityStore_; }
		public final int obtainLock() {
			 for(int i = 0 ; i < locks_.length ; i++) {
				 if(!locks_[i]) {
					 locks_[i]=true;
					 return i;
				 }
			 }
			 throw new RuntimeException("Assertion error : no locks available");
		 }
		 public final double[][][] getTransitionProbabilityStore() {
			 return transitionProbabilityStore_;
		 }
		 public final void returnLock(int lockNumber, double[][][] probabilities) {
			 if(locks_[lockNumber]) {
				 if(conditionalLikelihoodTables_[lockNumber]!=probabilities) {
					 throw new IllegalArgumentException("Table mismatch");
				 }
				 locks_[lockNumber]=false;
			 } else {
				 throw new IllegalArgumentException("Lock already unlocked:"+lockNumber);
			 }
		 }
		 public final void freeLock(int lockNumber) {
			 if(locks_[lockNumber]) {
				 locks_[lockNumber]=false;
			 } else {
				 throw new IllegalArgumentException("Lock already unlocked:"+lockNumber);
			 }
		 }
		 public final double[][][] getConditionalProbabilityTable(int lockNumber) {
			 if(locks_[lockNumber]) {
				 return conditionalLikelihoodTables_[lockNumber];
			 }else {
				 throw new IllegalArgumentException("Accessing unlocked table:"+lockNumber);
			 }
		 }
	}
// -=-=-=-=-=-==--==--==--=-=-==-=--=-=-=-==-=-=--==-=-=--==--==-=-=--==--==--=
// == UNode ===
// =--=-==-=--=-==--=-==--=-=-=-=-==-=-=--=-==-=-=-=--==-=-=-=-=--==-=-=-=-=-=-
	private static interface UNode {
		public String toString(Connection caller);
		public PatternInfo getPatternInfo(Connection caller);
		public Node buildPALNode(double branchLength, Connection caller);
		public void testLikelihood(Connection caller, SubstitutionModel model, ConstructionTool tool);
		public void getAllConnections(ArrayList store, Connection caller);
		public int getIndex();
		public UNode createAlteredCopy(Connection attachmentPoint, Node newSubtree, Connection originalParentConnection, Connection parentConnection, ConstructionTool tool);
		public UNode createAlteredCopy(Connection originalParentConnection, Connection parentConnection, ConstructionTool tool);
		public boolean hasDirectConnection(Connection c);
		public boolean hasConnection(Connection c, Connection caller);

		public void mark();
		public void undoToMark();

		public void instruct(UnrootedTreeInterface.UNode node, Connection callingConnection);

		public boolean isLeaf();
		public boolean hasLabel(String label);
		public void setAnnotation(Object annotation);
		public Object getAnnotation();
		public String getLabel();

		/**
		 * Instruct the node to extract itself from the two connections that aren't the caller
		 * One of the other two connections will become redunant.
		 * @return the redundant connection, or null of this node can't extract
		 */
		public Connection extract(Connection caller);

		/**
		 * @return the left connection with reference to the caller
		 * @note can return null if not possible (if leaf)
		 */
		public Connection getLeft(Connection caller);
		/**
		 * @return the right connection with reference to the caller
		 * @note can return null if not possible (if leaf)
		 */
		public Connection getRight(Connection caller);

		/**
		 * Set the connections to this node
		 * @param store a temporary store of the connections - node must copy references, do not use store
		 * @param number the number of connections to look at (ignore the length of store)
		 */
		public void setConnections(Connection[] store, int number);
		/**
		 * Should preserver tree integrity
		 */
		public void swapConnection(Connection original, UNode nodeToReplace, Connection newConnection);
		/**
		 * Should not do anything but swap connections around
		 */
		public void swapConnection(Connection original,Connection newConnection);

		/**
		 * Recurse to all neighbours but caller
		 * @return the maximum number of patterns from any neighbour
		 */
		public int rebuildPattern( ConstructionTool tool, Connection caller, boolean firstPass);
		/**
		 * Recurse to all neighbours
		 * @return the maximum number of patterns from any neighbour
		 */
		public int rebuildPattern( ConstructionTool tool);
		/**
		 * To be used by nodes that cannot properly do a rebuildPattern(tool) call, so they redirect to the other end of a connection
		 */
		public int redirectRebuildPattern( ConstructionTool tool);

		public ConditionalProbabilityStore getFlatConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, int depth, boolean isForLeft);
		public ConditionalProbabilityStore getFlatConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore);

		public ConditionalProbabilityStore getLeftExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore);
		public ConditionalProbabilityStore getRightExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore);
		/**
		 *
		 * @param caller
		 * @return Get the pattern info for the relative left (from the caller's perspective), or null if not left pattern info
		 */
		public PatternInfo getLeftPatternInfo(Connection caller);
		/**
		 *
		 * @param caller
		 * @return Get the pattern info for the relative right (from the caller's perspective), or null if not right pattern info
		 */
		public PatternInfo getRightPatternInfo(Connection caller);


		public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, SubstitutionModel model, boolean modelChanged,  Connection callingConnection, int depth, boolean isForLeft);
		public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore);

		public void getLeafNames(ArrayList store, Connection caller);

		public void getSplitInformation(int[] splitStore, String[] leafNames, int splitIndex, Connection caller);

	}

// -=-=-=-=-=-==--==--==--=-=-==-=--=-=-=-==-=-=--==-=-=--==--==-=-=--==--==--=
// == InternalNode ===
// =--=-==-=--=-==--=-==--=-=-=-=-==-=-=--=-==-=-=-=--==-=-=-=-=--==-=-=-=-=-=-
	private static final class InternalNode implements UNode {
		private final Connection[] connections_ = new Connection[3];
		private final Connection[] markConnections_ = new Connection[3];

		private final PatternInfo[] patterns_;

		private static final int[] LEFT_LOOKUP = {	1 , 0, 0	};
		private static final int[] RIGHT_LOOKUP = {	2 , 2, 1	};

		private final int index_;
		private final LHCalculator.Internal calculator_;
		private boolean topologyChangedSinceLastFlat_ = true;
		private boolean topologyChangedSincleLastExtended_ = true;

		private Object annotation_ = null;

		/**
		 * The random tree constructor
		 * @param leafNames The names of the leafs remaining to be created
		 * @param parentConnection The connection that the recursion is "comming from"
		 * @param tool A tool to aid in construction
		 * @param r A method for getting random numbers used in determining branching
		 */
		private InternalNode(String[] leafNames , Connection parentConnection, ConstructionTool tool, MersenneTwisterFast r) {
			this.connections_[0] = parentConnection;
			this.index_ = tool.allocateNextUNodeIndex(this);
			this.calculator_ = tool.allocateNewInternalCalculator();
			String[][] split = SearcherUtils.split(leafNames,r);
			this.connections_[1] = new Connection(split[0],this, tool,r);
			this.connections_[2] = new Connection(split[1],this, tool,r);

			final int numberOfSites = tool.getNumberOfSites();
			this.patterns_ = new PatternInfo[] {
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true)
				};
		}
		/**
		 * The altered tree constructor
		 * @param parentConnection The connection that the recursion is "comming from"
		 * @param attachmentPoint The connection that the new sub tree will be attached to
		 * @param originalSubTree
		 * @param originalSubTreeBranchLength
		 * @param originalSubTreeParentConnection
		 * @param appendedSubTree
		 * @param appendedSubTreeBranchLength
		 * @param tool to aid in construction
		 */
		public InternalNode(Connection parentConnection ,Connection attachmentPoint, UNode originalSubTree, double originalSubTreeBranchLength,  Connection originalSubTreeParentConnection, UNode appendedSubTree, double appendedSubTreeBranchLength, ConstructionTool tool) {
			//The first connection is the parent connection
			this.connections_[0] = parentConnection;

		  //The second connection
			this.connections_[1] = new Connection(appendedSubTree, this, appendedSubTreeBranchLength, tool);
			this.connections_[2] = new Connection(originalSubTree,originalSubTreeParentConnection, this, originalSubTreeBranchLength, tool);

			this.index_ = tool.allocateNextUNodeIndex(this);
			this.calculator_ = tool.allocateNewInternalCalculator();

			final int numberOfSites = tool.getNumberOfSites();
			this.patterns_ = new PatternInfo[] {
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true)
				};

		}
		public InternalNode(Node i, Connection parentConnection, ConstructionTool tool) {
			this.connections_[0] = parentConnection;
			this.connections_[1] = new Connection(i.getChild(0),this, tool);
			this.connections_[2] = new Connection(i.getChild(1),this, tool);
			this.index_ = tool.allocateNextUNodeIndex(this);
			this.calculator_ = tool.allocateNewInternalCalculator();
		  if(calculator_!=null) {
				final int numberOfSites = tool.getNumberOfSites();
				this.patterns_ = new PatternInfo[] {
												 new PatternInfo( numberOfSites, true ),
												 new PatternInfo( numberOfSites, true ),
												 new PatternInfo( numberOfSites, true )
				};
			} else {
				this.patterns_ = null;
			}
		}
		/**
		 * The cloning with attachment constructor
		 * @param original The node we are replacing
		 * @param attachmentPoint The original connection that will be the attachment point for the new sub tree
		 * @param newSubtree A PAL node representing new sub tree
		 * @param originalParentConnection The orginal parent connection for the original node
		 * @param parentConnection The new parent connection for us
		 * @param tool to aid in construction
		 */
		private InternalNode(InternalNode original, Connection attachmentPoint, Node newSubtree,  Connection originalParentConnection, Connection parentConnection, ConstructionTool tool) {
		  this.connections_[0] = parentConnection;
			final Connection originalLeft = original.getLeft(originalParentConnection);
			final Connection originalRight = original.getRight(originalParentConnection);

			this.connections_[1] = new Connection(originalLeft,attachmentPoint,newSubtree,original, this, tool);
			this.connections_[2] = new Connection(originalRight,attachmentPoint,newSubtree,original, this, tool);
			this.index_ = tool.allocateNextUNodeIndex(this);
			this.calculator_ = tool.allocateNewInternalCalculator();

			final int numberOfSites = tool.getNumberOfSites();
			this.patterns_ = new PatternInfo[] {
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true)
				};
		}
		/**
		 * The constructor for the internal node that is added to attach a new sub tree with the altered tree stuff
		 * @param parentConnection The newly create parent connection (the root of the recursion, the caller), forming one of the children from this node
		 * @param newSubtree The PAL node of the sub tree which forms one of the children from this node
		 * @param originalParentConnection The original parent connection
		 * @param originalOtherChild The remaining child, to be cloned from an original
		 * @param otherChildLength the length of the connection to the "other child"
		 * @param tool to aid in construction
		 */
		private InternalNode(Connection parentConnection, Node newSubTree, UNode originalOtherChild,   Connection originalOtherChildParentConnection, double otherChildLength, ConstructionTool tool) {
		  this.index_ = tool.allocateNextUNodeIndex(this);
			this.calculator_ = tool.allocateNewInternalCalculator();
			final int numberOfSites = tool.getNumberOfSites();
			this.patterns_ = new PatternInfo[] {
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true)
				};
			//From parent
			this.connections_[0] = parentConnection;

			//From new sub tree
			this.connections_[1] = new Connection(newSubTree,this, tool);

			//From other child
			this.connections_[2] = new Connection(originalOtherChild,originalOtherChildParentConnection,this, otherChildLength, tool);

		}
		/**
		 * Cloning constructor
		 * @param originalNode The orginal internal node that we are replacing
		 * @param originalParentConnection The orginal parent connection
		 * @param parentConnection The replacement for the parent connect
		 * @param tool to aid in construction
		 */
		private InternalNode(InternalNode originalNode, Connection originalParentConnection, Connection parentConnection, ConstructionTool tool) {
		  this.connections_[0] = parentConnection;
			final Connection originalLeft = originalNode.getLeft(originalParentConnection);
			final Connection originalRight = originalNode.getRight(originalParentConnection);

			this.connections_[1] = new Connection(originalLeft,originalNode, this, tool);
			this.connections_[2] = new Connection(originalRight,originalNode, this, tool);
			this.index_ = tool.allocateNextUNodeIndex(this);
			this.calculator_ = tool.allocateNewInternalCalculator();

			final int numberOfSites = tool.getNumberOfSites();
			this.patterns_ = new PatternInfo[] {
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true),
					new PatternInfo(numberOfSites,true)
				};
		}
		public UNode createAlteredCopy(Connection attachmentPoint, Node newSubtree, Connection originalParentConnection, Connection parentConnection, ConstructionTool tool) {
			return new InternalNode(this, attachmentPoint,newSubtree,originalParentConnection,parentConnection,tool);
		}
		public UNode createAlteredCopy( Connection originalParentConnection, Connection parentConnection, ConstructionTool tool) {
			return new InternalNode(this, originalParentConnection,parentConnection,tool);
		}

		public boolean isLeaf() { return false; }
		public boolean hasLabel(String label) { return false; }

		public void mark() {
			markConnections_[0] = connections_[0];
			markConnections_[1] = connections_[1];
			markConnections_[2] = connections_[2];
		}
		public void setAnnotation(Object annotation) {		  this.annotation_ = annotation;		}
		public Object getAnnotation() { return annotation_; }
		public String getLabel() { return null; }

		public void undoToMark() {
			connections_[0] = markConnections_[0];
			connections_[1] = markConnections_[1];
			connections_[2] = markConnections_[2];
			topologyChanged();
		}
		private final void topologyChanged() {
		  this.topologyChangedSinceLastFlat_ = true;
		  this.topologyChangedSincleLastExtended_ = true;
		}
		public boolean hasDirectConnection(Connection c) {
			for(int i = 0 ; i < connections_.length ; i++) {
					if(connections_[i]==c) {	return true;	}
				}
				return false;

		}
		public boolean hasConnection(Connection c, Connection caller) {
			for(int i = 0 ; i < connections_.length ; i++) {
				if((connections_[i]==c)||(connections_[i]!=caller&&connections_[i].hasConnection(c,this))) {
					return true;
				}
			}
			return false;
		}

		public final int getIndex() { return index_; }
		public void testLikelihood(Connection caller, SubstitutionModel model, ConstructionTool tool) {
			for(int i = 0 ; i < connections_.length ; i++) {
				if(connections_[i]!=caller) {
					connections_[i].testLikelihood(this,model,tool);
				}
			}
		}
		public void setConnections(Connection[] store, int number){
			if(number!=3) {
				throw new IllegalArgumentException("Must be three connections not:"+number);
			}
			System.arraycopy(store,0,connections_,0,3);
			topologyChanged();
		}

		public void getLeafNames(ArrayList store, Connection caller) {
			for(int i = 0 ; i < connections_.length ; i++) {
				if(connections_[i]!=caller) {
					connections_[i].getLeafNames(store,this);
				}
			}
		}

		public void getSplitInformation(int[] splitStore, String[] leafNames, int splitIndex, Connection caller) {
			for(int i = 0 ; i < connections_.length ; i++) {
				if(connections_[i]!=caller) {
					connections_[i].getSplitInformation(splitStore,leafNames,splitIndex,this);
				}
			}
		}

		//Interchange related
		public Connection getLeft(Connection caller) {
			return connections_[LEFT_LOOKUP[getCallerIndex(caller)]];
		}

		public Connection getRight(Connection caller) {
			return connections_[RIGHT_LOOKUP[getCallerIndex(caller)]];
		}
		public Connection extract(Connection caller) {
			int callerIndex = getCallerIndex(caller);
			Connection left = connections_[LEFT_LOOKUP[callerIndex]];
			Connection right = connections_[RIGHT_LOOKUP[callerIndex]];
			UNode rightNode = right.getOther(this);
			left.swapNode(this,rightNode);
			rightNode.swapConnection(right,left);
			topologyChanged();
			return right;
		}
		public void swapConnection(Connection original, UNode nodeToReplace, Connection newConnection) {
			int index = getCallerIndex(original);
			connections_[index] = newConnection;
			newConnection.swapNode(nodeToReplace,this);
			original.swapNode(this,nodeToReplace);

			nodeToReplace.swapConnection(newConnection,original);
			topologyChanged();
		}
		public void swapConnection(Connection original,Connection newConnection) {
			int index = getCallerIndex(original);
			connections_[index] = newConnection;
			topologyChanged();
		}
		public PatternInfo getPatternInfo(Connection caller) {
			return patterns_[getCallerIndex(caller)];
		}

		public Node buildPALNode(double branchLength, Connection caller) {
			final int callerIndex = getCallerIndex(caller);
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
			Node[] children = new Node[] {
				leftConnection.buildPALNode(this), rightConnection.buildPALNode(this)
			};
			return NodeFactory.createNodeBranchLength(branchLength,children);
		}

		public void instruct(UnrootedTreeInterface.UNode node, Connection callingConnection){
			if(annotation_!=null) {		  node.setAnnotation(annotation_);			}

			final int callerIndex = getCallerIndex(callingConnection);
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
			final UnrootedTreeInterface.UNode leftNode = node.createUChild();
			final UnrootedTreeInterface.UNode rightNode = node.createUChild();
		  leftConnection.instruct(leftNode.getParentUBranch(),this);
			rightConnection.instruct(rightNode.getParentUBranch(),this);
		}

		public String toString(Connection caller) {
			StringBuffer sb = new StringBuffer();
			boolean printed = false;
			for(int i = 0 ; i < connections_.length ; i++) {
				if(connections_[i]!=caller) {
					if(printed) {
						sb.append(", ");
					}
					printed = true;
					sb.append(connections_[i].toString(this));
				}
			}
			return sb.toString();
		}
		// --=-=-==--=
		public int rebuildPattern(ConstructionTool tool) {
			return rebuildPattern(tool, null,true);
		}
		/**
		 * We can handle being redirected to use, so we just call rebuild pattern
		 */
		public int redirectRebuildPattern(ConstructionTool tool) {
			return rebuildPattern(tool);
		}
		public void getAllConnections(ArrayList store, Connection caller) {
			for(int i = 0 ; i < connections_.length ; i++) {
				if(connections_[i]!=caller) {
					connections_[i].getAllConnections(store,this);
				}
			}
		}
		private final int getCallerIndex(Connection caller) {
			if(caller==null) {
				throw new IllegalArgumentException("getCallerIndex() called on null object");
			}
			if(caller==connections_[0]) { return 0; }
			if(caller==connections_[1]) { return 1; }
			if(caller==connections_[2]) { return 2; }
			throw new IllegalArgumentException("Unknown caller");
		}


		public int rebuildPattern(ConstructionTool tool, Connection caller,boolean firstPass) {
			if(firstPass) {
				//First pass
				for(int i = 0 ; i < connections_.length ; i++) {
					if(connections_[i]!=caller) {
						UNode other = connections_[i].getOther(this);
						other.rebuildPattern(tool,connections_[i],firstPass);
					}
				}
				if(caller==null) {
					//Second pass
					return rebuildPattern(tool,null,false);
				} else {
					return rebuildMyPattern(tool, getCallerIndex(caller));
				}
			} else {
				//Second pass
				for(int i = 0 ; i < connections_.length ; i++) {
					if(connections_[i]!=caller) {
						rebuildMyPattern(tool,i);
					}
				}
				for(int i = 0 ; i < connections_.length ; i++) {
					if(connections_[i]!=caller) {
						connections_[i].getOther(this).rebuildPattern(tool,connections_[i],firstPass);
					}
				}
				int max = 0;
				for(int i = 0; i < patterns_.length ; i++) {
					int count = patterns_[i].getNumberOfPatterns();
					if(count>max) { max = count; }
				}
				return max;
			}
		}

		public ConditionalProbabilityStore getLeftExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore) {
			final int callerIndex = getCallerIndex(callingConnection);
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			return leftConnection.getExtendedConditionalProbabilities(model, modelChanged, this,external, resultStore);
		}
		public ConditionalProbabilityStore getRightExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore) {
			final int callerIndex = getCallerIndex(callingConnection);
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
			return rightConnection.getExtendedConditionalProbabilities(model, modelChanged, this, external, resultStore);
		}
		public PatternInfo getLeftPatternInfo(Connection caller) {
			final int callerIndex = getCallerIndex(caller);
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final UNode other = leftConnection.getOther(this);
			return other.getPatternInfo(leftConnection);
		}

		public PatternInfo getRightPatternInfo(Connection caller) {
			final int callerIndex = getCallerIndex(caller);
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
			final UNode other = rightConnection.getOther(this);
			return other.getPatternInfo(rightConnection);
		}


		public ConditionalProbabilityStore getFlatConditionalProbabilities( final SubstitutionModel model, final boolean modelChanged, final Connection callerConnection, LHCalculator.External externalCalculator, ConditionalProbabilityStore resultStore) {
			final int callerIndex = getCallerIndex(callerConnection);
			final PatternInfo pi = patterns_[callerIndex];
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];

			externalCalculator.calculateFlat(
					 pi,
					 leftConnection.getExtendedConditionalProbabilities(model, modelChanged, this,0,true),
					 rightConnection.getExtendedConditionalProbabilities(model, modelChanged, this,0,false),
					 resultStore
					 );
			return resultStore;

		}
		public ConditionalProbabilityStore getFlatConditionalProbabilities( final SubstitutionModel model, final boolean modelChanged, final Connection callerConnection, int depth, boolean isForLeft) {
			final int callerIndex = getCallerIndex(callerConnection);
			final PatternInfo pi = patterns_[callerIndex];
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
			topologyChangedSinceLastFlat_ = false;
			return calculator_.calculateFlat( pi,
					 leftConnection.getExtendedConditionalProbabilities(model, modelChanged, this,depth+1,true),
					 rightConnection.getExtendedConditionalProbabilities(model, modelChanged, this,depth+1,false)
					 );
		}
		public ConditionalProbabilityStore getExtendedConditionalProbabilities( final double distance, final SubstitutionModel model, final boolean modelChanged,  final Connection callerConnection, LHCalculator.External externalCalculator, ConditionalProbabilityStore resultStore) {
			final int callerIndex = getCallerIndex(callerConnection);
			final PatternInfo pi = patterns_[callerIndex];
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];


			externalCalculator.calculateExtended(
				distance,model, pi,
				leftConnection.getExtendedConditionalProbabilities(model, modelChanged, this,0,true),
				rightConnection.getExtendedConditionalProbabilities(model, modelChanged, this,0,false),
				resultStore
				);
			return resultStore;
		}
		public ConditionalProbabilityStore getExtendedConditionalProbabilities( final double distance, final SubstitutionModel model, final boolean modelChanged,  final Connection callerConnection, int depth, boolean isForLeft) {
			final int callerIndex = getCallerIndex(callerConnection);
			final PatternInfo pi = patterns_[callerIndex];
			final Connection leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final Connection rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		  final boolean childrenChanged = topologyChangedSincleLastExtended_;
			topologyChangedSincleLastExtended_ = false;
			return calculator_.calculateExtended(
				distance,model, pi,
				leftConnection.getExtendedConditionalProbabilities(model, modelChanged, this,depth+1,true),
				rightConnection.getExtendedConditionalProbabilities(model, modelChanged, this,depth+1,false),
				modelChanged
				);
		}

		// -=-==--=
		private int rebuildMyPattern(ConstructionTool tool, int index) {
			Connection leftConnection = connections_[LEFT_LOOKUP[index]];
			Connection rightConnection = connections_[RIGHT_LOOKUP[index]];

			final UNode left = leftConnection.getOther(this);
			final UNode right = rightConnection.getOther(this);
			final PatternInfo leftPattern= left.getPatternInfo(leftConnection);
			final PatternInfo rightPattern= right.getPatternInfo(rightConnection);
			return tool.build(patterns_[index],leftPattern,rightPattern);
		} //End of rebuildPatternWeights()
	}

// -=-=-=-=-=-=-==--=-=-=-=-=-=-==--=-=-==-=--=-=-=-==-=--=-=-=-=-=-=-==-=-=-=-
// === LeafNode
// -=-=-=-=-=-=-==--=-=-=-=-=-=-==--=-=-==-=--=-=-=-==-=--=-=-=-=-=-=-==-=-=-=-

	private static final class LeafNode implements UNode {
		private final String id_;
		private Connection parentConnection_;
		private Connection markParentConnection_;
		private final int[] sequence_;
		private final PatternInfo pattern_;
		private final int index_;

		private final LHCalculator.Leaf leafCalculator_;

		private Object annotation_;

		public LeafNode(String id, Connection parentConnection, ConstructionTool tool ) {
			this.id_ = id;
			this.parentConnection_ = parentConnection;
			this.index_ = tool.allocateNextUNodeIndex(this);
			this.sequence_ = tool.getSequence(id_);
			if(sequence_==null) {
				this.leafCalculator_ = null; this.pattern_ = null;
			} else {
				final int numberOfSites = sequence_.length;
				final int numberOfStates = tool.getNumberOfStates();

				final int[] patternStateMatchup = new int[numberOfStates+1];
				final int[] sitePatternMatchup = new int[numberOfSites];

				final int uniqueCount = createMatchups( numberOfSites, numberOfStates, sitePatternMatchup, patternStateMatchup );

				this.leafCalculator_ = tool.createNewLeafCalculator( patternStateMatchup, uniqueCount );
				this.pattern_ = new PatternInfo( sitePatternMatchup, uniqueCount );
			}
		}
		public void getLeafNames(ArrayList store, Connection caller) {
		  if(caller!=parentConnection_) {
				throw new RuntimeException("Unknown caller!");
			}
			store.add(id_);
		}
		public boolean isLeaf() { return true; }
		public boolean hasLabel(String label) { return id_.equals(label); }

		public void getSplitInformation(int[] splitStore, String[] leafNames, int splitIndex, Connection caller) {
			if(caller!=parentConnection_) {
				throw new RuntimeException("Unknown caller!");
			}
			for(int i = 0 ; i < leafNames.length ;i++) {
				if(id_.equals(leafNames[i])) {
					splitStore[i] = splitIndex;
				}
			}
		}
		public void setAnnotation(Object annotation) {		  this.annotation_ = annotation;		}
		public Object getAnnotation() { return annotation_; }
		public String getLabel() { return id_; }
		/**
		 * Fill in matchup arrahs
		 * @param numberOfSites The number of sites
		 * @param sitePatternMatchup Should be of length numberOfSites
		 * @param patternStateMatchup Should be of length numberOfStates+1
		 * @return
		 */
		private final int createMatchups(final int numberOfSites, final int numberOfStates, final int[] sitePatternMatchup, final int[] patternStateMatchup) {
		  final int[] stateCount  = new int[numberOfStates+1];
			// StatePatternMatchup matches a state to it's new pattern (is undefined if state does not occur)
			final int[] statePatternMatchup = new int[numberOfStates+1];
			int uniqueCount = 0;
			for(int site = 0 ; site < numberOfSites ; site++) {
				final int state = sequence_[site];
				if(stateCount[state]==0) {
					stateCount[state] = 1;
					int pattern = uniqueCount++;
					patternStateMatchup[pattern] = state;
					statePatternMatchup[state] = pattern;
				} else {
					stateCount[state]++;
				}
				sitePatternMatchup[site] = statePatternMatchup[state];
			}
			return uniqueCount;
		}
		public LeafNode(Node c, Connection parentConnection, ConstructionTool tool ) {
			this(c.getIdentifier().getName(),parentConnection,tool);
		}
		private LeafNode(LeafNode base, Connection parentConnection, ConstructionTool tool ) {
			this.id_ = base.id_;
			this.parentConnection_ = parentConnection;
			this.index_ = tool.allocateNextUNodeIndex(this);
			this.sequence_ = tool.getSequence(id_);

			final int numberOfSites = sequence_.length;
			final int numberOfStates = tool.getNumberOfStates();

			final int[] patternStateMatchup = new int[numberOfStates+1];
			final int[] sitePatternMatchup = new int[numberOfSites];

			final int uniqueCount = createMatchups(numberOfSites,numberOfStates,sitePatternMatchup,patternStateMatchup);

			this.leafCalculator_ = tool.createNewLeafCalculator(patternStateMatchup,uniqueCount);
			this.pattern_ = new PatternInfo(sitePatternMatchup,uniqueCount);
		}


		public UNode createAlteredCopy(Connection attachmentPoint, Node newSubtree, Connection originalParentConnection, Connection parentConnection, ConstructionTool tool) {
			return new LeafNode(this, parentConnection,	tool);
		}
		public UNode createAlteredCopy(Connection originalParentConnection, Connection parentConnection, ConstructionTool tool) {
			return new LeafNode(this, parentConnection,	tool);
		}
		public void instruct(UnrootedTreeInterface.UNode node, Connection callingConnection){
		  node.setLabel(id_);
			if(annotation_!=null) {		  node.setAnnotation(annotation_);			}
			if(callingConnection!=parentConnection_) {
			  throw new IllegalArgumentException("Unknown calling connection!");
			}
		}

		public boolean hasDirectConnection(Connection c) {	return parentConnection_==c;	}
		public void mark() {		this.markParentConnection_ = parentConnection_;		}
		public void undoToMark() {	this.parentConnection_ = markParentConnection_;		}
		public boolean hasConnection(Connection c, Connection caller) {
			if(caller!=parentConnection_) {		throw new IllegalArgumentException("Unknown caller!");	}
			return parentConnection_==c;
		}
		public Connection extract(Connection caller) {
			if(caller!=parentConnection_) {		throw new IllegalArgumentException("Unknown caller!");	}
			return null;
		}

		public ConditionalProbabilityStore getLeftExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External externalCalculator, ConditionalProbabilityStore resultStore){
			throw new RuntimeException("Assertion error : Not applicable for leaf nodes!");
		}
		public ConditionalProbabilityStore getRightExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged,  Connection callingConnection, LHCalculator.External externalCalculator, ConditionalProbabilityStore resultStore){
			throw new RuntimeException("Assertion error : Not applicable for leaf nodes!");
		}
		public PatternInfo getLeftPatternInfo(Connection caller){		return null;	}
		public PatternInfo getRightPatternInfo(Connection caller) {	return null;	}

		public void setConnections(Connection[] store, int number){
			if(number!=1) {		throw new IllegalArgumentException("Must be one connection not:"+number);		}
			this.parentConnection_ = store[0];
		}

		public void testLikelihood(Connection caller, SubstitutionModel model, ConstructionTool tool) {
			if(caller!=parentConnection_) {	throw new IllegalArgumentException("Unknown caller!");		}
		}
		public final int getIndex() { return index_; }
		public void swapConnection(Connection original,  Connection newConnection) {
			if(original!=parentConnection_) {		throw new IllegalArgumentException("Unknown original");		}
			this.parentConnection_ = newConnection;
		}
		public void swapConnection(Connection original, UNode nodeToReplace, Connection newConnection) {
			swapConnection(original,newConnection);
			newConnection.swapNode(nodeToReplace,this);
			original.swapNode(this,nodeToReplace);
		}

		/**
		 * @return null (as not possible)
		 */
		public Connection getLeft(Connection caller) {	return null;	}
		/**
		 * @return null (as not possible)
		 */
		public Connection getRight(Connection caller) {	return null;	}

		public void getAllConnections(ArrayList store, Connection caller) {
			if(caller!=parentConnection_) {		throw new IllegalArgumentException("Unknown caller!");		}
		}
		public PatternInfo getPatternInfo(Connection caller){
			if(caller!=parentConnection_) {		throw new IllegalArgumentException("Unknown caller!");		}
			return pattern_;
		}
		public void rebuildConnectionPatterns(ConstructionTool tool, Connection caller) {
			if(caller!=parentConnection_){	throw new IllegalArgumentException("Unknown caller!");			}
		}

		public int rebuildPattern( ConstructionTool tool, Connection caller, boolean firstPass) {
			if(caller!=parentConnection_) {		throw new IllegalArgumentException("Uknown caller!");		}
			return pattern_.getNumberOfPatterns();
		}
		public int rebuildPattern(ConstructionTool tool) {	return parentConnection_.getOther(this).redirectRebuildPattern(tool);		}

		/**
		 * This should only be called by another leaf node on the other end of the connection.
		 * In this case we don't have to do much (tree is two node tree)
		 */
		public int redirectRebuildPattern(ConstructionTool tool) {		return pattern_.getNumberOfPatterns();		}

		public final ConditionalProbabilityStore getFlatConditionalProbabilities(SubstitutionModel model, boolean modelChanged, Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore) {
			if(callingConnection!=parentConnection_) {		throw new IllegalArgumentException("Unknown calling connection");			}
			return leafCalculator_.getFlatConditionalProbabilities();
		}
		public final ConditionalProbabilityStore getFlatConditionalProbabilities(final SubstitutionModel model, final boolean modelChanged, final Connection callingConnection, int depth, boolean isForLeft) {
			if(callingConnection!=parentConnection_) {		throw new IllegalArgumentException("Unknown calling connection");			}
			return leafCalculator_.getFlatConditionalProbabilities();
		}

		public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, SubstitutionModel model, boolean modelChanged, Connection callingConnection, LHCalculator.External external, ConditionalProbabilityStore resultStore) {
		  if(callingConnection!=parentConnection_) {	throw new IllegalArgumentException("Unknown calling connection");		}
			return leafCalculator_.getExtendedConditionalProbabilities(distance,model,modelChanged);
		}
		public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, SubstitutionModel model, boolean modelChanged, Connection callingConnection, int depth, boolean isForLeft) {
			if(callingConnection!=parentConnection_) {	throw new IllegalArgumentException("Unknown calling connection");			}
			return leafCalculator_.getExtendedConditionalProbabilities(distance,model,modelChanged);
		}
		public Node buildPALNode(double branchLength, Connection caller) {
			if(caller!=parentConnection_) {		throw new IllegalArgumentException("Unknown calling connection"); 	}
			return NodeFactory.createNodeBranchLength(branchLength, new Identifier(id_));
		}
		public String toString(Connection caller) {		return id_; 	}
	} //End of class Leaf

//

	private static final class BranchAccessImpl implements BranchAccess {
	  private final Connection peer_;
		private final UnrootedMLSearcher base_;
		public BranchAccessImpl(Connection peer, UnrootedMLSearcher base) {
		  this.peer_ = peer;
			this.base_ = base;
		}
		public boolean isLeafBranch(String leafLabel) {
		  return peer_.isLeafBranch(leafLabel);
		}
		public UnrootedMLSearcher attach(Node subTree, Alignment alignment) {
	  	return new UnrootedMLSearcher(base_, peer_, subTree, alignment,base_.model_ );
	  }
		public UnrootedMLSearcher attach(String sequence, Alignment alignment) {
	  	return new UnrootedMLSearcher(base_, peer_, NodeFactory.createNode(new Identifier(sequence)), alignment, base_.model_ );
	  }
		public UnrootedMLSearcher attach(Node subTree, Alignment alignment, SubstitutionModel model) {
	  	return new UnrootedMLSearcher(base_, peer_, subTree, alignment, model );
	  }
		public UnrootedMLSearcher attach(String sequence, Alignment alignment, SubstitutionModel model) {
	  	return new UnrootedMLSearcher(base_, peer_, NodeFactory.createNode(new Identifier(sequence)), alignment, model );
	  }
		public void setAnnotation(Object annotation) {
		  peer_.setAnnotation(annotation);
		}
		public Object getAnnotation() { return peer_.getAnnotation(); }
		public String[] getLeftLeafNames() {
			return peer_.getLeftLeafNames();
		}
		public String[] getRightLeafNames() {
		  return peer_.getRightLeafNames();
		}
		public int[] getSplitInformation(String[] leafNames) {
		  return peer_.getSplitInformation(leafNames);
		}
	}

	private static final class NodeAccessImpl implements NodeAccess {
	  private final UNode peer_;
		private final UnrootedMLSearcher base_;
		public NodeAccessImpl(UNode peer, UnrootedMLSearcher base) {
		  this.peer_ = peer;
			this.base_ = base;
		}
		public boolean isLeaf() {	  return peer_.isLeaf();		}
		public void setAnnotation(Object annotation) {		  peer_.setAnnotation(annotation);		}
		public Object getAnnotation() { return peer_.getAnnotation(); }
		public String getLabel() { return peer_.getLabel(); }
	}

// -=-=-=-=-=-==--==--==--=-=-==-=--=-=-=-==-=-=--==-=-=--==--==-=-=--==--==--=
// == Connection ===
// =--=-==-=--=-==--=-==--=-=-=-=-==-=-=--=-==-=-=-=--==-=-=-=-=--==-=-=-=-=-=-
	private static final class Connection {
		private UNode leftNode_;
		private UNode rightNode_;
		private double branchLength_;
		private final PatternInfo centerPattern_;
		private boolean centerPatternValid_;

		private final int index_;

		private UNode markLeftNode_ = null;
		private UNode markRightNode_ = null;
		private double markBranchLength_;

		private Object annotation_ = null;

		/**
		 * The random tree constructor, for the root node (recursion just started)
		 * @param leafNames the names of all the leafs to be placed in this tree
		 * @param tool an aid for construction
		 * @param r a random number generator to determine branching patterns
		 */
		public Connection(String[] leafNames, ConstructionTool tool, MersenneTwisterFast r) {
			this.index_ = tool.allocateNextConnectionIndex();
			String[][] split = SearcherUtils.split(leafNames,r);
			this.leftNode_ = createUNode(split[0],this,tool,r);
			this.rightNode_ = createUNode(split[1],this,tool,r);
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
			this.branchLength_ = CONSTRUCTED_BRANCH_LENGTH;
		}

		/**
		 * A random tree constructor, into the recursion
		 * @param leafNames The names of leaves remaining to be created
		 * @param parent the parent UNode (from previous recursion)
		 * @param tool to aid in construction
		 * @param r for determining branching
		 */
		public Connection(String[] leafNames , UNode parent, ConstructionTool tool, MersenneTwisterFast r) {
			this.index_ = tool.allocateNextConnectionIndex();
			this.branchLength_ = CONSTRUCTED_BRANCH_LENGTH;
			this.rightNode_ = parent;
			this.leftNode_ = createUNode(leafNames,this, tool,r);
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}
		/**
		 * The starting constructor for building from a given tree
		 * @param n The normal PAL node structure to base this tree on
		 * @param tool to aid in construction
		 */
		public Connection(Node n,  ConstructionTool tool) {
			if(n.getChildCount()!=2) {
				throw new IllegalArgumentException("Base tree must be bificating");
			}
			this.index_ = tool.allocateNextConnectionIndex();
			Node l = n.getChild(0);
			Node r = n.getChild(1);
			this.branchLength_ = l.getBranchLength()+r.getBranchLength();

			leftNode_ = createUNode(l, this, tool);
			rightNode_ = createUNode(r, this, tool);

			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}
		/**
		 * Continuing recurison constructor for a given tree
		 * @param n The PAL node structure to base sub tree on
		 * @param parent The parent node (sub tree in other direction)
		 * @param tool to aid in construction
		 */
		public Connection(Node n, UNode parent, ConstructionTool tool) {
			this.index_ = tool.allocateNextConnectionIndex();
			this.branchLength_ = n.getBranchLength();
			this.rightNode_ = parent;
			this.leftNode_ = createUNode(n,this, tool);
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}

		/**
		 * A generic constructor given two already defined left and right children
		 * @param left The left node
		 * @param right The right node
		 * @param branchLength The length of connection
		 * @param tool to aid in construction
		 */
		public Connection(UNode left, UNode right, double branchLength, ConstructionTool tool) {
			this.index_ = tool.allocateNextConnectionIndex();
			this.branchLength_ = branchLength;
			this.rightNode_ = right;
			this.leftNode_ = left;
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}
		/**
		 *
		 * @param originalLeft
		 * @param originalLeftParentConnection
		 * @param right
		 * @param branchLength
		 * @param tool
		 */
		public Connection(UNode originalLeft, Connection originalLeftParentConnection, UNode right, double branchLength, ConstructionTool tool) {
			this.index_ = tool.allocateNextConnectionIndex();
			this.branchLength_ = branchLength;
			this.rightNode_ = right;
			this.leftNode_ = originalLeft.createAlteredCopy(originalLeftParentConnection,this,tool);
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}
		/**
		 * An altered tree constructor, initial starting point for recursion
		 * @param original The connection to base tree on
		 * @param attachmentPoint The original connection that is the attachment point for the new sub tree
		 * @param newSubtree The new sub tree as a normal PAL node
		 * @param tool to aid in construction
		 */
		public Connection(Connection original, Connection attachmentPoint, Node newSubtree, ConstructionTool tool) {
			//Allocate index like normal (do not keep indexes of original)
			this.index_ = tool.allocateNextConnectionIndex();

			final UNode baseRightNode = original.rightNode_.createAlteredCopy(attachmentPoint,newSubtree,original,this,tool);
//			final UNode baseLeftNode = original.leftNode_.createAlteredCopy(attachmentPoint,newSubtree,original,this,tool);
			if(original==attachmentPoint) {
			  //If this connection is where the sub tree is attached we will need to create a new Internal node "in the middle"
				// to attach the sub tree
				//This requires the addition of two extra connections (we go to node from right, another connection from left,
				//  and a third from subtree). All three connections meet at internal node (which is now the left node of this
				//  connection).

				//First step is to create subtree node, with this connection as it's parent
				final double splitLength = original.getBranchLength()/2;
			  this.branchLength_ = splitLength;

				this.leftNode_ = new InternalNode(this,newSubtree, original.leftNode_, original, splitLength,tool);
				this.rightNode_ = baseRightNode;
			} else {
			  this.rightNode_ = baseRightNode;
			  this.leftNode_ = original.leftNode_.createAlteredCopy(attachmentPoint,newSubtree,original,this,tool);
			  this.branchLength_ = original.getBranchLength();
			}
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}
		/**
		 * Altered copy constructor
		 *
		 * @param original The original connection this one is based on
		 * @param attachmentPoint The connection that is the attachment point for the new sub tree
		 * @param newSubtree The new subtree in normal PAL form (as not part of the original)
		 * @param originalParent The original parent of the original connection (directing recursion)
		 * @param newParent The newly created parent (following same recursion as for original stuff)
		 * @param tool The normal construction tool to help us out
		 */
		public Connection(Connection original, Connection attachmentPoint, Node newSubtree, UNode originalParent, UNode newParent, ConstructionTool tool) {
			this.index_ = tool.allocateNextConnectionIndex();
			this.branchLength_ = original.getBranchLength();
			final boolean parentIsLeft;
			if(originalParent==original.leftNode_) {
				parentIsLeft = true;
			} else if(originalParent==original.rightNode_) {
				parentIsLeft = false;
			} else {
			  throw new RuntimeException("Assertion error : orginalParent does not belong to original connection!");
			}
			if(original==attachmentPoint) {
			  //If this connection is where the sub tree is attached we will need to create a new Internal node "in the middle"
				// to attach the sub tree
				//This requires the addition of two extra connections (we go to node from right, another connection from left,
				//  and a third from subtree). All three connections meet at internal node (which is now the left node of this
				//  connection).

				//First step is to create subtree node, with this connection as it's parent
				final double splitLength = original.getBranchLength()/2;
			  this.branchLength_ = splitLength;
				if(parentIsLeft) {
		  		this.leftNode_ = newParent;
					this.rightNode_ = new InternalNode(this,newSubtree, original.rightNode_, original, splitLength,tool);
				} else {
		  		this.rightNode_ = newParent;
					this.leftNode_ = new InternalNode(this,newSubtree, original.leftNode_, original, splitLength,tool);
				}
			} else {
			  if(parentIsLeft) {
				  this.leftNode_ = newParent;
			    this.rightNode_ = original.rightNode_.createAlteredCopy(attachmentPoint,newSubtree,original,this,tool);
				} else {
					//Parent is right
				  this.rightNode_ = newParent;
			    this.leftNode_ = original.leftNode_.createAlteredCopy(attachmentPoint,newSubtree,original,this,tool);
				}
			}
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}
		/**
		 * The cloning constructor
		 * @param original The original connection that is being cloned
		 * @param originalParent The parent (dictating recursion) of the original connection
		 * @param newParent The new parent to substitute the original parent in the copy
		 * @param tool A tool to help us
		 */
		public Connection(Connection original,  UNode originalParent, UNode newParent, ConstructionTool tool) {
			this.index_ = tool.allocateNextConnectionIndex();
			this.branchLength_ = original.getBranchLength();
			if(originalParent==original.leftNode_) {
			  this.leftNode_ = newParent;
			  this.rightNode_ = original.rightNode_.createAlteredCopy(original,this,tool);
			} else if(originalParent==original.rightNode_) {
			  this.rightNode_ = newParent;
			  this.leftNode_ = original.leftNode_.createAlteredCopy(original,this,tool);
			} else {
			  throw new RuntimeException("Assertion error : orginalParent does not belong to original connection!");
			}
			this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
			this.centerPatternValid_ = false;
		}
		public void setAnnotation(Object annotation) {	  this.annotation_ = annotation;		}
		public Object getAnnotation() { return this.annotation_; }
		/**
		 *
		 * @return The "right" node of this connection.
		 */
		public final UNode getLeft() { return leftNode_; }
		/**
		 *
		 * @return The "left" node of this connection.
		 */
		public final UNode getRight() { return rightNode_; }
		private final String[] getLeafNames(UNode base) {
			ArrayList al = new ArrayList();
			base.getLeafNames(al, this);
			String[] result = new String[al.size()];
			al.toArray(result);
			return result;
		}
		public String[] getLeftLeafNames() {		return getLeafNames(leftNode_);		}
		public String[] getRightLeafNames() {		return getLeafNames(rightNode_);		}
		public int[] getSplitInformation(String[] leafNames) {
		  int[] result = new int[leafNames.length];
			for(int i = 0 ; i < result.length ; i++) { result[i] = 0;		}
			leftNode_.getSplitInformation(result,leafNames,-1,this);
			rightNode_.getSplitInformation(result,leafNames,1,this);
			return result;
		}
		public boolean isLeafBranch(String leafLabel) {
			if(leftNode_.isLeaf() ){
				if(leftNode_.hasLabel(leafLabel) ) { return true; }
			}
			if(rightNode_.isLeaf() ){
				if(rightNode_.hasLabel(leafLabel) ) { return true; }
			}
			return false;
		}
		public void getLeafNames(ArrayList store, UNode caller) {
			if(caller==leftNode_) {
				rightNode_.getLeafNames(store,this);
			} else if (caller==rightNode_) {
				leftNode_.getLeafNames(store,this);
			} else {
				throw new RuntimeException("Unknown caller!");
			}
		}

		public void getSplitInformation(int[] splitStore, String[] leafNames, int splitIndex, UNode caller) {
			if(caller!=leftNode_) {
				rightNode_.getSplitInformation(splitStore,leafNames,splitIndex,this);
			} else if (caller!=rightNode_) {
				leftNode_.getSplitInformation(splitStore,leafNames,splitIndex,this);
			} else {
				throw new RuntimeException("Unknown caller!");
			}
		}

		/**
		 * Mark this node, or in other words store information on left and right nodes and branch length for later retreival (via undoToMark())
		 */
		public final void mark() {
			this.markBranchLength_ = branchLength_;
			this.markLeftNode_ = leftNode_;		this.markRightNode_ = rightNode_;
		}
		/**
		 * @return The pattern info object for the left node leading to this connection
		 */
		public final PatternInfo getLeftPatternInfo() {		return leftNode_.getPatternInfo(this);	}
		/**
		 * @return The pattern info object for the right node leading to this connection
		 */
		public final PatternInfo getRightPatternInfo() {
			return rightNode_.getPatternInfo(this);
		}
		/**
		 *
		 * @return The pattern info across this connection (for use if this connection is the "root" of the likelihood calculation)
		 */
		public final PatternInfo getCenterPatternInfo(ConstructionTool tool) {
			if(!centerPatternValid_) {
				tool.build(centerPattern_, getLeftPatternInfo(),getRightPatternInfo());
				centerPatternValid_ = true;
			}
			return centerPattern_;
		}
		public final void instructBase(UnrootedTreeInterface.BaseBranch base) {
		  base.setLength(this.branchLength_);
			if(annotation_!=null) { base.setAnnotation(annotation_); }
			leftNode_.instruct(base.getLeftNode(),this);
			rightNode_.instruct(base.getRightNode(),this);
		}
		public final void instruct(UnrootedTreeInterface.UBranch branch, UNode callingNode) {
		  branch.setLength(this.branchLength_);
			if(annotation_!=null) { branch.setAnnotation(annotation_); }
			if(callingNode==leftNode_) {
			  rightNode_.instruct(branch.getFartherNode(),this);
			} else if(callingNode==rightNode_) {
			  leftNode_.instruct(branch.getFartherNode(),this);
			} else {
			  throw new IllegalArgumentException("Unknown calling node!");
			}
		}
		public final void undoToMark() {
			if(markLeftNode_==null) {
				throw new RuntimeException("Assertion error : undo to mark when no mark made");
			}
			this.branchLength_ = markBranchLength_;
			this.leftNode_ = markLeftNode_;
			this.rightNode_ = markRightNode_;
		}

		public String toString() {
			return "("+leftNode_+", "+rightNode_+")";
		}
		public boolean hasConnection(Connection c, UNode caller) {
			if(c==this) { return true; }
			if(caller==leftNode_) {
				return rightNode_.hasConnection(c,this);
			}
			if(caller==rightNode_) {
				return leftNode_.hasConnection(c,this);
			}
			throw new IllegalArgumentException("Unknown caller");
		}

		/**
		 * @return the "left" connection of the left node
		 */
		public Connection getLeftLeftConnection() {
			return leftNode_.getLeft(this);
		}
		/**
		 * @return the "right" connection of the left node
		 */
		public Connection getLeftRightConnection() {
			return leftNode_.getRight(this);
		}
		/**
		 * @return the "left" connection of the right node
		 */
		public Connection getRightLeftConnection() {
			return rightNode_.getLeft(this);
		}
		/**
		 * @return the "right" connection of the left node
		 */
		public Connection getRightRightConnection() {
			return rightNode_.getRight(this);
		}

		/**
		 * @return connection that by attaching to we would undo this operation, null if operation no successful
		 *
		 *
		 */
		public Connection attachTo(Connection attachmentPoint, Connection[] store) {

			final UNode used = (leftNode_.hasConnection(attachmentPoint, this) ? leftNode_ : rightNode_ );
			if(used.hasDirectConnection(attachmentPoint)) {
				return null;
			}
			final Connection redundant = used.extract(this);
			final Connection reattachment;
			final Connection leftUsed = used.getLeft(this);
			final Connection rightUsed = used.getRight(this);

			if(leftUsed==redundant) {
				reattachment = rightUsed;
			} else if(rightUsed == redundant) {
				reattachment = leftUsed;
			} else {
				throw new IllegalArgumentException("Assertion error");
			}
			if(redundant==null) {
				throw new RuntimeException("Assertion error : I should be able to extract from one of my nodes!");
			}

			UNode attachmentOldRight = attachmentPoint.rightNode_;
			//We will attach the old right to redundant, and move in the used node to the attachment point
			attachmentPoint.swapNode(attachmentOldRight,used);
			redundant.swapNode(redundant.getOther(used),attachmentOldRight);

			//Fix up old right to have correct attachments
			attachmentOldRight.swapConnection(attachmentPoint,redundant);

			//c.swapNode();
			//Fix up the used connections
			store[0] = this;
			store[1] = redundant;
			store[2] = attachmentPoint;
			used.setConnections(store,3);

			return reattachment;
		}
		public Node buildPALNode() {
			Node[] children = new Node[] {
				leftNode_.buildPALNode(branchLength_/2,this),
				rightNode_.buildPALNode(branchLength_/2,this)
			};
			return NodeFactory.createNode(children);
		}
		public Node buildPALNode(UNode caller) {
			if(leftNode_==caller) {
				return rightNode_.buildPALNode(branchLength_,this);
			}
			if(rightNode_==caller) {
				return leftNode_.buildPALNode(branchLength_,this);
			}
			throw new IllegalArgumentException("Unknown caller!");
		}

		/**
		 * @return -1 if null
		 */
		private final static int getIndex(Connection c) {
			if(c==null) { return -1;}
			return c.index_;
		}
		/**
		 * Fill in the index information of the two connected nodes
		 */
		public void fillInConnectionState(int[] array, int insertionIndex) {
			final int l = leftNode_.getIndex();
			final int r = rightNode_.getIndex();
			if(l<r) {
				array[insertionIndex++] = l;
				array[insertionIndex] = r;
			} else {
				array[insertionIndex++] = r;
				array[insertionIndex] = l;
			}
		}
		/**
		 * Does nothing to fix up tree structure
		 */
		public void setNodes(UNode left, UNode right) {
			this.leftNode_ = left;
			this.rightNode_ = right;
		}
		/**
		 * @note does not change the nodes connection information. Leaves tree in an inconsitent state
		 */
		public void swapNode(UNode nodeToReplace, UNode replacement) {
			if(nodeToReplace==leftNode_) {
				leftNode_ = replacement;
			} else if(nodeToReplace==rightNode_) {
				rightNode_ = replacement;
			} else {
				throw new RuntimeException("Unknown node to replace");
			}
		}
		public final ConditionalProbabilityStore getLeftFlatConditionalProbabilities(SubstitutionModel model, boolean modelChanged) {
			return leftNode_.getFlatConditionalProbabilities(model,modelChanged, this,0,true);
		}
		public final ConditionalProbabilityStore getRightFlatConditionalProbabilities(SubstitutionModel model, boolean modelChanged) {
			return rightNode_.getFlatConditionalProbabilities(model,modelChanged, this,0,false);
		}

		//Branch Length stuff
		public final double getBranchLength() { return branchLength_; }
		public final void setBranchLength(double x) { this.branchLength_ = x; }

		public String toString(UNode caller) {
			if(caller==leftNode_) {
				return rightNode_.toString(this);
			}
			if(caller!=rightNode_) {
				throw new RuntimeException("Unknown caller");
			}
			return leftNode_.toString(this);
		}
		public void testLikelihood(SubstitutionModel model, ConstructionTool tool) {
			testLikelihood(null,model,tool);
		}
		public void testLikelihood(UNode caller, SubstitutionModel model, ConstructionTool tool) {
			final int numberOfCategories = tool.getNumberOfTransitionCategories();
			final int numberOfSites = tool.getNumberOfSites();
			final int numberOfStates = tool.getNumberOfStates();


			final double[][][] resultStore = new double[numberOfCategories][numberOfSites][numberOfStates];
			System.out.println("Likleihood:"+calculateLogLikelihood(model, true,tool.allocateNewExternalCalculator(), tool));
			if(caller!=leftNode_) {
				leftNode_.testLikelihood(this,model,tool);
			}
			if(caller!=rightNode_){
				rightNode_.testLikelihood(this,model,tool);
			}
		}

		public ConditionalProbabilityStore getExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged, UNode caller, int depth, boolean isForLeft) {
			UNode other = getOther(caller);
			return other.getExtendedConditionalProbabilities(branchLength_,model, modelChanged, this,depth, isForLeft);
		}
		public ConditionalProbabilityStore getExtendedConditionalProbabilities( SubstitutionModel model, boolean modelChanged, UNode caller, LHCalculator.External externalCalculator, ConditionalProbabilityStore extendedStore) {
			UNode other = getOther(caller);
			return other.getExtendedConditionalProbabilities(branchLength_,model, modelChanged, this, externalCalculator, extendedStore);
		}
		public void setup( ConstructionTool tool, Connection[] allConnections) {
			//A call to the right node is not needed as the recursion will cut back that way
			if(tool.hasSequences()) {
				leftNode_.rebuildPattern( tool );
			}
			for(int i = 0 ; i < allConnections.length ; i++) {
				allConnections[i].centerPatternValid_ = false;
			}
		}

		public void getAllConnections(ArrayList store) {
			getAllConnections(store,null);
		}
		public void getAllConnections(ArrayList store, UNode caller) {
			store.add(this);
			if(caller!=leftNode_) {
				leftNode_.getAllConnections(store,this);
			}
			if(caller!=rightNode_) {
				rightNode_.getAllConnections(store,this);
			}


		}
		public void getCenterPatternInfo(PatternInfo store, ConstructionTool tool) {
			PatternInfo left = leftNode_.getPatternInfo(this);
			PatternInfo right = rightNode_.getPatternInfo(this);
			tool.build(store, left,right);
		}

		public UNode getOther(UNode caller) {
			if(leftNode_==caller) {
				return rightNode_;
			}
			if(rightNode_==caller) {
				return leftNode_;
			}
			throw new RuntimeException("Unknown caller!");
		}

		public final void doNNI(MersenneTwisterFast r) {
			doNNI(r.nextBoolean(),r.nextBoolean());
		}
		/**
		 * Does not reconstruct patterns
		 */
		public boolean doNNI(boolean leftSwapLeft, boolean rightSwapLeft) {
			Connection first = leftSwapLeft ? leftNode_.getLeft(this) : leftNode_.getRight(this);
			if(first==null) {
				return false;
			}
			Connection second = rightSwapLeft ? rightNode_.getLeft(this) : rightNode_.getRight(this);
			if(second==null) {
				return false;
			}
			leftNode_.swapConnection(first,rightNode_,second);
//			rightNode_.swapConnection(first,rightNode_,second);
			return true;
		}

		public double calculateLogLikelihood(SubstitutionModel model, boolean modelChanged, LHCalculator.External calculator, ConstructionTool tool) {
			PatternInfo pi = getCenterPatternInfo(tool);
			final ConditionalProbabilityStore leftConditionalProbabilityProbabilties =
				leftNode_.getFlatConditionalProbabilities(model, modelChanged, this,0,true);
			final ConditionalProbabilityStore rightConditionalProbabilityProbabilties =
				rightNode_.getExtendedConditionalProbabilities(branchLength_, model, modelChanged,  this,0,false);
			return calculator.calculateLogLikelihood(model, pi, leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties);
		}
		public double calculateLogLikelihood2(SubstitutionModel model, boolean modelChanged, LHCalculator.External calculator, ConstructionTool tool) {
		 PatternInfo pi = getCenterPatternInfo(tool);
		 final ConditionalProbabilityStore left = leftNode_.getFlatConditionalProbabilities(model, modelChanged, this,0,true);
		 final ConditionalProbabilityStore right = rightNode_.getFlatConditionalProbabilities(model, modelChanged,  this,0,false);
		 return calculator.calculateLogLikelihood(branchLength_, model, pi, left,right,tool.newConditionalProbabilityStore(false));
	 }
		public SiteDetails calculateSiteDetails(SubstitutionModel model, boolean modelChanged, LHCalculator.External calculator, ConstructionTool tool) {
			PatternInfo pi = getCenterPatternInfo(tool);
			final ConditionalProbabilityStore left = leftNode_.getFlatConditionalProbabilities(model, modelChanged, this,0,true);
			final ConditionalProbabilityStore right = rightNode_.getFlatConditionalProbabilities(model, modelChanged,  this,0,false);
		  return calculator.calculateSiteDetailsUnrooted(branchLength_, model,pi,left,right,tool.newConditionalProbabilityStore(false));
		}
	}

// -==--=-=-=-=-=-=-=-=-=-=-=-=-==--==-=-=--==--==-=--==-=--=-==-=--==--==-=-=-
// ==== OptimisationHandler ====
// -==--=-=-==--=-=-=-==--=-=-=-=-==--=-=-=-==-=-=-=-=-=-===--=-==--=-==--=-==-
	private static final class OptimisationHandler implements UnivariateFunction {
		private final double[][][] transitionProbabiltityStore_ ;
		private ConditionalProbabilityStore leftFlatConditionalProbabilities_;
		private ConditionalProbabilityStore rightFlatConditionalProbabilities_;

		private final SubstitutionModel model_;
		private final int numberOfCategories_;
		private final int numberOfStates_;
		private PatternInfo currentPatternInfo_;
		private final LHCalculator.External calculator_;
		private final ConditionalProbabilityStore tempStore_;
		private final UnivariateMinimum um_;
		private final ConstructionTool tool_;
		public OptimisationHandler(SubstitutionModel model,  ConstructionTool tool) {
			numberOfStates_ = model.getDataType().getNumStates();
			this.calculator_ = tool.allocateNewExternalCalculator();
			this.tempStore_ = tool.newConditionalProbabilityStore(false);
			this.tool_ = tool;
			numberOfCategories_ = model.getNumberOfTransitionCategories();
			this.transitionProbabiltityStore_ = new double[numberOfCategories_][numberOfStates_][numberOfStates_];
			this.model_ = model;
			this.um_ = new UnivariateMinimum();
		}
		/**
		 * Optimise the branch length of a certain connection
		 *
		 * @param c
		 * @param modelChanged
		 * @return maximum
		 */
		public double optimiseBranchLength(Connection c, boolean modelChanged) {
			setup(c,modelChanged);
			um_.findMinimum(c.getBranchLength(),this);

			c.setBranchLength(um_.minx);
			return -um_.fminx;
	}

		public void setup(Connection c, boolean modelChanged) {
			this.leftFlatConditionalProbabilities_ = c.getLeftFlatConditionalProbabilities(model_, modelChanged);

			this.rightFlatConditionalProbabilities_ = c.getRightFlatConditionalProbabilities(model_,modelChanged);
			this.currentPatternInfo_ = c.getCenterPatternInfo(tool_);
		}

		public double evaluate(double argument) {

			return -calculator_.calculateLogLikelihood(argument,model_,currentPatternInfo_,leftFlatConditionalProbabilities_,rightFlatConditionalProbabilities_, tempStore_);
		}

		public double getLowerBound() {	return MINIMUM_BRANCH_LENGTH; }
		public double getUpperBound() { return MAXIMUM_BRANCH_LENGTH; }

	}
// -==--=-=-=-=-=-=-=-=-=-=-=-=-==--==-=-=--==--==-=--==-=--=-==-=--==--==-=-=-
// ==== OptimisationHandler ====
// -==--=-=-==--=-=-=-==--=-=-=-=-==--=-=-=-==-=-=-=-=-=-===--=-==--=-==--=-==-
	private static final class NNIOptimisationHandler implements UnivariateFunction {
		private final double[][][] transitionProbabiltityStore_ ;
		private ConditionalProbabilityStore leftFlatConditionalProbabilities_;
		private ConditionalProbabilityStore rightFlatConditionalProbabilities_;

		private final ConditionalProbabilityStore leftFlatStore_;
		private final ConditionalProbabilityStore rightFlatStore_;
		private final ConditionalProbabilityStore leftLeftExtendedStore_;
		private final ConditionalProbabilityStore leftRightExtendedStore_;
		private final ConditionalProbabilityStore rightLeftExtendedStore_;
		private final ConditionalProbabilityStore rightRightExtendedStore_;

		private final ConditionalProbabilityStore tempStore_;

		private final SubstitutionModel model_;
		private final int numberOfCategories_;
		private final int numberOfStates_;
		private PatternInfo currentPatternInfo_;

		private final LHCalculator.External calculator_;
		private final UnivariateMinimum um_;
		private final ConstructionTool tool_;
		private final PatternInfo leftPatternStore_;
		private final PatternInfo rightPatternStore_;
		private final PatternInfo centerPatternStore_;
		private final Connection[] allConnections_;
		public NNIOptimisationHandler(Connection[] allConnections, SubstitutionModel model, ConstructionTool tool) {
			numberOfStates_ = model.getDataType().getNumStates();
			this.allConnections_ = allConnections;
			this.calculator_ = tool.allocateNewExternalCalculator();

			numberOfCategories_ = model.getNumberOfTransitionCategories();
			this.transitionProbabiltityStore_ = new double[numberOfCategories_][numberOfStates_][numberOfStates_];
			this.model_ = model;
			this.tool_ = tool;
			this.leftPatternStore_ = tool.constructFreshPatternInfo(true);
			this.rightPatternStore_ = tool.constructFreshPatternInfo(true);
			this.centerPatternStore_ = tool.constructFreshPatternInfo(true);
			this.tempStore_ = tool.newConditionalProbabilityStore(false);
			this.leftFlatStore_ = tool.newConditionalProbabilityStore(false);
			this.rightFlatStore_ = tool.newConditionalProbabilityStore(false);
			this.leftLeftExtendedStore_ = tool.newConditionalProbabilityStore(false);
			this.leftRightExtendedStore_ = tool.newConditionalProbabilityStore(false);
			this.rightLeftExtendedStore_ = tool.newConditionalProbabilityStore(false);
			this.rightRightExtendedStore_ = tool.newConditionalProbabilityStore(false);

			this.um_ = new UnivariateMinimum();
		}
		private final ConditionalProbabilityStore calculateFlat(PatternInfo patternInfo, ConditionalProbabilityStore resultStore, ConditionalProbabilityStore leftExtend, ConditionalProbabilityStore rightExtend) {
			calculator_.calculateFlat(patternInfo,leftExtend,rightExtend,resultStore);
			return resultStore;
		}

		/**
		 * Optimise the branch length of a certain connection
		 *
		 * @param c
		 * @param modelChanged
		 * @return True likelihood (not negated_
		 */
		public double optimiseSimulataneousNNIBranchLength(Connection c, boolean modelChanged) {
			 UNode leftNode = c.getLeft();
			 UNode rightNode = c.getRight();
			PatternInfo leftLeftPI = leftNode.getLeftPatternInfo(c);
			PatternInfo rightRightPI = rightNode.getRightPatternInfo(c);
			if(leftLeftPI==null||rightRightPI==null) {
				//One of the nodes is a leaf or something incompatible for NNI
				setup(c,modelChanged);
				um_.findMinimum(c.getBranchLength(),this);
				c.setBranchLength(um_.minx);
				return -um_.fminx;
			}
// =--=-=-=-==-=-
			PatternInfo leftRightPI = leftNode.getRightPatternInfo(c);
			PatternInfo rightLeftPI = rightNode.getLeftPatternInfo(c);

			ConditionalProbabilityStore leftLeftExtended = leftNode.getLeftExtendedConditionalProbabilities(model_,false,c,calculator_, leftLeftExtendedStore_);
			ConditionalProbabilityStore leftRightExtended = leftNode.getRightExtendedConditionalProbabilities(model_,false,c,calculator_, leftRightExtendedStore_);
			ConditionalProbabilityStore rightLeftExtended = rightNode.getLeftExtendedConditionalProbabilities(model_,false,c,calculator_,rightLeftExtendedStore_);
			ConditionalProbabilityStore rightRightExtended = rightNode.getRightExtendedConditionalProbabilities(model_,false,c,calculator_,rightRightExtendedStore_);

			tool_.build(rightPatternStore_, rightLeftPI,rightRightPI);
			tool_.build(leftPatternStore_, leftLeftPI,leftRightPI);
			tool_.build(centerPatternStore_, leftPatternStore_,rightPatternStore_);

			ConditionalProbabilityStore leftFlat = calculateFlat(c.getLeftPatternInfo(),leftFlatStore_,leftLeftExtended,leftRightExtended);
			ConditionalProbabilityStore rightFlat = calculateFlat(c.getRightPatternInfo(),rightFlatStore_, rightLeftExtended,rightRightExtended);
			setPatternInfo(leftFlat,rightFlat,c.getCenterPatternInfo(tool_));

			um_.findMinimum(c.getBranchLength(),this);
			final double standardMaxFX = -um_.fminx;
			final double standardMaxX = um_.minx;


			//Swap left left with right right
			tool_.build(leftPatternStore_, rightLeftPI,leftRightPI);
			tool_.build(rightPatternStore_, leftLeftPI,rightRightPI);
			tool_.build(centerPatternStore_, leftPatternStore_,rightPatternStore_);

			leftFlat = calculateFlat(leftPatternStore_,leftFlatStore_,rightLeftExtended,leftRightExtended);
			rightFlat = calculateFlat(rightPatternStore_,rightFlatStore_, leftLeftExtended,rightRightExtended);
			setPatternInfo(leftFlat,rightFlat,centerPatternStore_);

			um_.findMinimum(c.getBranchLength(),this);
			final double leftLeftSwapMaxX = um_.findMinimum(c.getBranchLength(),this);
			final double leftLeftSwapMaxFX = -um_.fminx;
// =--=-=-=-==-=-

			//Diag swap
			tool_.build(leftPatternStore_, rightRightPI,leftRightPI);
			tool_.build(rightPatternStore_, rightLeftPI,leftLeftPI);
			tool_.build(centerPatternStore_, leftPatternStore_,rightPatternStore_);

			leftFlat = calculateFlat(leftPatternStore_,leftFlatStore_,rightRightExtended,leftRightExtended);
			rightFlat = calculateFlat(rightPatternStore_,rightFlatStore_, rightLeftExtended,leftLeftExtended);



			setPatternInfo(leftFlat,rightFlat,centerPatternStore_);
			um_.findMinimum(c.getBranchLength(),this);

			final double diagSwapMaxX = um_.minx;
			final double diagSwapMaxFX = -um_.fminx;



			if(standardMaxFX>diagSwapMaxFX) {
				if(standardMaxFX>leftLeftSwapMaxFX) {
					//Standard wins!
					c.setBranchLength(standardMaxX);
					return standardMaxFX;
				} else {
					//left left wins
					c.doNNI(true,true);
					c.setup(tool_,allConnections_);
					c.setBranchLength(leftLeftSwapMaxX);
					return leftLeftSwapMaxFX;
				}
			} else {
				if(diagSwapMaxFX>leftLeftSwapMaxFX) {
					//Diag diag wins!
					c.setBranchLength(diagSwapMaxX);
					c.doNNI(false, true);
					c.setup(tool_,allConnections_);
					return diagSwapMaxFX;
				} else {
					//left left wins
					c.setBranchLength(leftLeftSwapMaxX);
					c.doNNI(true,true);
					c.setup(tool_,allConnections_);
					return leftLeftSwapMaxFX;
				}
			}
		}
		private void setup(Connection c, boolean modelChanged) {
			this.leftFlatConditionalProbabilities_ = c.getLeftFlatConditionalProbabilities(model_, modelChanged);
			this.rightFlatConditionalProbabilities_ = c.getRightFlatConditionalProbabilities(model_,modelChanged);
			this.currentPatternInfo_ = c.getCenterPatternInfo(tool_);
		}
		public void setPatternInfo(
				ConditionalProbabilityStore leftFlatConditionalProbabilities,
				ConditionalProbabilityStore rightFlatConditionalProbabilities,
				PatternInfo pi
			) {
			this.leftFlatConditionalProbabilities_ = leftFlatConditionalProbabilities;
			this.rightFlatConditionalProbabilities_ = rightFlatConditionalProbabilities;
			this.currentPatternInfo_ = pi;
		}

		public double evaluate(double argument) {
			return -calculator_.calculateLogLikelihood(argument,model_,currentPatternInfo_,leftFlatConditionalProbabilities_,rightFlatConditionalProbabilities_,tempStore_);
		}

		public double getLowerBound() {	return 0; }
		public double getUpperBound() { return 1; }

	}


	private static final class ConstructionTool {
		private final String[] names_;
		private final int[][] sequences_;
		private final int numberOfStates_;
		private final int numberOfCategories_;
		private final int numberOfSites_;
		private final DataType dt_;
		private int nextConnectionIndex_ = 0;
		private final ArrayList allUNodes_ = new ArrayList();
		private final LHCalculator.Generator calcGenerator_;
		public ConstructionTool(Alignment alignment, int numberOfStates, int numberOfCategories, LHCalculator.Factory calculatorFactory) {
		  if(alignment!=null) {
				DataType dt = alignment.getDataType();
				if( dt.isAmbiguous() ) {
					this.dt_ = dt.getAmbiguousVersion().getSpecificDataType();
				} else {
					this.dt_ = alignment.getDataType();
				}

				this.numberOfSites_ = alignment.getSiteCount();
			} else {
			  this.dt_ = null;
				this.numberOfSites_ = 0;
			}
			this.numberOfStates_ = numberOfStates;
			this.numberOfCategories_ = numberOfCategories;
			if(alignment!=null) {
		  	this.names_ = Identifier.getNames(alignment);
				this.sequences_ = pal.alignment.AlignmentUtils.getAlignedStates( alignment, dt_.getNumStates() );
				this.calcGenerator_ = calculatorFactory.createSeries(numberOfCategories,dt_);
			} else {
				this.names_ = null;
			  this.sequences_ = null;
				this.calcGenerator_ = null;
			}

		}
		public boolean hasSequences() { return sequences_!=null&&sequences_.length>0; }
		public PatternInfo constructFreshPatternInfo(boolean binaryPattern) {
			return new PatternInfo(numberOfSites_,binaryPattern);
		}
		public final ConditionalProbabilityStore newConditionalProbabilityStore(boolean isForLeaf) {
			return calcGenerator_.createAppropriateConditionalProbabilityStore(isForLeaf);
		}
		public final int allocateNextConnectionIndex() {
			return nextConnectionIndex_++;
		}
		public LHCalculator.Internal allocateNewInternalCalculator() {
			if(calcGenerator_!=null) {
				return calcGenerator_.createNewInternal();
			}
			return null;
		}
		public LHCalculator.External allocateNewExternalCalculator() {
			if(calcGenerator_!=null) {
				return calcGenerator_.createNewExternal();
			}
			return null;
		}
		public LHCalculator.Leaf createNewLeafCalculator(int[] patternStateMatchup, int numberOfPatterns) {
			if(calcGenerator_!=null) {
				return calcGenerator_.createNewLeaf( patternStateMatchup, numberOfPatterns );
			}
			return null;
		}
		public int build(PatternInfo beingBuilt, PatternInfo left, PatternInfo right) {
			return beingBuilt.build(left,right,numberOfSites_);
		}
		public final int allocateNextUNodeIndex(UNode node) {
			int index = allUNodes_.size();
			allUNodes_.add(node);
			return index;
		}
		public final UNode[] getOrderedNodes() {
			UNode[] result = new UNode[allUNodes_.size()];
			allUNodes_.toArray(result);
			return result;
		}
		public DataType getDataType() { return dt_; }
		public final int getNumberOfSites() { return numberOfSites_; }

		public int[] getSequence(String name) {
			if(sequences_==null) {
			  return null;
			}
			for(int i = 0 ; i < names_.length ; i++) {
				if(name.equals(names_[i])) {
					return sequences_[i];
				}
			}
			throw new IllegalArgumentException("Unknown sequence:"+name);
		}
		public int getNumberOfStates() { return numberOfStates_; }
		public int getNumberOfTransitionCategories() { return numberOfCategories_; }
	}
}