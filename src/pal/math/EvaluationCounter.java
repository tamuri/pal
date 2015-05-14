// OrthogonalSearch.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.math;

/**
 * A utiltity class that can be used to track the number of evaluations of a
 * general function
 *
 * @author Matthew Goode
 */

public class EvaluationCounter implements MultivariateFunction {
	private final MultivariateFunction base_;
	private int evaluationCount_ = 0;
	public EvaluationCounter(MultivariateFunction base) {
		this.base_ = base;
	}
	public final double evaluate(double[] argument) {
		evaluationCount_++;
		return base_.evaluate(argument);
	}
	public final void reset() { evaluationCount_=0; }
	public final int getEvaluationCount() {  return evaluationCount_; }
	public final int getNumArguments() { return base_.getNumArguments(); }
	public final double getLowerBound(int n) { return base_.getLowerBound(n); }
	public final double getUpperBound(int n) { return base_.getUpperBound(n); }
	public final OrthogonalHints getOrthogonalHints() { return base_.getOrthogonalHints();	}
}