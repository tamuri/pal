// ShimodairaHasegawaStatistics.java
//
// (c) 2000-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.statistics;

/**
 * <p>Title: ShimodairaHasegawaStatistics </p>
 * <p>Description:The Statistics Handler for the ShimodairaHasegawa test </p>
 * @author Matthew Goode
 * @version 1.0
 */

public class ShimodairaHasegawaStatistics implements TopologyTestEngine.StatisticsHandler {
  public ShimodairaHasegawaStatistics() {  }

	public double[] getOriginalTestStatistics(double[] originalOptimisedLogLikelihoods, int numberOfTopologies) {
		double maxLikelihood = pal.misc.Utils.getMax(originalOptimisedLogLikelihoods,0,numberOfTopologies);
		double[] testStatistics = new double[numberOfTopologies];
		for(int i = 0 ; i < numberOfTopologies ; i++) {
		  testStatistics[i] = maxLikelihood-originalOptimisedLogLikelihoods[i];
		}
		return testStatistics;
	}

	private final static double[][] createCentered(double[][] replicateLogLiklihoods, int numberOfReplicates, int numberOfTopologies) {
		double[][] result = new double[numberOfReplicates][numberOfTopologies];
		for(int topology = 0 ; topology < numberOfTopologies ; topology++) {
			double total = 0;
			for(int replicate = 0 ; replicate < numberOfReplicates ; replicate++) {
				total+=replicateLogLiklihoods[replicate][topology];
			}
			final double average = total/numberOfReplicates;
			for(int replicate = 0 ; replicate < numberOfReplicates ; replicate++) {
				result[replicate][topology]=replicateLogLiklihoods[replicate][topology]-average;
			}
		}
		return result;
	}
	private final static double[][] createReplicateStatistics(double[][] centeredValues, int numberOfReplicates, int numberOfTopologies) {
		double[][] result = new double[numberOfReplicates][numberOfTopologies];
		for(int replicate = 0 ; replicate < numberOfReplicates ; replicate++) {
			double max = pal.misc.Utils.getMax(centeredValues[replicate]);

			for(int topology = 0 ; topology < numberOfTopologies ; topology++) {
				result[replicate][topology]=max-centeredValues[replicate][topology];
			}
		}
		return result;
	}
	/**
	 * The pValue in this case represents the alpha value ( CI  1-alpha) when topology becomes significantly different from the ML topology (or stops being not significantly different)
	 * @param originalOptimisedLogLikelihoods stored as [topology]
	 * @param replicateLogLikelihoods store as [replicate][topology]
	 * @param numberOfTopologies the number of topologies
	 * @param numberOfReplicates the number of replicates
	 * @return the pValue array
	 */
	public double[] getPValues(double[] originalOptimisedLogLikelihoods, double[][] replicateLogLikelihoods, int numberOfReplicates, int numberOfTopologies ) {
		double[][] replicateStatistics =
		  createReplicateStatistics(
			  createCentered(replicateLogLikelihoods,numberOfReplicates,numberOfTopologies),
				numberOfReplicates,numberOfTopologies
			);
		double[] testStatistics = getOriginalTestStatistics(originalOptimisedLogLikelihoods,numberOfTopologies);
		double[] pValues = new double[numberOfTopologies];
		for(int topology = 0 ; topology < numberOfTopologies ; topology++) {
			double statistic = testStatistics[topology];
			int count = 0;
			for(int replicate = 0 ; replicate < numberOfReplicates ; replicate++) {
				if(replicateStatistics[replicate][topology]>= statistic) {
					count++;
				}
			}
			pValues[topology] = count/(double)numberOfReplicates;
		}
		return pValues;
	}


}