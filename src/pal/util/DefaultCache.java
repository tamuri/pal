// DefaultCache.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.util;


import pal.math.*;
import java.io.*;
import java.util.*;

/**
 * @author Alexei Drummond
 * @version $Id: DefaultCache.java,v 1.3 2001/10/10 04:02:45 matt Exp $
 * This class is not threadsafe!
 */
public class DefaultCache implements DoubleKeyCache {

	ArrayList list;
	int maxSize;
	DoubleKey nearest, nearBelow, nearAbove;
	double dist, distToBelow, distToAbove;
	int index;
	static MersenneTwisterFast random = new MersenneTwisterFast();
	//private final static Comparator KEY_COMPARE = new DoubleKeyComparator();

	private DefaultCache(DefaultCache toCopy) {
		this.maxSize = toCopy.maxSize;
		this.list = new ArrayList(maxSize);
	}
	public DefaultCache() {
		this(1000);
	}

	public DefaultCache(int maxSize) {
		this.maxSize = maxSize;
		list = new ArrayList(maxSize);
	}
	public Object getNearest(double key, double tolerance) {
		SimpleDoubleKey sdk = ((SimpleDoubleKey)getNearest(new SimpleDoubleKey(key),tolerance));
		if(sdk==null) {
			return null;
		}
		return sdk.getValue();

	}
	/**
	 * @return the object with the key nearest to given value.
	 * if no objects within the given tolerance exist then
	 * null is returned.
	 */
	public DoubleKey getNearest(DoubleKey d, double tolerance) {

		if (list.isEmpty()) return null;

		index = binarySearch(list, d);

		if (index >= 0) return (DoubleKey)list.get(index);

		// convert failed index to nearest insertion point;
		index = -index-1;

		if (index == 0) {
			nearest = (DoubleKey)list.get(0);
		} else if (index == list.size()) {
			nearest = (DoubleKey)list.get(list.size()-1);
		} else {
			nearBelow = (DoubleKey)list.get(index - 1);
			nearAbove = (DoubleKey)list.get(index);
			distToBelow = Math.abs(nearBelow.getKey() - d.getKey());
			distToAbove = Math.abs(nearAbove.getKey() - d.getKey());
			if (distToBelow < distToAbove) {
				if (distToBelow < tolerance) {
					return nearBelow;
				} else return null;
			} else {
				if (distToAbove < tolerance) {
					return nearAbove;
				} else return null;
			}
		}
		dist = Math.abs(nearest.getKey() - d.getKey());
		if (dist < tolerance) {
			return nearest;
		} else return null;
	}
	public void addDoubleKey(double relatedKey, Object o) {
		addDoubleKey(new SimpleDoubleKey(relatedKey,o));
	}
	public void addDoubleKey(DoubleKey d) {

		if (list.isEmpty()) {
			list.add(d);
		} else {

			index = binarySearch(list, d);

			// already exists
			if (index >= 0) return;

			// convert failed index to nearest insertion point;
			index = -index-1;
			list.add(index, d);

			if (list.size() > maxSize) {
				// remove the biggest one
				list.remove(list.size()-1);
				// and remove a random one
				list.remove(random.nextInt(list.size()));
			}
		}
	}

	public void setMaxCacheSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMaxCacheSize() {
		return maxSize;
	}

	public void clearCache() {
		list.clear();
	}
	/*static class DoubleKeyComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			double k1 = ((DoubleKey)o1).getKey();
			double k2 = ((DoubleKey)o2).getKey();
			if(k1<k2) {
				return -1;
			}
			if(k1>k2) {
				return 1;
			}
			return 0;
		}

		public boolean equals(Object o1, Object o2) {
			return( ((DoubleKey)o1).getKey()==((DoubleKey)o2).getKey());
		}

	}
*/
	static class SimpleDoubleKey implements DoubleKey {
		double key_;
		Object value_;
		public SimpleDoubleKey(double key, Object value) {
			this.value_ = value;
			this.key_ = key;
		}
		public SimpleDoubleKey(double key) {
			this(key,null);
		}
		public double getKey() {
			return key_;
		}
		public Object getValue() {
			return value_;
		}
		public int compareTo(Object other) {
			if(other instanceof SimpleDoubleKey) {
				SimpleDoubleKey ok = (SimpleDoubleKey)other;
				if(ok.key_ < key_) {
					return -1;
				} else if (ok.key_ > key_) {
					return 1;
				}
				return 0;
			}
			throw new RuntimeException("Assertion error ! Object other should be a SimpleDoubleKey");
		}
	}
	public Object clone() {
		return new DefaultCache(this);
	}

	/** this method reserves the right to be non-threadsafe! */
	private static int binarySearch(List list, Object key) {
		int low = 0;
		int high = list.size()-1;

		while (low <= high) {
			int mid = (low + high) >> 1;
			Object midVal = list.get(mid);
			int cmp = ((java.lang.Comparable)midVal).compareTo(key);

			if (cmp < 0)
			low = mid + 1;
			else if (cmp > 0)
			high = mid - 1;
			else
			return mid; // key found
		}
		return -(low + 1);  // key not found
	}
}
