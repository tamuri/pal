// CachedSubstitutionModel.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.util.*;
import pal.math.*;
import pal.datatype.*;
import pal.mep.*;

import java.io.*;


/**
 * <b>a cached rate matrix</b>.
 * This model caches transition probabilities by distance in order
 * to increase speed of likelihood calculations for repeated calls with same/similar
 * branch lengths.
 * <br>
 * <em> AS OF 11 November 2003 this class has been made abstract and will be removed at a later point (as it never really helped much) </em>
 * @version $Id: CachedRateMatrix.java,v 1.13 2003/11/13 04:05:39 matt Exp $
 *
 * @author Alexei Drummond
 */
public abstract class CachedRateMatrix implements RateMatrix, PalObjectListener {

	private static final double TOLERANCE = 1e-8;

	private DoubleKeyCache cache;
	private Pij pij;

	private RateMatrix rateMatrix;
	private int dimension;

	boolean modelChanged_ = false;

	/**
	 * @param model the underlying substitution model
	 * @param cache the caching method used
	 */
	public CachedRateMatrix(RateMatrix rateMatrix, DoubleKeyCache cache) {
		this.rateMatrix = rateMatrix;
		dimension = rateMatrix.getDimension();
		this.cache = cache;
		this.rateMatrix.addPalObjectListener(this);
	}

	/**
	 * @param model the underlying substitution model
	 */
	public CachedRateMatrix(RateMatrix rateMatrix, int maxCacheSize) {
		this(rateMatrix, new DefaultCache(maxCacheSize));
	}

	/**
	 * @return a clone of this cached model.
	 * @param model the previous cached model.
	 */
	public CachedRateMatrix(CachedRateMatrix cachedRateMatrix) {

		this.rateMatrix = (RateMatrix)cachedRateMatrix.rateMatrix.clone();
		this.rateMatrix.addPalObjectListener(this);
		this.dimension = cachedRateMatrix.dimension;
		this.cache = (DoubleKeyCache)cachedRateMatrix.cache;
	}

	// interface Report
	public final void report(PrintWriter out) {
		rateMatrix.report(out);
	}

	public final int getTypeID() {
		return rateMatrix.getTypeID();
	}

	public final int getModelID() {
		return rateMatrix.getModelID();
	}

	public final int getDimension() {
		return dimension;
	}

	public final double[] getEquilibriumFrequencies() {
		return rateMatrix.getEquilibriumFrequencies();
	}

	public final double getEquilibriumFrequency(int i) {
		return rateMatrix.getEquilibriumFrequency(i);
	}

	public final double[][] getRelativeRates() {
		return rateMatrix.getRelativeRates();
	}


	/**
	 * Return string representation of substitution model.
	 */
	public String toString() {
		return rateMatrix.toString();
	}

	// interface Parameterized

	public final int getNumParameters() { return rateMatrix.getNumParameters(); }

	public final void setParameter(double param, int n) {
		rateMatrix.setParameter(param, n);
		modelChanged_ = true;
	}

	public final double getParameter(int n) { return rateMatrix.getParameter(n); }
	public final void setParameterSE(double paramSE, int n) { rateMatrix.setParameterSE(paramSE, n);}
	public final double getLowerLimit(int n) { return rateMatrix.getLowerLimit(n);}
	public final double getUpperLimit(int n) { return rateMatrix.getUpperLimit(n);}
	public final double getDefaultValue(int n) { return rateMatrix.getDefaultValue(n); }
	public final String getParameterName(int i) { return rateMatrix.getParameterName(i); }

	/**
	 * set distance and corresponding computation transition probabilities
	 *
	 * @param k distance
	 */
	public final void setDistance(double k) {
		if(modelChanged_) {
			cache.clearCache();
		}
		pij = (Pij)cache.getNearest(k, TOLERANCE);

		if (pij == null ) {
			rateMatrix.setDistance(k);

			double[][] probs = new double[dimension][dimension];
			rateMatrix.getTransitionProbabilities(probs);
			pij = new Pij(k, probs);
			cache.addDoubleKey(k,pij);
		}
		modelChanged_ = false;
	}
	public final void setDistanceTranspose(double k) {
		throw new RuntimeException("Not implemented yet!");
	}


	/**
	 * get transition probability for the preselected model and
	 * the previously specified distance
	 *
	 * @param i start state
	 * @param j end state
	 *
	 * @return transition probability
	 */
	public final double getTransitionProbability(int i, int j) {
		return pij.probs[i][j];
	}

	public final void getTransitionProbabilities(double[][] probs) {
		pal.misc.Utils.copy(pij.probs, probs);
	}

	/** A non shallow implementation of clone() */
	public final Object clone() {
		throw new RuntimeException("Not implemented yet!");
//		return new CachedRateMatrix((CachedRateMatrix)this);
	}

	public final DataType getDataType() { return rateMatrix.getDataType();}

	public final String getUniqueName() { return rateMatrix.getUniqueName(); }

	static final class DK implements DoubleKey {
		double d;

		public DK(double d) {
			this.d = d;
		}

		public final double getKey() {
			return d;
		}

		public int compareTo(Object o) {
			DoubleKey dk = (DoubleKey)o;
			double d2 = dk.getKey();

			if (d < d2) return -1;
			if (d == d2) return 0;
			return 1;
		}
	}

	static final class Pij implements DoubleKey {

		public double[][] probs;
		public double distance;

		public Pij(double distance, double[][] probs) {
			this.probs = probs;
			this.distance = distance;
		}

		public double getKey() {
			return distance;
		}

		public int compareTo(Object o) {
			DoubleKey dk = (DoubleKey)o;
			double d2 = dk.getKey();

			if (distance < d2) return -1;
			if (distance == d2) return 0;
			return 1;
		}
	}
	public void structureChanged(PalObjectEvent pe) {
		modelChanged_ = true;
	}
	public void parametersChanged(PalObjectEvent pe) {
		modelChanged_ = true;
	}

	public void addPalObjectListener(PalObjectListener pol) {
		rateMatrix.addPalObjectListener(pol);
	}
	public void removePalObjectListener(PalObjectListener pol) {
		rateMatrix.removePalObjectListener(pol);
	}
	/**
	 * @return null
	 */
	public OrthogonalHints getOrthogonalHints() { return null; }


}
