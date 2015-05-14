// GeneralTopologyPool.java
//
// (c) 2000-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.statistics;

/**
 * <p>Title: GeneralTopologyPool </p>
 * <p>Description: A collection of topologies </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.misc.Utils;
import pal.tree.*;
import pal.alignment.*;
import pal.util.AlgorithmCallback;
public class GeneralTopologyPool implements TopologyTestEngine.TopologyPool {
	private final Tree[] baseTopologies_;
	private final double[] logLikelihoods_;
	private final LikelihoodEvaluator baseCalculator_;
	private final ReplicateLikelihoodEvaluator replicateCalculator_;
	private final Alignment baseAlignment_;
	private final AlignmentGenerator replicateGenerator_;

	private boolean logLikelihoodsCalculated_;

	public GeneralTopologyPool( Tree[] topologies, LikelihoodEvaluator baseCalculator, ReplicateLikelihoodEvaluator replicateCalculator, Alignment baseAlignment, AlignmentGenerator replicateGenerator ) {
		this.baseCalculator_ = baseCalculator;
		this.replicateCalculator_ = replicateCalculator;
		this.baseAlignment_ = baseAlignment;
		this.replicateGenerator_ = replicateGenerator;
		this.baseTopologies_ = new Tree[topologies.length];
		System.arraycopy( topologies, 0, baseTopologies_, 0, topologies.length );
		this.logLikelihoods_ = new double[topologies.length];
		logLikelihoodsCalculated_ = false;
	}

	public void optimiseOriginalTopologies( AlgorithmCallback callback ) {
		for( int i = 0; i<baseTopologies_.length; i++ ) {
			callback.updateProgress( i/( double )baseTopologies_.length );
			AlgorithmCallback subCallback = AlgorithmCallback.Utils.getSubCallback(callback,"Optimisation:"+i,i/(double)baseTopologies_.length, (i+1)/(double)baseTopologies_.length);
			LikelihoodEvaluator.MLResult result = baseCalculator_.getMLOptimised( baseTopologies_[i], baseAlignment_,subCallback );
			baseTopologies_[i] = result.getOptimisedTree();
			logLikelihoods_[i] = result.getLogLikelihood();
		}
		logLikelihoodsCalculated_ = true;
	}

	private void calculateOriginalLogLikelihoods() {
		System.out.println("Calculate original");
		for( int i = 0; i<baseTopologies_.length; i++ ) {
			logLikelihoods_[i] = baseCalculator_.calculateLikelihood( baseTopologies_[i], baseAlignment_ );
		}
		System.out.println("Finished Calculate original");
		logLikelihoodsCalculated_ = true;
	}

	private void checkLogLikelihoods() {
		if( !logLikelihoodsCalculated_ ) { calculateOriginalLogLikelihoods(); }
	}

	public int getNumberOfTopologies() {	return baseTopologies_.length; }

	public Tree[] getOriginalOptimisedTrees() {	return baseTopologies_; }

	public double[] getOriginalOptimisedLogLikelihoods() {	checkLogLikelihoods(); return logLikelihoods_; }

	public double[] getNewReplicateLogLikelihoods( AlgorithmCallback callback ) {
		final AlgorithmCallback replicateCallback  = AlgorithmCallback.Utils.getSubCallback(callback,"Replicate Generator",0,0.1);
		final AlgorithmCallback remainderCallback  = AlgorithmCallback.Utils.getSubCallback(callback,"Log Likelihood Calculation",0.1,1);
		Alignment replicateAlignment = replicateGenerator_.getNextAlignment( replicateCallback );
		double[] replicateLogLikelihoods = new double[baseTopologies_.length];

		for(int topology = 0; topology < baseTopologies_.length ; topology++) {
			remainderCallback.updateProgress(topology/(double)baseTopologies_.length);
		  replicateLogLikelihoods[topology]  = replicateCalculator_.getReplicateLogLikelihood(baseTopologies_[topology],replicateAlignment);
		}
		return replicateLogLikelihoods;
	}

// ===============================================================================================




	// ======


}