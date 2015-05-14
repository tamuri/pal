// ConstantMutationRate.java
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
import pal.treesearch.*;
import pal.eval.*;
/**
 * This class models a constant mutation rate
 * (parameter: mu = mutation rate). <BR>
 *
 * @version $Id: ConstantMutationRate.java,v 1.13 2004/08/02 05:22:04 matt Exp $
 *
 * @author Alexei Drummond
 */
public class ConstantMutationRate extends MutationRateModel implements Report, Summarizable, Parameterized, Serializable
{
	//
	// private stuff
	//
	/** The summary descriptor stuff for the public values of this
			class
			@see Summarizable, getSummaryDescriptors()
	*/
	private static final String[] CP_SUMMARY_TYPES = {"mu", "muSE"}; //This is still 1.0 compliant...
	private static final double DEFAULT_RATE_VALUE = 1e-06;
	//
	// Public stuff
	//

	/** mutation rate */
	private double mu;
	private double muSE;

	private double minimumMutationRate_;
	private boolean parameterize_;

	//
	// Serialization Stuff
	//

	private static final long serialVersionUID=-6086097377649319118L;

	//serialver -classpath ./classes pal.mep.ConstantMutationRate

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeDouble(mu);
		out.writeDouble(muSE);
		out.writeDouble(minimumMutationRate_);
		out.writeBoolean(parameterize_);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				mu = in.readDouble();
				muSE = in.readDouble();
				minimumMutationRate_ = in.readDouble();
				parameterize_ = in.readBoolean();
				break;
			}
		}
	}

	/**
	 * Construct demographic model with default settings
	 * @param maximumMutationRate The maximum Mutation rate should be selected
	 * wisely - too small and it might not include the "true" rate, too high and
	 * the optimisers may have trouble optimising
	 */
	public ConstantMutationRate(int units, double maximumMutationRate) {
		this(DEFAULT_RATE_VALUE,units,maximumMutationRate);
	}
	/**
	 * Construct mutation rate model of a give rate in given units
	 * @param maximumMutationRate The maximum Mutation rate should be selected
	 * wisely - too small and it might not include the "true" rate, too high and
	 * the optimisers may have trouble optimising
	 */
	public ConstantMutationRate(double rate, int units, double maximumMutationRate) {
		this(rate,units,0,maximumMutationRate);
	}

	/**
	 * Construct mutation rate model of a give rate in given units, with a given range of possible values
	 */
	public ConstantMutationRate(double rate, int units, double minimumMu, double maximumMu) {
		this(rate,units,minimumMu,maximumMu,true);
	}
	/**
	 * Construct mutation rate model of a give rate in given units, with a given range of possible values
	 * @param parameterize If true, gives rate as a parameter, otherwise has no parameters
	 */
	private ConstantMutationRate(double rate, int units, double minimumMu, double maximumMu, boolean parameterize) {
		super(units,maximumMu);
		mu = Math.max(Math.min(maximumMu,rate),minimumMu);
		this.parameterize_ = parameterize;
		this.minimumMutationRate_ = minimumMu;
	}

	protected ConstantMutationRate(ConstantMutationRate toCopy) {
		super(toCopy);
		this.mu = toCopy.mu;
		this.muSE = toCopy.muSE;
		this.minimumMutationRate_ = toCopy.minimumMutationRate_;
		this.parameterize_ = toCopy.parameterize_;
	}

	public Object clone()	{ return getCopy(); }
	public MutationRateModel getCopy() {
		return new ConstantMutationRate(this);
	}

	public String[] getSummaryTypes() {
		return CP_SUMMARY_TYPES;
	}

	public double getSummaryValue(int summaryType) {
		switch(summaryType) {
			case 0 : {
				return mu;
			}
			case 1 : {
				return muSE;
			}
		}
		throw new RuntimeException("Assertion error: unknown summary type :"+summaryType);
	}

	/**
	 * returns initial population size.
	 */
	public double getMu()
	{
		return mu;
	}

	public void setMu(double m) {
		mu = m;
	}

	// Implementation of abstract methods

	public final double getMutationRate(double t)
	{
		return mu;
	}

	public final double getExpectedSubstitutions(double t)
	{
		return mu * t;
	}

	public final double getEndTime(double expectedSubs, double startTime) {
		return expectedSubs / mu;
	}


	/**
	 * Linearly scales this mutation rate model.
	 * @param scale getExpectedSubstitutions should return scale instead of 1.0 at time t.
	 */
	public final void scale(double scale) {
		mu *= scale;
	}

	// Parameterized interface

	public int getNumParameters()
	{

		return (parameterize_ ? 1 : 0) ;
	}

	public double getParameter(int k)
	{
		return mu;
	}

	public double getUpperLimit(int k)
	{
		return getMaximumMutationRate();
	}

	public double getLowerLimit(int k)
	{
		return minimumMutationRate_;
	}

	public double getDefaultValue(int k)
	{
		//arbitrary default values
		return DEFAULT_RATE_VALUE;
	}

	public void setParameter(double value, int k)
	{
		mu = value;
	}

	public void setParameterSE(double value, int k) {
		muSE = value;
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
		out.println("Mutation rate model: constant mutation rate ");
		out.print("Unit of time: ");
		out.print(Units.UNIT_NAMES[getUnits()]);
		out.println();
		out.println();
		out.println("Parameters of function: mu(t) = mu");
		out.print(" mutation rate: ");
		fo.displayDecimal(out, mu, 6);
	}

	public String toSingleLine() {
		String s= "Single rate model. Mutation rate, mu = " + mu;
		if(minimumMutationRate_==getMaximumMutationRate()&&minimumMutationRate_==mu) {
			s+=" (fixed)";
		}
		return s;
	}

// ===========================================================================
// Static stuff

	/**
	 * Generate a MutationRateModel.Factory class for a ConstantMutationRate
	 * @note rate is fixed
	 */
	public static final Factory getFixedFactory(double rate, int units) {
		return new RateFactory(rate,units,false,rate);
	}
	/**
	 * Generate a MutationRateModel.Factory class for a ConstantMutationRate
	 */
	public static final Factory getFreeFactory(int units, double maximumMutationRate) {
		return new RateFactory(maximumMutationRate/2,units,true,maximumMutationRate);
	}
	/**
	 * Generate a MutationRateModel.Factory class for a ConstantMutationRate
	 */
	public static final Factory getFreeFactory(double initialRate, int units, double maximumMutationRate) {
		return new RateFactory(initialRate,units,true,maximumMutationRate);
	}
	/**
	 * Generate a MutationRateModel.Factory class for a ConstantMutationRate
	 */
	public static final Factory getFreeFactory(double initialRate, int units, double minimumRate, double maximumRate) {
		return new RateFactory(initialRate,units,true,minimumRate,maximumRate);
	}

	public Factory generateFactory() {
		return new RateFactory(mu,getUnits(),parameterize_,getMaximumMutationRate());
	}

	/**
	 * @return A ConstantMutationRate with a fixed rate (no parameters)
	 */
	public static final ConstantMutationRate createFixed(double rate, int units) {
		return new ConstantMutationRate(rate,units,rate,rate,false);
	}
	//
	// Rate Factory
	//
	private static final class RateFactory implements Factory {
		private final double initialRate_;
		private final int units_;
		private final boolean parameterise_;
		private final double maximumMutationRate_;
		private final double minimumMutationRate_;
		public RateFactory(double initialRate, int units, boolean parameterise, double maximumMutationRate) {
			this(initialRate,units,parameterise,0,maximumMutationRate);
		}
		public RateFactory(double initialRate, int units, boolean parameterise, double minimumMutationRate, double maximumMutationRate) {
			this.initialRate_ = initialRate;
			this.units_ = units;
			this.maximumMutationRate_ = maximumMutationRate;
			this.minimumMutationRate_ = minimumMutationRate;
			this.parameterise_ = parameterise;
		}
		public ConstraintModel buildConstraintModel(SampleInformation si, MolecularClockLikelihoodModel.Instance likelihoodModel) {
		  return new SRDTGlobalClockModel(si,likelihoodModel);
		}
		public MutationRateModel generateNewModel() {
			if(parameterise_) {
				return new ConstantMutationRate(initialRate_, units_,minimumMutationRate_,maximumMutationRate_);
			}
			return createFixed(initialRate_,units_);

		}
	}
}
