// RateDistribution.java
//
// (c) 1999-2000 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.io.*;

import java.io.*;


/**
 * abstract base class for models of rate variation over sites
 * employing a discrete rate distribution
 *
 * @version $Id: RateDistribution.java,v 1.12 2004/05/19 04:05:21 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 * @note This class has it's days numbered... MG
 */

public abstract class RateDistribution extends PalObjectListener.EventGenerator implements Parameterized, Report, Cloneable, Serializable
{
	//
	// Public stuff
	//

	/** number of rate categories*/
	public int numRates;

	/** rates of each rate category */
	public double[] rate;

	/** probability of each rate */
	public double[] probability;

	//
	// Protected stuff
	//

	protected FormattedOutput format;

	//
	// Serialization code
	//
	private static final long serialVersionUID= -5584969247361304141L;

	//serialver -classpath ./classes pal.substmodel.RateDistribution
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeObject(rate);
		out.writeObject(probability);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				rate= (double[])in.readObject();
				probability= (double[])in.readObject();
				numRates= rate.length;
				format = FormattedOutput.getInstance();
				break;
			}
		}
	}

	public final int getNumberOfRates() {
		return numRates;
	}
	public final double[] getRates() { return rate; }
	public final double getRate(int category) { return rate[category]; }
	/**
	 * construct discrete distribution
	 *
	 *  @param n number of rate categories
	 */
	public RateDistribution(int n)
	{
		format = FormattedOutput.getInstance();

		numRates = n;
		rate = new double[n];
		probability = new double[n];
	}

	// interface Report (remains abstract)

	// interface Parameterized (remains abstract)

	protected void printRates(PrintWriter out)
	{
		out.println("Relative rates and their probabilities:\n");
		format.displayIntegerWhite(out, numRates);
		out.println("   Rate      Probability");
		for (int i = 0; i < numRates; i++)
		{
			format.displayInteger(out, i+1, numRates);
			out.print("   ");
			format.displayDecimal(out, rate[i], 5);
			out.print("   ");
			format.displayDecimal(out, probability[i], 5);
			out.println();
		}
	}
	/**
	 * The non direct access method
	 */
	public final double[] getCategoryProbabilities() { return probability;	}
	public final double getCategoryProbability(int category) { return probability[category];	}
	public Object clone() {
		try {
			RateDistribution rd = (RateDistribution)super.clone();
			return rd;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen
			throw new InternalError();
		}
	}

}
