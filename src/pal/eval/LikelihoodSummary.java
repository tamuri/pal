// LikelihoodSummary.java
//
// (c) 1999-2002 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <B>Title:</B> Likelihood Summary <BR>
 * <B>Description:</B> A container for advanced information derived from a likelihood analysis.<BR>
 * (To fill the gaps between the abilities of GeneralLikelihoodCalculator and LikelihoodValue)
 * @author Matthew Goode
 * @version $Id: LikelihoodSummary.java,v 1.2 2003/10/13 04:15:59 matt Exp $
 */
import pal.datatype.*;
import pal.misc.Utils;

public class LikelihoodSummary implements java.io.Serializable {


  private double overallLogLikelihood_;
  private double[] categoryProbabilities_;
  private double[][] individualLikelihoods_;
  private int[] sitePatternMatchup_;
  private DataType dataType_;

  //
  // Serialization code
  //
  private static final long serialVersionUID=-37625234234158192L;

  //serialver -classpath ./classes pal.eval.LikelihoodSummary

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    out.writeByte(1); //Version number
    out.writeDouble(overallLogLikelihood_);
    out.writeObject(categoryProbabilities_);
    out.writeObject(individualLikelihoods_);
    out.writeObject(sitePatternMatchup_);
    out.writeObject(dataType_);
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
    byte version = in.readByte();
    switch(version) {
      default : {
        overallLogLikelihood_ = in.readDouble();
        categoryProbabilities_ = (double[])in.readObject();
        individualLikelihoods_ = (double[][])in.readObject();
        sitePatternMatchup_ = (int[])in.readObject();
        dataType_ = (DataType)in.readObject();
      }
    }
  }
  /**
   * @param dt The data type used (for reference)
   * @param overallLogLikelihood (the overall log likelihood found)
   * @param categoryProbabilities (the probabilities of each category ([1] if not separate categories)
   * @param individualLikelihoods The individual likelihoods of each pattern/category (organised [site][category])
   * @param sitePatternMatchup for each site indicates which is the related pattern (it is assumed categoryProbabilities given with regard to patterns, if not sitePatternMatchup should contain {0,1,2, ... numberOfSites-1)
   */
  public LikelihoodSummary(DataType dt, double overallLogLikelihood, double[] categoryProbabilities, double[][] individualLikelihoods, int[] sitePatternMatchup) {
    this.dataType_ = dt;
    this.overallLogLikelihood_ = overallLogLikelihood;
    this.categoryProbabilities_ = Utils.getCopy(categoryProbabilities);
    this.individualLikelihoods_ = Utils.getCopy(individualLikelihoods);
    this.sitePatternMatchup_ = Utils.getCopy(sitePatternMatchup);
  }

  public final double getOverallLogLikelihood() {
    return overallLogLikelihood_;
  }
  public final int[][] generateCategoryRankings() {
    int[][] rankings = new int[sitePatternMatchup_.length][];
    for(int i = 0 ; i < rankings.length ; i++) {
      rankings[i] = generateCategoryRanking(i);
    }
    return rankings;
  }
  public final double[] generateSiteLikelihoods(int site) {
    return Utils.getCopy(individualLikelihoods_[sitePatternMatchup_[site]]);
  }
  public final double[] generateSitePosteriors(int site) {
    double[] rs = generateSiteLikelihoods(site);
    double total = 0;
    for(int i = 0 ; i < rs.length ;i++) {
      total+=rs[i];
    }
    for(int i = 0 ; i < rs.length ;i++) {
      rs[i]/=total;
    }
    return rs;
  }

  public final int[] generateCategoryRanking(int site) {
    double[] likelihoods = individualLikelihoods_[sitePatternMatchup_[site]];
    int[] ranking = new int[likelihoods.length];
    boolean[] used = new boolean[likelihoods.length];
    for(int i = 0 ; i < used.length ; i++) { used[i] = false;	} //Yes, I know this is redundant, but it makes me feel better to do it.
    for(int i = 0 ; i < ranking.length ; i++) {
      int max = -1;
      double maxValue = -1;
      for(int j = 0 ; j < used.length ; j++) {
        if(!used[j]) {
          if(max<0||likelihoods[j]>maxValue) {
            max = j;
            maxValue = likelihoods[j];
          }
        }
      }
      used[max] = true;
      ranking[i] = max;
    }
    return ranking;
  }
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Likelihood Summary\n\n");
    sb.append("Data Type:"+dataType_+"\n");
    sb.append("Overall Log Likelihood:"+overallLogLikelihood_+"\n");
    sb.append("Number of sites:"+sitePatternMatchup_.length+"\n\n");

    for(int i = 0 ; i < sitePatternMatchup_.length ; i++) {
      double[] sitePosteriors = generateSitePosteriors(i);
      int[] ranking = generateCategoryRanking(i);
      sb.append("Site:"+i);
      sb.append(' ');
      sb.append(Utils.toString(ranking));
      sb.append("\n");
      sb.append(" posteriors:"+Utils.toString(sitePosteriors));
      sb.append("\n");
    }
    return sb.toString();

  }
}