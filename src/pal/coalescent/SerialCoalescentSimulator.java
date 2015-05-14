// SerialCoalescentSimulator.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.coalescent;

import pal.util.*;
import pal.misc.*;
import pal.tree.*;
import java.util.*;
import pal.math.*;
import java.io.*;

/**
 * Simulates a set of coalescent intervals given a demographic model.
 *
 * @author Alexei Drummond
 * @version $Id: SerialCoalescentSimulator.java,v 1.3 2002/10/03 06:06:55 matt Exp $
 */
public class SerialCoalescentSimulator implements Serializable{

	private SimpleTree tree = null;
	private static MersenneTwisterFast rand = new MersenneTwisterFast();

	/**
	 * Simulates a set of CoalescentIntervals from a genealogy.
	 * @param tocd the time order character data determining the
	 * order and time in which samples are added.
	 * @param model the demographic model to use
	 */
	public CoalescentIntervals simulateIntervals(
		TimeOrderCharacterData tocd,
		DemographicModel model,
		boolean createTree) {

		// nodes used to build tree if necessary
		Vector currentTreeNodes = null;
		Node[] nodes = null;

		double[] times = tocd.getCopyOfTimes();
		int[] indices = new int[times.length];

		HeapSort.sort(times, indices);

		if (!createTree) {
			tree = null;
		} else {
			nodes = new Node[times.length];
			IdGroup ids = tocd;
			for (int i = 0; i < ids.getIdCount(); i++) {
				nodes[i] = new SimpleNode();
				nodes[i].setIdentifier(ids.getIdentifier(i));
			}
			currentTreeNodes = new Vector();
		}

		if (tocd.getUnits() != model.getUnits()) {
			System.err.println("Units do not match");
			System.err.println("tocd units = " + tocd.getUnits());
			System.err.println("model units = " + model.getUnits());

			return null;
		}

		int uniqueIntervals = 0;
		double currentTime = 0.0;
		for (int i = 0; i < times.length; i++) {
			double time = times[indices[i]];
			if (Math.abs(time - currentTime) > 1e-12) {
				uniqueIntervals += 1;
				currentTime = time;
			}
		}

		//System.out.println("Unique intervals = " + uniqueIntervals);
		CoalescentIntervals ci = new CoalescentIntervals(uniqueIntervals + times.length - 1);

		currentTime = 0.0;
		int count = 0;
		int numLines = 0;

		//add in all tips
		for (int i = 0; i < times.length; i++) {

			// find next tip time
			double nextTipTime = times[indices[i]];

			// if next tip time is appreciably different from current time then one
			// or more intervals will be added between them.
			if (Math.abs(nextTipTime - currentTime) > 1e-12) {

				double newTime = currentTime + model.getSimulatedInterval(numLines, currentTime);

				while ((newTime < nextTipTime) && (numLines > 1)) {

					ci.setInterval(count, newTime - currentTime);
					ci.setNumLineages(count, numLines);

					// add an internal node to the tree
					if (createTree) {
						addInternalNode(currentTreeNodes, numLines, newTime);
					}

					numLines -= 1;
					count += 1;
					currentTime = newTime;

					if (numLines > 1) {
						newTime = currentTime +
							model.getSimulatedInterval(numLines, currentTime);
					}
				}

				// add new sample interval
				//ci.setInterval(count, nextTipTime - currentTime,
				//	numLines, CoalescentIntervals.NEW_SAMPLE);

				ci.setInterval(count, newTime - currentTime);
				ci.setNumLineages(count, numLines);

				numLines += 1;

				// add new tip to the tree
				if (createTree) {

					Node newNode = nodes[indices[i]];
					newNode.setNodeHeight(nextTipTime);
					currentTreeNodes.addElement(newNode);
				}

				count += 1;
				currentTime = nextTipTime;
			} else {
				//otherwise just add tip
				numLines += 1;

				if (createTree) {
					Node newNode = nodes[indices[i]];
					newNode.setNodeHeight(currentTime);
					currentTreeNodes.addElement(newNode);
				}
			}
		}
		while (numLines > 1) {

			double newTime = currentTime + model.getSimulatedInterval(numLines, currentTime);

			//ci.setInterval(count, newTime - currentTime,
			//		numLines, CoalescentIntervals.COALESCENT);

			ci.setInterval(count, newTime - currentTime);
			ci.setNumLineages(count, numLines);

			// add an internal node to the tree
			if (createTree) {
				addInternalNode(currentTreeNodes, numLines, newTime);
			}

			numLines -= 1;
			count += 1;
			currentTime = newTime;
		}

		if (createTree) {
			int size = currentTreeNodes.size();
			if (size > 1) {
				System.err.println("ERROR: currentTreeNodes.size() = " + size);
			}
			Node root = (Node)currentTreeNodes.elementAt(0);
			NodeUtils.heights2Lengths(root);

			tree = new SimpleTree(root);
			tree.setUnits(model.getUnits());
		}

		return ci;
	}

	private void addInternalNode(Vector currentTreeNodes, int numLines, double newTime) {

		if (numLines != currentTreeNodes.size()) {
			System.err.println("ERROR: Wrong number of nodes available!");
		}
		int node1 = rand.nextInt(currentTreeNodes.size());
		int node2 = node1;
		while (node2 == node1) {
			node2 = rand.nextInt(currentTreeNodes.size());
		}

		Node left = (Node)currentTreeNodes.elementAt(node1);
		Node right = (Node)currentTreeNodes.elementAt(node2);

		Node newNode = new SimpleNode();
		newNode.setNodeHeight(newTime);
		newNode.addChild(left);
		newNode.addChild(right);

		currentTreeNodes.removeElement(left);
		currentTreeNodes.removeElement(right);
		currentTreeNodes.addElement(newNode);
	}

	public Tree getTree() {
		return tree;
	}
}

