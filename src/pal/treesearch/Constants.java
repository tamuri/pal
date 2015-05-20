// Constants.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: Constants </p>
 * <p>Description: Various constants used in the pal.treesearch package </p>
 *
 * @author Matthew Goode
 * @version 1.0
 */

public interface Constants {
    double CONSTRUCTED_BRANCH_LENGTH = 0.01;

    double MINIMUM_BRANCH_LENGTH = 0;
    double MAXIMUM_BRANCH_LENGTH = 10;

    int CONSTRAINED_NODE_COMPONENT_TYPE = 0;
    int FREE_NODE_COMPONENT_TYPE = 1;
    int FREE_BRANCH_COMPONENT_TYPE = 2;
    int FREE_LEAF_COMPONENT_TYPE = 3;
    int CONSTRAINED_LEAF_COMPONENT_TYPE = 4;
    int PIVOT_COMPONENT_TYPE = 5;
    int GENERAL_OPTIMISABLE_TYPE = 6;

}