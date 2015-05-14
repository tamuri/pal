// Attribute.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

import java.io.*;
import pal.util.Comparable;

/**
 * An immutable attribute has a name and value.
 * A convenience constructor for conversion from 
 * string to types Boolean, Integer, Double, Float is available.
 *
 * @version $Id: Attribute.java,v 1.1 2001/11/21 22:17:07 alexi Exp $
 *
 * @author Alexei Drummond
 */


public class Attribute { 
	
	public static final String STRING = "string";
	public static final String INTEGER = "integer";
	public static final String BOOLEAN = "boolean";
	public static final String DOUBLE = "double";
	public static final String FLOAT = "float";
	
	/** the name of this attribute */
	private String name = null;

	/** the value of this attribute */
	private Object value = null;
	
	/**
	 * @param name the name of the attribute.
	 * @param val the value as a string
	 * @param type a string description of the type the value is. One of 'boolean', 
	 * 'integer', 'double', 'float', 'string'
	 */
	public Attribute(String name, String val, String type) {
		
		this.name = name;
		
		if (type == null) {
			try {
				value = new Integer(val);
			} catch (NumberFormatException nfe1) {
				try {
					value = new Double(val);
				} catch (NumberFormatException nfe2) {
					value = val;
				}
			} 
		} else if (type.equals(BOOLEAN)) {
			value = new Boolean(val);
		} else if (type.equals(INTEGER)) {
			value = new Integer(val);
		} else if (type.equals(DOUBLE)) {
			value = new Double(val);
		} else if (type.equals(FLOAT)) {
			value = new Float(val);
		} 
		value = val;
	}
	
	public Attribute(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
}

