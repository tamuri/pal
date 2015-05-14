// SiteDetails.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: SiteDetails (was Posteriors)</p>
 * <p>Description: Access for site based information that are calculated post ML optimisation </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.substmodel.*;
public interface SiteDetails {
  public SubstitutionModel getRelatedModel();
  public double[] getSitePosteriors(int site);
	/**
	 * Get the likelihoods for each site (not the log likelihoods)
	 * @return a double array of likelihoods matching each site
	 */
	public double[] getSiteLikelihoods();

	/**
	 * Get the likelihoods for each site (logged)
	 * @return a double array of log likelihoods matching each site
	 */
	public double[] getSiteLogLikelihoods();


// -=-=-==--==-
	public static final class Utils {
		/**
		 * Create a Postriors object
		 * @param categoryPatternConditionalProbabilities An array arranged [category][pattern] that holds the conditional probabilities for each category at each site
		 * @param isLoggedConditionals should be true if the conditional probabilities are stored as logged values, false if not
		 * @param model the related substitution models
		 * @param numberOfPatterns the number of patterns
		 * @param sitePatternMatchup an array that identifies what pattern is to used at which site
		 * @param numberOfSites the number of sites
		 * @param siteLikelihoods the site likelihood (unlogged) at each site
		 * @return an appropriate Posteriors object
		 */
		public static final SiteDetails create(double[][] categoryPatternConditionalProbabilities, boolean isLoggedConditionals, SubstitutionModel model, int numberOfPatterns, int[] sitePatternMatchup, int numberOfSites, double[] siteLikelihoods) {
			return new SimpleSiteDetails(categoryPatternConditionalProbabilities,isLoggedConditionals,	model, numberOfPatterns, sitePatternMatchup,numberOfSites,siteLikelihoods);
		}
		/**
		 * Create a Postriors object with no related substitution model
		 * @param categoryPatternConditionalProbabilities An array arranged [category][pattern] that holds the conditional probabilities for each category at each site
		 * @param isLoggedConditionals should be true if the conditional probabilities are stored as logged values, false if not
		 * @param numberOfPatterns the number of patterns
		 * @param sitePatternMatchup an array that identifies what pattern is to used at which site
		 * @param numberOfSites the number of sites
		 * @param siteLikelihoods the site likelihood (unlogged) at each site
		 * @return an appropriate Posteriors object
		 */
		 public static final SiteDetails create(double[][] categoryPatternConditionalProbabilities, boolean isLoggedConditionals,int numberOfPatterns, int[] sitePatternMatchup, int numberOfSites, double[] siteLikelihoods) {
			return new SimpleSiteDetails(categoryPatternConditionalProbabilities,isLoggedConditionals,	 numberOfPatterns, sitePatternMatchup,numberOfSites,siteLikelihoods);
		}
		//-=-=-=-=-=
		private final static class SimpleSiteDetails implements SiteDetails {
			private final double[][] categoryPatternConditionalProbabilities_;
			private final double[][] patternPosteriors_;
			private final double[] siteLikelihoods_;
			private final double[] siteLogLikelihoods_;
			private final int[] sitePatternMatchup_;
			private final SubstitutionModel model_;
			private final int numberOfSites_;
		  public SimpleSiteDetails( double[][] categoryPatternConditionalProbabilities, boolean isLoggedConditionals, int numberOfPatterns, int[] sitePatternMatchup, int numberOfSites, double[] siteLikelihoods ) {
		    this(categoryPatternConditionalProbabilities,isLoggedConditionals, null,numberOfPatterns,sitePatternMatchup, numberOfSites, siteLikelihoods);
			}
		 	public SimpleSiteDetails( double[][] categoryPatternConditionalProbabilities, boolean isLoggedConditionals, SubstitutionModel model, int numberOfPatterns, int[] sitePatternMatchup, int numberOfSites, double[] siteLikelihoods ) {
				final int numberOfCategories = model.getNumberOfTransitionCategories();
				this.siteLikelihoods_ = pal.misc.Utils.getCopy(siteLikelihoods);
				this.siteLogLikelihoods_ = new double[numberOfSites];
				double llh = 0;
				for(int i = 0 ; i < numberOfSites ; i++) {
				  this.siteLogLikelihoods_[i] = Math.log(this.siteLikelihoods_[i]);
					llh+=this.siteLogLikelihoods_[i];
				}
				System.out.println("Total:"+llh);
				if( isLoggedConditionals ) {
					this.categoryPatternConditionalProbabilities_ = convertLogged( categoryPatternConditionalProbabilities, numberOfCategories, numberOfPatterns );
				} else {
					this.categoryPatternConditionalProbabilities_ = pal.misc.Utils.getCopy( categoryPatternConditionalProbabilities );
				}
				this.numberOfSites_ = numberOfSites;
				this.sitePatternMatchup_ = pal.misc.Utils.getCopy( sitePatternMatchup );
				this.patternPosteriors_ = new double[numberOfPatterns][numberOfCategories];
				for( int p = 0; p<numberOfPatterns; p++ ) {
					double total = 0;
					for( int c = 0; c<numberOfCategories; c++ ) {
						total += categoryPatternConditionalProbabilities[c][p];
					}
					for( int c = 0; c<numberOfCategories; c++ ) {
						patternPosteriors_[p][c] = categoryPatternConditionalProbabilities[c][p]/total;
					}
				}

				this.model_ = model;
			}
			public double[] getSiteLikelihoods() { return siteLikelihoods_; }
			public double[] getSiteLogLikelihoods() { return siteLogLikelihoods_; }

			/**
			 * May return null
			 * @return null if not related model, otherwise the model
			 */
			public SubstitutionModel getRelatedModel() {	return model_; }

			public double[] getSitePosteriors( int site ) {
				return patternPosteriors_[sitePatternMatchup_[site]]; }

			private final static double[][] convertLogged( double[][] loggedStore, int numberOfCategories, int numberOfPatterns ) {
				double[][] result = new double[numberOfCategories][numberOfPatterns];
				for( int c = 0; c<numberOfCategories; c++ ) {
					for( int p = 0; p<numberOfPatterns; p++ ) {
						result[c][p] = Math.exp( loggedStore[c][p] );
					}
				}
				return result;
			}
			public String toString() {
				StringBuffer sb = new StringBuffer();
				for( int i = 0; i<numberOfSites_; i++ ) {
					double[] sitePosteriors = getSitePosteriors( i );
					sb.append(pal.misc.Utils.argmax(sitePosteriors));
					sb.append(" - ");
					sb.append( "Site " );
					sb.append( ( i+1 ) );
					sb.append( ":" );
					sb.append( pal.misc.Utils.toString( sitePosteriors ) );
					sb.append( "\n" );
				}
				return sb.toString();
			}
		} //End of class SimplePosteriors
	}
}