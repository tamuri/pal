// SubstitutionTool.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * Simple access for substitution model functions. The purpose of this class is to provide a set
 * interface for constructing substitution models, and doing basic operations.
 *
 * <b>History</b>
 * <ul>
 *  <li> 15/09/2003 - Created </li>
 *  <li> 20/04/2004 - Changed Codon model names (eg M1->M0) as I got the names wrong... </li>
 * </ul>
 *
 * @version $Id: SubstitutionTool.java,v 1.3 2004/04/25 22:53:14 matt Exp $
 *
 * @author Matthew Goode
 *
 */
import pal.datatype.*;

public final class SubstitutionTool {
	/**
	 * Create a Jukes-cantor model of substitution
	 * @return A substitution model representing JC69
	 */
	public static final SubstitutionModel createJC69Model() {
		return F81.JC69_MODEL;
	}
	/**
	 * Create a Jukes-cantor model of substitution
	 * @return A rate matrix representing JC69
	 */
	public static final RateMatrix createJC69Matrix() {
		return F81.JC69_MATRIX;
	}
	/**
	 * Create an F81 model of substitution
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related substitution model
	 */
	public static final SubstitutionModel createF81Model(double[] baseFrequencies) {
		return SubstitutionModel.Utils.createSubstitutionModel(new F81(baseFrequencies));
	}
	/**
	 * Create an F81 model of substitution
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related ratematrix
	 */
	public static final RateMatrix createF81Matrix(double[] baseFrequencies) {
		return new F81(baseFrequencies);
	}
	/**
	 * Create an F84 model of substitution
	 * @param expectedTsTv The expected ratio of transition to transversions
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related substitution model
	 */
	public static final SubstitutionModel createF84Model(double expectedTsTv, double[] baseFrequencies) {
		return SubstitutionModel.Utils.createSubstitutionModel(new F84(expectedTsTv, baseFrequencies));
	}
	/**
	 * Create an F84 model of substitution
	 * @param expectedTsTv The expected ratio of transition to transversions
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related ratematrix
	 */
	public static final RateMatrix createF84Matrix(double expectedTsTv, double[] baseFrequencies) {
		return new F84(expectedTsTv,baseFrequencies);
	}
	/**
	 * Create an Tamura-Nei model of substitution
	 * @param kappa transition/transversion rate ratio
	 * @param r pyrimidine/purin transition rate ratio
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related substitution model
	 */
	public static final SubstitutionModel createTNModel(double kappa, double r, double[] baseFrequencies) {
		return SubstitutionModel.Utils.createSubstitutionModel(new TN(kappa,r, baseFrequencies));
	}
	/**
	 * Create an Tamura-Nei model of substitution
	 * @param kappa transition/transversion rate ratio
	 * @param r pyrimidine/purin transition rate ratio
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related ratematrix
	 */
	public static final RateMatrix createTNMatrix(double kappa, double r, double[] baseFrequencies) {
		return new TN(kappa,r,baseFrequencies);
	}
	/**
	 * Create an GTR model of substitution
	 * @param a entry in rate matrix
	 * @param b entry in rate matrix
	 * @param c entry in rate matrix
	 * @param d entry in rate matrix
	 * @param e entry in rate matrix
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related substitution model
	 * @note matrix organised
	 *  <code>
	 *      . a b c
	 *      - . d e
	 *      - - . 1
	 *      - - - .
	 *  </code>
	 */
	public static final SubstitutionModel createGTRModel(double a, double b, double c, double d, double e, double[] baseFrequencies) {
		return SubstitutionModel.Utils.createSubstitutionModel(new GTR(a,b,c,d,e, baseFrequencies));
	}
	/**
	 * Create an GTR model of substitution
	 * @param a entry in rate matrix
	 * @param b entry in rate matrix
	 * @param c entry in rate matrix
	 * @param d entry in rate matrix
	 * @param e entry in rate matrix
	 * @param baseFrequencies The equilibrium frequencies of the four nucleotide bases (ordered A, G, C, T)
	 * @return The related ratematrix
	 * @note matrix organised
	 *  <code>
	 *      . a b c
	 *      - . d e
	 *      - - . 1
	 *      - - - .
	 *  </code>
	 */
	public static final RateMatrix createGTRMatrix(double a, double b, double c, double d, double e, double[] baseFrequencies) {
		return new GTR(a,b,c,d,e,baseFrequencies);
	}

	/**
	 * Create an base Yang Codon model (M0) of substitution
	 * @param kappa transition/transversion rate ratio
	 * @param omega non-synonymous/synonymous rate ratio
	 * @param baseFrequencies The equilibrium frequencies of the 64 codon bases (zero for stop codons please)
	 * @return The related substitution model
	 * @note using universal codon table
	 */
	public static final SubstitutionModel createM0YangCodonModel(double kappa, double omega, double[] baseFrequencies) {
		return SubstitutionModel.Utils.createSubstitutionModel(new YangCodonModel(omega, kappa, baseFrequencies));
	}
	/**
	 * Create an neutral Yang Codon model (M1) of substitution
	 * @param kappa transition/transversion rate ratio
	 * @param p0 The proporition under purifying selection
	 * @param baseFrequencies The equilibrium frequencies of the 64 codon bases (zero for stop codons please)
	 * @return The related substitution model
	 * @note using universal codon table
	 */
	public static final SubstitutionModel createM1YangCodonModel(double kappa, double p0, double[] baseFrequencies) {
		return new YangCodonModel.SimpleNeutralSelection( CodonTableFactory.createUniversalTranslator(), baseFrequencies, kappa, p0 );
	}
	/**
	 * Create an Positive Yang Codon model (M2) of substitution
	 * @param kappa transition/transversion rate ratio
	 * @param p0 The proporition under purifying selection
	 * @param p1 The proporition under neutral selection
	 * @param omega The free omega
	 * @param baseFrequencies The equilibrium frequencies of the 64 codon bases (zero for stop codons please)
	 * @return The related substitution model
	 * @note using universal codon table
	 */
	public static final SubstitutionModel createM2YangCodonModel(double kappa, double p0, double p1, double omega, double[] baseFrequencies) {
		return new YangCodonModel.SimplePositiveSelection( CodonTableFactory.createUniversalTranslator(), baseFrequencies, kappa, omega, p0, p1  );
	}


}