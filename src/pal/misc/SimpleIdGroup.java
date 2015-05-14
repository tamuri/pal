// SimpleIdGroup.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

import java.io.*;
import java.util.*;

/**
 * Default implementation of IdGroup interface.
 * Memory-inefficient to allow fast whichIdNumber calls.
 *
 * @version $Id: SimpleIdGroup.java,v 1.8 2001/11/26 03:59:24 matt Exp $
 *
 * @author Alexei Drummond
 */

public class SimpleIdGroup implements IdGroup, Serializable, Nameable {

	private String name;
	private Identifier[] ids;
	private Hashtable indices;

	//
	// Serialization code
	//
	private static final long serialVersionUID= -4266575329980153075L;

	//serialver -classpath ./classes pal.misc.SimpleIdGroup
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeObject(name);
		out.writeObject(ids);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				name = (String)in.readObject();
				ids = (Identifier[])in.readObject();
				indices = new Hashtable(ids.length);
				for(int i = 0 ; i < ids.length ; i++) {
					indices.put(ids[i].getName(), new Integer(i));
				}
				break;
			}
		}
	}

	/**
	 * Constructor taking the size of the group.
	 */
	public SimpleIdGroup(int size) {
		this(size,false);

	}

	/**
	 * Constructor taking an array of strings.
	 */
	public SimpleIdGroup(String[] labels) {
		this(labels.length);
		for (int i = 0; i < labels.length; i++) {
			setIdentifier(i, new Identifier(labels[i]));
		}
	}

	/**
	 * Constructor taking the size of the group.
	 * @param size - the number of ids
	 * @param createIDs - if true creates default Identifiers.
	 * Otherwise leaves blank (for user to fill in)
	 */
	public SimpleIdGroup(int size, boolean createIDs) {

		ids = new Identifier[size];
		indices = new Hashtable(size);
		if(createIDs) {
			for(int i = 0 ; i < size ; i++ ) {
				setIdentifier(i, new Identifier(""+i));
			}
		}
	}

	/**
	 * Constructor taking an array of identifiers.
	 */
	public SimpleIdGroup(Identifier[] id) {
		this(id.length);
		for (int i = 0; i < id.length; i++) {
			setIdentifier(i, id[i]);
		}
	}

	/**
	 * Constructor taking two separate id groups and merging them.
	 */
	public SimpleIdGroup(IdGroup a, IdGroup b) {
		this(a.getIdCount() + b.getIdCount());

		for (int i = 0; i < a.getIdCount(); i++) {
			setIdentifier(i, a.getIdentifier(i));
		}
		for (int i = 0; i < b.getIdCount(); i++) {
			setIdentifier(i + a.getIdCount(), b.getIdentifier(i));
		}
	}

	/**
	 * Impersonating Constructor.
	 */
	public SimpleIdGroup(IdGroup a) {
		this(a.getIdCount());

		for (int i = 0; i < a.getIdCount(); i++) {
			setIdentifier(i, a.getIdentifier(i));
		}
	}
	/**
	 * Impersonating Constructor.
	 * @param toIgnore - will ignore the identifier at the index specified by toIgnore
	 */
	public SimpleIdGroup(IdGroup a, int toIgnore) {
		this((toIgnore < 0 ||toIgnore > a.getIdCount() ? a.getIdCount() : a.getIdCount()-1));
		int index = 0;
		for (int i = 0; i < a.getIdCount(); i++) {
			if(i!=toIgnore) {
				setIdentifier(index++, a.getIdentifier(i));
			}
		}
	}

	/**
	 * Returns the number of identifiers in this group
	 */
	public int getIdCount() {
		return ids.length;
	}

	/**
	 * Returns the ith identifier.
	 */
	public Identifier getIdentifier(int i) {
		return ids[i];
	}

	/**
	 * Convenience method to return the name of identifier i
	 */
	public final String getName(int i) {
		return ids[i].getName();
	}

	/**
	 * Sets the ith identifier.
	 */
	public void setIdentifier(int i, Identifier id) {
		ids[i] = id;
		indices.put(id.getName(), new Integer(i));
	}

	/**
	 * Return index of identifier with name or -1 if not found
	 */
	public int whichIdNumber(String name) {

		Integer index = (Integer)indices.get(name);
		if (index != null) {
			return index.intValue();
		}
		return -1;
	}

	/**
	 * Returns a string representation of this IdGroup in the form of
	 * a bracketed list.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		for (int i = 0; i < getIdCount(); i++) {
			sb.append(getIdentifier(i) + " ");
		}
		sb.append("]");
		return new String(sb);
	}

	// implement Nameable interface

	/**
	 * Return the name of this IdGroup.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this IdGroup.
	 */
	public void setName(String n) {
		name = n;
	}
}

