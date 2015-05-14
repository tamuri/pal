// SearchEngine.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.algorithmics;

import pal.util.*;
import java.util.*;

/**
 * A simplistic class (most of the work is done elsewhere) that handles basic search algorithms
 *
 * @version $Id: SearchEngine.java,v 1.2 2003/10/19 02:35:26 matt Exp $
 *
 * @author Matthew Goode
 */

public class SearchEngine {
  private final ProbabilityIterator.Factory probabilityIteratorFactory_;


  public SearchEngine( ProbabilityIterator.Factory probabilityIteratorFactory) {
    this.probabilityIteratorFactory_ = probabilityIteratorFactory;

  }

  public void run(AlgorithmCallback callback, final double initialScore, ObjectState subject, StoppingCriteria.Factory stoppingCriteria, Ranker ranker) {
      Object bestState = subject.getStateReference();
      double score = initialScore;
      StoppingCriteria stopper = stoppingCriteria.newInstance();
      ProbabilityIterator acceptanceProbability = probabilityIteratorFactory_.newInstance();
      double bestScore = Double.POSITIVE_INFINITY;
      int evaluationCount = 0;
      final boolean maximising = subject.isMaximiseScore();
      while(!stopper.isTimeToStop()) {
        double newScore = subject.doAction(score,stopper.getRelativeStoppingRatio());
//			double newScore = assessor.evaluate(subject);
        evaluationCount++;
        double probability = acceptanceProbability.getNextProbability(score, newScore, maximising);
        if(ranker.isWorthAdding(newScore,maximising)) {
          ranker.add(subject.getStateReference(), newScore,maximising);
          System.out.println("Ranker best score:"+ranker.getBestScore());
					if(ranker.getBestScore()!=bestScore) {
            bestScore = ranker.getBestScore();
            System.out.println("Best score:"+bestScore);
          }
        }
        if(
          (!maximising&&(newScore<=score)) ||
          (maximising&&(newScore>=score))||
          probability==1.0||
          Math.random()<probability) {
          score = newScore;
        } else {
          if(!subject.undoAction())  {
            //Undo was unsuccessful so we have to stick to what we have even if it was worse!
            score = newScore;
          }
        }
        stopper.newIteration(score,ranker.getBestScore(),!maximising,acceptanceProbability.isStablised(), callback);
        if(callback.isPleaseStop()) { break; }
      }
      callback.updateStatus("Finished:"+score);
      System.out.println("Evaluation Count:"+(evaluationCount+1));
    }

}

