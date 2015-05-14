// PivotNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: PivotNode</p>
 * <p>Description: A pivot node connects to two ConstrainedNodes, and one FreeBranch </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.eval.*;
import pal.tree.*;
import java.util.*;
import pal.math.*;

public class PivotNode extends AbstractParentableConstrainedNode implements GeneralOptimisable, ParentableConstrainedNode, FreeNode, RootAccess, GroupLeader {
  private FreeBranch freeConnection_;
	private final UnconstrainedLikelihoodModel.Internal freeInternal_;

	private final GeneralConstraintGroupManager constraintGroupManager_;

	private final PatternInfo leftAscendedentPattern_;
	private boolean leftAscendentPatternValid_;

	private final PatternInfo rightAscendedentPattern_;
	private boolean rightAscendentPatternValid_;

	private final NonRootOptimisationHandler nonRootOptimistaionHandler_;
	private final RootOptimisationHandler rootOptimistaionHandler_;
	private final RootSubTreeShiftOptimisationHandler subTreeShiftOptimisationHandler_;
	private final RootPartialSubTreeShiftOptimisationHandler partialSubTreeShiftOptimisationHandler_;
	private final OptimisationHandler optimistaionHandler_;


	/**
	 * The subtree constructor with a connection to the rest of the tree
	 * @param tree the PAL node tree to base this tree on
	 * @param parentConnection The connection with the rest of tree (must be free, otherwise this shouldn't be the pivot)
	 * @param tool A general construction tool object for objtaining/building various components
	 * @param groupConstraints The constraints object that manages the leaf constraints
	 */
	public PivotNode(Node tree, FreeBranch parentConnection, GeneralConstructionTool tool, GeneralConstraintGroupManager constraintGroupManager, GeneralConstraintGroupManager.Store store) {
		super(tree,tool,store, constraintGroupManager);
		this.freeConnection_ = parentConnection;

		if(parentConnection!=null) {
			this.freeInternal_ = tool.allocateNewFreeInternalCalculator();
		} else {
			this.freeInternal_ = null;
		}
		this.constraintGroupManager_ = constraintGroupManager;

		this.leftAscendedentPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.leftAscendentPatternValid_ = false;

		this.rightAscendedentPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.rightAscendentPatternValid_ = false;
		if(freeConnection_==null) {
		  this.nonRootOptimistaionHandler_ = null;
			this.rootOptimistaionHandler_	= new RootOptimisationHandler(tool,getConstrainedInternal());
			this.optimistaionHandler_ = rootOptimistaionHandler_;
			this.partialSubTreeShiftOptimisationHandler_ =new RootPartialSubTreeShiftOptimisationHandler(tool,getConstrainedInternal());
			subTreeShiftOptimisationHandler_ = new RootSubTreeShiftOptimisationHandler(tool,getConstrainedInternal());
		} else {
		  this.nonRootOptimistaionHandler_ = new NonRootOptimisationHandler(tool,getConstrainedInternal());
			this.rootOptimistaionHandler_	= null;
			this.optimistaionHandler_ = nonRootOptimistaionHandler_;
			this.partialSubTreeShiftOptimisationHandler_ = null;
			subTreeShiftOptimisationHandler_ = null;
		}
		//Add ourselves to the constraint group manager (as we are a "group leader")
		recursivelySetChildrenParentPivot(this);
		constraintGroupManager_.addGroupLeader(this);
  }
	public void postSetupNotify(ConstraintModel.GroupManager groupConstraints) {
		setupInternalNodeHeights(groupConstraints);

	}


	/**
	 * The root constructor, only used when the whole tree is constrained
	 * @param tool A general construction tool object for objtaining/building various components
	 * @param subTree The subtree
	 * @param groupConstraints The constraints object that manages the leaf constraints
	 */
	public PivotNode(Node subTree, GeneralConstructionTool tool, GeneralConstraintGroupManager groupManager, GeneralConstraintGroupManager.Store store) {
		this(subTree, null, tool,  groupManager,store);
  }
	// ==================================================================================================
	// ===== Root Access stuff ============================================================================
	// ==================================================================================================

	public Node buildPALNodeBase() {
		Node n = buildDescendentPALNodeBase();
		NodeUtils.heights2Lengths(n);
		if(freeConnection_!=null) {
			Node free = freeConnection_.buildPALNodeBase(this);
			free.setBranchLength(freeConnection_.getBranchLength());
			n.addChild(free);
		  NodeUtils.lengths2Heights(free);
		}
		return n;
	}
	public Node buildPALNodeES() {
		Node n = buildDescendentPALNodeES(constraintGroupManager_.getRelatedGroup());
		NodeUtils.heights2Lengths(n);
		if(freeConnection_!=null) {
			Node free = freeConnection_.buildPALNodeES(this);
			free.setBranchLength(freeConnection_.getBranchLength());
			n.addChild(free);
		  NodeUtils.lengths2Heights(free);
		}
		return n;
	}
	public String toString() { return toStringLengths(getNodeHeight()); }
	public ConditionalProbabilityStore getAscendentExtended(double baseHeight, ConstrainedNode childCaller, GeneralConstructionTool tool, boolean allowCaching) {
		double height = getNodeHeight();
		if(isLeftChild(childCaller)) {
			ConditionalProbabilityStore right = getRightDescendentExtendedConditionals(tool,allowCaching);
			if(freeConnection_==null) {
				obtainConstrainedExternalCalculator().calculateSingleAscendentExtendedConditionalsDirect(height,baseHeight, getAscendentPatternInfo(childCaller,tool),right);
				return right;
			}
			ConditionalProbabilityStore top = freeConnection_.getExtendedConditionalProbabilities(this,tool);
			return getConstrainedInternal().calculateAscendentExtendedConditionals(height,baseHeight, getAscendentPatternInfo(childCaller,tool),top,right);
		} else {
			ConditionalProbabilityStore left = getLeftDescendentExtendedConditionals(tool,allowCaching);
			if(freeConnection_==null) {
				obtainConstrainedExternalCalculator().calculateSingleAscendentExtendedConditionalsDirect(height,baseHeight, getAscendentPatternInfo(childCaller,tool),left);
				return left;
			}
			ConditionalProbabilityStore top = freeConnection_.getExtendedConditionalProbabilities(this,tool);
			return getConstrainedInternal().calculateAscendentExtendedConditionals(height,baseHeight,getAscendentPatternInfo(childCaller,tool),top,left);
		}
	}
	public ConditionalProbabilityStore getAscendentFlat(ConstrainedNode childCaller, GeneralConstructionTool tool, boolean allowCaching) {
		if(isLeftChild(childCaller)) {
			ConditionalProbabilityStore right = getRightDescendentExtendedConditionals(tool,allowCaching);
			if(freeConnection_==null) {  return right;			}
			ConditionalProbabilityStore top = freeConnection_.getExtendedConditionalProbabilities(this,tool);
			return freeInternal_.calculateFlat(getAscendentPatternInfo(childCaller,tool),top,right);
		} else {
			ConditionalProbabilityStore left = getLeftDescendentExtendedConditionals(tool,allowCaching);
			if(freeConnection_==null) {  return left;			}
			ConditionalProbabilityStore top = freeConnection_.getExtendedConditionalProbabilities(this,tool);
			return freeInternal_.calculateFlat(getAscendentPatternInfo(childCaller,tool),top,left);

		}
	}
	public PatternInfo getAscendentPatternInfo(ConstrainedNode childCaller, GeneralConstructionTool tool) {
		if(isLeftChild(childCaller)) {
			if(freeConnection_==null) {  return getRightChildPatternInfo(tool);			}
			if(!leftAscendentPatternValid_) {
				tool.build(leftAscendedentPattern_,freeConnection_.getPatternInfo(tool,this),getRightChildPatternInfo(tool));
				leftAscendentPatternValid_ = true;
			}
			return leftAscendedentPattern_;
		} else {
			if(freeConnection_==null) {  return getLeftChildPatternInfo(tool);			}
			if(!rightAscendentPatternValid_) {
				tool.build(rightAscendedentPattern_,freeConnection_.getPatternInfo(tool,this),getLeftChildPatternInfo(tool));
				rightAscendentPatternValid_ = true;
			}
			return rightAscendedentPattern_;
		}
	}

	public double calculateLogLikelihood(  GeneralConstructionTool tool) {
		if(freeConnection_==null) {
			return getDescendentLogLikelihood(tool,false);
		}
		return freeConnection_.calculateLogLikelihood(tool);
	}


// ==================================================================================================
// ===== Free Node stuff ============================================================================
// ==================================================================================================
	private final void checkCaller(FreeBranch caller) {
		if(caller!=freeConnection_) { throw new RuntimeException("Assertion error : caller is not free connection!");		}
	}
	public PatternInfo getPatternInfo(GeneralConstructionTool tool, FreeBranch caller) {
		checkCaller(caller);	return getDescendentPatternInfo(tool);
	}
	public boolean hasConnection(FreeBranch c, FreeBranch caller) {
		checkCaller(caller); return c==freeConnection_;
	}
	public FreeBranch getLeftBranch(FreeBranch caller) { checkCaller(caller); return null;	}
	public FreeBranch getRightBranch(FreeBranch caller) { checkCaller(caller); return null; }


	public void getAllComponents(ArrayList store, Class componentType, FreeBranch caller) {
		checkCaller(caller);	getSubTreeComponents(store,componentType);
	}

	public void testLikelihood(FreeBranch caller, GeneralConstructionTool tool) {
		checkCaller(caller);
		testLikelihood(tool);
	}

	public void testLikelihood(GeneralConstructionTool tool) {
	  System.out.println("Test1 (Pivot)"+calculateLogLikelihood(tool));
		getLeftChild().testLikelihood(tool);
		getRightChild().testLikelihood(tool);
	}

	public PatternInfo getLeftPatternInfo(GeneralConstructionTool tool, FreeBranch caller) {
		return getLeftChildPatternInfo(tool);
	}
	public PatternInfo getRightPatternInfo(GeneralConstructionTool tool, FreeBranch caller) {
		return getRightChildPatternInfo(tool);
	}

	public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, FreeBranch caller, GeneralConstructionTool tool) {
		checkCaller(caller);
		throw new RuntimeException("Finish me!");
	}
	public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, FreeBranch caller, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore, GeneralConstructionTool tool) {
		checkCaller(caller);
		throw new RuntimeException("Finish me!");
	}
	/**
	 * We can't extract
	 * @param caller should be the same as free connection!
	 * @return null
	 */
	public FreeBranch extract(FreeBranch caller) { checkCaller(caller); return null;	}


	public Node buildPALNodeES(double branchLength_,FreeBranch caller) {
		checkCaller(caller);
		throw new RuntimeException("Finish me!");
	}
	public Node buildPALNodeBase(double branchLength_,FreeBranch caller) {
		checkCaller(caller);
		throw new RuntimeException("Finish me!");
	}

	public ConditionalProbabilityStore getFlatConditionalProbabilities(FreeBranch caller, GeneralConstructionTool tool) {
		checkCaller(caller);
		throw new RuntimeException("Finish me!");
	}

	public String toString(FreeBranch caller) {
		checkCaller(caller);
		throw new RuntimeException("Finish me!");
	}

	public void setConnectingBranches(FreeBranch[] store, int number) {
		if(number!=1) {
			throw new IllegalArgumentException("Invalid number of branches, must be 1");
		}
		this.freeConnection_ = store[0];
	}

	public boolean hasDirectConnection(FreeBranch query) { return query==query;	}

	/**
	 * Should not do anything but swap branches around
	 */
	public void swapConnection(FreeBranch original,FreeBranch newConnection) {
		if(original==freeConnection_) {
			freeConnection_ = newConnection;
		} else {
			throw new IllegalArgumentException("Unknown original node!");
		}
	}

	/**
	 * Should preserve tree integrity
	 */
	public void swapConnection(FreeBranch original, FreeNode nodeToReplace, FreeBranch newConnection) {
		throw new RuntimeException("Finish me!");
	}
//
// ==================================================================================================
// ===== Constrained Node stuff ============================================================================
// ==================================================================================================

	public void getNonSubTreeComponents(ArrayList store, Class componentType) {
		if(freeConnection_!=null) {
			freeConnection_.getAllComponents( store, componentType, this );
		}
	}


// ====================================================================================================
// == General Optimisable stuff
// ============================
	public int getNumberOfOptimisationTypes() { return (freeConnection_ ==null ? 3 : 1); }
	public double optimise(int optimisationType, UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
		switch(optimisationType) {
		  case 0 : { return optimiseLocalShift(minimiser,tool,fracDigits); }
			case 1 : { return optimisePartialSubTreeShift(minimiser,tool,fracDigits); }
			default :  { return optimiseSubTreeShift(minimiser,tool,fracDigits); }
		}
	}

	private final double optimiseSubTreeShift(UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
		double minOffset = getMinimumLeafChildSeperation();
		double baseHeight = getNodeHeight();

		MolecularClockLikelihoodModel.External external = obtainConstrainedExternalCalculator();

		subTreeShiftOptimisationHandler_.setup(
			getLeftChild(),getRightChild(),
			getDescendentPatternInfo(tool),baseHeight,
			-minOffset,
			100-minOffset,
			obtainConstrainedExternalCalculator(),
			fracDigits
		);
		subTreeShiftOptimisationHandler_.optimise(minimiser);
		recursivelyAdjustNodeHeight(subTreeShiftOptimisationHandler_);
		return subTreeShiftOptimisationHandler_.getLogLikelihood();
	}
	private final double optimisePartialSubTreeShift(UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
		double minOffset = getMinimumLeafChildSeperation();
		double baseHeight = getNodeHeight();

		MolecularClockLikelihoodModel.External external = obtainConstrainedExternalCalculator();

		partialSubTreeShiftOptimisationHandler_.setup(
			getLeftChild(),getRightChild(),
			getDescendentPatternInfo(tool),baseHeight,
			-minOffset,
			baseHeight*2+100-minOffset,
			obtainConstrainedExternalCalculator(),
			fracDigits
		);
		partialSubTreeShiftOptimisationHandler_.optimise(minimiser);
		recursivelyAdjustNodeHeight(partialSubTreeShiftOptimisationHandler_);
		return partialSubTreeShiftOptimisationHandler_.getLogLikelihood();
	}

	private final double optimiseLocalShift(UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
		double maxChildHeight = getMaxChildHeight();
		final ConditionalProbabilityStore leftDescendentBaseExtended = getLeftDescendentExtendedConditionals(maxChildHeight, tool,false);
		final ConditionalProbabilityStore rightDescendentBaseExtended = getRightDescendentExtendedConditionals(maxChildHeight, tool,false);
		final MolecularClockLikelihoodModel.External external = obtainConstrainedExternalCalculator();
		final double height = getNodeHeight();
		if(freeConnection_==null) {
		  rootOptimistaionHandler_.setup(
				leftDescendentBaseExtended, rightDescendentBaseExtended,
				getDescendentPatternInfo( tool ), height,
				maxChildHeight, obtainConstrainedExternalCalculator(), fracDigits
				);
		} else {
			final ConditionalProbabilityStore parentFlat = freeConnection_.getExtendedConditionalProbabilities( this, tool );
			nonRootOptimistaionHandler_.setup(
				parentFlat, freeConnection_.getPatternInfo( tool, this ),
				leftDescendentBaseExtended, rightDescendentBaseExtended,
				getDescendentPatternInfo( tool ), height,
				maxChildHeight, obtainConstrainedExternalCalculator(), fracDigits
				);
		}
		optimistaionHandler_.optimise(minimiser);
		final double heightDifference = optimistaionHandler_.getHeight() - height ;
//		System.out.println("height difference:"+heightDifference);
		setNodeHeight(optimistaionHandler_.getHeight());
		return optimistaionHandler_.getLogLikelihood();
	}

// - - - - - - - - - - - - - - -- - - - --- - -  -- - ---- -  ------ -- -

	private static interface OptimisationHandler {
		public double getHeight();
		public double getLogLikelihood();
		public void optimise(UnivariateMinimum minimiser);
	}

	private static final class NonRootOptimisationHandler implements UnivariateFunction,OptimisationHandler {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;
		private double height_;
		private ConditionalProbabilityStore ascendentFlat_;
		private ConditionalProbabilityStore leftBaseExtended_;
		private ConditionalProbabilityStore rightBaseExtended_;

		private PatternInfo descendentPattern_;
		private PatternInfo ascendentPattern_;
		private PatternInfo centerPattern_;

		private double maxChildHeight_;
		private double maxHeight_;

		private int fracDigits_;

		public NonRootOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
		  this.tool_ = tool;
			this.internal_ = internal;
		}
		public void setup(
		  ConditionalProbabilityStore ascendentFlat,
			PatternInfo ascendentPattern,
			ConditionalProbabilityStore leftBaseExtended,
			ConditionalProbabilityStore rightBaseExtended,
			PatternInfo descendentPattern, double startingHeight, double maxChildHeight,
			MolecularClockLikelihoodModel.External external,
			int fracDigits) {
		  this.height_ = startingHeight;
			this.ascendentFlat_ = ascendentFlat;
			this.ascendentPattern_ = ascendentPattern;
			this.leftBaseExtended_ = leftBaseExtended;
			this.rightBaseExtended_ = rightBaseExtended;
			this.descendentPattern_ = descendentPattern;

			this.external_ = external;
			this.fracDigits_ = fracDigits;
			this.maxChildHeight_ = maxChildHeight;
			this.maxHeight_ = maxChildHeight_+maxChildHeight*2+100;

		}
		public void optimise(UnivariateMinimum minimiser) {
			minimiser.findMinimum(height_,this,fracDigits_);
		  this.height_ = minimiser.minx;
			this.logLikelihood_ = -minimiser.fminx;
		}

		public double getHeight() { return height_; }
		public double getLogLikelihood() { return logLikelihood_; }

// - - - - - - - - - - - - - - -- - - - --- - -  -- - ---- -  ------ -- -

		// Univariate Function stuff
	  public double evaluate(double height) {
			ConditionalProbabilityStore descendentExtended = internal_.calculatePostExtendedFlatConditionals(height,maxChildHeight_,descendentPattern_,leftBaseExtended_,rightBaseExtended_);
		  double result = -external_.calculateLogLikelihoodNonRoot(height,centerPattern_,ascendentFlat_,descendentExtended);
			return result;
		}
		public double getLowerBound() { return maxChildHeight_; }

	  public double getUpperBound() { return maxHeight_; }
	} //End of class NonRootOptimisationHandler
	// ==========
	private static final class RootOptimisationHandler implements UnivariateFunction,OptimisationHandler {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;
		private double height_;
		private ConditionalProbabilityStore leftBaseExtended_;
		private ConditionalProbabilityStore rightBaseExtended_;

		private PatternInfo descendentPattern_;

		private double maxChildHeight_;
		private double maxHeight_;
		private double baseHeight_;

		private int fracDigits_;

		public RootOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
		  this.tool_ = tool;
			this.internal_ = internal;
		}
		public void setup(
		  ConditionalProbabilityStore leftBaseExtended,
			ConditionalProbabilityStore rightBaseExtended,
			PatternInfo descendentPattern, double startingHeight, double maxChildHeight, 			MolecularClockLikelihoodModel.External external,
			int fracDigits) {
		  this.height_ = startingHeight;
			this.leftBaseExtended_ = leftBaseExtended;
			this.rightBaseExtended_ = rightBaseExtended;
			this.descendentPattern_ = descendentPattern;
		  this.baseHeight_ = startingHeight;
			this.external_ = external;
			this.fracDigits_ = fracDigits;
			this.maxChildHeight_ = maxChildHeight;
			this.maxHeight_ = maxChildHeight_+100.0;

		}
		public void optimise(UnivariateMinimum minimiser) {
			minimiser.findMinimum(height_,this,fracDigits_);
		  this.height_ = minimiser.minx;
			this.logLikelihood_ = -minimiser.fminx;
		}

		public double getHeight() { return height_; }
		public double getLogLikelihood() { return logLikelihood_; }
		// Univariate Function stuff
	  public double evaluate(double height) {
			ConditionalProbabilityStore descendentExtended = internal_.calculatePostExtendedFlatConditionals(height,maxChildHeight_,descendentPattern_,leftBaseExtended_,rightBaseExtended_);
		  double result = -external_.calculateLogLikelihoodSingle(height,descendentPattern_,descendentExtended);
			return result;
		}
		public double getLowerBound() { return maxChildHeight_; }

	  public double getUpperBound() { return maxHeight_; }
	} //End of class RootOptimisationHandler

	private static final class RootSubTreeShiftOptimisationHandler implements UnivariateFunction, ConstrainedNode.HeightAdjustment {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;


		private PatternInfo descendentPattern_;

		private ConstrainedNode leftChild_;
		private ConstrainedNode rightChild_;

		private double baseHeight_;
		private double offset_;
		private double minimumOffset_;
		private double maximumOffset_;

		private int fracDigits_;

		public RootSubTreeShiftOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
		  this.tool_ = tool;
			this.internal_ = internal;
		}

// =============================================================
// ==== Height Adjustment Stuff ================================
		public double getAdjustedHeight(Object relatedNode, double baseHeightToAdjust) {
		  return baseHeightToAdjust+offset_;
		}

		public void setup(
			ConstrainedNode leftChild,
			ConstrainedNode rightChild,
			PatternInfo descendentPattern, double baseHeight, double minimumOffset, double maximumOffset,
			MolecularClockLikelihoodModel.External external,
			int fracDigits) {
		  this.baseHeight_ = baseHeight;

			this.descendentPattern_ = descendentPattern;
		  this.leftChild_ = leftChild;		this.rightChild_ = rightChild;
		  this.minimumOffset_ = minimumOffset;
			this.maximumOffset_ = maximumOffset;

			this.external_ = external;
			this.fracDigits_ = fracDigits;
			this.offset_ = 0;
		}
		public void optimise(UnivariateMinimum minimiser) {
			minimiser.findMinimum(0,this,fracDigits_);
		  this.offset_ = MathUtils.ensureBounded(minimiser.minx,minimumOffset_,maximumOffset_);
//			System.out.println("Offset:"+offset_);
			this.logLikelihood_ = -minimiser.fminx;
//			System.out.println("Log likelihood:"+logLikelihood_);
		}

		public double getHeight() { return baseHeight_+offset_; }
		public double getOffset() { return offset_; }
		public double getLogLikelihood() { return logLikelihood_; }
// - - - - - - - - - - - - - - -- - - - --- - -  -- - ---- -  ------ -- -
		// Univariate Function stuff
	  public double evaluate(double offset) {
		  this.offset_ = offset;
		  final double adjustedHeight = baseHeight_+offset;
			ConditionalProbabilityStore left = leftChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool_,this,false);
			ConditionalProbabilityStore right = rightChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool_,this,false);
			double result = -external_.calculateLogLikelihood(adjustedHeight,descendentPattern_,left,right);
			return result;
		}
		public double getLowerBound() { return minimumOffset_; }

	  public double getUpperBound() { return maximumOffset_; }
	}

// - - - - - - - - - - - - - - -- - - - --- - -  -- - ---- -  ------ -- -
	private final class RootPartialSubTreeShiftOptimisationHandler implements UnivariateFunction, ConstrainedNode.HeightAdjustment {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;

		private PatternInfo descendentPattern_;

		private ConstrainedNode leftChild_;
		private ConstrainedNode rightChild_;

		private double baseHeight_;
		private double offset_;
		private double minimumOffset_;
		private double maximumOffset_;

		private int fracDigits_;
		private ConstrainedNode[] affectedNodes_;
		private int numberOfAffectedNodes_ = 0;
		public RootPartialSubTreeShiftOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
		  this.tool_ = tool;
			this.internal_ = internal;
		}

// =============================================================
// ==== Height Adjustment Stuff ================================
		public double getAdjustedHeight(Object relatedNode, double baseHeightToAdjust) {
		  for(int i = 0; i < numberOfAffectedNodes_ ; i++) {
			  if(relatedNode == affectedNodes_[i]) {
				  return baseHeightToAdjust+offset_;
				}
			}
			return baseHeightToAdjust;
		}
		public int getNumberOfAffected() { return numberOfAffectedNodes_; }
		public void addMultification(ConstrainedNode base) {
//			System.out.print("*");
			addAffectedNode(base);
			final ConstrainedNode left = base.getLeftChild();
			final ConstrainedNode right = base.getRightChild();
			final double baseHeight = base.getNodeHeight();
			if(left!=null) {
//				System.out.println((baseHeight-left.getNodeHeight())+"    "+(baseHeight-right.getNodeHeight()));
			  if(baseHeight-left.getNodeHeight()<0.000001) {
				  addMultification(left);
				}
				if(baseHeight-right.getNodeHeight()<0.000001) {
				  addMultification(right);
				}
			}
		}
		public double getMinimumAffectedChildDistance() {
		  double distance = Double.POSITIVE_INFINITY;
			for(int i = 0 ; i < numberOfAffectedNodes_ ; i++) {
				distance = Math.min(distance,affectedNodes_[i].getMinimumDirectChildDistance());
			}
			return distance;
		}
		public void resetAffectedNodes() { this.numberOfAffectedNodes_ = 0; }
		public void addAffectedNode(ConstrainedNode node) {
		  if(affectedNodes_==null) {
			  this.affectedNodes_ = new ConstrainedNode[10];
			} else {
			  if(numberOfAffectedNodes_==affectedNodes_.length) {
				  ConstrainedNode[] newAffected = new ConstrainedNode[numberOfAffectedNodes_+5];
					System.arraycopy(affectedNodes_,0,newAffected,0,numberOfAffectedNodes_);
					this.affectedNodes_ = newAffected;
				}
			}
			affectedNodes_[numberOfAffectedNodes_++] = node;
		}
		public void setup(
			ConstrainedNode leftChild,
			ConstrainedNode rightChild,
			PatternInfo descendentPattern,
			double baseHeight, double minimumOffset, double maximumOffset,
			MolecularClockLikelihoodModel.External external,
			int fracDigits) {
		  this.baseHeight_ = baseHeight;

			 this.descendentPattern_ = descendentPattern;
		  this.leftChild_ = leftChild;		this.rightChild_ = rightChild;
		  this.minimumOffset_ = minimumOffset;
			this.maximumOffset_ = maximumOffset;

			this.external_ = external;
			this.fracDigits_ = fracDigits;
			this.offset_ = 0;
//		  test();
//			System.out.println("Evaluate:"+evaluate(baseHeight));
//			System.out.println("Base:"+calculateLogLikelihood(tool_));
		}
		public void optimise(UnivariateMinimum minimiser) {
			minimiser.findMinimum(0,this,fracDigits_);
		  this.offset_ = MathUtils.ensureBounded(minimiser.minx,minimumOffset_,maximumOffset_);
			this.logLikelihood_ = -minimiser.fminx;
		}

		public double getHeight() { return baseHeight_+offset_; }
		public double getOffset() { return offset_; }
		public double getLogLikelihood() { return logLikelihood_; }
// - - - - - - - - - - - - - - -- - - - --- - -  -- - ---- -  ------ -- -
		// Univariate Function stuff
	  public double evaluate(double offset) {
		  this.offset_ = offset;
		  final double adjustedHeight = baseHeight_+offset;
			ConditionalProbabilityStore left = leftChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool_,this,false);
			ConditionalProbabilityStore right = rightChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool_,this,false);
			double result = -external_.calculateLogLikelihood(adjustedHeight,descendentPattern_,left,right);
			return result;
		}
		public double getLowerBound() { return minimumOffset_; }
	  public double getUpperBound() { return maximumOffset_; }
	}
}