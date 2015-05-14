// Ranker.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.algorithmics;

/**
 * Title:        Ranker
 * Description:  Maintains a list of ranked objects
 * @author Matthew Goode
 * @version 1.0
 */

public class Ranker {
	private final RankedObject[] store_;
	private int numberInStore_;
	private double worstScore_;
	public Ranker(int maximumSize) {
		this.store_ = new RankedObject[maximumSize];
		numberInStore_ = 0;
	}
	public final Object getBestObject() {
		if(numberInStore_>0){
			return store_[0].getObject();
		}
		return null;
	}
	/**
	 * Obtain the best score which may be the highest score (if maximising), or the lowest score (if minimising)
	 * @return the best score
	 */
	public final double getBestScore() { return numberInStore_==0? 0 : store_[0].getScore(); }
	/**
	 * Enquire to the merits of adding an object with a particular score
	 * @param score The score in question
	 * @return true if an object with such a score is going to make a difference to the current state of this ranker
	 */
	public final boolean isWorthAdding(final double score, boolean maximising) {
		return (numberInStore_!=store_.length) || (numberInStore_==0) ||
					 (maximising ? score > worstScore_: score < worstScore_ );
	}
	/**
	 * Obtain the objects in this ranker
	 * @return the objects in the order of bestness (such that the first is the best)
	 */
	public final Object[] getObjects() {
		Object[] result = new Object[numberInStore_];
		for(int i = 0 ; i < numberInStore_ ; i++) {
			result[i] = store_[i].getObject();
		}
		return result;
	}
	/**
	 * Add in (if it's good enough) a new object based on a score
	 * If an object has equality with an object already in the store that object is replaced by the new version
	 * @param object The object to add in
	 * @param score The score of the object
	 */
	public void add(Object object, double score, boolean maximising) {
		int insertionPoint = numberInStore_;
		//Need to fix so that first sweep checks if object is already in store (by object equality),
		// and if so just replace and reshuffle according to score
		//Else do as done here and just insert...
		if(maximising) {
			for(int i = 0 ; i < numberInStore_ ; i++) {
				if(store_[i].getObject().equals(object)) {
					store_[i].update(object,score);
					return;
				}
				if(store_[i].hasLowerScore(score)) {
					insertionPoint = i; break;
				}
			}
		} else {
			for(int i = 0 ; i < numberInStore_ ; i++) {
				if(store_[i].getObject().equals(object)) {
					store_[i].update(object,score);
					return;
				}
				if(store_[i].hasHigherScore(score)) {
					insertionPoint = i; break;
				}
			}
		}
		insert(insertionPoint, new RankedObject(object,score));
	}

	private void insert(int insertionPoint, RankedObject ro) {
		if(insertionPoint<store_.length) {
			if(!((insertionPoint==numberInStore_)||(insertionPoint>=store_.length-1))) {
				if(store_.length==numberInStore_) {
					System.arraycopy(store_,insertionPoint,store_,insertionPoint+1,numberInStore_-insertionPoint-1);
				} else {
					System.arraycopy(store_,insertionPoint,store_,insertionPoint+1,numberInStore_-insertionPoint);
				}
			}
			store_[insertionPoint] = ro;
			if(numberInStore_!=store_.length) {
				numberInStore_++;
			}
			worstScore_ = store_[numberInStore_-1].getScore();
		}
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('(');
		sb.append(numberInStore_);
		sb.append(") ");

		for(int i = 0 ; i < numberInStore_ ; i++) {
			sb.append(store_[i]);
			if(i!=numberInStore_-1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
// -=-==--=-=-=-=-=-=-=-==--==--=-=-=-==-=-=-=-
	/**
	 * A coupling of object and score
	 */
	private static final class RankedObject {
		private Object object_;
		private double score_;
		public RankedObject(Object object, double score) {
		  update(object,score);
		}
		public final boolean hasLowerScore(double otherScore) {
			return score_<otherScore;
		}
		public final boolean hasHigherScore(double otherScore) {
			return score_>otherScore;
		}
		public final void update(Object object, double score){
			this.object_ =    object;
			this.score_ = score;
		}
		public Object getObject() { return object_; }
		public double getScore() { return score_; }
		public String toString() { return "["+object_+", "+score_+"]"; }
	}
}