// WindowedMutationRate.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.mep;

import pal.math.*;
import pal.misc.*;
import pal.io.*;
import pal.eval.*;
import pal.treesearch.*;

import java.io.*;

/**
 * This class models a windowed mutation rate
 * (parameter: mu = mutation rate). <BR>
 *
 * @version $Id: WindowedMutationRate.java,v 1.8 2004/08/02 05:22:04 matt Exp $
 *
 * @author Alexei Drummond
 */
public class WindowedMutationRate extends MutationRateModel implements Report, Summarizable, Parameterized, Serializable
{
	//
	// Private stuff
	//

	/** mutation rates */
	private double muBackground;
	private double muWindow;

	/** mutation rate SEs */
	private double muBackgroundSE;
	private double muWindowSE;

	/** mutation rate change times */
	private double windowCenter;
	private double windowWidth;

	private boolean backgroundFixed = false;

	String[] summaryTypes = null;

	protected WindowedMutationRate(WindowedMutationRate toCopy) {
		super(toCopy);
		this.muBackground = toCopy.muBackground;
		this.muWindow = toCopy.muWindow;

		this.muBackgroundSE = toCopy.muBackgroundSE;
		this.muWindowSE = toCopy.muWindowSE;
		this.windowCenter = toCopy.windowCenter;
		this.windowWidth = toCopy.windowWidth;
	}
	/**
	 * Construct mutation model with default settings
	 */
	public WindowedMutationRate(double windowCenter, double windowWidth, int units, double maximumMutationRate) {
		super(units,maximumMutationRate);

		this.windowCenter = windowCenter;
		this.windowWidth = windowWidth;

		muBackground = getDefaultValue(0);
		muWindow = getDefaultValue(0);
	}


	/**
	 * Construct mutation rate model of a give rate in given units.
	 */
	public WindowedMutationRate(double muBackground,
		double windowCenter, double windowWidth, int units, double maximumMutationRate) {

		super(units,maximumMutationRate);
		this.muBackground = muBackground;
		backgroundFixed = true;
		this.windowCenter = windowCenter;
		this.windowWidth = windowWidth;
		muWindow = getDefaultValue(0);
	}

	/**
	 * Construct mutation rate model of a give rate in given units.
	 */
	public WindowedMutationRate(double muWindow, double muBackground,
		double windowCenter, double windowWidth, int units, boolean fixedb, double maximumMutationRate) {

		super(units,maximumMutationRate);

		this.muWindow = muWindow;
		this.muBackground = muBackground;
		backgroundFixed = fixedb;

		this.windowCenter = windowCenter;
		this.windowWidth = windowWidth;

	}

	/**
	 * Construct mutation rate model of a give rate in given units.
	 */
	public WindowedMutationRate(double muWindow, double muBackground,
		double windowCenter, double windowWidth, int units, double maximumMutationRate) {

		this(muWindow, muBackground, windowCenter, windowWidth, units, false,maximumMutationRate);
	}


	public Object clone() {
		return getCopy();
	}
	public MutationRateModel getCopy() {
		return new WindowedMutationRate(this);
	}

	public String[] getSummaryTypes() {
		if (summaryTypes == null) {
			summaryTypes = new String[4];
			summaryTypes[0] = "window mu";
			summaryTypes[1] = "background mu";
			summaryTypes[2] = "window center";
			summaryTypes[3] = "window width";
		}
		return summaryTypes;
	}

	public double getSummaryValue(int summaryType) {

		switch (summaryType) {
			case 0: return muWindow;
			case 1: return muBackground;
			case 2: return windowCenter;
			case 3: return windowWidth;
		}
		throw new RuntimeException("Assertion error: unknown summary type :"+summaryType);
	}

	/**
	 * returns current day mutation rate.
	 */
	public double getMu()
	{
		return getMutationRate(0.0);
	}


	// Implementation of abstract methods

	public final double getMutationRate(double t)
	{
		if ((t > windowCenter - (windowWidth / 2.0)) &&
			(t <= windowCenter + (windowWidth / 2.0))) {

			return muWindow;
		}

		return muBackground;
	}

	/**
	 * Window must not span zero!
	 */
	public final double getExpectedSubstitutions(double time)
	{
		double height = 0.0;

		// bit before window
		double totalTime = windowCenter - (windowWidth / 2.0);
		if (totalTime > time) return muBackground * time;
		if (totalTime >= 0.0)
			height += muBackground * totalTime;
		else System.err.println("Mutation window spans time zero!");

		// window
		if ((totalTime + windowWidth) > time) {
			return height + (muWindow * (time - totalTime));
		}
		height += muWindow * windowWidth;

		totalTime += windowWidth;

		// bit after window
		return height + (muBackground * (time - totalTime));
	}

	/**
	 * Window must not span zero!
	 */
	public final double getEndTime(double expectedSubs, double startTime) {
		double windowStart = windowCenter-windowWidth;
		double windowEnd = windowCenter+windowWidth;

		if(startTime>=windowStart) {
			//Start is after window starts
			if(startTime>windowEnd) {
				//Start is after window finishes
				return expectedSubs/muBackground+startTime;
			}
			//Start is within window
			double eTime = expectedSubs / muWindow + startTime;
			if(eTime< windowEnd) {
				//End is within window
				return eTime;
			}
			//End is outside window
			//Calculate window substitutions
			double wSubs = muWindow*(windowEnd-startTime);
			//Calculate end via subs outside window
			return (expectedSubs-wSubs)/muBackground+windowEnd;
		}
		//Start is before window start
		double eTime = expectedSubs / muWindow + startTime;
		if(eTime<windowStart) {
			//End is before window start
			return eTime;
		}
		double bwSubs = muBackground*(windowStart-startTime);
		eTime = (expectedSubs-bwSubs)/muWindow + windowStart;
		if(eTime<windowEnd) {
			//End is within window
			return eTime;
		}
		//End is after window (start is before window)
		double windowSubs = muWindow*(windowWidth);
		return (expectedSubs-windowSubs-bwSubs)/muBackground+windowEnd;
	}

	/**
	 * Linearly scales this mutation rate model.
	 * @param scale getExpectedSubstitutions should return scale instead of 1.0 at time t.
	 */
	public final void scale(double scale) {
		muBackground *= scale;
		muWindow *= scale;
	}

	// Parameterized interface

	public int getNumParameters()
	{
		if (backgroundFixed) return 1;
		return 2;
	}

	public double getParameter(int k)
	{
		switch (k) {
			case 0: return muWindow;
			case 1: return muBackground;
		}
		return muWindow;
	}

	public double getUpperLimit(int k)
	{
		return 1e12;
	}

	public double getLowerLimit(int k)
	{
		return 1e-12;
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
		switch (k) {
			case 0: muWindow = value; break;
			case 1: muBackground = value; break;
		}
	}

	public void setParameterSE(double value, int k) {
		switch (k) {
			case 0: muWindowSE = value; break;
			case 1: muBackgroundSE = value; break;
		}
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
		out.println("Mutation rate model: windowed mutation rate ");

		out.print("Unit of time: ");
		out.print(Units.UNIT_NAMES[getUnits()]);
		out.println();
		out.println();
		out.println("Parameters of demographic function:");
		out.print("window = ");
		fo.displayDecimal(out, windowCenter - (windowWidth / 2.0), 6);
		out.print(" - ");
		fo.displayDecimal(out, windowCenter + (windowWidth / 2.0), 6);
		out.println();
		out.print("window mutation rate = ");
		fo.displayDecimal(out, muWindow, 9);
		out.println();
		out.print("background mutation rate = ");
		fo.displayDecimal(out, muBackground, 9);
		out.println();
		if (backgroundFixed) {
			out.println("background mutation rate fixed.");
		} else {
			out.println("background mutation rate free to vary.");
		}
	}

	public String toSingleLine() {
		String line = "";
		line += "win mu\t" + muWindow + "\t";
		line += "bg mu\t" + muBackground + "\t";
		line += "win cen\t" + windowCenter + "\t";
		line += "win wid\t" + windowWidth + "\t";
		return line;
	}
	public Factory generateFactory() {
		return new RateFactory(muWindow,muBackground,windowCenter,windowWidth,getUnits(),getMaximumMutationRate());
	}
// ===========================================================================
// Static stuff
	/**
	 * Generate a MutationRateModel.Factory class for a WindowedMutationRate
	 */
	public static final Factory getFactory(double muWindow, double muBackground,
			double windowCenter, double windowWidth, int units, double maximumMutationRate) {
		return new RateFactory(muWindow,muBackground,windowCenter,windowWidth,units, maximumMutationRate);
	}
	private static final class RateFactory implements Factory {
		private final double muWindow_;
		private final double muBackground_;
		private final double maximumMutationRate_;
		private final double windowCenter_;
		private final double windowWidth_;
		private final int units_;

		public RateFactory(double muWindow, double muBackground,
			double windowCenter, double windowWidth, int units,
			double maximumMutationRate) {
			this.muWindow_ = muWindow;	this.muBackground_ = muBackground;
			this.windowCenter_ = windowCenter;	this.windowWidth_ = windowWidth;
			this.units_ = units;
			this.maximumMutationRate_ = maximumMutationRate;
		}
		public MutationRateModel generateNewModel() {
			return new WindowedMutationRate(muWindow_, muBackground_, windowCenter_, windowWidth_, units_,maximumMutationRate_);
		}
				public ConstraintModel buildConstraintModel(SampleInformation si, MolecularClockLikelihoodModel.Instance likelihoodModel) {
					throw new RuntimeException("Not implemented yet!");
		}
	}
}
