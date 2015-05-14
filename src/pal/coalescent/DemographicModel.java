// DemographicModel.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.coalescent;

import pal.math.*;
import pal.misc.*;
import pal.io.*;
import java.io.*;


/**
 * This abstract class contains methods that are of general use for
 * modelling coalescent intervals given a demographic model.
 *
 * Parts of this class were derived from C++ code provided by Oliver Pybus.
 *
 * @version $Id: DemographicModel.java,v 1.12 2002/02/02 08:32:52 alexi Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public abstract class DemographicModel implements Units,
	Parameterized, Report, Cloneable, Serializable, Summarizable
{
	
	//
	// Public stuff
	//
	
	public DemographicModel()
	{
		rng = new MersenneTwisterFast();
		binom = new Binomial();
	
		units = GENERATIONS;
		
		fo = FormattedOutput.getInstance();
	}

	public abstract Object clone();


	//
	// functions that define a demographic model (left for subclass)
	//

	/**
	 * Gets the value of the demographic function N(t) at time t.
	 */
	public abstract double getDemographic(double t);

	/**
	 * Returns value of demographic intensity function at time t
	 * (= integral 1/N(x) dx from 0 to t).
	 */
	public abstract double getIntensity(double t);

	/**
	 * Returns value of inverse demographic intensity function 
	 * (returns time, needed for simulation of coalescent intervals).
	 */
	public abstract double getInverseIntensity(double x);

	// Parameterized and Report interface is also left for subclass


	// general functions

	/**
	 * Returns an random interval size selected from the Kingman prior of the demographic model.
	 */
	public double getSimulatedInterval (int numLin, double timeOfLastCoal)
	{
		double U = rng.nextDouble(); // create unit uniform random variate				
		// has to be done somewhere! 
		binom.setMax(numLin);
			
		double tmp = -Math.log(U)/binom.getNChoose2(numLin) + getIntensity(timeOfLastCoal);
		double interval = getInverseIntensity(tmp) - timeOfLastCoal;
		
		return interval;
	}

	/**
	 * Calculates the integral 1/N(x) dx between start and finish.
	 */
	public double getIntegral(double start, double finish)
	{
		return getIntensity(finish) - getIntensity(start);
	}
	
	
	/**
	 * Returns the likelihood of a given *coalescent* interval
	 */
	public double computeLogLikelihood(double width, double timeOfPrevCoal, int numLineages)
	{
		
		return computeLogLikelihood(width, timeOfPrevCoal, 
			numLineages, CoalescentIntervals.COALESCENT);
	}
	
	/**
	 * Returns the likelihood of a given interval,coalescent or otherwise.
	 */
	public double computeLogLikelihood(double width, 
		double timeOfPrevCoal, int numLineages, int type)
	{
		binom.setMax(numLineages);
		
		double timeOfThisCoal = width + timeOfPrevCoal;

		double intervalArea = getIntegral(timeOfPrevCoal, timeOfThisCoal);
		double like = 0;
		switch (type) {
			case CoalescentIntervals.COALESCENT:
				like = -Math.log(getDemographic(timeOfThisCoal)) - 
					(binom.getNChoose2(numLineages)*intervalArea);
				break;
			case CoalescentIntervals.NEW_SAMPLE:
				like = -(binom.getNChoose2(numLineages)*intervalArea);
				break;
		}
	
		return like;
	}


	/**
	 * Units in which population size is measured.
	 */
	private int units;

	/**
	 * sets units of measurement.
	 *
	 * @param u units
	 */
	public void setUnits(int u)
	{
		units = u;
	}

	/**
	 * returns units of measurement.
	 */
	public int getUnits()
	{
		return units;
	}
	
	private double logL = 0.0;
	
	/**
	 * sets log likelihood
	 *
	 * @param l log-likelihood
	 */
	public void setLogL(double l)
	{
		logL = l;
	}

	/**
	 * returns log-likelihood.
	 */
	public double getLogL()
	{
		return logL;
	}

	/**
	 * This function tests the consistency of the 
	 * getIntensity and getInverseIntensity methods
	 * of this demographic model. If the model is
	 * inconsistent then a RuntimeException will be thrown.
	 * @param model the demographic model to test.
	 * @param steps the number of steps between 0.0 and maxTime to test.
	 * @param maxTime the maximum time to test.
	 */
	public void testConsistency(int steps, double maxTime) {
		
		double delta = maxTime / (double)steps;
		
		System.out.println("time\tN(time)\tIntensity(time)\tinverse");
		for (int i =0; i <= steps; i++) {
			double time = (double)i * delta;
			double intensity = getIntensity(time);
			double newTime = getInverseIntensity(intensity);
			
			System.out.println(time + "\t" + getDemographic(time) + "\t" + intensity + "\t" + newTime);
			
			if (Math.abs(time-newTime) > 1e-12) {
				throw new RuntimeException(
					"Demographic model not consistent! error size = " + 
					Math.abs(time-newTime)); 
			}
		}
		System.out.println("Demographic model is consistent!");
	}
		
	//
	// Private and protected stuff
	//
	
	private MersenneTwisterFast rng;
	private Binomial binom;
		
	protected FormattedOutput fo;
}
