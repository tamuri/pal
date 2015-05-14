// ConstrainedInternalNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: ConstrainedInternalNode </p>
 * <p>Description: An internal node that is constrained by a molecular clock constraint</p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.math.*;
import pal.tree.*;


public class ConstrainedInternalNode extends AbstractParentableConstrainedNode implements ConstrainedNode,GeneralOptimisable {
  private ParentableConstrainedNode parentNode_;

	private final PatternInfo centerPattern_;
	private boolean centerPatternValid_;

	private final PatternInfo leftAscendedentPattern_;
	private boolean leftAscendentPatternValid_;

	private final PatternInfo rightAscendedentPattern_;
	private boolean rightAscendentPatternValid_;

	private final LocalShiftOptimisationHandler localShiftOptimisationHandler_;
	private final SubTreeShiftOptimisationHandler subTreeShiftOptimisationHandler_;
	private final PartialSubTreeShiftOptimisationHandler partialSubTreeShiftOptimisationHandler_;

	private PivotNode parentPivot_;

	public ConstrainedInternalNode(Node peer, ParentableConstrainedNode parentNode,  GeneralConstructionTool tool, GeneralConstraintGroupManager.Store store, GeneralConstraintGroupManager groupManager) {
		super(peer, tool,store,groupManager);
		this.parentNode_ = parentNode;

		this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.centerPatternValid_ = false;

		this.leftAscendedentPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.leftAscendentPatternValid_ = false;

		this.rightAscendedentPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.rightAscendentPatternValid_ = false;

		this.localShiftOptimisationHandler_ = new LocalShiftOptimisationHandler(tool,getConstrainedInternal());
		this.subTreeShiftOptimisationHandler_ = new SubTreeShiftOptimisationHandler(tool,getConstrainedInternal());
  	this.partialSubTreeShiftOptimisationHandler_ = new PartialSubTreeShiftOptimisationHandler(tool,getConstrainedInternal());
  }
	public void recursivelySetParentPivot(PivotNode parentPivot) {
		this.parentPivot_ = parentPivot;
		recursivelySetChildrenParentPivot(parentPivot);
	}
	public void getNonSubTreeComponents(ArrayList store, Class componentType) {
		if(parentNode_!=null) {	parentNode_.getNonSubTreeOfChildComponents(store, componentType, this);		}
	}
	public double calculateLogLikelihood(GeneralConstructionTool tool) {
		double height = getNodeHeight();
//		final ConditionalProbabilityStore descendentFlat = getDescendentFlatConditionals(tool);
//		final ConditionalProbabilityStore parentExtended = parentNode_.getAscendentExtended(height,this,tool);
		final ConditionalProbabilityStore descendentExtended = getDescendentExtendedConditionals(parentNode_.getNodeHeight(), tool,false);
		final ConditionalProbabilityStore parentFlat = parentNode_.getAscendentFlat(this,tool,false);
		MolecularClockLikelihoodModel.External external = obtainConstrainedExternalCalculator();
//		return external.calculateLogLikelihoodNonRoot(height,getCenterPattern(tool),parentExtended,descendentFlat);
		return external.calculateLogLikelihoodNonRoot(parentNode_.getNodeHeight(),getCenterPattern(tool),parentFlat,descendentExtended);
	}
	/**
	 * For center pattern, left is ascendent component, right is descendent component
	 * @param tool The consturction tool
	 * @return Pattern info
	 */
	public PatternInfo getCenterPattern(GeneralConstructionTool tool) {
		if(!centerPatternValid_) {
			tool.build(centerPattern_,parentNode_.getAscendentPatternInfo(this,tool),getDescendentPatternInfo(tool));
		  centerPatternValid_ = true;
		}
		return centerPattern_;
	}
	public ConditionalProbabilityStore getAscendentExtended(double baseHeight, ConstrainedNode childCaller, GeneralConstructionTool tool, boolean allowCaching) {
		final double height = getNodeHeight();
		if(isLeftChild(childCaller)) {
			ConditionalProbabilityStore right = getRightDescendentExtendedConditionals(tool,allowCaching);
			ConditionalProbabilityStore top = parentNode_.getAscendentExtended(height, this,tool,allowCaching);
			return getConstrainedInternal().calculateAscendentExtendedConditionals(height,baseHeight,getAscendentPatternInfo(childCaller,tool),top,right);
		} else {
			ConditionalProbabilityStore left = getLeftDescendentExtendedConditionals(tool,allowCaching);
			ConditionalProbabilityStore top = parentNode_.getAscendentExtended(height, this,tool,allowCaching);
			return getConstrainedInternal().calculateAscendentExtendedConditionals(height,baseHeight,getAscendentPatternInfo(childCaller,tool),top,left);
		}
	}
	public ConditionalProbabilityStore getAscendentFlat(ConstrainedNode childCaller, GeneralConstructionTool tool, boolean allowCaching) {
		final double height = getNodeHeight();
		if(isLeftChild(childCaller)) {
			ConditionalProbabilityStore right = getRightDescendentExtendedConditionals(tool,allowCaching);
			ConditionalProbabilityStore top = parentNode_.getAscendentExtended(height, this,tool,allowCaching);
			return getConstrainedInternal().calculateAscendentFlatConditionals(getAscendentPatternInfo(childCaller,tool),top,right);
		} else {
			ConditionalProbabilityStore left = getLeftDescendentExtendedConditionals(tool,allowCaching);
			ConditionalProbabilityStore top = parentNode_.getAscendentExtended(height, this,tool,allowCaching);
			return getConstrainedInternal().calculateAscendentFlatConditionals(getAscendentPatternInfo(childCaller,tool),top,left);
		}
	}
	public void testLikelihood(GeneralConstructionTool tool) {
		System.out.println("Test (CIN):"+calculateLogLikelihood(tool));
		getLeftChild().testLikelihood(tool);
		getRightChild().testLikelihood(tool);
	}
	public PatternInfo getAscendentPatternInfo(ConstrainedNode childCaller, GeneralConstructionTool tool) {
		if(isLeftChild(childCaller)) {
			if(!leftAscendentPatternValid_) {
				tool.build(leftAscendedentPattern_,parentNode_.getAscendentPatternInfo(this,tool),getRightChildPatternInfo(tool));
				leftAscendentPatternValid_ = true;
			}
			return leftAscendedentPattern_;
		} else {
			if(!rightAscendentPatternValid_) {
				tool.build(rightAscendedentPattern_,parentNode_.getAscendentPatternInfo(this,tool),getLeftChildPatternInfo(tool));
				rightAscendentPatternValid_ = true;
			}
			return rightAscendedentPattern_;
		}
	}

// ========================================================================================================
// ========== General Optimiser Stuff
	public int getNumberOfOptimisationTypes() { return 3; }
	/**
	 *
	 * @param minimiser The single dimensional minimisation tool
	 * @param tool The construction tool
	 * @param fracDigits the number of fractional digits to converge to
	 * @return The optimised log likelihood
	 */
	public double optimise(int optimisationType, UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
	  switch(optimisationType) {
			case 0:
				return optimiseLocalShift(minimiser, tool, fracDigits);
			case 1:
				return optimisePartialSubTreeShift(minimiser, tool, fracDigits);
			default:
				return optimiseSubTreeShift(minimiser, tool, fracDigits);
		}
	}

	private final double optimiseLocalShift(UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
		double maxChildHeight = getMaxChildHeight();
		double parentHeight = parentNode_.getNodeHeight();
		if(maxChildHeight>=parentHeight) { return 1; }
		double myHeight = getNodeHeight();

		final ConditionalProbabilityStore temp = tool.obtainTempConditionalProbabilityStore();
		final ConditionalProbabilityStore leftDescendentBaseExtended = getLeftDescendentExtendedConditionals(maxChildHeight, tool,false);
		final ConditionalProbabilityStore rightDescendentBaseExtended = getRightDescendentExtendedConditionals(maxChildHeight, tool,false);

		final ConditionalProbabilityStore parentFlat = parentNode_.getAscendentFlat(this,tool,false);

		MolecularClockLikelihoodModel.External external = obtainConstrainedExternalCalculator();
		localShiftOptimisationHandler_.setup(
		  parentFlat,
			parentNode_.getAscendentPatternInfo(this,tool),
			getCenterPattern(tool),
			leftDescendentBaseExtended, rightDescendentBaseExtended,
			getDescendentPatternInfo(tool),myHeight,
			maxChildHeight,parentHeight,
			obtainConstrainedExternalCalculator(),
			temp,fracDigits
		);
		localShiftOptimisationHandler_.optimise(minimiser);
		setNodeHeight(localShiftOptimisationHandler_.getHeight());
		return localShiftOptimisationHandler_.getLogLikelihood();
	}

	private final double optimiseSubTreeShift(UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
		final double minOffset = getMinimumLeafChildSeperation();
		final double parentHeight = parentNode_.getNodeHeight();
		final double maxChildHeight = getMaxChildHeight();
		final double baseHeight = getNodeHeight();
//		System.out.println("Min offset:"+minOffset);
//		if(minOffset<=0) {	  return 1;		}
//		if((baseHeight-maxChildHeight)>0.000000001) {
//		  return 1;
//		}

		final ConditionalProbabilityStore temp = tool.obtainTempConditionalProbabilityStore();

		final ConditionalProbabilityStore parentFlat = parentNode_.getAscendentFlat(this,tool,false);

		MolecularClockLikelihoodModel.External external = obtainConstrainedExternalCalculator();

		subTreeShiftOptimisationHandler_.setup(
		  parentFlat,
			parentNode_.getAscendentPatternInfo(this,tool),
			getCenterPattern(tool),
			getLeftChild(),getRightChild(),
			getDescendentPatternInfo(tool),baseHeight,
			-minOffset,parentHeight-baseHeight,
			parentHeight,
			obtainConstrainedExternalCalculator(),
			temp,fracDigits
		);
		subTreeShiftOptimisationHandler_.optimise(minimiser);
		recursivelyAdjustNodeHeight(subTreeShiftOptimisationHandler_);
		return  subTreeShiftOptimisationHandler_.getLogLikelihood();
	}
	private final double optimisePartialSubTreeShift(UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
//		final double minOffset = getMinimumLeafChildSeperation();
		final double parentHeight = parentNode_.getNodeHeight();
		final double maxChildHeight = getMaxChildHeight();
		final double baseHeight = getNodeHeight();
//		if( minOffset<0 ) {	return 1; }
//		if((baseHeight-maxChildHeight)>0.000000001) {
//		  return 1;
//		}

		final ConditionalProbabilityStore temp = tool.obtainTempConditionalProbabilityStore();

		final ConditionalProbabilityStore parentFlat = parentNode_.getAscendentFlat( this, tool,false );

		MolecularClockLikelihoodModel.External external = obtainConstrainedExternalCalculator();
		partialSubTreeShiftOptimisationHandler_.resetAffectedNodes();
		partialSubTreeShiftOptimisationHandler_.addMultification(this);
		if(partialSubTreeShiftOptimisationHandler_.getNumberOfAffected()==1) {
		  return 1;
		}
		partialSubTreeShiftOptimisationHandler_.setup(
			parentFlat,
			parentNode_.getAscendentPatternInfo( this, tool ),
			getCenterPattern( tool ),
			getLeftChild(), getRightChild(),
			getDescendentPatternInfo( tool ), baseHeight,
			-partialSubTreeShiftOptimisationHandler_.getMinimumAffectedChildDistance(), parentHeight-baseHeight,
			parentHeight,
			obtainConstrainedExternalCalculator(),
			temp, fracDigits
			);
		partialSubTreeShiftOptimisationHandler_.optimise( minimiser );
		recursivelyAdjustNodeHeight( partialSubTreeShiftOptimisationHandler_ );
		double l = partialSubTreeShiftOptimisationHandler_.getLogLikelihood();
		return l;
	}


// ========================================================================================================

	private final class LocalShiftOptimisationHandler implements UnivariateFunction {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;
		private double height_;
		private ConditionalProbabilityStore ascendentFlat_;
		private ConditionalProbabilityStore leftBaseExtended_;
		private ConditionalProbabilityStore rightBaseExtended_;

		private ConditionalProbabilityStore tempConditionals_;

		private PatternInfo descendentPattern_;

		private PatternInfo ascendentPattern_;

		private PatternInfo centerPattern_;

		private double maxChildHeight_;
		private double parentHeight_;

		private int fracDigits_;

		public LocalShiftOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
		  this.tool_ = tool;
			this.internal_ = internal;
		}
		public void setup(
		  ConditionalProbabilityStore ascendentFlat,
			PatternInfo ascendentPattern,
			PatternInfo centerPattern,
			ConditionalProbabilityStore leftBaseExtended,
			ConditionalProbabilityStore rightBaseExtended,
			PatternInfo descendentPattern,
			double startingHeight,
			double maxChildHeight,
			double parentHeight,
			MolecularClockLikelihoodModel.External external,
			ConditionalProbabilityStore tempConditionals,
			int fracDigits) {
		  this.height_ = startingHeight;
			this.ascendentFlat_ = ascendentFlat;
			this.ascendentPattern_ = ascendentPattern;
			this.centerPattern_ = centerPattern;
			this.leftBaseExtended_ = leftBaseExtended;
			this.rightBaseExtended_ = rightBaseExtended;
			this.tempConditionals_ = tempConditionals;
			this.descendentPattern_ = descendentPattern;

			this.external_ = external;
			this.fracDigits_ = fracDigits;
			this.parentHeight_ = parentHeight;
			this.maxChildHeight_ = maxChildHeight;
		}
		public void optimise(UnivariateMinimum minimiser) {

			minimiser.findMinimum(height_,this,fracDigits_);
		  this.height_ = MathUtils.ensureBounded(minimiser.minx, maxChildHeight_, parentHeight_);
			this.logLikelihood_ = -minimiser.fminx;
		}

		public double getHeight() { return height_; }
		public double getLogLikelihood() { return logLikelihood_; }
// - - - - - - - - - - - - - - -- - - - --- - -  -- - ---- -  ------ -- -
		// Univariate Function stuff
	  public double evaluate(double height) {
			ConditionalProbabilityStore descendentExtended = internal_.calculatePostExtendedFlatConditionals(height,maxChildHeight_,descendentPattern_,leftBaseExtended_,rightBaseExtended_);
		  external_.calculateSingleAscendentExtendedConditionalsIndirect(parentHeight_,height,ascendentPattern_,ascendentFlat_,tempConditionals_);
			double result = -external_.calculateLogLikelihoodNonRoot(height,centerPattern_,tempConditionals_,descendentExtended);
			return result;
		}
		public double getLowerBound() { return maxChildHeight_; }

	  public double getUpperBound() { return parentHeight_; }
	}
// ========================================================================================================
// ========================================================================================================

	private static final class LocalShiftPlusOptimisationHandler implements UnivariateFunction {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;
		private double height_;
		private ConditionalProbabilityStore ascendentFlat_;
		private ConditionalProbabilityStore leftBaseExtended_;
		private ConditionalProbabilityStore rightBaseExtended_;

		private ConditionalProbabilityStore tempConditionals_;

		private PatternInfo descendentPattern_;

		private PatternInfo ascendentPattern_;

		private PatternInfo centerPattern_;

		private double maxChildHeight_;
		private double parentHeight_;

		private int fracDigits_;

		public LocalShiftPlusOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
		  this.tool_ = tool;
			this.internal_ = internal;
		}
		public void setup(
		  ConditionalProbabilityStore ascendentFlat,
			PatternInfo ascendentPattern,
			PatternInfo centerPattern,
			ConditionalProbabilityStore leftBaseExtended,
			ConditionalProbabilityStore rightBaseExtended,
			PatternInfo descendentPattern,
			double startingHeight,
			double maxChildHeight,
			double parentHeight,
			MolecularClockLikelihoodModel.External external,
			ConditionalProbabilityStore tempConditionals,
			int fracDigits) {
		  this.height_ = startingHeight;
			this.ascendentFlat_ = ascendentFlat;
			this.ascendentPattern_ = ascendentPattern;
			this.centerPattern_ = centerPattern;
			this.leftBaseExtended_ = leftBaseExtended;
			this.rightBaseExtended_ = rightBaseExtended;
			this.tempConditionals_ = tempConditionals;
			this.descendentPattern_ = descendentPattern;

			this.external_ = external;
			this.fracDigits_ = fracDigits;
			this.parentHeight_ = parentHeight;
			this.maxChildHeight_ = maxChildHeight;
		}
		public void optimise(UnivariateMinimum minimiser) {

			minimiser.findMinimum(height_,this,fracDigits_);
		  this.height_ = MathUtils.ensureBounded(minimiser.minx, maxChildHeight_, parentHeight_);
			this.logLikelihood_ = -minimiser.fminx;
//			System.out.println("Post:"+evaluate(height_)+"  to "+logLikelihood_);
		}

		public double getHeight() { return height_; }
		public double getLogLikelihood() { return logLikelihood_; }
// - - - - - - - - - - - - - - -- - - - --- - -  -- - ---- -  ------ -- -
		// Univariate Function stuff
	  public double evaluate(double height) {
			ConditionalProbabilityStore descendentExtended = internal_.calculatePostExtendedFlatConditionals(height,maxChildHeight_,descendentPattern_,leftBaseExtended_,rightBaseExtended_);
		  external_.calculateSingleAscendentExtendedConditionalsIndirect(parentHeight_,height,ascendentPattern_,ascendentFlat_,tempConditionals_);
			double result = -external_.calculateLogLikelihoodNonRoot(height,centerPattern_,tempConditionals_,descendentExtended);
			return result;
		}
		public double getLowerBound() { return maxChildHeight_; }

	  public double getUpperBound() { return parentHeight_; }
	}
// ========================================================================================================

	private final class SubTreeShiftOptimisationHandler implements UnivariateFunction, ConstrainedNode.HeightAdjustment {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;

		private ConditionalProbabilityStore ascendentFlat_;

		private ConditionalProbabilityStore tempConditionals_;

		private PatternInfo descendentPattern_;

		private PatternInfo ascendentPattern_;

		private PatternInfo centerPattern_;

		private ConstrainedNode leftChild_;
		private ConstrainedNode rightChild_;

		private double parentHeight_;
		private double baseHeight_;
		private double offset_;
		private double minimumOffset_;
		private double maximumOffset_;

		private int fracDigits_;

		public SubTreeShiftOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
		  this.tool_ = tool;
			this.internal_ = internal;
		}

// =============================================================
// ==== Height Adjustment Stuff ================================
		public double getAdjustedHeight(Object related, double baseHeightToAdjust) {
		  return baseHeightToAdjust+offset_;
		}

		public void setup(
		  ConditionalProbabilityStore ascendentFlat,
			PatternInfo ascendentPattern,
			PatternInfo centerPattern,
			ConstrainedNode leftChild,
			ConstrainedNode rightChild,
			PatternInfo descendentPattern,
			double baseHeight, double minimumOffset, double maximumOffset, double parentHeight,
			MolecularClockLikelihoodModel.External external,
			ConditionalProbabilityStore tempConditionals,
			int fracDigits) {
		  this.baseHeight_ = baseHeight;
			this.ascendentFlat_ = ascendentFlat;		this.ascendentPattern_ = ascendentPattern;
			this.centerPattern_ = centerPattern;    this.descendentPattern_ = descendentPattern;
		  this.leftChild_ = leftChild;		this.rightChild_ = rightChild;
		  this.minimumOffset_ = minimumOffset;
			this.maximumOffset_ = maximumOffset;

	  	this.parentHeight_ = parentHeight;

			this.tempConditionals_ = tempConditionals;

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
		  ConditionalProbabilityStore descendentExtended = internal_.calculateFlatConditionals(descendentPattern_,left,right);
			external_.calculateSingleAscendentExtendedConditionalsIndirect(parentHeight_,adjustedHeight,ascendentPattern_,ascendentFlat_,tempConditionals_);
			double result = -external_.calculateLogLikelihoodNonRoot(adjustedHeight,centerPattern_,tempConditionals_,descendentExtended);
			return result;
		}
		private final void test() {
		  ConditionalProbabilityStore left = leftChild_.getDescendentExtendedConditionals(baseHeight_,tool_,false);
			ConditionalProbabilityStore right = rightChild_.getDescendentExtendedConditionals(baseHeight_,tool_,false);
		  ConditionalProbabilityStore descendentExtended = internal_.calculateFlatConditionals(descendentPattern_,left,right);
			external_.calculateSingleAscendentExtendedConditionalsIndirect(parentHeight_,baseHeight_,ascendentPattern_,ascendentFlat_,tempConditionals_);
			double result = -external_.calculateLogLikelihoodNonRoot(baseHeight_,centerPattern_,tempConditionals_,descendentExtended);
			System.out.println("Test:"+result);
		}
		public double getLowerBound() { return minimumOffset_; }
	  public double getUpperBound() { return maximumOffset_; }
	}
	private final class PartialSubTreeShiftOptimisationHandler implements UnivariateFunction, ConstrainedNode.HeightAdjustment {
		private final GeneralConstructionTool tool_;
		private final MolecularClockLikelihoodModel.Internal internal_;
		private MolecularClockLikelihoodModel.External external_;

		private double logLikelihood_;

		private ConditionalProbabilityStore ascendentFlat_;

		private ConditionalProbabilityStore tempConditionals_;

		private PatternInfo descendentPattern_;

		private PatternInfo ascendentPattern_;

		private PatternInfo centerPattern_;

		private ConstrainedNode leftChild_;
		private ConstrainedNode rightChild_;

		private double parentHeight_;
		private double baseHeight_;
		private double offset_;
		private double minimumOffset_;
		private double maximumOffset_;

		private int fracDigits_;
		private ConstrainedNode[] affectedNodes_;
		private int numberOfAffectedNodes_ = 0;
		public PartialSubTreeShiftOptimisationHandler(GeneralConstructionTool tool, MolecularClockLikelihoodModel.Internal internal) {
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
		  ConditionalProbabilityStore ascendentFlat,
			PatternInfo ascendentPattern,
			PatternInfo centerPattern,
			ConstrainedNode leftChild,
			ConstrainedNode rightChild,
			PatternInfo descendentPattern,
			double baseHeight, double minimumOffset, double maximumOffset, double parentHeight,
			MolecularClockLikelihoodModel.External external,
			ConditionalProbabilityStore tempConditionals,
			int fracDigits) {
		  this.baseHeight_ = baseHeight;
			this.ascendentFlat_ = ascendentFlat;		this.ascendentPattern_ = ascendentPattern;
			this.centerPattern_ = centerPattern;    this.descendentPattern_ = descendentPattern;
		  this.leftChild_ = leftChild;		this.rightChild_ = rightChild;
		  this.minimumOffset_ = minimumOffset;
			this.maximumOffset_ = maximumOffset;

	  	this.parentHeight_ = parentHeight;

			this.tempConditionals_ = tempConditionals;

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
		  final double adjustedHeight =	baseHeight_+offset_;

			ConditionalProbabilityStore left = leftChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool_,this,false);
			ConditionalProbabilityStore right = rightChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool_,this,false);
		  ConditionalProbabilityStore descendentExtended = internal_.calculateFlatConditionals(descendentPattern_,left,right);
			external_.calculateSingleAscendentExtendedConditionalsIndirect(parentHeight_,adjustedHeight,ascendentPattern_,ascendentFlat_,tempConditionals_);
			double result = -external_.calculateLogLikelihoodNonRoot(adjustedHeight,centerPattern_,tempConditionals_,descendentExtended);
			return result;
		}
		private final void test() {
		  ConditionalProbabilityStore left = leftChild_.getDescendentExtendedConditionals(baseHeight_,tool_,false);
			ConditionalProbabilityStore right = rightChild_.getDescendentExtendedConditionals(baseHeight_,tool_,false);
		  ConditionalProbabilityStore descendentExtended = internal_.calculateFlatConditionals(descendentPattern_,left,right);
			external_.calculateSingleAscendentExtendedConditionalsIndirect(parentHeight_,baseHeight_,ascendentPattern_,ascendentFlat_,tempConditionals_);
			double result = -external_.calculateLogLikelihoodNonRoot(baseHeight_,centerPattern_,tempConditionals_,descendentExtended);
			System.out.println("Test:"+result);
		}
		public double getLowerBound() { return minimumOffset_; }
	  public double getUpperBound() { return maximumOffset_; }
	}

}