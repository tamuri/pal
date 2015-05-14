// GTR.java
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
 * GTR (general time reversible) model of nucleotide evolution
 *
 * <i>Lanave, C., G. Preparata, C. Saccone, and G. Serio. 1984. A new method for calculating evolutionary substitution rates. J Mol Evol 20: 86-93. </i>
 *
 * @version $Id: GTR.java,v 1.13 2003/11/30 05:29:22 matt Exp $
 * <p>
 * <em>Parameters</em>
 * <ol>
 *  <li> A </li> <!-- 0 -->
 *  <li> B </li> <!-- 1 -->
 *  <li> C </li> <!-- 2 -->
 *  <li> D </li> <!-- 3 -->
 *  <li> E </li> <!-- 4 -->
 * </ol>
 * </p>
 *
 * @author Korbinian Strimmer
 */
public class GTR extends NucleotideModel implements Serializable, XMLConstants
{

	//
	// Private stuff
	//

	private boolean showSE;
	private double a, b, c, d, e;
	private double aSE, bSE, cSE, dSE, eSE;

	//
	// Serialization code
	//
	private static final long serialVersionUID= -8557884770092535699L;

	//serialver -classpath ./classes pal.substmodel.GTR
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeBoolean(showSE);
		out.writeDouble(a);
		out.writeDouble(b);
		out.writeDouble(c);
		out.writeDouble(d);
		out.writeDouble(e);
		out.writeDouble(aSE);
		out.writeDouble(bSE);
		out.writeDouble(cSE);
		out.writeDouble(dSE);
		out.writeDouble(eSE);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				showSE = in.readBoolean();
				a = in.readDouble();
				b = in.readDouble();
				c = in.readDouble();
				d = in.readDouble();
				e = in.readDouble();
				aSE = in.readDouble();
				bSE = in.readDouble();
				cSE = in.readDouble();
				dSE = in.readDouble();
				eSE = in.readDouble();
				break;
			}
		}
	}


	/**
	 * constructor 1
	 *
	 * @param a entry in rate matrix
	 * @param b entry in rate matrix
	 * @param c entry in rate matrix
	 * @param d entry in rate matrix
	 * @param e entry in rate matrix
	 * @param freq nucleotide frequencies
	 */
	public GTR(double a, double b, double c, double d, double e, double[] freq)	{
		super(freq);
		this.a = a; this.b = b; this.c = c; this.d = d; this.e = e;
		setParameters(new double[] {a,b,c,d,e});
		showSE = false;
	}

	/**
	 * constructor 2
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 */
	public GTR(double[] params, double[] freq)	{
		this(params[0], params[1], params[2],
			params[3], params[4], freq);
	}

	public Object clone() {
		return new GTR(this);
	}

	private GTR(GTR gtr) {
		this(gtr.a, gtr.b, gtr.c, gtr.d, gtr.e, gtr.getEquilibriumFrequencies());
	}

	// Get numerical code describing the model type
	public int getModelID()	{
		return 0;
	}

	// interface Report

	public void report(PrintWriter out)
	{
		out.println("Model of substitution: GTR (Lanave et al. 1984)");

		out.print("Parameter a: ");
		format.displayDecimal(out, a, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, aSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Parameter b: ");
		format.displayDecimal(out, b, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, bSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Parameter c: ");
		format.displayDecimal(out, c, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, cSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Parameter d: ");
		format.displayDecimal(out, d, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, dSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Parameter e: ");
		format.displayDecimal(out, e, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, eSE, 2);
			out.print(")");
		}
		out.println();

		out.println("                                   A  C  G  T");
		out.println("Corresponding rate matrix      ----------------");
		out.println("(shown without frequencies):     A    a  b  c");
		out.println("                                 C       d  e");
		out.println("                                 G          1");

		out.println();
		printFrequencies(out);
		printRatios(out);
	}

	// interface Parameterized

	public int getNumParameters()	{
		return 5;
	}

	public void setParameterSE(double paramSE, int n)	{
		switch(n)	{
			case 0: aSE = paramSE; break;
			case 1: bSE = paramSE; break;
			case 2: cSE = paramSE; break;
			case 3: dSE = paramSE; break;
			case 4: eSE = paramSE; break;

			default: throw new IllegalArgumentException();
		}
		showSE = true;
	}

	public double getLowerLimit(int n)	{
		return 0.0001;
	}

	public double getUpperLimit(int n)	{
		return 10000.0;
	}

	public double getDefaultValue(int n)	{
		return 1.0;
	}

	/**
	 * @return the name of this rate matrix
	 */
	public String getUniqueName() {
		return GTR;
	}

	public String getParameterName(int i) {
		switch (i) {
			case 0: return A_TO_C;
			case 1: return A_TO_G;
			case 2: return A_TO_T;
			case 3: return C_TO_G;
			case 4: return C_TO_T;
			default: return UNKNOWN;
		}
	}

	// Make REV model
	protected void rebuildRateMatrix(double[][] rate, double[] parameters){
		this.a = parameters[0];
		this.b = parameters[1];
		this.c = parameters[2];
		this.d = parameters[3];
		this.e = parameters[4];
		// Q matrix
		rate[0][1] = a; rate[0][2] = b; rate[0][3] = c;
		rate[1][2] = d; rate[1][3] = e;
		rate[2][3] = 1;
	}
}
