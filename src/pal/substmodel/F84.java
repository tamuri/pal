// F84.java
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
 * Felsenstein 1984 (PHYLIP) model of nucleotide evolution
 *
 * @version $Id: F84.java,v 1.12 2003/11/30 05:29:22 matt Exp $
 * <p>
 * <em>Parameters</em>
 * <ol>
 *  <li> ExpectedTsTv </li> <!-- 0 -->
 * </ol>
 * </p>
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class F84 extends NucleotideModel implements Serializable, XMLConstants {
	public static final int EXPECTED_TS_TV_PARAMETER_INDEX = 0;
	/**
	 * constructor 1
	 *
	 * @param expectedTsTv expected transition-transversion ratio
	 * @param freq nucleotide frequencies
	 */
	public F84(double expectedTsTv, double[] freq)
	{
		super(freq);

		this.expectedTsTv = expectedTsTv;
		setParameters(new double[] { expectedTsTv } );
		convertToTN();
		showSE = false;
	}

	/**
	 * Constructor 2
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 */
	public F84(double[] params, double[] freq)
	{
		this(params[0], freq);
	}

	// Get numerical code describing the model type
	public int getModelID()
	{
		return 3;
	}

	// interface Report

	public void report(PrintWriter out) {
		out.println("Model of substitution: F84 (Felsenstein 1984, PHYLIP)");
		out.print("PHYLIP Transition/transversion parameter: ");
		format.displayDecimal(out, expectedTsTv, 2);
		if (showSE)	{
			out.print("  (S.E. ");
			format.displayDecimal(out, expectedTsTvSE, 2);
			out.print(")");
		}
		out.println();

		out.println();
		printFrequencies(out);
		printRatios(out);
		out.println();
		out.println("This model corresponds to a Tamura-Nei (1993) model with");
		out.print(" Transition/transversion rate ratio kappa: ");
		format.displayDecimal(out, kappa, 2);
		out.println();
		out.print(" Y/R transition rate ratio: ");
		format.displayDecimal(out, r, 2);
		out.println();
		out.println("and the above nucleotide frequencies.");
		out.println();
	}
	// interface InterfaceReporter

	/*public void report(ReportContainer rc) {
		new Object[] {
			"Model Substitution",
			new Citation("f84"),

		}
		<title>Model of Substitution <cite>f84</cite></title>
		<data>
			<double name = "PHYLIP Transition/transversion parameter">32</double>

		rc.addItem(
		rc.addItem("Model of substitution",null,"F84 (Felsenstein 1984, PHYLIP)");
		rc.addItem("PHYLIP Transition/transversion parameter", null, new Integer(expectedTsTV));
		if (showSE)	{
			rc.supplementItem("S.E.", "Standard error of ts/tv parameter", new Integer(expectedTsTvSE));
		}
		reportFrequencies(rc);
		reportRatios(rc);
		ReportContainer rc2 = rc.addContainer("Tamura-Nei parameters", "This model corresponds to a Tamura-Nei (1993) model
		out.println("This model corresponds to a Tamura-Nei (1993) model with");
		out.print(" Transition/transversion rate ratio kappa: ");
		format.displayDecimal(out, kappa, 2);
		out.println();
		out.print(" Y/R transition rate ratio: ");
		format.displayDecimal(out, r, 2);
		out.println();
		out.println("and the above nucleotide frequencies.");
		out.println();
	}*/

	// interface Parameterized

	public int getNumParameters() {
		return 1;
	}


	public void setParameterSE(double paramSE, int n)
	{
		expectedTsTvSE = paramSE;

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
		return 2.0;
	}

	/**
	 * @return the name of this rate matrix
	 */
	public String getUniqueName() {
		return F84;
	}

	public String getParameterName(int i) {
		switch (i) {
			case 0: return TS_TV_RATIO;
			default: return UNKNOWN;
		}
	}

	//
	// Private stuff
	//

	private boolean showSE;
	private double kappa, r;
	private double expectedTsTv, expectedTsTvSE;

	private void convertToTN()
	{
		double[] frequency = getEquilibriumFrequencies();
		double piA = frequency[0];
		double piC = frequency[1];
		double piG = frequency[2];
		double piT = frequency[3];
		double piR = piA + piG;
		double piY = piC + piT;

		double rho = (piR*piY*(piR*piY*expectedTsTv - (piA*piG + piC*piT)))/
			(piC*piT*piR + piA*piG*piY);

		kappa = 1.0 + 0.5*rho*(1.0/piR + 1.0/piY);
		r = (piY + rho)/piY * piR/(piR + rho);
	}

	// Make TN model
	protected void rebuildRateMatrix(double[][] rate, double[] parameters)	{
		this.expectedTsTv = parameters[EXPECTED_TS_TV_PARAMETER_INDEX];
		convertToTN();
		// Q matrix
		rate[0][1] = 1; rate[0][2] = 2.0*kappa/(r+1.0); rate[0][3] = 1;
		rate[1][2] = 1; rate[1][3] = 2.0*kappa*r/(r+1.0);
		rate[2][3] = 1;
	}
	public String toString() {
		StringWriter sw = new StringWriter();
		report(new PrintWriter(sw));
		return sw.toString();
	}
}
