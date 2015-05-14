// BranchAccess.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: BranchAccess </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.alignment.*;
import pal.substmodel.*;
import pal.tree.*;

public interface BranchAccess {
	/**
	 * Set the annotation for this branch (will be used when instructing TreeInterfaces
	 * @param annotation The annotation object (dependent on the TreeInterface instructed)
	 */
	public void setAnnotation(Object annotation);

	public Object getAnnotation();

	/**
	 * Test if this branch leads directly to a leaf of a particular label
	 * @param leafLabel the label of the leaf
	 * @return true if this branch is a leaf branch and the leaf has the right label
	 */
	public boolean isLeafBranch(String leafLabel);

	/**
	 * Create a new Tree Searcher with a new sub tree attached
	 * @param subTree the sub tree to attach at this branch
	 * @param fullAlignment the full alignment including the sequences already part of the base tree
	 * @return a new unrooted searcher
	 */
	public UnrootedMLSearcher attach(Node subTree, Alignment fullAlignment);
	/**
	 * Create a new Tree Searcher with a new sub tree attached
	 * @param newSequence the new leaf to attach at this branch
	 * @param fullAlignment the full alignment including the sequences already part of the base tree
	 * @return a new unrooted searcher
	 */
	public UnrootedMLSearcher attach(String newSequence, Alignment fullAlignment);
	 /**
	 * Create a new Tree Searcher with a new sub tree attached
	 * @param subTree the sub tree to attach at this branch
	 * @param fullAlignment the full alignment including the sequences already part of the base tree
	 * @param model the new substitution model to use
	 * @return a new unrooted searcher
	 */
	public UnrootedMLSearcher attach(Node subTree, Alignment fullAlignment, SubstitutionModel model);
	/**
	 * Create a new Tree Searcher with a new sub tree attached
	 * @param newSequence the new sequence to attach at this branch
	 * @param fullAlignment the full alignment including the sequences already part of the base tree
	 * @param model the new substitution model to use
	 * @return a new unrooted searcher
	 */
	public UnrootedMLSearcher attach(String newSequence, Alignment fullAlignment, SubstitutionModel model);



	/**
	 * Obtain the leaf names to the "left" of this branch (left/right is an arbitary name to either end of branch - the only guarantee is that left is not right)
	 * @return the appropriate leaf names
	 */
	 public String[] getLeftLeafNames();

	/**
	 * Obtain the leaf names to the "right" of this branch (left/right is an arbitary name to either end of branch - the only guarantee is that left is not right)
	 * @return the appropriate leaf names
	 */
	public String[] getRightLeafNames();

	/**
	 * Constructe an array detailing the split information
	 * @param leafNames the names of the leaves
	 * @return an array matching the input array length, where each element should have the values -1, 1, or 0 depending on whether the name is in the "left" set, the "right" set, or unknown respectively.
	 */
	public int[] getSplitInformation(String[] leafNames);

	public static final class Utils {
		private static final boolean isContained(String[] set, String query) {
			for(int i = 0 ; i < set.length ; i++) {
				if(query.equals(set[i])) {
					return true;
				}
			}
			return false;
		}
		public static final boolean isContained(String[] larger, String[] smaller) {
			for(int i = 0 ; i < smaller.length ; i++) {
				if(!isContained(larger,smaller[i])){
					return false;
				}
			}
			return true;
		}
		public static final boolean isIntersection(String[] one, String[] two) {
			for(int i = 0 ; i < one.length ; i++) {
				if(isContained(two,one[i])){
					return true;
				}
			}
			return false;
		}
		public static final boolean isMatching(BranchAccess branch,  String[] queryLeftLeaves, String[] queryRightLeaves) {
		  final String[] branchRight = branch.getRightLeafNames();
//			System.out.println("is matching(q-left):"+pal.misc.Utils.toString(queryLeftLeaves));
//			System.out.println("is matching(q-right):"+pal.misc.Utils.toString(queryRightLeaves));

//			System.out.println("is matching(branch-right):"+pal.misc.Utils.toString(branchRight));
			if(isIntersection(queryLeftLeaves,branchRight)) {
//				System.out.println("Is l-r interesection");
			  return false;
			}
			final String[] branchLeft = branch.getLeftLeafNames();
//			System.out.println("is matching(branch-left):"+pal.misc.Utils.toString(branchLeft));
			if(isIntersection(queryRightLeaves,branch.getLeftLeafNames())) {
//			  System.out.println("Is r-l interesection");
			  return false;
			}
			return isIntersection(branchLeft,queryLeftLeaves)&&isIntersection(branchRight,queryRightLeaves);
		}
		public static final BranchAccess getMatching(BranchAccess[] base, BranchAccess query) {
			return getMatching(base,query.getLeftLeafNames(),query.getRightLeafNames());
		}
		public static final BranchAccess getMatching(BranchAccess[] base, String[] queryLeftLeaves, String[] queryRightLeaves) {
		  for(int i = 0 ; i < base.length ; i++) {
				if(isMatching(base[i],queryLeftLeaves, queryRightLeaves)) {
					return base[i];
				}
			}
			return null;
		}
		public static final void transferAnnotation(BranchAccess source, BranchAccess[] desination) {
			transferAnnotation(source.getLeftLeafNames(),source.getRightLeafNames(),source.getAnnotation(), desination);
		}
		public static final void transferAnnotation(String[] sourceLeftLeaves, String[] sourceRightLeaves, Object annotation, BranchAccess[] desination) {
		  for(int i = 0 ; i < desination.length ; i++) {
				if(isMatching(desination[i],sourceLeftLeaves, sourceRightLeaves)) {
				  desination[i].setAnnotation(annotation);
				}
			}
		}
		/**
		 * Transfers the annotation across two independent sets of BranchAccess objects. It is expected that
		 * the leaf sets are relatively common (they don't have to match - if there are no matches in leaf sets this
		 * method won't do anything except eat CPU time).
		 * This method is time consuming
		 * @param source The source of the annotation
		 * @param destination The destination of the annotation
		 */
		public static final void transferAnnotation(BranchAccess[] source, BranchAccess[] destination) {
		  for(int i = 0 ; i < source.length ; i++) {
				transferAnnotation(source[i], destination );
			}
		}
	}
}