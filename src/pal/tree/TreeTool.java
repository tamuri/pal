// TreeTool.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.misc.*;
import pal.io.*;

import java.io.*;
import java.util.*;
import pal.distance.*;

/**
 * Simple access for tree functions. The purpose of this class is to provide a set
 * interface for doing basic tree operations, and for example code.
 *
 * <b>History</b>
 * <ul>
 *  <li> 15/09/2003 - Created </li>
 * </ul>
 *
 * @version $Id: TreeTool.java,v 1.4 2004/04/25 22:53:14 matt Exp $
 *
 * @author Matthew Goode
 *
 */
import java.io.*;

public final class TreeTool  {
	/**
	 * Read a tree from an input source. Currently only understands the Newick format
	 * @param r A reader object (is not closed)
	 * @return A tree
	 * @throws IOException if there was a problem
	 */
	public final static Tree readTree(Reader r) throws IOException {
		try {
			return new ReadTree(new PushbackReader(r));
		} catch(TreeParseException e) {
			throw new IOException("Parse exception:"+e);
		}
	}
	/**
	 * Neighbour-joining tree construction based on a distance matrix
	 * @param dm The related DistanceMatrix object
	 * @return A tree formed by the neighbour-joining process using the input distance matrix
	 */
	public static final Tree createNeighbourJoiningTree(DistanceMatrix dm) {
	  return new NeighborJoiningTree(dm);
	}
	/**
	 * UPGMA tree construction based on a distance matrix
	 * @param dm The related DistanceMatrix object
	 * @return A tree formed by the UPGMA process using the input distance matrix
	 */
	public static final Tree createUPGMA(DistanceMatrix dm) {
	  return new UPGMATree(dm);
	}


	/**
	 * Neighbour-joining tree construction based on a distance matrix
	 * @param dm A matrix of doubles that forms the distance matrix. It is assumed this matrix is perfectly square and the diagonals match
	 * @param otuNames The list of operational taxonimic units that match the column/rows of the distance matrix.
	 * @return A tree formed by the neighbour-joining process using the input distance matrix
	 */
	public static final Tree createNeighbourJoiningTree(double[][] dm, String[] otuNames) {
	  return new NeighborJoiningTree(new DistanceMatrix(dm, new SimpleIdGroup(otuNames)));
	}
	/**
	 * UPGMA tree construction based on a distance matrix
	 * @param dm A matrix of doubles that forms the distance matrix. It is assumed this matrix is perfectly square and the diagonals match
	 * @param otuNames The list of operational taxonimic units that match the column/rows of the distance matrix.
	 * @return A tree formed by the neighbour-joining process using the input distance matrix
	 */
	public static final Tree createUPGMATree(double[][] dm, String[] otuNames) {
	  return new UPGMATree(new DistanceMatrix(dm, new SimpleIdGroup(otuNames)));
	}
	/**
	 * Unroot a tree (makes the base of the tree a trification). Total Branch lengths are conserved
	 * @param t The rooted (or unrooted) tree
	 * @return An unrooted tree
	 * @see class TreeManipulator
	 */
	public static final Tree getUnrooted(Tree t) {
	  return TreeManipulator.getUnrooted(t);
	}
	/**
	 * Root a tree around it's midpoint. Total Branch lengths are conserved
	 * @param t The unrooted (or rooted) tree
	 * @return A rooted tree
	 * @see class TreeManipulator
	 */
	public static final Tree getMidPointRooted(Tree t) {
	  return TreeManipulator.getMidpointRooted(t);
	}
	/**
	 * Root a tree by an outgroup. Total Branch lengths are conserved
	 * @param t The unrooted (or rooted) tree
	 * @param outgroupMembers The names of the outgroup members (must be at least one). If there are more than one outgroup than the clade that contains all members is used as the outgroup. In some case poorly choosen outgroup members can result in multiple ways of rooting. If for some reason this is what is wanted see the TreeManipulator class for more powerful options.
	 * @return A rooted tree
	 * @see class TreeManipulator
	 */
	public static final Tree getRooted(Tree t, String[] outgroupMembers) {
	  return TreeManipulator.getRootedBy(t,outgroupMembers);
	}

}
