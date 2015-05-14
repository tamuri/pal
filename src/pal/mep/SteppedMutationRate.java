// SteppedMutationRate.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.mep;

import pal.math.*;
import pal.misc.*;
import pal.io.*;

import java.io.*;
import pal.eval.*;
import pal.treesearch.*;
/**
 * This class models a step-wise mutation rate. <BR>
 * parameters: <BR>
 * mus[] = vector of mutation rates <BR>
 * muChanges[] = vector of change times <P>
 * Drummond, Forsberg and Rodrigo (2001). The inference of step-wise changes in substitution rates using serial sequence samples. accepted in MBE.
 *
 * @version $Id: SteppedMutationRate.java,v 1.13 2004/10/18 03:45:17 matt Exp $
 *
 * @author Alexei Drummond
 */
public class SteppedMutationRate extends MutationRateModel implements Report, Summarizable, Parameterized, Serializable
{
	//
	// Private stuff
	//

	/** mutation rates */
	private double[] mus;

	/** mutation rate SEs */
	private double[] muSEs;

	/** mutation rate change times */
	private double[] muChanges;

	/** whether or not the mu values are optimizable */
	private boolean fixedMus = false;


	String[] summaryTypes = null;

	/**
	 * Construct demographic model with default settings
	 */
	public SteppedMutationRate(double[] muChanges, int units, double maximumMutationRate) {
		super(units, maximumMutationRate);
		this.muChanges = muChanges;
		mus = new double[muChanges.length + 1];
		muSEs = new double[muChanges.length + 1];
		for (int i = 0; i < mus.length; i++) {
			mus[i] = getDefaultValue(0);
		}
	}

	/**
	 * Construct mutation rate model of a give rate in given units.
	 * @param rates The initial values of the rates (this array is used for storing the rates)
	 * @param muChanges The times for when the mutation rate can change
	 * @param maximumMutationRate The maximum mutation rate (1 is a good value...)

	 */
	public SteppedMutationRate(double[] rates, double[] muChanges, int units, double maximumMutationRate) {
		this(rates, muChanges, units, false,maximumMutationRate);
	}

	/**
	 * Construct mutation rate model of a give rate in given units.
	 * @param fixed if true the mutation rates are set and are not parameters
	 * @param rates The initial values of the rates (this array is used for storing the rates)
	 * @param muChanges The times for when the mutation rate can change
	 * @param maximumMutationRate The maximum mutation rate (related to how much difference there is between samples, max mu*time diff ~= 1 is a good estimate...)
	 */
	public SteppedMutationRate(double[] rates, double[] muChanges, int units, boolean fixed, double maximumMutationRate) {
		super(units,maximumMutationRate);
		fixedMus = fixed;
		mus = rates;
		muSEs = new double[rates.length];
		this.muChanges = muChanges;

	}
	/**
	 * Construct mutation rate model of a give rate in given units.
	 * @param rates The initial values of the rates (this array is used for storing the rates)
	 * @param timeInfo the sample information object that relates times to sequences. Will extract the mu change information from this input, such that the mutation rate changes at each sample point
	 * @note is excpected the rates array is the right length and setup correctly
	 */
	public SteppedMutationRate(double[] rates, TimeOrderCharacterData timeInfo) {
		this(rates,timeInfo,false);
	}
	/**
	 * Construct mutation rate model of a give rate in given units.
	 * @param fixed if true the mutation rates are set and are not parameters
	 * @param rates The initial values of the rates (this array is used for storing the rates)
	 * @param timeInfo the sample information object that relates times to sequences. Will extract the mu change information from this input, such that the mutation rate changes at each sample point
	 * @note is excpected the rates array is the right length and setup correctly
	 */
	public SteppedMutationRate(double[] rates, TimeOrderCharacterData timeInfo, boolean fixed) {
		super(timeInfo.getUnits(),timeInfo.getSuggestedMaximumMutationRate());
		fixedMus = fixed;
		mus = rates;
		muSEs = new double[rates.length];
		this.muChanges = timeInfo.getUniqueTimeArray();

	}
	private SteppedMutationRate(SteppedMutationRate toCopy) {
		super(toCopy);
		this.mus = pal.misc.Utils.getCopy(toCopy.mus);
		this.muSEs = pal.misc.Utils.getCopy(toCopy.muSEs);
		this.muChanges = pal.misc.Utils.getCopy(toCopy.muChanges);
		this.fixedMus = toCopy.fixedMus;
	}
	public Object clone()	{
		return getCopy();
	}

	public MutationRateModel getCopy() {
		return new SteppedMutationRate(this);
	}

	public String[] getSummaryTypes() {
		if (summaryTypes == null) {
			summaryTypes = new String[mus.length];
			for (int i = 0; i < summaryTypes.length; i++) {
				summaryTypes[i] = "mu " + i;
			}
		}
		return summaryTypes;
	}

	public double getSummaryValue(int summaryType) {

		if (summaryType < mus.length) {
			return mus[summaryType];
		}
		throw new RuntimeException("Assertion error: unknown summary type :"+summaryType);
	}

	/**
	 * returns current day mutation rate.
	 */
	public double getMu(){	return mus[0];	}

	public void setMu(double m) {	mus[0] = m;	}

	public void getMus(double[] muStore) {
		System.arraycopy(mus,0,muStore,0,muStore.length);
	}

	// Implementation of abstract methods

	public final double getMutationRate(double t)
	{
		int muIndex = 0;
		while ((muIndex < muChanges.length) && (t > muChanges[muIndex])) {
			muIndex += 1;
		}
		return mus[muIndex];
	}

	public final double getExpectedSubstitutions(double time)
	{
		double currentTime = 0.0;
		double height = 0.0;
		int muIndex = 0;
		double timeInterval = 0.0;

		while (time > currentTime) {

			// if no more changes in mu go straight to the end
			if (muIndex >= muChanges.length) {
				timeInterval = time - currentTime;

				//update current time
				currentTime = time;

			} else {
				//find out the next time interval
				timeInterval = muChanges[muIndex] - currentTime;

				//truncate time interval if it exceeds node height.
				if ((currentTime + timeInterval) > time) {
					timeInterval = time - currentTime;

					//update current time
					currentTime = time;
				} else {
					//update current time
					currentTime = muChanges[muIndex];
				}

			}


			// update total height in substitutions
			height += mus[muIndex] * timeInterval;

			//update mu index
			muIndex += 1;

		}
		return height;
	}

	public final double getEndTime(double expectedSubs, double startTime) {
		expectedSubs += getExpectedSubstitutions(startTime);
		int changePoint = 0;
		while ((changePoint < muChanges.length) &&
			(expectedSubs < getExpectedSubstitutions(muChanges[changePoint]))) {
			changePoint += 1;
		}

		if (changePoint == 0) {
			// before first change point
			return expectedSubs / mus[changePoint];
		} else {
			double time = muChanges[changePoint-1];
			double expectedSoFar = getExpectedSubstitutions(time);
			time += (expectedSubs - expectedSoFar) / mus[changePoint];
			return time;
		}
	}

	/**
	 * Linearly scales this mutation rate model.
	 * @param scale getExpectedSubstitutions should return scale instead of 1.0 at time t.
	 */
	public final void scale(double scale) {
		for (int i =0 ; i < mus.length; i++) {
			mus[i] *= scale;
		}
	}

	public static double[] getTimeIntervals(double[] muChanges, double smallTime, double bigTime) {

		double[] intervals = new double[muChanges.length + 1];

		double currentTime = smallTime;
		double height = 0.0;

		int muIndex = 0;
		while((muIndex < muChanges.length) && (muChanges[muIndex] < smallTime)) {
			muIndex += 1;
		}

		double timeInterval = 0.0;

		while (bigTime > currentTime) {

			// if no more changes in mu go straight to the end
			if (muIndex >= muChanges.length) {
				intervals[muIndex] = bigTime - currentTime;

				//update current time
				currentTime = bigTime;
			} else {
				//find out the next time interval
				intervals[muIndex] = muChanges[muIndex] - currentTime;

				//truncate time interval if it exceeds node height.
				if ((currentTime + intervals[muIndex]) > bigTime) {
					intervals[muIndex] = bigTime - currentTime;

					//update current time
					currentTime = bigTime;
				} else {
					//update current time
					currentTime = muChanges[muIndex];
				}
			}

			//update mu index
			muIndex += 1;
		}

		return intervals;
	}


	public double[] getDeltas(double[] times) {
		double height = 0.0;
		double[] deltas = new double[times.length-1];
		for (int i = 0; i < deltas.length; i++) {
			deltas[i] = getExpectedSubstitutions(times[i+1]) - height;
			height += deltas[i];
		}
		return deltas;
	}

	// Parameterized interface

	public int getNumParameters()
	{
		if (fixedMus)
			return 0;
		else return mus.length;
	}

	public double getParameter(int k)
	{
		return mus[k];
	}

	public double getUpperLimit(int k)
	{
		return getMaximumMutationRate();
	}

	public double getLowerLimit(int k)
	{
		return 0.0;
	}

	public double getDefaultValue(int k)
	{
		//arbitrary default values
		if (getUnits() == GENERATIONS) {
			return 1e-6;
		} else {
			return 1e-6;
		}
	}

	public void setParameter(double value, int k)
	{
		mus[k] = value;
	}

	public void setParameterSE(double value, int k) {
		muSEs[k] = value;
	}

	public String toString()
	{
		OutputTarget out = OutputTarget.openString();
		report(out);
		out.close();

		return out.getString();
	}

	public void report(PrintWriter out)
	{
		out.println("Mutation rate model: stepped mutation rate ");

		out.print("Unit of time: ");
		out.println(Units.UNIT_NAMES[getUnits()]);
		out.println();
		out.println();
		out.println("Parameters of demographic function:");
		out.println(" mu\tinterval");
		for (int i = 0; i < mus.length; i++) {
			fo.displayDecimal(out, mus[i], 6);
			if (i == 0) {
				out.print("\t0.0 to ");
				fo.displayDecimal(out, muChanges[i], 6);
				out.println();
			} else {
				out.print("\t");
				fo.displayDecimal(out, muChanges[i-1], 6);
				out.print(" to ");
				if (i < muChanges.length) {
					fo.displayDecimal(out, muChanges[i], 6);
					out.println();
				} else {
					out.println("infinity");
				}
			}
		}
	}

	public double[] getMus() {
		double[] newMus = new double[mus.length];
		for (int i = 0; i < newMus.length; i++) {
			newMus[i] = mus[i];
		}
		return newMus;
	}

	public final double[] getMuChanges() {
		return pal.misc.Utils.getCopy(muChanges);
	}

	public String toSingleLine() {
		return "Stepped rate model. Interval rates:"+FormattedOutput.getInstance().getSFString(mus,4,", ");
	}

	public Factory generateFactory() {
		return new GivenMURateFactory(mus,muChanges,getUnits(),getMaximumMutationRate());
	}

	// ===========================================================================
	// Static stuff
	/**
	 * Generate a MutationRateModel.Factory class for a SteppedMutationRate
	 */
	public static final Factory getFactory(double[] muChanges, int units, double maximumMutationRate) {
		return new RateFactory(muChanges,units,maximumMutationRate);
	}
		/**
	 * Generate a MutationRateModel.Factory class for a SteppedMutationRate
	 */
	public static final Factory getFactory(double[] rates, double[] muChanges, int units, double maximumMutationRate) {
		return new GivenMURateFactory(rates, muChanges,units,maximumMutationRate);
	}
//	/**
//	 * Generate a MutationRateModel.Factory class for a SteppedMutationRate
//	 * @note maximumMutation rate will be 100
//	 */
//	public static final Factory getFactory(double[] muChanges, int units) {
//		return new RateFactory(muChanges,units,100);
//	}
	/**
	 * Generate a MutationRateModel.Factory class for a SteppedMutationRate
	 * @note maximumMutation rate will be 100
	 */
	public static final Factory getFactory(double[] muChanges, TimeOrderCharacterData tocd) {
		return new RateFactory(muChanges,tocd.getUnits(),tocd.getSuggestedMaximumMutationRate());
	}

	public static final String REPEATED_TIMES_TEXT = "Repeated times";
	public static final String NEGATIVE_VALUES_TEXT = "Negative values";
	public static final String MAX_TIME_IS_TEXT = "Max time is ";
	public static final String ZERO_TIME_TEXT = "Zero time";
	public static final String INVALID_INTERVALS_TEXT = "Incompatible intervals";
	/**
	 * Checks if mu changes are valid for a particular set of sample times.
	 * @return null if muChanges okay, or a message describing what is wrong.
	 * The things that may be a problem are
	 * <ul>
	 *  <li>There is a negative muChange time</li>
	 *  <li>There is a repeated muChange time</li>
	 *  <li>There are two or more muChange intervals with in a sample interval that do
	 *  not overlap with another sample interval (this makes it impossible to infer anything
	 *  between the separate intervals)</li>
	 * </ul>
	 */
	public static final String checkMuChanges(boolean allowEstimationOutsideSamplingTimes, double[] muChanges, boolean sortMuChanges, double[] sampleTimes, boolean sortSampleTimes) {
		if(sortMuChanges) {
			pal.util.HeapSort.sort(muChanges);
		} else {
			muChanges = pal.util.HeapSort.getSorted(muChanges);
		}
		if(sortSampleTimes) {
			pal.util.HeapSort.sort(sampleTimes);
		} else {
			sampleTimes = pal.util.HeapSort.getSorted(sampleTimes);
		}
		if(
			(!allowEstimationOutsideSamplingTimes)&&
			(muChanges[muChanges.length-1]>sampleTimes[sampleTimes.length-1])
			) {
			return MAX_TIME_IS_TEXT+sampleTimes[sampleTimes.length-1];
		}

		for(int i = 0 ; i < muChanges.length ; i++) {

			if(muChanges[i]<0) {
				return NEGATIVE_VALUES_TEXT;
			}
			if(muChanges[0]<0.00001) {
				return ZERO_TIME_TEXT;
			}
			if(i!=muChanges.length-1&&(Math.abs(muChanges[i]-muChanges[i+1])<0.00001)) {
				return REPEATED_TIMES_TEXT;
			}
		}
		for(int t = 0 ; t < sampleTimes.length-1 ; t++) {
			double lowerTime = sampleTimes[t];
			double higherTime = sampleTimes[t+1];
			double timeOne = 0;
			for(int i = 0 ; i < muChanges.length-1 ; i++) {
				double timeTwo = muChanges[i];
				double timeThree = muChanges[i+1];
				if(timeOne>=lowerTime&&timeThree<=higherTime) {
					return INVALID_INTERVALS_TEXT;
				}
				timeOne = timeTwo;
			}
			/*double higherTime = muChanges[i];
			//ToFinish
			for(int t = 0 ; t < sampleTimes.length ; t++) {
				//double timeTwo = sampleTimes[t+1];
				if(t!=(sampleTimes.length-1)) {
					double timeOne = sampleTimes[t];
					double timeTwo = sampleTimes[t+1];
					if(lowerTime>timeOne&&higherTime<timeTwo) {
						return INVALID_INTERVALS_TEXT;
					}
				} else {
					//Check so only allow one muChange time point over end, such that interval starts before last sample time point
					double time = sampleTimes[t];
					if(allowEstimationOutsideSamplingTimes) {
						if(lowerTime>=time&&higherTime>time) {	return INVALID_INTERVALS_TEXT;	}
					} else {
						if(higherTime>time) {	return INVALID_INTERVALS_TEXT;	}
					}
				}
			}
			lowerTime = higherTime;
			*/
		}
		return null;
	}
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private static final class RateFactory implements Factory {
		private final double[] muChanges_;
		private final int units_;
		private final double maximumMutationRate_;
		public RateFactory(double[] muChanges, int units, double maximumMutationRate) {
			this.muChanges_ = pal.misc.Utils.getCopy(muChanges);
			this.units_ = units;
			this.maximumMutationRate_ = maximumMutationRate;
		}
		public MutationRateModel generateNewModel() {
			return new SteppedMutationRate(pal.misc.Utils.getCopy(muChanges_), units_, maximumMutationRate_);
		}
		public ConstraintModel buildConstraintModel(SampleInformation si, MolecularClockLikelihoodModel.Instance likelihoodModel) {
			return new MRDTGlobalClockModel(si,likelihoodModel,muChanges_);
		}
	}
	private static final class GivenMURateFactory implements Factory {
		private final double[] muChanges_;
		private final double[] rates_;
		private final int units_;
		private final double maximumMutationRate_;
		public GivenMURateFactory(double[] rates, double[] muChanges,  int units, double maximumMutationRate) {
			this.muChanges_ = pal.misc.Utils.getCopy(muChanges);
			this.rates_ = pal.misc.Utils.getCopy(rates);
			this.units_ = units;
			this.maximumMutationRate_ = maximumMutationRate;
		}
		public MutationRateModel generateNewModel() {
			return new SteppedMutationRate(pal.misc.Utils.getCopy(rates_), pal.misc.Utils.getCopy(muChanges_), units_, maximumMutationRate_);
		}
				public ConstraintModel buildConstraintModel(SampleInformation si, MolecularClockLikelihoodModel.Instance likelihoodModel) {
					throw new RuntimeException("Not implemented yet!");
		}
	}

}
