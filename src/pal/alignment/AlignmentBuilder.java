// AlignmentBuilder.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.datatype.*;
import pal.misc.*;
/**
 * A class for the gradual construction of an alignment. To supersede simular classes hidden away in PAL
 *
 * <b>History</b>
 * <ul>
 *  <li> 25/11/2003 - Created </li>
 * </ul>
 *
 * @version $Id: AlignmentBuilder.java,v 1.1 2003/11/25 01:06:21 matt Exp $
 *
 * @author Matthew Goode
 *
 */

public class AlignmentBuilder {
	private Sequence[] store_;
	private int numberOfSequencesInStore_;
	/**
	 * The constructor
	 * @param initialCapacity The initial amount of space to allocate for sequence storage (dynamically adjusts if number of sequences exceeds capacity)
	 */
	public AlignmentBuilder(int initialCapacity) {
	  this.store_ = new Sequence[initialCapacity];
		numberOfSequencesInStore_ = 0;
	}
	/**
	 * Remove all currently stored sequences.
	 */
	public void clearAll() {
		for(int i = 0 ; i < numberOfSequencesInStore_ ; i++) {
		  store_[i] = null;
		}
	  numberOfSequencesInStore_ = 0;
	}
	private final Identifier[] generateIdentifiers() {
	  Identifier[] ids = new Identifier[numberOfSequencesInStore_];
		for(int i = 0 ; i < numberOfSequencesInStore_ ; i++) {
		  ids[i] = store_[i].generateIdentifier();
		}
		return ids;
	}
	private final int[][] generateStateData() {
	  int[][] data = new int[numberOfSequencesInStore_][];
		for(int i = 0 ; i < numberOfSequencesInStore_ ; i++) {
		  data[i] = store_[i].getStates();
		}
		return data;
	}
	/**
	 * Build an alignment based on sequences stored.
	 * @param dt The datatype of the sequence data
	 * @return The generated alignment
	 */
	public Alignment generateAlignment(DataType dt) {
		Identifier[] ids = generateIdentifiers();
		int[][] stateData = generateStateData();
		return new SimpleAlignment(new SimpleIdGroup(ids),dt,stateData);
	}
	private final void ensureSpace(int requiredSize) {
	  if(requiredSize>=store_.length) {
		  Sequence[] newStore = new Sequence[requiredSize+5];
			System.arraycopy(store_,0,newStore,0,numberOfSequencesInStore_);
			this.store_ = newStore;
		}
	}
	/**
	 * Add sequence data to store
	 * @param states The states of the sequence (builder assumes all state arrays are equal length)
	 * @param name The name of the sequence
	 */
	public void addSequence(int[] states, String name) {
		ensureSpace(numberOfSequencesInStore_+1);
		store_[numberOfSequencesInStore_++] = new Sequence(states,name);
	}
	// -=-==-=--=-==-
	private final static class Sequence {
	  private final int[] states_;
		private final String name_;
		public Sequence(int[] states, String name) {
			this.states_ = states;
			this.name_ = name;
	  }
		public Identifier generateIdentifier() {
		  return new Identifier(name_);
		}
		public int[] getStates() {
			return states_;
		}
	}

}