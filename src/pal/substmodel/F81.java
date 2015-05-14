// F81.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.util.*;
import java.io.*;


/**
 * Felsenstein 1981 model of nucleotide evolution
 *
 * @version $Id: F81.java,v 1.10 2003/11/13 04:05:39 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class F81 extends NucleotideModel implements Serializable, XMLConstants
{
	public final static RateMatrix JC69_MATRIX = new F81(new double[] { 0.25, 0.25, 0.25, 0.25});
	public final static SubstitutionModel JC69_MODEL = SubstitutionModel.Utils.createSubstitutionModel(JC69_MATRIX);

	private final static long serialVersionUID=-8473405513320987709L;
	/**
	 * constructor
	 *
	 * @param freq nucleotide frequencies
	 */
	public F81(double[] freq)
	{
		super(freq);

	}

	// Get numerical code describing the model type
	public int getModelID()
	{
		return 4;
	}

	// interface Report

	public void report(PrintWriter out)
	{
		out.println("Model of substitution: F81 (Felsenstein 1981)");
		printFrequencies(out);
		printRatios(out);
	}

	// interface Parameterized

	public int getNumParameters()
	{
		return 0;
	}


	public void setParameterSE(double paramSE, int n)	{
		return;
	}

	public double getLowerLimit(int n)
	{
		return 0.0;
	}

	public double getUpperLimit(int n)
	{
		return 0.0;
	}

	public double getDefaultValue(int n)
	{
		return 0.0;
	}
	public String getParameterName(int i) {
		throw new RuntimeException("This model has no parameters!");
	}

	/**
	 * @return the name of this rate matrix
	 */
	public String getUniqueName() {
		return F81;
	}

	//
	// Private stuff
	//

	// Make F81 model
	protected void rebuildRateMatrix(double[][] rate, double[] parameters)
	{
		// Q matrix
		rate[0][1] = 1; rate[0][2] = 1.0; rate[0][3] = 1;
		rate[1][2] = 1; rate[1][3] = 1.0;
		rate[2][3] = 1;
	}
}
