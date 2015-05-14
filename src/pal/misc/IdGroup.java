// IdGroup.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * An indexed group of identifiers. For example of group of taxa
 * related by a phylogenetic tree.
 * <BR><B>NOTE:</B> Was called Taxa but not general enough.
 *
 * @version $Id: IdGroup.java,v 1.10 2002/10/27 05:46:28 matt Exp $
 *
 * @author Alexei Drummond
 */
public interface IdGroup extends java.io.Serializable {

	/**
	 * Returns the number of identifiers in this group
	 */
	int getIdCount();

	/**
	 * Returns the ith identifier.
	 */
	Identifier getIdentifier(int i);

	/**
	 * Sets the ith identifier.
	 */
	void setIdentifier(int i, Identifier id);

	/**
	 * returns the index of the identifier with the given name.
	 */
	int whichIdNumber(String name);

// ============================================================================
// =================== Utility Class for IdGroup stuff ========================
// ============================================================================
	public static final class Utils{
		/**
		 * @return true if <i>sub</i> IdGroup completely contained within <i>full</i>, false otherwise
		 */
		public static final boolean isContainedWithin(IdGroup sub, IdGroup full) {
			for(int i = 0 ; i < sub.getIdCount() ; i++) {
				boolean found = false;
				Identifier subID = sub.getIdentifier(i);
				for(int j = 0 ; j < full.getIdCount() ;j++) {
					Identifier fullID = full.getIdentifier(j);
					if(fullID.equals(subID)) {
						found= true;
						break;
					}
				}
				if(!found) {
					return false;
				}
			}
			return true;
		}
		/**
		 * @return true if <i>id1</i> and <i>id2</i> share exactly the same identifiers (.equals() based, not reference base). The order is not important.
		 */
		public static final boolean isEqualIgnoringOrder(IdGroup id1, IdGroup id2) {
			return(isContainedWithin(id1,id2)&&isContainedWithin(id2,id1));
		}

		/**
		 * A convenience implementation of whichIdNumber that can be used by
		 * IdGroup implementations
		 * @return -1 if <i>s</i> not in <i>group</i>
		 */
		public static final int whichIdNumber(IdGroup group, String s) {
			for(int i = 0 ; i < group.getIdCount() ; i++ ) {
				if(s.equals(group.getIdentifier(i).getName())) {
					return i;
				}
			}
			return -1;
		}

	}
}
