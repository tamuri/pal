// Identifier.java
//
// (c) 1999-2000 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

import java.io.*;
import java.util.*;
import pal.util.Comparable;

/**
 * An identifier for some sampled data. This will most often be
 * for example, the accession number of a DNA sequence, or the
 * taxonomic name that the sequence represents, et cetera.
 *
 * @version $Id: Identifier.java,v 1.9 2002/11/25 05:40:54 matt Exp $
 *
 * @author Alexei Drummond
 */


public class Identifier implements Serializable,
					 pal.util.Comparable, Nameable {

	private String name = null;

	private static final long serialVersionUID=-7873729831795750538L;

	/** Versioning control... only works with 1.1+ (1.0 should be fine though... this is just pointless that's all) */
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeObject(name);
	}

	/** Versioning control... only works with 1.1+ (1.0 should be fine though... this is just pointless that's all) */
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				name = (String)in.readObject();
				break;
			}
		}
	}
	public static Identifier ANONYMOUS = new Identifier("");

		public Identifier() {}

		public Identifier(String name) {
	setName(name);
		}

		public String toString() {
	return getName();
		}

		// implements Comparable interface

		public int compareTo(Object c) {

	return getName().compareTo(((Identifier)c).getName());
		}

		public boolean equals(Object c) {

	if (c instanceof Identifier) {
			return getName().equals(((Identifier)c).getName());
	} else return false;
		}

		// implements Nameable interface

		public String getName() {
	return name;
		}

		public void setName(String s) {
	name = s;
		}
	/**
	 * Translates an array of identifiers into an array of strings
	 */
	public final static String[] getNames(Identifier[] ids) {
		String[] names = new String[ids.length];
		for(int i = 0 ; i < names.length ; i++) {
			names[i] = ids[i].getName();
		}
		return names;
	}
	/**
	 * Translates an array of identifiers into an array of strings, with optional removal of particular identifier
	 * @param toIgnoreIndex the index of an idetifier to ignore, if <0 no element is ignored
	 */
	public final static String[] getNames(Identifier[] ids, int toIgnore) {
		if(toIgnore<0||toIgnore>=ids.length) {
			return getNames(ids);
		}
		String[] names = new String[ids.length-1];
		int index = 0;
		for(int i = 0 ; i < names.length ; i++) {
			if(i!=toIgnore) {
				names[index] = ids[i].getName();
				index++;
			}
		}
		return names;
	}
	/**
	 * Translates an an array of strings into an array of identifiers
	 */
	public final static Identifier[] getIdentifiers(String[] names) {
		Identifier[] ids = new Identifier[names.length];
		for(int i = 0 ; i < names.length ; i++) {
			ids[i] = new Identifier(names[i]);
		}
		return ids;
	}
	/**
	 * Translates an IdGroup into an array of identifiers
	 */
	public final static Identifier[] getIdentifiers(IdGroup idGroup) {
		Identifier[] ids = new Identifier[idGroup.getIdCount()];
		for(int i = 0 ; i < ids.length ; i++) {
			ids[i] = idGroup.getIdentifier(i);
		}
		return ids;
	}
	/**
	 * Translates an IdGroup into an array of strings
	 */
	public final static String[] getNames(IdGroup ids) {
		String[] names = new String[ids.getIdCount()];
		for(int i = 0 ; i < names.length ; i++) {
			names[i] = ids.getIdentifier(i).getName();
		}
		return names;
	}
	/**
	 * Translates an IDgroup into an array of strings, with optional removal of particular identifier
	 * @param toIgnoreIndex the index of an idetifier to ignore, if <0 no element is ignored
	 */
	public final static String[] getNames(IdGroup ids, int toIgnore) {
		if(toIgnore<0||toIgnore>=ids.getIdCount()) {
			return getNames(ids);
		}
		int numberOfIDS = ids.getIdCount();
		String[] names = new String[numberOfIDS-1];
		int index = 0;
		for(int i = 0 ; i < numberOfIDS ; i++) {
			if(i!=toIgnore) {
				names[index] = ids.getIdentifier(i).getName();
				index++;
			}
		}
		return names;
	}
	/**
	 * Translates an IDgroup into an array of strings, with optional removal of particular identifier
	 * @param toIgnoreIndex the indexes of an idetifier to ignore, does not need to be sorted	 */
	public final static String[] getNames(IdGroup ids, int[] toIgnore) {
		if(toIgnore==null) {
			return getNames(ids);
		}

		int numberOfIDS = ids.getIdCount();
		Vector names = new Vector(numberOfIDS);

		for(int i = 0 ; i < numberOfIDS ; i++) {
			boolean ignore = false;
			for(int j = 0 ; j < toIgnore.length ; j++) {
				if(toIgnore[j]==i) {
					ignore = true;
					break;
				}
			}
			if(!ignore) {
				names.addElement(ids.getIdentifier(i).getName());
			}
		}
		String[] namesFinal = new String[names.size()];
		names.copyInto(namesFinal);
		return namesFinal;
	}
}

