// SUPGMATree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.supgma;

import pal.distance.*;
import pal.misc.*;
import pal.mep.*;
import pal.tree.*;
/**
 * constructs an SUPGMA tree from pairwise distances. <BR>
 * Reference: <BR>
 * Alexei Drummond and Allen G. Rodrigo (2000). Reconstructing Genealogies of Serial Samples Under the Assumption of a Molecular Clock Using Serial-Sample UPGMA. Molecular Biology and Evolution 17:1807-1815
 *
 * @version $Id: SUPGMATree.java,v 1.1 2003/10/19 02:35:26 matt Exp $
 *
 * @author Alexei Drummond
 * @author Matthew Goode
 */
public class SUPGMATree extends ClusterTree
{
	//
	// Public stuff
	//

	//
	// Private stuff
	//
	private TimeOrderCharacterData tocd;

	/**
	 * constructor SUPGMA tree
	 *
	 * @param m *uncorrected* distance matrix
	 */
	public SUPGMATree(DistanceMatrix m, TimeOrderCharacterData tocd, double rate, ClusterTree.ClusteringMethod cm) {
		this(
			m,tocd,
			DeltaModel.
				Utils.
					getMutationRateModelBased(
						ConstantMutationRate.getFixedFactory(
							rate, tocd.getUnits()
						)
					),true,cm
				);
	}


	/**
	 * constructor SUPGMA tree
	 *
	 * @param m *uncorrected* distance matrix
	 */
	public SUPGMATree(DistanceMatrix m, TimeOrderCharacterData tocd, DeltaModel deltaModel, boolean allowNegatives, ClusteringMethod cm) {
		super(new SUPGMADistanceMatrix(m, tocd, deltaModel), cm);
		this.tocd = tocd;

		IdGroup idgroup = tocd;

		createNodeList();
		DeltaModel.Instance deltaModelInstance = deltaModel.generateInstance(tocd);
		Node node = null;
		// go through and set heights.
		for (int i = 0; i < getExternalNodeCount(); i++) {
			node = getExternalNode(i);
			int index = idgroup.whichIdNumber(node.getIdentifier().getName());
			node.setNodeHeight(deltaModelInstance.getExpectedSubstitutions(index));
			if (!allowNegatives) {
				if (node.getParent().getNodeHeight() < node.getNodeHeight()) {
					fixHeight(node.getParent(), node.getNodeHeight());
				}
			}
		}
		NodeUtils.heights2Lengths(getRoot());
	}



	private void fixHeight(Node node, double height) {
		node.setNodeHeight(height);
		if (!node.isRoot()) {
			if (node.getParent().getNodeHeight() < height) {
				fixHeight(node.getParent(), height);
			}
		}
	}

}
