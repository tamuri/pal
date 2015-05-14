// ParameterizedNeoWrapper.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * <p>Title: ParameterizedNeoWrapper </p>
 * <p>Description: A wrapper for the old parameterized interface over the new parameterized interface</p>
 * @author Matthew Goode
 * @version 1.0
 */

public class ParameterizedNeoWrapper implements NeoParameterized {
  private final Parameterized base_;
	private final int numberOfParameters_;
	public ParameterizedNeoWrapper(Parameterized base) {
		this.base_ = base;
		this.numberOfParameters_ = base.getNumParameters();
  }
	public int getNumberOfParameters() { return numberOfParameters_; }

	public void setParameters(double[] parameters, int startIndex) {
		for(int i = 0 ; i < numberOfParameters_ ; i++) {
			base_.setParameter(parameters[i+startIndex],i);
		}
	}

	public void getParameters(double[] store, int startIndex) {
		for(int i = 0 ; i < numberOfParameters_ ; i++) {
			store[i+startIndex] = base_.getParameter(i);
		}
	}

	public double getLowerLimit(int n) { return base_.getLowerLimit(n); }

	public double getUpperLimit(int n) { return base_.getUpperLimit(n); }

	public void getDefaultValues(double[] store, int startIndex) {
		for(int i = 0 ; i < numberOfParameters_ ; i++) {
			store[i+startIndex] = base_.getDefaultValue(i);
		}
	}
}