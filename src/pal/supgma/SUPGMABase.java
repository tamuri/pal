// SUPGMABase.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.supgma;

/**
 * Title:        For SUPGMA stuff
 * Description:  Stuff
 * @author Matthew Goode
 */
import pal.tree.*;
import pal.misc.*;
import pal.distance.*;
import pal.alignment.Alignment;
import pal.alignment.BootstrappedAlignment;
import pal.alignment.SitePattern;
import pal.substmodel.SubstitutionModel;
import pal.coalescent.*;
import pal.mep.*;
import pal.io.FormattedOutput;
import pal.util.AlgorithmCallback;
import pal.math.*;

public class SUPGMABase {
	private static final int SF_DIGITS = 5;
	private final DistanceMatrixAccess distanceAccess_;
	private final DistanceMatrixGenerator replicateGenerator_;
	private final TimeOrderCharacterData tocd_;
	/**
	 * Only used if time information
	 */
	private RateHandler rateHandler_ = null;
	private ThetaHandler thetaHandler_ = null;

	public SUPGMABase(DistanceMatrixAccess distanceAccess, DistanceMatrixGenerator replicateGenerator, TimeOrderCharacterData tocd) {
		this.distanceAccess_ = distanceAccess;
		this.replicateGenerator_ = replicateGenerator;
		this.tocd_ = tocd;
	}
	public String toString() {
		return "SUPGMA BASE: (DMA:"+ distanceAccess_+") "+"(Rate Handler:"+rateHandler_+") "+"(Theta Handler:"+thetaHandler_+")";
	}
	/**
	 * @default false
	 */
	public void setThetaHandler(ThetaHandler handler) { this.thetaHandler_ = handler; }
	/**
	 * Makes no difference if no time info available
	 */
	public void setRateHandler(RateHandler rateHandler) {
		this.rateHandler_ = rateHandler;
	}


	/**
	 * Get all distances as one long array
	 */
	private double[] getDistances(AlgorithmCallback callback) {
		int num = tocd_.getIdCount();
		double[] distanceArray= new double[num*(num-1)/2];
		int index = 0;
		DistanceMatrix distances = distanceAccess_.obtainMatrix(callback);
		for(int i = 0 ; i < num; i++) {
			for(int j = i+1 ; j < num ; j++) {
				//From i to j;
				distanceArray[index++] = distances.getDistance(i,j);
			}
		}
		return distanceArray;
	}

	public Tree solve(AlgorithmCallback callback, ClusterTree.ClusteringMethod cm, LMSSolver solver) {
		Analyser a = generateAnalyser();
		PopulationParameters pp = a.analyse(distanceAccess_.obtainMatrix(callback),tocd_,solver);
		return pp.generateSUPGMATree(cm);
	}
	public PopulationParameters process(DistanceMatrixAccess alternativeSource, AlgorithmCallback callback, LMSSolver solver) {
		Analyser a = generateAnalyser();
		return a.analyse(alternativeSource.obtainMatrix(callback),tocd_,solver);
	}
	public PopulationParameters process(AlgorithmCallback callback, LMSSolver solver) {
		Analyser a = generateAnalyser();
		return a.analyse(distanceAccess_.obtainMatrix(callback),tocd_,solver);
	}
	public Tree generateAlignmentBootstrappedSUPGMATree(AlgorithmCallback callback, ClusterTree.ClusteringMethod cm, PopulationParameters pp, int numberOfReplicates, LMSSolver solver) {
		return pp.generateSUPGMATree(callback,cm,replicateGenerator_,numberOfReplicates,solver);
	}


	/**
	 * Generates a suitable analyser.
	 */
	public Analyser generateAnalyser() {
		if(tocd_.hasTimes()) {
			return new TimeBasedAnalyser(thetaHandler_,rateHandler_);
		} else { //No time
			return new NoTimeBasedAnalyser(thetaHandler_);
		}
	}

// ==================================================================


	private static interface Analyser {
		public PopulationParameters analyse(DistanceMatrix dm, TimeOrderCharacterData tocd, LMSSolver solver );
	}

// =================================================================
// ===== Time Based Builder ========================================
	private static final class TimeBasedAnalyser implements Analyser {
		private final ThetaHandler thetaHandler_;
		private final RateHandler rateHandler_;
		public TimeBasedAnalyser(ThetaHandler thetaHandler, RateHandler rateHandler) {
			this.thetaHandler_ = thetaHandler;
			this.rateHandler_ = rateHandler;
		}
		private final static double[] getResults(ThetaHandler thetaHandler, RateHandler rateHandler, double[][] distanceMatrix, TimeOrderCharacterData tocd, LMSSolver solver) {
			int num = tocd.getIdCount();
			int totalNumberOfDistances = num*(num-1)/2;

			int numberOfThetas = thetaHandler.getNumberOfParameters(tocd);
			int numberOfRates = rateHandler.getNumberOfParameters(tocd);
			if((numberOfRates+numberOfThetas)==0) {
				//Ain't much use doing anything as there isn't anything to calculate!
				//(Happens when theta and rate is fixed)
				return new double[0];
			}

			double[][] m = new double[totalNumberOfDistances][numberOfThetas+numberOfRates];
			double[] distanceArray = new double[totalNumberOfDistances];
			//double[][] distances = dm.getDistances();
			double[] sampleTimes = tocd.getUniqueTimeArray();
			double[][] adjustedDistanceMatrix = pal.misc.Utils.getCopy(distanceMatrix);
			rateHandler.adjustDistances(adjustedDistanceMatrix,tocd);

			int index = 0;
			for(int i = 0 ; i < num ; i++) {
				double iTime = tocd.getTime(i);
				int iSample = tocd.getTimeOrdinal(i);
				for(int j = i+1 ; j < num ; j++) {
					int jSample = tocd.getTimeOrdinal(j);
					int minSample = Math.min(iSample,jSample);
					int maxSample = Math.max(iSample,jSample);

					//From i to j;
					//m[index][singleTheta_ ? 0 : maxSample] = 1;
					thetaHandler.fillInLSInfo(m[index],0,minSample,maxSample);
					rateHandler.fillInLSInfo(m[index],numberOfThetas, minSample, maxSample, sampleTimes);

					distanceArray[index] = adjustedDistanceMatrix[i][j];
					index++;
				}
			}
			return solver.solve(m,distanceArray);
		}

		public PopulationParameters analyse(DistanceMatrix dm, TimeOrderCharacterData tocd, LMSSolver solver ) {
			return analyseImpl(this, thetaHandler_, rateHandler_, dm,tocd,solver);
		}
		protected static final TimedPopulationParameters analyseImpl(Analyser analyser, ThetaHandler thetaHandler, RateHandler rateHandler, DistanceMatrix dm, TimeOrderCharacterData tocd, LMSSolver solver ) {
			int numberOfThetas = thetaHandler.getNumberOfParameters(tocd);
			int numberOfRates = rateHandler.getNumberOfParameters(tocd);
			double[] values = getResults(thetaHandler, rateHandler, dm.getClonedDistances(),tocd,solver);
			double[] rates = new double[numberOfRates];
			double[] thetas = new double[numberOfThetas];
			double[] times = tocd.getUniqueTimeArray();
			System.arraycopy(values,0,thetas,0,numberOfThetas);
			System.arraycopy(values,numberOfThetas,rates,0,numberOfRates);
			return
				new TimedPopulationParameters(
					analyser,
					rateHandler,
					thetaHandler,
					dm,
					tocd,
					rates,
					times,
					thetas,
					solver
				);
		}
	}


// =================================================================
// ===== No Time Based Builder ========================================
	private static final class NoTimeBasedAnalyser implements Analyser {
		ThetaHandler thetaHandler_;
		public NoTimeBasedAnalyser(ThetaHandler thetaHandler) {
			this.thetaHandler_ = thetaHandler;
		}

		private final static int getNumberOfDeltas(TimeOrderCharacterData tocd) {
			return tocd.getSampleCount() - 1 ;
		}
		protected static final NoTimePopulationParameters analyseImpl(final Analyser analyser, final ThetaHandler thetaHandler, final DistanceMatrix dm, final TimeOrderCharacterData tocd, final LMSSolver solver) {
			int num = tocd.getIdCount();
			int totalNumberOfDistances = num*(num-1)/2;
			int numberOfThetas = thetaHandler.getNumberOfParameters(tocd);
			int numberOfDeltas = getNumberOfDeltas(tocd);

			double[][] m = new double[totalNumberOfDistances][numberOfDeltas+numberOfThetas];
			double[] distanceArray = new double[totalNumberOfDistances];
			//double[][] distances = dm.getDistances();

			int index = 0;
			for(int i = 0 ; i < num ; i++) {
				int iSample = tocd.getTimeOrdinal(i);
				for(int j = i+1 ; j < num ; j++) {
					int jSample = tocd.getTimeOrdinal(j);
					int minSample = Math.min(iSample,jSample);
					int maxSample = Math.max(iSample,jSample);

					//From i to j;

					//m[index][ singleTheta_ ? 0 : maxSample] = 1;
					thetaHandler.fillInLSInfo(m[index],0,minSample,maxSample);
					for(int sample = minSample ; sample < maxSample ; sample++) {
						m[index][sample+numberOfThetas] = 1;
					}
					distanceArray[index++] = dm.getDistance(i, j);
				}
			}
			double[] values = solver.solve(m,distanceArray);
			double[] deltas = new double[numberOfDeltas];
			double[] thetas = new double[numberOfThetas];

			System.arraycopy(values,0,thetas,0,numberOfThetas);
			System.arraycopy(values,numberOfThetas,deltas,0,numberOfDeltas);
			return new NoTimePopulationParameters(analyser, thetaHandler, dm,tocd,deltas,thetas);
		}
		public PopulationParameters analyse(DistanceMatrix dm, TimeOrderCharacterData tocd, LMSSolver solver ) {
			return analyseImpl(this, thetaHandler_, dm,tocd,solver);
		}
	}
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	abstract private static class AbstractPopulationParameters implements PopulationParameters {
		private DistanceMatrix dm_;
		private TimeOrderCharacterData baseTOCD_;
		private TimeOrderCharacterData esTOCD_;
		private double[] deltas_;
		private double[] thetas_;
		private ThetaHandler thetaHandler_;
		private final Analyser analyser_;

		protected AbstractPopulationParameters(Analyser analyser, ThetaHandler thetaHandler, DistanceMatrix dm, TimeOrderCharacterData baseTOCD, double[] deltas, double [] thetas) {
			this.dm_ = dm;
			this.analyser_ = analyser;
			this.baseTOCD_ = baseTOCD;
			this.deltas_ = pal.misc.Utils.getCopy(deltas);
			this.thetas_ = pal.misc.Utils.getCopy(thetas);
			this.thetaHandler_ = thetaHandler;
		}
		public final Tree[] simulateTrees(int numberOfTreesToSimulate, AlgorithmCallback callback, LMSSolver solver) {
			TimeOrderCharacterData suitableTOCD = getTOCDSuitableForDeltas();
			SerialCoalescentGenerator scg =
				new SerialCoalescentGenerator(
					suitableTOCD,
					thetaHandler_.generateDemographicModel(deltas_,thetas_,suitableTOCD),
					numberOfTreesToSimulate
				);
			return scg.generateTrees(callback);
		}
		public final Tree simulateTree() {
			TimeOrderCharacterData suitableTOCD = getTOCDSuitableForDeltas();
			SerialCoalescentGenerator scg =
				new SerialCoalescentGenerator(
					suitableTOCD,
					thetaHandler_.generateDemographicModel(deltas_,thetas_,suitableTOCD)
					,1
				);
			return scg.generateTree();
		}
		protected final Analyser getCreatingAnalyser() { return analyser_; }
		public final Tree generateSUPGMATree(ClusterTree.ClusteringMethod cm) {
			return new SUPGMATree(dm_,getBaseTOCD(),generateDeltaModel(),true,cm);
		}
		/**
		 * @return null if callback indicates stopping
		 */
		public Tree generateSUPGMATree(AlgorithmCallback callback, ClusterTree.ClusteringMethod cm, DistanceMatrixGenerator replicateSource, int numberOfAlignmentBootstrapReplicates, LMSSolver solver) {
			final Tree[] trees = new Tree[numberOfAlignmentBootstrapReplicates];

			DeltaModel deltaModel = generateDeltaModel();

			Tree base = new SUPGMATree(dm_,getBaseTOCD(),deltaModel,true,cm);

			if(numberOfAlignmentBootstrapReplicates==0) {
				return base;
			}
			pal.tree.TreeGenerator tg = new Bootstrapper(getBaseTOCD(),replicateSource,analyser_,cm,solver);
			return
				TreeUtils.getReplicateCladeSupport(
					pal.gui.TreePainter.BOOTSTRAP_ATTRIBUTE_NAME,
					base,
					tg,
					numberOfAlignmentBootstrapReplicates,
					callback
				);
		}

		/**
		 * @return false
		 */
		public  boolean isCICompatible() {
			return
				thetaHandler_.canGenerateDemogrpahicModel()&&
					(
						thetaHandler_.isCICompatible()||isDeltasCICompatible()
					);
		}
		protected final int getNumberOfDeltas() { return deltas_.length; }
		protected final int getNumberOfThetas() { return thetas_.length; }
		protected final double getFirstDelta() { return deltas_[0]; }
		protected final double getFirstTheta() { return thetas_[0]; }
		protected final String getThetaModelType() { return thetaHandler_.getInfo(); }
		protected final double[] getDeltas() { return deltas_; }
		protected final double[] getThetas() { return thetas_; }
		protected final TimeOrderCharacterData getBaseTOCD() { return baseTOCD_; }
		protected final boolean isThetaHandlerCICompatible() {  return thetaHandler_.isCICompatible(); }
		protected final ThetaHandler getThetaHandler() { return thetaHandler_; }
		/**
		 * uses base TOCD
		 */
		protected final DemographicModel generateDemographicModel() {
			return thetaHandler_.generateDemographicModel(deltas_,thetas_,baseTOCD_);
		}
		/**
		 * uses alternative TOCD
		 */
		protected final DemographicModel generateDemographicModel(TimeOrderCharacterData tocd) {
			return thetaHandler_.generateDemographicModel(deltas_,thetas_,tocd);
		}

		abstract protected boolean isDeltasCICompatible();
		abstract protected DeltaModel generateDeltaModel();
		abstract protected TimeOrderCharacterData getTOCDSuitableForDeltas();
	}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static final class NoTimePopulationParameters extends AbstractPopulationParameters implements PopulationParameters{
		private final TimeOrderCharacterData esTOCD_;

		protected NoTimePopulationParameters(Analyser analyser, ThetaHandler thetaHandler, DistanceMatrix dm, TimeOrderCharacterData tocd, double[] deltas, double [] thetas) {
			super(analyser, thetaHandler, dm, tocd,deltas,thetas);

			//Generate alternative TOCD
			esTOCD_ = tocd.generateExpectedSubsitutionsTimedTOCD(deltas);
		}
		protected boolean isDeltasCICompatible() {	return getNumberOfDeltas()==1;		}

		protected TimeOrderCharacterData getTOCDSuitableForDeltas() { return esTOCD_; }
		protected DeltaModel generateDeltaModel() {
			return DeltaModel.Utils.getUntimedBased(getDeltas() );
		}
		public CISummary inferCI(AlgorithmCallback callback, int numberOfReplicates, SimulatedAlignment.Factory alignmentFactory, SubstitutionModel evolutionaryModel, LMSSolver solver) {
			throw new RuntimeException("Assertion error : Not possible!");
		}
		public String generateHTML() {
			return Utils.generateHTML("Straight Delta values (no time information)",getDeltas(),getThetaModelType(),getThetas());
		}
	}
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static final class TimedPopulationParameters extends AbstractPopulationParameters implements PopulationParameters {
		private final double[] times_;
		private final RateHandler rateHandler_;
		private final LMSSolver solver_;
		protected TimedPopulationParameters(Analyser analyser, RateHandler rateHandler, ThetaHandler thetaHandler, DistanceMatrix dm, TimeOrderCharacterData tocd, double[] deltas, double[] times, double [] thetas, LMSSolver solver) {
			super(analyser, thetaHandler,dm,tocd,deltas,thetas);
			this.solver_ = solver;
			this.times_ = times;
			this.rateHandler_ = rateHandler;
		}

		protected boolean isDeltasCICompatible() { return rateHandler_.isCICompatible(); }

		protected DeltaModel generateDeltaModel() {
			return DeltaModel.Utils.getMutationRateModelBased(rateHandler_.generateRateModelFactory(getDeltas(), getBaseTOCD()));
		}
		protected TimeOrderCharacterData getTOCDSuitableForDeltas() {
			return getBaseTOCD();
		}

		public CISummary inferCI(AlgorithmCallback callback, int numberOfReplicates, SimulatedAlignment.Factory alignmentFactory, SubstitutionModel evolutionaryModel, LMSSolver solver) {
			double[] thetaValues = null, rateValues = null;
			TimeOrderCharacterData baseTOCD = getBaseTOCD();
			MutationRateModel.Factory mepFactory = rateHandler_.generateRateModelFactory(getDeltas(),baseTOCD);
			MutationRateModel mep = mepFactory.generateNewModel();

			double numberOfReplicatesD = numberOfReplicates;
			DemographicModel demo = generateDemographicModel();
			TimeOrderCharacterData scaledTOCD =	mep.scale(baseTOCD);
			SerialCoalescentGenerator scg = new SerialCoalescentGenerator(scaledTOCD, demo,1);
			if(isThetaHandlerCICompatible()) {	thetaValues = new double[numberOfReplicates]; }
			if(rateHandler_.isCICompatible()) {	rateValues = new double[numberOfReplicates]; }
			for (int i = 0; i < numberOfReplicates; i++) {
				if (callback.isPleaseStop()) return null;
				Tree tree = scg.generateTree();
				Alignment alignment = alignmentFactory.generateAlignment(tree);
				tree = null;
				if (callback.isPleaseStop()) return null;
				DistanceMatrix dm = new AlignmentDistanceMatrix(SitePattern.getSitePattern(alignment), evolutionaryModel);

				//We create a new TOCD object such that the indexes match the distance matrix/alignment
				TimeOrderCharacterData rearrangedTOCD = new TimeOrderCharacterData(alignment, baseTOCD.getUnits());
				rearrangedTOCD.setTimesAndOrdinals(baseTOCD);

				TimedPopulationParameters pp = TimeBasedAnalyser.analyseImpl(getCreatingAnalyser(), getThetaHandler(), rateHandler_, dm,rearrangedTOCD,solver_);
				if(thetaValues!=null) {	thetaValues[i] = pp.getFirstTheta();	}
				if(rateValues!=null) { rateValues[i] = pp.getFirstDelta();	}
				callback.updateProgress(i/numberOfReplicatesD);
			}

			SummaryData rateSummary = null;
			SummaryData thetaSummary = null;

			if(rateValues!=null) {
				pal.util.HeapSort.sort(rateValues);
				rateSummary = new SummaryData("Mutation rate", rateValues);
			}
			if(thetaValues!=null) {
				pal.util.HeapSort.sort(thetaValues);
				thetaSummary = new SummaryData("Theta", thetaValues);
			}
			SummaryData[] summaryData;
			if(rateValues!=null) {
				if(thetaValues!=null) {
					summaryData = new SummaryData[] {
						rateSummary, thetaSummary
					};
				}else {
					summaryData = new SummaryData[] { rateSummary };
				}
			} else {
				summaryData= new SummaryData[] { thetaSummary };
			}
			return new SimpleCISummary(getDeltas(), getThetas(), summaryData);
		}
		private String getDeltaModelType() {
			return "Mutation rate based, "+rateHandler_.getInfo();
		}
		public String generateHTML() {
			return Utils.generateHTML(getDeltaModelType(),getDeltas(),getThetaModelType(),getThetas());
		}
	}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private static final class SummaryData {
		double[] values_;
		String details_;
		public SummaryData(String details, double[] values) {
			this.values_ = values;
			this.details_ = details;
		}
		public String toSummary(double alphaLevel) {
			int ciPercent = 100- (int)(alphaLevel*100);
			int lowerIndex = (int)((alphaLevel/2)*values_.length);
			int upperIndex = values_.length-(int)(alphaLevel/2*values_.length);
			FormattedOutput fo = FormattedOutput.getInstance();
			String lowerValue = fo.getSFString(values_[lowerIndex],SF_DIGITS);
			String upperValue = fo.getSFString(values_[upperIndex],SF_DIGITS);

			return
				"<b>"+
					details_+
					" interval (alpha = "+
						alphaLevel+" => "+
						ciPercent+"%)<br>"+
				"</b>"+
					"[ "+lowerValue +" - "+upperValue+"]";
		}
	}
	private static final class SimpleCISummary implements CISummary {
		SummaryData[] data_;

		double[] rateValues_;
		double[] thetaValues_;

		public SimpleCISummary( double[] rateValues, double[] thetaValues, SummaryData[] data) {
			this.data_ = data;
			this.rateValues_ = rateValues;
			this.thetaValues_ = thetaValues;

		}
		public String toSummary(double alpha) {
			String s ="<ul>";
			for(int i = 0 ; i < data_.length ; i++) {
				s+="<li>"+data_[i].toSummary(alpha)+"</li>";
			}
			s+="</ul>";
			return s;
		}

	}

// ============================================================================
// === For performs Alignment Bootstraping
	private final static class Bootstrapper implements pal.tree.TreeGenerator {
		private final ClusterTree.ClusteringMethod cm_;
		private final Analyser analyser_;
		private final DistanceMatrixGenerator dms_;
		private final TimeOrderCharacterData tocd_;
		private final LMSSolver solver_;
		public Bootstrapper(TimeOrderCharacterData tocd, DistanceMatrixGenerator dms, Analyser analyser, ClusterTree.ClusteringMethod cm, LMSSolver solver) {
			this.dms_ = dms;
			this.cm_ = cm;
			this.solver_ = solver;
			this.analyser_ = analyser;
			this.tocd_ =tocd;
		}
		public Tree getNextTree(pal.util.AlgorithmCallback callback) {
			DistanceMatrix dm = dms_.generateNextMatrix(callback);
			PopulationParameters pp = analyser_.analyse(dm,tocd_,solver_);
			return pp.generateSUPGMATree(cm_);
		}
	}

// ============================================================================
// ========================= Public Interfaces/Classes ========================
// ============================================================================

	public static interface PopulationParameters {
		/**
		 * @return true if it possible to do Confidence Interval stuff
		 */
		public boolean isCICompatible();
		/**
		 * @returns an object capable of calculating the CI information (null if not possible)
		 */
		public CISummary inferCI(AlgorithmCallback callback, int numberOfReplicates, SimulatedAlignment.Factory alignmentFactory, SubstitutionModel evolutionaryModel, LMSSolver solver);
		public Tree generateSUPGMATree(ClusterTree.ClusteringMethod cm);
		public Tree generateSUPGMATree(AlgorithmCallback callback, ClusterTree.ClusteringMethod cm, DistanceMatrixGenerator replicateSource, int numberOfAlignmentBootstrapReplicates, LMSSolver solver);

		public Tree[] simulateTrees(int numberOfTreesToSimulate, AlgorithmCallback callback, LMSSolver solver);
		public Tree simulateTree();
		public String generateHTML();
	}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	public static interface CISummary {
		public String toSummary(double alphaLevel);
	}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	static final class Utils {

		static final String generateHTML(String model, double[] values) {
			return
				"<ul>"+
					"<li>Type:"+
						model+
					"</li>"+
					"<li>"+
						(values.length == 1 ?
							"Parameter Value:" :
							"Parameter Values:"
						)+
						FormattedOutput.getInstance().getSFString(values,SF_DIGITS,", ")+
					"</li>"+
				"</ul>"+(values.length == 1 ?
							"" :
							"<i>Parameter values are ordered with most recent first. </i>"
						)
						;
		}
		static final String generateHTML(String deltaModel, double[] deltaValues, String thetaModel, double[] thetaValues) {
			return

				"<ul>"+
					"<li><b>Delta Model:</b>"+generateHTML(deltaModel,deltaValues)+"</li>"+
					"<li><b>Theta Model:</b>"+generateHTML(thetaModel,thetaValues)+"</li>"+
				"</ul>";
		}
	}
}