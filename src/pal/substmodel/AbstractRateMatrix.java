// RateMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.io.*;
import pal.datatype.*;
import pal.util.*;
import pal.mep.*;
import pal.math.OrthogonalHints;

import java.io.*;


/**
 * abstract base class for all rate matrices
 *
 * @version $Id: AbstractRateMatrix.java,v 1.30 2004/04/05 05:14:39 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
abstract public class AbstractRateMatrix implements RateMatrix, ExternalParameterListener
{

	//
	// Public stuff
	//

	// Constraints and conventions:
	// - first argument: row
	// - second argument: column
	// - transition: from row to column
	// - sum of every row = 0
	// - sum of frequencies = 1
	// - frequencies * rate matrix = 0 (stationarity)
	// - expected number of substitutions = 1 (Sum_i pi_i*R_ii = 0)

	/** dimension */
	private int dimension;

	/** stationary frequencies (sum = 1.0) */
	private double[] frequency;

	/**
	 * rate matrix (transition: from 1st index to 2nd index)
	 */
	private double[][] rate;

	/** data type */
	private DataType dataType;

	//
	// Protected Stuff
	//
	protected FormattedOutput format;

	//
	// Private Stuff
	//

	private transient MatrixExponential matrixExp_;

	private transient PalObjectListener listeners_ = null;

	private transient PalObjectEvent defaultPalEvent_ = null;

	/* The following is set to true in parameterChange(), and reset to false
	 * in setDistance()
	 */
	private transient boolean rebuildModel_ = false;

	private double[] parameterStore_ = null;

	//
	// Serialization code
	//
	private static final long serialVersionUID=7726654175983028192L;

	//serialver -classpath ./classes pal.substmodel.AbstractRateMatrix

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(4); //Version number
		out.writeObject(frequency);
		out.writeObject(rate);
		out.writeObject(dataType);
		out.writeObject(parameterStore_);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			case 1 : {
				frequency = (double[])in.readObject();
				dimension = frequency.length;
				rate = (double[][])in.readObject();
				dataType = (DataType)in.readObject();
				matrixExp_ = (MatrixExponential)in.readObject();
				format = FormattedOutput.getInstance();
				rebuildModel_ = true;
				this.parameterStore_ = null;
				break;
			}
			case 2 :
			case 3 : {
				frequency = (double[])in.readObject();
				dimension = frequency.length;
				rate = (double[][])in.readObject();
				dataType = (DataType)in.readObject();
				matrixExp_ = null;
				format = FormattedOutput.getInstance();
				rebuildModel_ = true;
				this.parameterStore_ = null;
				break;
			}
			default : {
				if(version!=4) {
					System.err.println("Warning: unknown matrix version:"+version);
				}
				frequency = (double[])in.readObject();
				dimension = frequency.length;
				rate = (double[][])in.readObject();
				dataType = (DataType)in.readObject();
				matrixExp_ = null;
				format = FormattedOutput.getInstance();
				rebuildModel_ = true;
				this.parameterStore_ = (double[])in.readObject();
				break;
			}
		}
	}

	// Constructor
	protected AbstractRateMatrix(int dim) {
		format = FormattedOutput.getInstance();
		dimension = dim;
		frequency = new double[dim];
		rate = new double[dim][dim];
		scheduleRebuild();
	}
	private final void scheduleRebuild() {
		rebuildModel_ = true;
	}

	/**
	 * get numerical code describing the data type
	 *
	 * @return integer code identifying a data type
	 */
	public int getTypeID()	{
		return dataType.getTypeID();
	}

	/**
	 * get numerical code describing the model type
	 *
	 * @return integer code identifying a substitution model
	 */
	abstract public int getModelID();

	public int getDimension() {   return dimension;  }

	/**
		* @return stationary frequencies (sum = 1.0)
		*/
	public double[] getEquilibriumFrequencies() {  return frequency;  }

	/**
		* @return stationary frequencie (sum = 1.0) for ith state
		*/
	public double getEquilibriumFrequency(int i) {   return frequency[i];  }

	public DataType getDataType() {   return dataType;  }
	protected final void setDataType(DataType dt) { this.dataType = dt; }

	/**
	 * @return rate matrix (transition: from 1st index to 2nd index)
	 */
	public double[][] getRelativeRates() {  return rate;  }

	/** Returns the probability of going from one state to another
	 *       	given the current distance
	 *       	@param fromState The state from which we are starting
	 *       	@param toState The resulting state
	 */
	public double getTransitionProbability(int fromState, int toState) {
		return matrixExp_.getTransitionProbability(fromState,toState);
	}
	private final void handleRebuild() {
		if(matrixExp_==null) {
			matrixExp_ = new MatrixExponential(this);
		}
		if(rebuildModel_) {
			checkParameters();
			rebuildRateMatrix(rate,parameterStore_);
			fromQToR();
		}
	}
	public final void rebuild() {}

	/** Sets the distance (such as time/branch length) used when calculating
	 *       	the probabilities.
	 */
	public final void setDistance(double distance) {
		handleRebuild();
		matrixExp_.setDistance(distance);
	}


	/** Sets the distance (such as time/branch length) used when calculating
	 *       	the probabilities.
	 *  @note The resulting transition probabilities will be in reverse
	 *  (that is in the matrix instead of [from][to] it's [to][from])
	 */
	public final void setDistanceTranspose(double distance) {
		handleRebuild();
		matrixExp_.setDistanceTranspose(distance);
	}

	/** A utility method for speed, transfers trans prob information quickly
	 *       	into store
	 */
	public final void getTransitionProbabilities(double[][] probabilityStore) {
		matrixExp_.getTransitionProbabilities(probabilityStore);
	}
	private final static void cleanup(double[][] tableStore, int numberOfStates) {
		for(int i = 0 ; i < numberOfStates ; i++) {
			for(int j = 0 ; j < numberOfStates ; j++) {
				if(Double.isNaN(tableStore[i][j])) {
					tableStore[i][j] = 0;
				}
			}
		}
	}

	public void scale(double scale) {
		normalize(scale);
		updateMatrixExp();
		fireParametersChangedEvent();
	}


	// interface Report (remains abstract)

	// interface Parameterized (remains abstract)


	//
	// Protected stuff (for use in derived classes)
	//

	protected void printFrequencies(PrintWriter out)
	{
		for (int i = 0; i < dimension; i++)
		{
			out.print("pi(" + dataType.getChar(i) + ") = ");
			format.displayDecimal(out, frequency[i], 5);
			out.println();
		}
		out.println();
	}

	protected void setFrequencies(double[] f) {
		for (int i = 0; i < dimension; i++) {
			frequency[i] = f[i];
		}
		checkFrequencies();
		scheduleRebuild();
	}
	public double setParametersNoScale(double[] parameters) {
		rebuildRateMatrix(rate,parameters);
		double result = incompleteFromQToR();
		rebuildModel_ = false;
		return result;
	}

	public void setParameters(double[] parameters) {
		checkParameters();
		System.arraycopy(parameters,0,parameterStore_,0,getNumParameters());
		scheduleRebuild();
	}

	/** Computes normalized rate matrix from Q matrix (general reversible model)
	 * - Q_ii = 0
	 * - Q_ij = Q_ji
	 * - Q_ij is stored in R_ij (rate)
	 * - only upper triangular is used
	 * Also updates related MatrixExponential
	 */
	private void fromQToR() {
		double q;
		for (int i = 0; i < dimension; i++)  {
			for (int j = i + 1; j < dimension; j++) {
				q = rate[i][j];
				rate[i][j] = q*frequency[j];
				rate[j][i] = q*frequency[i];
			}
		}
		makeValid();
		normalize();
		updateMatrixExp();
		fireParametersChangedEvent();
	}

	private double incompleteFromQToR() {
		double q;
		for (int i = 0; i < dimension; i++) {
			for (int j = i + 1; j < dimension; j++) {
				q = rate[i][j];
				rate[i][j] = q*frequency[j];
				rate[j][i] = q*frequency[i];
			}
		}
		return makeValid();
	}
	private void finishFromQToR(double substitutionScale)  {
		normalize(substitutionScale);
		updateMatrixExp();
		fireParametersChangedEvent();
	}
	private final void checkParameters() {
		if(parameterStore_==null) {
			parameterStore_ = new double[getNumParameters()];
			for(int i = 0; i < parameterStore_.length ; i++) {
				parameterStore_[i] = getDefaultValue(i);
			}
		}
	}
	public final void setParameter(double value, int parameter) {
		checkParameters();
		this.parameterStore_[parameter] = value;
		scheduleRebuild();
	}
	public final double getParameter(int parameter) {
		checkParameters();
		return parameterStore_[parameter];
	}

	abstract protected void rebuildRateMatrix(double[][] rate, double[] parameters);



	public void addPalObjectListener(PalObjectListener pol) {
		listeners_ = PalEventMulticaster.add(listeners_,pol);
	}
	public void removePalObjectListener(PalObjectListener pol) {

		listeners_ = PalEventMulticaster.remove(listeners_,pol);
	}

	protected void fireParametersChangedEvent() {
		if(listeners_!=null) {
			if(defaultPalEvent_==null) {
				defaultPalEvent_ = new PalObjectEvent(this);
			}
			listeners_.parametersChanged(defaultPalEvent_);
		}
	}

	protected void fireParametersChangedEvent(PalObjectEvent pe) {
		if(listeners_!=null) {
			listeners_.parametersChanged(pe);
		}
	}
	protected void updateMatrixExp() {
		if(matrixExp_==null) {
			matrixExp_ = new MatrixExponential(this);
		} else {
			matrixExp_.setMatrix(this);
		}
	}
	//
	// Private stuff
	//

	/** Make it a valid rate matrix (make sum of rows = 0)
		* @return current rate scale
		*/
	private double makeValid() {
		double total = 0;
		for (int i = 0; i < dimension; i++){
			double sum = 0.0;
			for (int j = 0; j < dimension; j++)
			{
				if (i != j)
				{
					sum += rate[i][j];
				}
			}
			rate[i][i] = -sum;
			total+=frequency[i]*sum;
		 }
		 return total;
	}

	// Normalize rate matrix to one expected substitution per unit time
	private void normalize()
	{
		double subst = 0.0;

		for (int i = 0; i < dimension; i++)
		{
			subst += -rate[i][i]*frequency[i];
		}
		for (int i = 0; i < dimension; i++)
		{
			for (int j = 0; j < dimension; j++)
			{
				rate[i][j] = rate[i][j]/subst;
			}
		}
	}
	 // Normalize rate matrix by a certain scale to acheive an overall scale (used with a complex site class model)
	private void normalize(double substitutionScale)  {
		for (int i = 0; i < dimension; i++)  {
			for (int j = 0; j < dimension; j++)  {
				rate[i][j] = rate[i][j]/substitutionScale;
			}
		}
	}

	/**
	 * ensures that frequencies are not smaller than MINFREQ and
	 * that two frequencies differ by at least 2*MINFDIFF.
	 * This avoids potentiak problems later when eigenvalues
	 * are computed.
	 */
	private void checkFrequencies()
	{
		// required frequency difference
		double MINFDIFF = 1e-10;

		// lower limit on frequency
		double MINFREQ = 1e-10;

		int maxi = 0;
		double sum = 0.0;
		double maxfreq = 0.0;
		for (int i = 0; i < dimension; i++)
		{
			double freq = frequency[i];
			if (freq < MINFREQ) frequency[i] = MINFREQ;
			if (freq > maxfreq)
			{
				maxfreq = freq;
				maxi = i;
			}
			sum += frequency[i];
		}
		frequency[maxi] += 1.0 - sum;

		for (int i = 0; i < dimension - 1; i++)
		{
			for (int j = i+1; j < dimension; j++)
			{
				if (frequency[i] == frequency[j])
				{
					frequency[i] += MINFDIFF;
					frequency[j] -= MINFDIFF;
				}
			}
		}
	}

	/**
	 * For the external parameter interface. This can be ignored unless you want to
	 * use this Rate Matrix as an external parameter listener
	 * This method does two things.First it sets a flag indicating that rebuild method should be called the next time a setDistance method is called, and then it notifies any PalObject listeners that a parameter has changed
	 */
	public void parameterChanged(ParameterEvent pe) {
		scheduleRebuild();
		fireParametersChangedEvent();
	}


	public Object clone() {
		try {
			RateMatrix matrix = (RateMatrix)super.clone();
			if(matrix instanceof AbstractRateMatrix) {
				((AbstractRateMatrix)matrix).listeners_ = null;
			}
			return matrix;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen
			throw new InternalError();
		}
	}

	/**
	 * @return null
	 */
	public OrthogonalHints getOrthogonalHints() { return null; }

// ============================================================================
// ==== Protected Stuff ==========
	protected final double[] getFrequencies() {  return frequency; }
}
