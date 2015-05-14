// ConstExpGrowth.java
//
// (c) 1999-2002 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.coalescent;

import pal.math.*;
import pal.misc.*;
import pal.io.*;

import java.io.*;

/**
 * This class models a population that grows exponentially from an 
 * initial population size alpha N0 at time y to a size N0 
 * at time x until the present-day.
 * (Parameters: N0=present-day population size; r=growth rate; alpha: ratio of
 * population sizes).
 *   or
 * (Parameters: N0=present-day population size; r=growth rate; N1: pre-growth
 * ancestral population size).
 * This model is nested with the exponential-growth model (alpha -> 0 and tx -> 0).
 * 
 * @version $Id: ConstExpConst.java,v 1.2 2002/02/16 00:51:43 alexi Exp $
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public class ConstExpConst extends ConstExpGrowth 
	implements Report, Parameterized, Serializable {
	
	//
	// Public stuff
	//
	
	/** time of end of exponential growth */
	public double tx; 

	/** standard error of time of growth  */
	public double txSE; 


	/**
	 * Construct demographic model with default settings.
	 */
	public ConstExpConst(int units, int parameterization) {
	
		super(units, parameterization);
		
		this.parameterization = parameterization;
		
		tx = getDefaultValue(3);
	}


	/**
	 * Construct demographic model of constexpconst population.
	 */
	public ConstExpConst(
			double size, 
			double growth, 
			double ancestral, 
			double timeX, 
			int units, 
			int parameterization) {
	
		super(size, growth, ancestral, units, parameterization);
	
		tx = timeX;
	}

	/**
	 * Makes a copy of this demographic model.
	 */
	public Object clone() {
		return new ConstExpConst(getN0(), getGrowthParam(), getAncestral(), 
getTimeX(), getUnits(), getParameterization()); 
	}

	/**
	 * Gets the time of transition from initial constant phase to exponential phase.
	 */
	public double getTransitionTime() {

		if (isLxParameterized()) return lx + tx;
		return tx - (Math.log(getAncestralN0()) - Math.log(N0)) / r;
	}

	/**
	 * @return the duration of the growth phase
	 */
	public double getGrowthPhaseDuration() {
		if (isLxParameterized()) return lx;
		return getTransitionTime() - getTimeX();
	}
	
	//NOTE: setGrowthPhaseDuration is inherited.
	
	/**
	 * @return the time at which the modern constant pop size 
	 * gives way to exponential phase.
	 */
	public double getTimeX()
	{
		return tx;
	}

	public void setTimeX(double timeX) {
		tx = timeX;
	}

	// Implementation of abstract methods
	
	/**
	 * @return the population size at time t.
	 */
	public double getDemographic(double t)
	{
		if (isN1Parameterized()) alpha = N1 / N0;
		if (isLxParameterized()) calculateRFromLx();

		if (alpha == 1.0 || r == 0.0)
		{ // Constant size
			return N0;
		}
		else if (alpha == 0.0 && tx == 0.0)
		{ // Exponential
			return N0 * Math.exp(-t * r);
		}
		else
		{
			double tc = tx - Math.log(alpha)/r;
			
			if (t < tx)
			{
				return N0;
			}
			else if (t < tc)
			{
				return N0 * Math.exp(- (t - tx) * r); 
			}
			else
			{
				return N0 * alpha;
			} 		
		}
	}

	/**
	 * @return the integral of 1 / N(t) from 0 to t.
	 */
	public double getIntensity(double t) {
		
		if (isN1Parameterized()) alpha = N1 / N0;
		if (isLxParameterized()) calculateRFromLx();

		if (alpha == 1.0 || r == 0.0)
		{
			return t/N0;
		}
		else if (alpha == 0.0 && tx == 0.0)
		{
			return (Math.exp(t*r)-1.0)/N0/r;
		}
		else
		{
			double tc = -Math.log(alpha)/r + tx;
			
			if (t < tx) {
				return t / N0; 
				
			} else if (t < tc) {
			
				return 
					// constant phase up to tx
					(tx / N0) + 
					// exponential phase from tx to t
					(Math.exp(r*(t-tx))-1.0)/N0/r; 
			} else {
				return 
					// constant phase up to tx;
					(tx / N0) + 
					// exponential phase from tx to tc
					((Math.exp(r*(tc-tx))-1.0)/N0/r) + 
					// constant phase from tc to t
					((t-tc)/(alpha*N0)); 
			} 		
		} 
		
	
	}
	
	/**
	 * @return the time for the given intensity.
	 */
	public double getInverseIntensity(double x)
	{

		if (isN1Parameterized()) alpha = N1 / N0;
		if (isLxParameterized()) calculateRFromLx();
		
		if (r == 0)
		{
			return N0*x;
		}
		else if (alpha == 0)
		{
			return Math.log(1.0+N0*x*r)/r;
		}
		else
		{
			double xx = tx/N0;
			double xc = (1.0-alpha)/(alpha*N0*r) + xx;
			
			if (x < xx) {
				return N0*x;
			} else if (x < xc)
			{
				return 
					// time of modern constant phase
					tx + 
					// time of exponential phase
					Math.log(1.0+N0*r*(x-xx))/r; 
			}
			else
			{
				return 
				// time of modern constant phase
				tx + 
				// time of exponential phase
				Math.log(1.0+N0*r*(xc-xx))/r + 
				// time of ancient constant phase 
				(N0*alpha)*(x-xc);
			} 		
		}
		
		// To be done...
	}
	
	// Parameterized interface

	public int getNumParameters()
	{
		return 4;
	}
	
	public double getParameter(int k)
	{
		switch (k)
		{
			case 0: return N0;
			case 1: return r;
			case 2: 		
				if (isN1Parameterized())
					return N1;
				else
					return alpha;
			case 3: return tx;
			default: return 0;
		}
	}

	public double getUpperLimit(int k)
	{
		double max = 0;
		switch (k)
		{
			case 0: max = 1e50; break;
			case 1: max = 1000; break;
			// we have to to compute lots of exp(rt) !!
			case 2: 		
				if (isN1Parameterized())
					max = 1e50;
				else
					max = 1.0;
			break;
			case 3: max = 1e50; break; 
			default: break;
		}

		return max;
	}

	public double getLowerLimit(int k)
	{
		double min = 0;
		switch (k)
		{
			case 0: min = 1e-12; break;
			case 1: min = 0; break;
			case 2: min = 0; break;
			case 3: min = 0; break;
			default: break;
		}
		return min;
	}

	public double getDefaultValue(int k)
	{
	
		if (k == 0)
		{
			//arbitrary default values
			if (getUnits() == GENERATIONS) {
				return 1000.0;
			} else {
				return 0.2;
			}
		}
		else if (k == 1)
		{
			return 0; //constant population
		}
		else if (k == 2)
		{
			if (isN1Parameterized())
				return getDefaultValue(0); 
			else
				return 0.5; 
		}
		else
		{
			return 0; 
		}
	}

	public void setParameter(double value, int k)
	{
		switch (k)
		{
			case 0: N0 = value; break;
			case 1: r = value; break;
			case 2: 
				if (isN1Parameterized())
					N1 = value; 
				else
					alpha = value; 
			break;
			case 3: tx = value; break;
			default: break;
		}
	}

	public void setParameterSE(double value, int k)
	{
		switch (k)
		{
			case 0: N0SE = value; break;
			case 1: rSE = value; break;
			case 2: 
				if (isN1Parameterized())
					N1SE = value; 
				else
					alphaSE = value; 
			break;
			case 3: txSE = value; break;
			default: break;
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
		out.println("Demographic model: const-exp-const");
		if (isN1Parameterized()) {
			out.println("Demographic function: N(t) = N0           for t < x");
			out.println("                             N0 exp(-r*(t-x)) for x < t < x - ln(N1/N0)/r");
			out.println("                             N1           otherwise");
		} else {
			out.println("Demographic function: N(t) = N0           for t < x");
			out.println("                             N0 exp(-r*(t-x)) for x < t < x - ln(alpha)/r");
			out.println("                             N0 alpha     otherwise");
		}	
		out.print("Unit of time: ");
		if (getUnits() == GENERATIONS)
		{
			out.print("generations");
		}
		else
		{
			out.print("expected substitutions");
		}
		out.println();
		out.println();
		out.println("Parameters of demographic function:");
		out.print(" present-day population size N0: ");
		fo.displayDecimal(out, N0, 6);
		if (N0SE != 0.0)
		{
			out.print(" (S.E. ");
			fo.displayDecimal(out, N0SE, 6);
			out.print(")");
		}	
		out.println();
		
		out.print(" growth rate r: ");
		fo.displayDecimal(out, r, 6);
		if (rSE != 0.0)
		{
			out.print(" (S.E. ");
			fo.displayDecimal(out, rSE, 6);
			out.print(")");
		}	
		out.println();

		if (isN1Parameterized()) {
			out.print(" pre-growth population size N1: ");
			fo.displayDecimal(out, N1, 6);
			if (N1SE != 0.0)
			{
				out.print(" (S.E. ");
				fo.displayDecimal(out, N1SE, 6);
				out.print(")");
			}	
			 
			out.println();
			out.print(" Ratio of poulation sizes alpha: ");
			fo.displayDecimal(out, N1/N0, 6);
			out.println();
		} else {
			out.print(" ratio of population sizes alpha: ");
			fo.displayDecimal(out, alpha, 6);
			if (alphaSE != 0.0)
			{
				out.print(" (S.E. ");
				fo.displayDecimal(out, alphaSE, 6);
				out.print(")");
			}	

			out.println();
			out.print(" initial population size alpha N0: ");
			fo.displayDecimal(out, alpha*N0, 6);
			out.println();
		}	
		out.println();
		
		out.print(" time of end of expansion phase x: ");
		fo.displayDecimal(out, tx, 6);
		if (txSE != 0.0)
		{
			out.print(" (S.E. ");
			fo.displayDecimal(out, txSE, 6);
			out.print(")");
		}	
		out.println();

		if (getLogL() != 0.0)
		{
			out.println();
			out.print("log L: ");
			fo.displayDecimal(out, getLogL(), 6);
			out.println();
		}
	}

	public static void main(String[] args) {

		double size = 100.0;
		double growth = 0.02;
		double ancestral = 0.2;
		double timeX = 25;
		int units = Units.GENERATIONS;
		int param = ALPHA_PARAMETERIZATION;
		
		
	
		ConstExpConst model = new ConstExpConst(size, growth, ancestral, timeX, units, param);
		ConstExpGrowth model2 = new ConstExpGrowth(size, growth, ancestral, units, param);
		ConstExpConst model3 = new ConstExpConst(size, growth, ancestral, 0.0, units, param);
		
		model.testConsistency(5000, 200.0);
		model2.testConsistency(5000, 200.0);
		model3.testConsistency(5000, 200.0);
	}
}

