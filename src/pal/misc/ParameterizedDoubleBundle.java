// ParameterizedDoubleBundle.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * <p>Title: ParameterizedDoubleBundle </p>
 * <p>Description: A parameterized double bundle is simple a parameterized collection of mutable double objects. The idea is, all the changable attributes of the phylogenetic entities that are being accessed (eg the branch lengths of a tree, and the parameters of a model) can be grouped together externally (the point of this approach will become more apparent over time as PAL evolves) </p>
 * @author Matthew Goode
 * @version 1.0
 */

public class ParameterizedDoubleBundle implements NeoParameterized {
  private final MutableDouble[] parameters_;
	public ParameterizedDoubleBundle(MutableDouble[] parameters) {
		this.parameters_ = parameters;
  }
	public int getNumberOfParameters() {
		return parameters_.length;
	}

	public void setParameters(double[] parameters, int startIndex) {
	  for(int i = 0 ; i < parameters_.length ; i++) {
		  parameters_[i].setValue(parameters[startIndex+i]);
		}
	}

	public void getParameters(double[] parameterStore, int startIndex) {
	  for(int i = 0 ; i < parameters_.length ; i++) {
		  parameterStore[i+startIndex] = parameters_[i].getValue();
		}
	}
	public double getLowerLimit(int n) { return parameters_[n].getLowerLimit(); }
	public double getUpperLimit(int n) { return parameters_[n].getUpperLimit(); }

	public void getDefaultValues(double[] store, int startIndex) {
	  for(int i = 0 ; i < parameters_.length ; i++) {
		  store[i+startIndex] = parameters_[i].getDefaultValue();
		}
	}
	public String toString() {
	  StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < parameters_.length ; i++) {
		  sb.append(parameters_[i].getName()+" = "+parameters_[i].getValue());
			if(i!=parameters_.length-1) {
			  sb.append(", ");
			}
		}
		return sb.toString();
	}
}