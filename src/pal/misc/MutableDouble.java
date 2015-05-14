// MutableDouble.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

import java.io.*;
import java.util.Enumeration;



/**
 * interface for holding a double, that may be mutated and referenced by more than one user
 *
 * @version $Id: MutableDouble.java,v 1.2 2004/10/19 02:23:19 matt Exp $
 *
 * @author Matthew Goode
 */

import java.util.*;

public class MutableDouble implements java.io.Serializable{
	private final double defaultValue_;
	private final double minimumValue_;
	private final double maximumValue_;
	private double se_;
	private double currentValue_;

	private final String name_;

	/** The default value is also the initial value.
	*/
	public MutableDouble(double initialValue, double defaultValue, double minimumValue, double maximumValue, String name) {
		this.currentValue_ = defaultValue;
		this.defaultValue_ = defaultValue;
		this.minimumValue_ = minimumValue;
		this.maximumValue_ = maximumValue;
		this.name_ = name;
	}

	/** Set the current value of this double */
	public final void setValue(double value) { this.currentValue_ = value;	}

	/** Get the current value of this double */
	public final double getValue() {	return currentValue_;	}

	public final double getLowerLimit() {	return minimumValue_;	}

	public final double getUpperLimit() {	return maximumValue_;	}

	public final double getDefaultValue() {	return defaultValue_;	}

	public final double getSE() {	return se_;	}
	public final void setSE(double value) {	se_ = value;	}
	public final String getName() { return name_; }
	public String toString() {
		return name_+":"+currentValue_;
	}
}
