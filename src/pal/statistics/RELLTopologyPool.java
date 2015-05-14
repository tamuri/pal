// RELLTopologyPool.java
//
// (c) 2000-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.statistics;

/**
 * <p>Title: RELLTopologyPool </p>
 * <p>Description: For non-parameteric RELL analysis </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.misc.Utils;
import pal.tree.*;
import pal.alignment.*;
import pal.eval.*;
import pal.util.AlgorithmCallback;
import pal.math.MersenneTwisterFast;

public class RELLTopologyPool implements TopologyTestEngine.TopologyPool {
	private final SiteDetails[] baseTopologies_;
	private final double[] logLikelihoods_;
	private final int numberOfSites_;
	private final MersenneTwisterFast random_ = new MersenneTwisterFast();
	public RELLTopologyPool( SiteDetails[] topologies, int numberOfSites) {
		this.numberOfSites_ = numberOfSites;
		this.baseTopologies_ = new SiteDetails[topologies.length];
		System.arraycopy( topologies, 0, baseTopologies_, 0, topologies.length );
		this.logLikelihoods_ = new double[topologies.length];
		calculateOriginalLogLikelihoods();
	}

	private void calculateOriginalLogLikelihoods() {

		for( int i = 0; i<baseTopologies_.length; i++ ) {

			logLikelihoods_[i] = pal.misc.Utils.getSum(baseTopologies_[i].getSiteLogLikelihoods());
		}
	}

	public int getNumberOfTopologies() {	return baseTopologies_.length; }

	public double[] getOriginalOptimisedLogLikelihoods() {	return logLikelihoods_; }
	private final double getReplicateLogLikelihood(int[] siteLookup,int topology) {
	  double[] siteLogLikelihoods = baseTopologies_[topology].getSiteLogLikelihoods();
		double total = 0;
		for(int i = 0 ; i < numberOfSites_ ; i++) {
		  total += siteLogLikelihoods[siteLookup[i]];
		}
		return total;
	}
	public double[] getNewReplicateLogLikelihoods(  AlgorithmCallback callback ) {
		double[] replicateLogLikelihoods = new double[baseTopologies_.length];
		int[] siteLookup = new int[numberOfSites_];
		for(int i = 0 ; i < numberOfSites_ ; i++) {
		  siteLookup[i] = random_.nextInt(numberOfSites_);
		}

		for(int topology = 0; topology < baseTopologies_.length ; topology++) {
			callback.updateProgress(topology/(double)baseTopologies_.length);

			replicateLogLikelihoods[topology] = getReplicateLogLikelihood(siteLookup, topology);
		}
		return replicateLogLikelihoods;
	}

// ===============================================================================================




	// ======


}