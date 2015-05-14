// SUPGMADistanceMatrix.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.supgma;

import pal.distance.*;
import pal.misc.*;
import pal.mep.*;


/**
 * Corrects distances in a distance matrix such that all tips appear
 * contemporaneous, given a time/date and rate information for the
 * taxa.
 *
 * @version $Id: SUPGMADistanceMatrix.java,v 1.1 2003/10/19 02:35:26 matt Exp $
 *
 * @author Alexei Drummond
 * @author Matthew Goode
 */
public class SUPGMADistanceMatrix extends DistanceMatrix {


	/**
	 * Uses date/time information and a constant rate to correct distance matrices.
	 */
	public SUPGMADistanceMatrix(DistanceMatrix raw, TimeOrderCharacterData tocd, DeltaModel deltaModel) {
		super(raw);
		DeltaModel.Instance deltaModelInstance = deltaModel.generateInstance(tocd);
		double[] tips = new double[tocd.getIdCount()];
		for (int i = 0; i < tips.length; i++) {
			double tipOffset = deltaModelInstance.getExpectedSubstitutions(i);

			for (int j = 0; j < tips.length; j++) {
				if (i != j) {
					addDistance(i, j, tipOffset);
				}
			}
		}
	}
}
