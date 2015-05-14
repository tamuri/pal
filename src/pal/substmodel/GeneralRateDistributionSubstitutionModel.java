// GeneralRateDistributionSubstitutionModel.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: GeneralRateDistributionSubstitutionModel </p>
 * <p>Description: A SubstitutionModel class for the new style of rate matrix. Can use getMatrixParameters() to match up with rate matrix parameters.</p>
 * @author Matthew Goode
 */
import pal.misc.*;
import pal.math.*;
import pal.datatype.*;
import java.io.*;

public class GeneralRateDistributionSubstitutionModel extends Parameterized.ParameterizedUser implements SubstitutionModel {
	private RateMatrixHandler handler_;
	private RateDistribution rateDistribution_;
	private DataType dataType_;

	private MultiParameterized parameterization_;
	private int numberOfCategories_;

	private static final long serialVersionUID = 34127557628342342L;

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeObject(handler_);
		out.writeObject(dataType_);
		out.writeObject(rateDistribution_);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				this.handler_ = (RateMatrixHandler)in.readObject();
				this.dataType_ = (DataType)in.readObject();
				this.rateDistribution_ = (RateDistribution)in.readObject();
				setup();
				break;
			}
		}
	}

	private GeneralRateDistributionSubstitutionModel(GeneralRateDistributionSubstitutionModel toCopy) {
		this.handler_ = toCopy.handler_.getCopy();
		this.dataType_ = toCopy.dataType_;
		this.rateDistribution_ = (RateDistribution)toCopy.rateDistribution_.clone();
	  setup();
	}

	public GeneralRateDistributionSubstitutionModel(NeoRateMatrix base, RateDistribution rateDistribution, DataType dt, double[] frequencies) {
		this.handler_ = new RateMatrixHandler(base, frequencies);
		this.dataType_ = dt;
		this.rateDistribution_ = rateDistribution;
		setup();
	}

	private final void setup() {
		this.parameterization_ = new MultiParameterized(handler_,rateDistribution_);
		this.numberOfCategories_ = rateDistribution_.getNumberOfRates();
		setParameterizedBase(this.parameterization_);
	}
	public double[] getMatrixParameters() { return parameterization_.getBaseParameters(handler_); }
	public double[] getRateDistributionParameters() { return parameterization_.getBaseParameters(rateDistribution_); }


	public DataType getDataType() {		return dataType_;		}
	public int getNumberOfTransitionCategories() {	return rateDistribution_.getNumberOfRates(); 	}
	public double getTransitionCategoryProbability(int category) {	return rateDistribution_.getCategoryProbability(category);		}
	public double[] getTransitionCategoryProbabilities() {		return rateDistribution_.getCategoryProbabilities();		}

	public double[] getEquilibriumFrequencies() {	return handler_.getEquilibriumFrequencies();			}

	public void getTransitionProbabilities(double branchLength, double[][][] store) {
		double[] rates = rateDistribution_.getRates();
		for(int i = 0 ;i < numberOfCategories_ ; i++) {
			handler_.getTransitionProbabilities(branchLength*rates[i], store[i]);
		}
	}
	public void getTransitionProbabilities(double branchLength, int category, double[][] store) {
		handler_.getTransitionProbabilities(branchLength*rateDistribution_.getRate(category), store);
	}
	public void getTransitionProbabilitiesTranspose(double branchLength, double[][][] store) {
		double[] rates = rateDistribution_.getRates();
		for(int i = 0 ;i < numberOfCategories_ ; i++) {
			handler_.getTransitionProbabilitiesTranspose(branchLength*rates[i], store[i]);
		}
	}
	public void getTransitionProbabilitiesTranspose(double branchLength, int category, double[][] store) {
		handler_.getTransitionProbabilitiesTranspose(branchLength*rateDistribution_.getRate(category), store);
	}
	public void addPalObjectListener(PalObjectListener l) {
		throw new RuntimeException("Sorry, NeoRateMatrix stuff does not work with old likelihood calculators!");
	}
	public void removePalObjectListener(PalObjectListener l) {
		throw new RuntimeException("Sorry, NeoRateMatrix stuff does not work with old likelihood calculators!");
	}
	public OrthogonalHints getOrthogonalHints() {		return null; 	 }

	// interface Report
	public void report(PrintWriter out) {		handler_.report(out);		}
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw,true);
		report(pw);
		return "General Rate Distribution Substitution Model:\n"+sw.toString();
	}
	public Object clone() {
		return new GeneralRateDistributionSubstitutionModel(this);
	}
	public SubstitutionModel getCopy() {
		return new GeneralRateDistributionSubstitutionModel(this);
	}
}