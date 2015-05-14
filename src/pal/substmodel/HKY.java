// HKY.java
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
 * Hasegawa-Kishino-Yano model of nucleotide evolution
 * <i>Hasegawa, M., H. Kishino, and T. Yano. 1985. Dating of the human-ape splitting by a molecular clock of mitchondrial DNA. J. Mol. Evol. 22:160-174. </i>
 * @version $Id: HKY.java,v 1.11 2003/11/30 05:29:22 matt Exp $
 * <p>
 * <em>Parameters</em>
 * <ol>
 *  <li> Kappa </li> <!-- 0 -->
 * </ol>
 * </p>
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class HKY extends NucleotideModel implements Serializable, XMLConstants {
	public static final int KAPPA_PARAMETER_INDEX = 0;
	/**
	 * Constructor 1
	 *
	 * @param kappa transition/transversion rate ratio
	 * @param freq nucleotide frequencies
	 */
	public HKY(double kappa, double[] freq)
	{
		super(freq);
		this.kappa = kappa;
		setParameters(new double[] { kappa });

		showSE = false;
	}

	/**
	 * Constructor 2
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 */
	public HKY(double[] params, double[] freq)
	{
		this(params[0], freq);
	}


	public Object clone() {
		return new HKY(this);
	}

	private HKY(HKY hky) {
		this(hky.kappa, hky.getEquilibriumFrequencies());
	}

	// Get numerical code describing the model type
	public int getModelID()
	{
		return 2;
	}


	// interface Report

	public void report(PrintWriter out)
	{
		out.println("Model of substitution: HKY (Hasegawa et al. 1985)");
		out.print("Transition/transversion rate ratio kappa: ");
		format.displayDecimal(out, kappa, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, kappaSE, 2);
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
		return 1;
	}

	public void setParameterSE(double paramSE, int n)
	{
		kappaSE = paramSE;

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
		return 4.0;
	}

	public String getParameterName(int i) {
		if (i == 0) return KAPPA;
		return UNKNOWN;
	}

	public String getUniqueName() {
		return HKY;
	}

	//
	// Private stuff
	//

	private boolean showSE;
	private double kappa, kappaSE;

	// Make HKY model
	protected void rebuildRateMatrix(double[][] rate, double[] parameters)	{
		this.kappa = parameters[KAPPA_PARAMETER_INDEX];
		// Q matrix
		rate[0][1] = 1; rate[0][2] = kappa; rate[0][3] = 1;
		rate[1][2] = 1; rate[1][3] = kappa;
		rate[2][3] = 1;
	}
}
