// TN.java
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
 * Tamura-Nei model of nucleotide evolution
 * <i>Tamura, K. and M. Nei. (1993). Estimation of the number of nucleotide substitutions in the control region of mitochondrial DNA in humans and chimpanzees. Mol. Bio. Evol. 10:512-526.</i>
 * <p>
 * <em>Parameters</em>
 * <ol>
 *  <li> Kappa </li> <!-- 0 -->
 *  <li> r </li> <!-- 1 -->
 * </ol>
 * </p>
 * @version $Id: TN.java,v 1.8 2003/11/13 04:05:39 matt Exp $
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class TN extends NucleotideModel implements XMLConstants {
	public static final int KAPPA_PARAMETER_INDEX = 0;
	public static final int R_PARAMETER_INDEX = 1;

	/**
	 * constructor 1
	 *
	 * @param kappa transition/transversion rate ratio
	 * @param r pyrimidine/purin transition rate ratio
	 * @param freq nucleotide frequencies
	 */
	public TN(double kappa, double r, double[] freq)
	{
		super(freq);

		this.kappa = kappa;
		this.r = r;
		setParameters(new double[] { kappa,r } );

		showSE = false;
	}

	/**
	 * constructor 2
	 *
	 * @param params parameter list
	 * @param freq nucleotode frequencies
	 */
	public TN(double[] params, double[] freq)
	{
		this(params[0], params[1], freq);
	}

	// Get numerical code describing the model type
	public int getModelID()
	{
		return 1;
	}

	// interface Parameterized

	public void report(PrintWriter out)
	{
		out.println("Model of substitution: TN (Tamura-Nei 1993)");
		out.print("Transition/transversion rate ratio kappa: ");
		format.displayDecimal(out, kappa, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, kappaSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Y/R transition rate ratio: ");
		format.displayDecimal(out, r, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, rSE, 2);
			out.print(")");
		}
		out.println();

		out.println();
		printFrequencies(out);
		printRatios(out);
	}

	// interface Parameterized

	public int getNumParameters()
	{
		return 2;
	}

	public void setParameterSE(double paramSE, int n)
	{
		switch(n)
		{
			case 0: kappaSE = paramSE; break;
			case 1: rSE = paramSE; break;

			default: throw new IllegalArgumentException();
		}

		showSE = true;
	}

	public double getLowerLimit(int n)
	{
		return 0.0001;
	}

	public double getUpperLimit(int n)
	{
		return 100.0;
	}

	public double getDefaultValue(int n)
	{
		double value;

		switch(n)
		{
			case 0: value = 4.0; break;
			case 1: value = 0.5; break;

			default: throw new IllegalArgumentException();
		}

		return value;
	}

	public String getUniqueName() {
		return TN;
	}

	public String getParameterName(int i) {
		switch (i) {
			case 0: return KAPPA;
			case 1: return PYRIMIDINE_PURINE_RATIO;
			default: return UNKNOWN;
		}
	}

	//
	// Private stuff
	//

	private boolean showSE;
	private double kappa, kappaSE, r, rSE;

	// Make TN model
	protected void rebuildRateMatrix(double[][] rate, double[] parameters)
	{
		this.kappa = parameters[KAPPA_PARAMETER_INDEX];
		this.r = parameters[R_PARAMETER_INDEX];

		// Q matrix
		rate[0][1] = 1; rate[0][2] = 2.0*kappa/(r+1.0); rate[0][3] = 1;
		rate[1][2] = 1; rate[1][3] = 2.0*kappa*r/(r+1.0);
		rate[2][3] = 1;
	}
}
