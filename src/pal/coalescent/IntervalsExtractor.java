// IntervalsExtractor.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.coalescent;

import pal.mep.MutationRateModel;
import pal.misc.BranchLimits;
import pal.misc.Units;
import pal.tree.Node;
import pal.tree.NodeUtils;
import pal.tree.Tree;
import pal.tree.TreeUtils;
import pal.util.*;
import pal.util.Comparable;

import java.util.ArrayList;
import java.util.List;

/**
 * A series of coalescent intervals representing the time
 * order information contained in a (serial) clock-constrained
 * tree.
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 * @version $Id: IntervalsExtractor.java,v 1.12 2001/07/12 12:17:43 korbinian Exp $
 */
public class IntervalsExtractor implements Units {

    /**
     * extracts intervals from clock tree.
     */
    public static CoalescentIntervals extractFromClockTree(Tree tree) {
        return extractFromClockTree(tree, -1);
    }

    /**
     * extracts intervals from clock tree. Leafs are assumed to have
     * height zero.  Starting at time zero, small (<= minSize) intervals are pooled
     * with the next non-small interval (if this does not exist then
     * with the previous non-small interval)
     */
    public static CoalescentIntervals extractFromClockTree(Tree tree, double minSize) {
        tree.createNodeList(); //make consistent
        NodeUtils.lengths2Heights(tree.getRoot());
        //NodeUtils.lengths2HeightsKeepTips(tree.getRoot(),true);

        // Set heights of all external nodes to zero
        // we need a proper clock-tree
        //for (int i = 0; i < tree.getExternalNodeCount(); i++)
        //{
        //	tree.getExternalNode(i).setNodeHeight(0.0);
        //}


        List<Comparable> times = new ArrayList<>();
        List<Integer> childs = new ArrayList<>();
        collectInternalNodeHeights(tree.getRoot(), times, childs);
        int[] indices = new int[times.size()];

        HeapSort.sort(times, indices);

        int uniqueIntervals = 0;

        double currentTime = 0.0;
        for (int i = 0; i < times.size(); i++) {
            double time = ((ComparableDouble) times.get(indices[i])).doubleValue();
            if (Math.abs(time - currentTime) > minSize) {
                uniqueIntervals += 1;
            }
            currentTime = time;
        }
        if (uniqueIntervals == 0) uniqueIntervals = 1;

        CoalescentIntervals ci = new CoalescentIntervals(uniqueIntervals);
        ci.setUnits(tree.getUnits());

        double start = 0.0;
        int numLines = tree.getExternalNodeCount();

        int count = 0;
        int coalescences = 0;
        for (int i = 0; i < times.size(); i++) {
            double finish = ((ComparableDouble) times.get(indices[i])).doubleValue();
            int childCount = childs.get(indices[i]);

            double length = Math.abs(finish - start);
            coalescences += childCount - 1;

            ci.setInterval(count, length + ci.getInterval(count));
            ci.setNumLineages(count, numLines);

            if (length > minSize) {
                count++;
                if (count == uniqueIntervals) count--;
                numLines = numLines - coalescences;

                coalescences = 0;
            }

            start = finish;
        }

        return ci;
    }

    /**
     * extracts intervals in generation times from serial clock tree (in mutation times)
     * after taking into account mutation rate model.
     */
    public static CoalescentIntervals extractFromTree(Tree tree, MutationRateModel muModel) {

        Tree newTree = TreeUtils.mutationsToGenerations(tree, muModel);
        return extractFromTree(newTree);
    }

    /**
     * extracts intervals from serial clock tree.
     */
    public static CoalescentIntervals extractFromTree(Tree tree) {
        double MULTIFURCATION_LIMIT = BranchLimits.MINARC;

        // get heights if it looks necessary
        if (tree.getRoot().getNodeHeight() == 0.0) {
            NodeUtils.lengths2Heights(tree.getRoot());
        }

        List<pal.util.Comparable> times = new ArrayList<>();
        List<Integer> childs = new ArrayList<>();
        collectAllTimes(tree.getRoot(), times, childs);
        int[] indices = new int[times.size()];
        List<Integer>lineages = new ArrayList<>();
        List<Double> intervals = new ArrayList<>();

        HeapSort.sort(times, indices);

        double start = 0.0;
        int numLines = 0;
        int i = 0;
        while (i < times.size()) {

            int lineagesRemoved = 0;
            int lineagesAdded = 0;

            double finish = ((ComparableDouble) times.get(indices[i])).doubleValue();
            double next = finish;

            while (Math.abs(next - finish) < MULTIFURCATION_LIMIT) {
                int children = childs.get(indices[i]);
                if (children == 0) {
                    lineagesAdded += 1;
                } else {
                    lineagesRemoved += (children - 1);
                }
                i += 1;
                if (i < times.size()) {
                    next = ((ComparableDouble) times.get(indices[i])).doubleValue();
                } else break;
            }
            //System.out.println("time = " + finish + " removed = " + lineagesRemoved + " added = " + lineagesAdded);
            if (lineagesAdded > 0) {

                if ((intervals.size() > 0) || ((finish - start) > MULTIFURCATION_LIMIT)) {
                    intervals.add(finish - start);
                    lineages.add(numLines);
                }

                start = finish;
            }
            // add sample event
            numLines += lineagesAdded;

            if (lineagesRemoved > 0) {

                intervals.add(finish - start);
                lineages.add(numLines);
                start = finish;
            }
            // coalescent event
            numLines -= lineagesRemoved;


        }

        CoalescentIntervals ci = new CoalescentIntervals(intervals.size());
        for (i = 0; i < intervals.size(); i++) {
            ci.setInterval(i, intervals.get(i));
            ci.setNumLineages(i, lineages.get(i));
        }

        // Same Units as tree
        ci.setUnits(tree.getUnits());


        return ci;
    }

    // PRIVATE STUFF

    /**
     * extract coalescent times and tip information into Vector times from tree.
     */
    private static void collectAllTimes(Node node, List<Comparable> times, List<Integer> childs) {

        times.add(new ComparableDouble(node.getNodeHeight()));
        childs.add(node.getChildCount());

        for (int i = 0; i < node.getChildCount(); i++) {
            collectAllTimes(node.getChild(i), times, childs);
        }
    }

    /**
     * extract internal node heights Vector times from tree.
     */
    private static void collectInternalNodeHeights(Node node, List<Comparable> times, List<Integer> childs) {
        if (!node.isLeaf()) {
            times.add(new ComparableDouble(node.getNodeHeight()));
            childs.add(node.getChildCount());

            for (int i = 0; i < node.getChildCount(); i++) {
                collectInternalNodeHeights(node.getChild(i), times, childs);
            }
        }
    }
}
