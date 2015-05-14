// DeltaRate.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.mep;

/**
 * Title:        DeltaModel
 * Description:  A more general approach to mep stuff that allows for order only analysis (instead of requiring times like MutationRateModel)
 *               Also allows for more esoteric models (such as the Dual Rate model in the current version of PEBBLE - http://www.cebl.auckland.ac.nz/)
 * @author Matthew Goode
 * @version 1.0
 */
import pal.misc.*;
import pal.mep.*;
import pal.math.*;
import pal.eval.*;
import pal.tree.Tree;
import pal.tree.TreeUtils;
import pal.io.FormattedOutput;

import java.io.Serializable;
import pal.treesearch.*;

public interface DeltaModel extends Serializable {
	public Instance generateInstance(TimeOrderCharacterData tocd);
	public boolean canGenerateAlternativeTreeRepresentation();
	public ConstraintModel buildConstraintModel(SampleInformation si, MolecularClockLikelihoodModel.Instance likelihoodModel);

	public static interface Instance extends Parameterized, Serializable {
		public double getExpectedSubstitutions(int taxon);
		public double getExpectedSubstitutionsToTime(double time) throws UnsupportedOperationException;
		public OrthogonalHints getOrthogonalHints();
		public Tree generateAlternativeRepresentation(Tree expectedSubstitutionsTree);
		public String getHTMLInfo();
		public void addPalObjectListener(PalObjectListener pol);
		public void removePalObjectListener(PalObjectListener pol);


	}
	public static final class Utils {
		/**
		 * For interfacing with time based mutation rate models
		 */
		public static final DeltaModel getMutationRateModelBased(MutationRateModel.Factory mepFactory) {
			return new MutationRateDeltaModel(mepFactory);
		}
		/**
		 * For serial sampled analysis when no time information is available.
		 */
		public static final DeltaModel getUntimedBased() {
			return UntimedDeltaModel.DEFAULT_INSTNACE;
		}
		/**
		 * For serial sampled analysis when no time information is available.
		 * @param initialDeltas, if not of required length (for given tocd) uses subset (or sets remainder to zero)
		 */
		public static final DeltaModel getUntimedBased(double[] initalDeltas) {
			return new UntimedDeltaModel(initalDeltas);
		}
		public static final DeltaModel getDisjointBased(
			DeltaModel primaryModel,
			DeltaModel subgroupModel,
			TimeOrderCharacterData subgroupTOCD
			) {
			return new DisjointDeltaModel(primaryModel,subgroupModel,subgroupTOCD);
		}

	// --------------------------------------------------------------------------
		private static final class MutationRateDeltaModel implements DeltaModel {
			private final MutationRateModel.Factory mepFactory_;
			public MutationRateDeltaModel(MutationRateModel.Factory mepFactory) {
				this.mepFactory_ = mepFactory;
			}
			public ConstraintModel buildConstraintModel(SampleInformation si, MolecularClockLikelihoodModel.Instance likelihoodModel) {
			  return mepFactory_.buildConstraintModel(si,likelihoodModel);
			}
			public Instance generateInstance(TimeOrderCharacterData tocd) {
				return new InstanceImpl(mepFactory_.generateNewModel(),tocd);
			}
			public boolean canGenerateAlternativeTreeRepresentation() { return true; }

			private static final class InstanceImpl  extends Parameterized.ParameterizedUser implements Instance {
				private final MutationRateModel model_;
				private final TimeOrderCharacterData tocd_;
				public InstanceImpl(MutationRateModel model, TimeOrderCharacterData tocd) {
					this.model_ = model;
					this.tocd_ = tocd;
					setParameterizedBase(model_);
				}
				public Tree generateAlternativeRepresentation(Tree expectedSubstitutionsTree) {
					return TreeUtils.scale(expectedSubstitutionsTree,model_,model_.getUnits());
				}
				public double getExpectedSubstitutionsToTime(double time) throws UnsupportedOperationException {
					return model_.getExpectedSubstitutions(time);
				}
				public OrthogonalHints getOrthogonalHints() {
					return model_.getOrthogonalHints();
				}
				public double getExpectedSubstitutions(int taxon) {
					return model_.getExpectedSubstitutions(tocd_.getTime(taxon));
				}
				public String getHTMLInfo() {
					return
						"<ul>"+
							"<li>Rate Inferable (time information available)</li>"+
							"<li>Details:"+model_.toSingleLine()+"</li>"+
						"</ul>";
				}
			}
		} //End of MutationRateDeltaModel
		// --------------------------------------------------------------------------
		private static final class DisjointDeltaModel implements DeltaModel {
			private final DeltaModel primaryModel_;
			private final DeltaModel subgroupModel_;
			private final TimeOrderCharacterData subgroupTOCD_;

			public DisjointDeltaModel(
							DeltaModel primaryModel,
							DeltaModel subgroupModel,
							TimeOrderCharacterData subgroupTOCD) {
				this.primaryModel_ = primaryModel;
				this.subgroupModel_ = subgroupModel;
				this.subgroupTOCD_ = subgroupTOCD;
			}
			public ConstraintModel buildConstraintModel(SampleInformation si,MolecularClockLikelihoodModel.Instance likelihoodModel) {
			  throw new RuntimeException("Not implemented yet!");
			}
			public Instance generateInstance(TimeOrderCharacterData tocd) {
				return
					new
						InstanceImpl(
							primaryModel_.generateInstance(tocd),
							tocd,
							subgroupModel_.generateInstance(subgroupTOCD_),
							subgroupTOCD_);
			}
			public boolean canGenerateAlternativeTreeRepresentation() { return true; }

			private static final class InstanceImpl  extends Parameterized.ParameterizedUser implements Instance {
				private final DeltaModel.Instance primaryModel_;
				private final DeltaModel.Instance subgroupModel_;

				private final TimeOrderCharacterData primaryTOCD_;
				private final TimeOrderCharacterData subgroupTOCD_;
				private final int[] subgroupTaxonLookup_;
				public InstanceImpl(
					DeltaModel.Instance primaryModel,
					TimeOrderCharacterData primaryTOCD,
					DeltaModel.Instance subgroupModel,
					TimeOrderCharacterData subgroupTOCD) {
					this.primaryModel_ = primaryModel;
					this.primaryTOCD_ = primaryTOCD;
					this.subgroupModel_ = subgroupModel;
					this.subgroupTOCD_ = subgroupTOCD;
					setParameterizedBase(Parameterized.Utils.combine(primaryModel,subgroupModel));
					this.subgroupTaxonLookup_ = new int[primaryTOCD.getIdCount()];
					for(int i = 0 ; i < subgroupTaxonLookup_.length ; i++) {
						subgroupTaxonLookup_[i] = subgroupTOCD.whichIdNumber(primaryTOCD.getIdentifier(i).getName());
					}
				}
				public Tree generateAlternativeRepresentation(Tree expectedSubstitutionsTree) {
					return expectedSubstitutionsTree;
				}
				public double getExpectedSubstitutionsToTime(double time) throws UnsupportedOperationException {
					throw new UnsupportedOperationException("Can't do");
				}

				public OrthogonalHints getOrthogonalHints() {
					return null;
				}
				public double getExpectedSubstitutions(int taxon) {
					int subgroupIndex = subgroupTaxonLookup_[taxon];
					if(subgroupIndex>=0) {
						return subgroupModel_.getExpectedSubstitutions(subgroupIndex);
					}
					return primaryModel_.getExpectedSubstitutions(taxon);
				}
				public String getHTMLInfo() {
					return
						"<ul>"+
							"<li>Disjoint Model</li>"+
							"<li>Primary Model:"+primaryModel_.getHTMLInfo()+"</li>"+
							"<li>Sub group Model:"+subgroupModel_.getHTMLInfo()+"</li>"+
							"<li>Sub group members:"+
								pal.misc.Utils.toString(Identifier.getNames(subgroupTOCD_),", ")+"</li>"+
						"</ul>";
				}
			}
		}
		// --------------------------------------------------------------------------
		private static final class UntimedDeltaModel implements DeltaModel {
			private static final double DEFAULT_MAX_HEIGHT = 10000;
			public static final DeltaModel DEFAULT_INSTNACE = new UntimedDeltaModel(DEFAULT_MAX_HEIGHT);
			private final double maxRelativeHeight_;
			private final double[] initialDeltas_;

			public UntimedDeltaModel(double maxRelativeHeight) {
				this(maxRelativeHeight,null);
			}
			public UntimedDeltaModel(double[] initalDeltas) {
				this(DEFAULT_MAX_HEIGHT,initalDeltas);
			}
		  public ConstraintModel buildConstraintModel(SampleInformation si, MolecularClockLikelihoodModel.Instance likelihoodModel) {
			  return new pal.treesearch.MRDTGlobalClockModel(si,likelihoodModel);
			}
			public UntimedDeltaModel(double maxRelativeHeight, double[] initalDeltas) {
				this.maxRelativeHeight_ = maxRelativeHeight;
				this.initialDeltas_ = initalDeltas;
			}
			public Instance generateInstance(TimeOrderCharacterData tocd) {
				return new InstanceImpl(tocd,maxRelativeHeight_,initialDeltas_);
			}
			public boolean canGenerateAlternativeTreeRepresentation() { return true; }

			private static class InstanceImpl extends PalObjectListener.EventGenerator implements Instance {
				TimeOrderCharacterData tocd_;
				final double[] intervalRates_;

				final double maxIntervalWidth_;
				public InstanceImpl(TimeOrderCharacterData tocd) {
					this(tocd,DEFAULT_MAX_HEIGHT);
				}
				public InstanceImpl(TimeOrderCharacterData tocd, double maxIntervalWidth) {
					this(tocd,maxIntervalWidth,null);
				}
				public InstanceImpl(TimeOrderCharacterData tocd, double maxIntervalWidth, double[] initalDeltas) {
					this.tocd_ = tocd;

					this.maxIntervalWidth_ = maxIntervalWidth;

					intervalRates_ = new double[tocd_.getSampleCount()-1];
					if(initalDeltas!=null) {
						int maxIndex = Math.min(intervalRates_.length,initalDeltas.length);
						System.arraycopy(initalDeltas,0,intervalRates_,0,Math.min(intervalRates_.length,initalDeltas.length));
					}
				}
				/**
				 * @return input tree
				 */
				public Tree generateAlternativeRepresentation(Tree expectedSubstitutionsTree) {
					return expectedSubstitutionsTree;
				}
				public OrthogonalHints getOrthogonalHints() { return null; }

				public int getNumParameters() { return intervalRates_.length;  }
				public void setParameter(double param, int n) {
					intervalRates_[n] = param;
					fireParametersChangedEvent();
				}
				public double getParameter(int n) { return intervalRates_[n]; }
				public void setParameterSE(double paramSE, int n) {  }
				public double getLowerLimit(int n) {	return 0;	}
				public double getUpperLimit(int n) { return maxIntervalWidth_; }
				public double getDefaultValue(int n) { return 0; }

				public double getExpectedSubstitutionsToTime(double time) throws UnsupportedOperationException {
					throw new UnsupportedOperationException("Can't do");
				}

				public double getExpectedSubstitutions(int taxon) {
					final int sample = tocd_.getTimeOrdinal(taxon);
					double total = 0;
					for(int i = 0 ; i < sample ; i++) {
						total+=intervalRates_[i];
					}
					return total;
				}
				public String getHTMLInfo() {
					return
						"<ul>"+
							"<li>No time information was available</li>"+
							"<li>Interval deltas (in expected substitutions):"+
								FormattedOutput.getInstance().getSFString(intervalRates_,4,", ")+
							"</li>"+
						"</ul>";
				}
			} //End of class InstanceImpl
		} //End of class UntimedDeltaModel
	} //End of Utils
}