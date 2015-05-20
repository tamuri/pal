// UniformRate.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import java.io.PrintWriter;
import java.io.Serializable;


/**
 * uniform rate distribution
 *
 * @author Korbinian Strimmer
 * @version $Id: UniformRate.java,v 1.5 2003/06/11 05:26:46 matt Exp $
 */
public class UniformRate extends RateDistribution implements Serializable {
    //
    // Public stuff
    //

    /**
     * construct uniform rate distribution
     */
    public UniformRate() {
        super(1);

        rate[0] = 1.0;
        probability[0] = 1.0;
    }

    // interface Report

    public void report(PrintWriter out) {
        out.println("Model of rate heterogeneity: Uniform rate at all sites");
    }

    // interface Parameterized

    public int getNumParameters() {
        return 0;
    }

    public void setParameter(double param, int n) {
        return;
    }

    public double getParameter(int n) {
        return 0.0;
    }

    public void setParameterSE(double paramSE, int n) {
        return;
    }

    public double getLowerLimit(int n) {
        return 0.0;
    }

    public double getUpperLimit(int n) {
        return 0.0;
    }

    public double getDefaultValue(int n) {
        return 0.0;
    }
}
