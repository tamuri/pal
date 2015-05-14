// YangCodonModel.java
//
// (c) 1999-2001 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

import pal.misc.*;
import pal.datatype.*;
import pal.util.*;
import pal.mep.*;
import pal.math.*;

import java.io.*;

/**
 * Yang's model of codon evolution
 *
 * More advanced codon Substitution Models (of Neilson and Yang) are now included (the M1, and M2 models).
 * They appear to be correct compare to PAML for the purposes of evaluating the likelihood. More models (eg M3 and others)
 * will be added over time.
 *
 * @version $Id: YangCodonModel.java,v 1.25 2004/10/18 01:45:40 matt Exp $
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Matthew Goode
 *
 */
public class YangCodonModel extends CodonModel implements Serializable, XMLConstants {
	public static final double MAXIMUM_OMEGA = 100;
	public static final double MAXIMUM_KAPPA = 100;
	public static final double MINIMUM_OMEGA = 0.000000;
	public static final double MINIMUM_KAPPA = 0.000001;

	public static final double DEFAULT_KAPPA = 2;
	public static final double DEFAULT_OMEGA = 1;

	public static final int KAPPA_PARAMETER = 0;
	public static final int OMEGA_PARAMETER = 1;


	//
	// Private stuff
	//

	private boolean showSE;
	private double kappa, omega;
	private double kappaSE, omegaSE;
	private byte[] rateMap;

	// genetic code used to figure out stop codons
	private CodonTable codonTable;

	//
	// Serialization Code
	//
	private static final long serialVersionUID=-3955993899328983304L;

	//Used by the unscaled stuff
	private final double[] parameterStore_ = new double[2];
	/**
	 * constructor 1
	 *
	 * @param omega N/S rate ratio
	 * @param kappa transition/transversion rate ratio
	 * @param freq codon frequencies
	 * @param codonTable codon table
	 */
	public YangCodonModel( double omega, double kappa, double[] freq,
												 CodonTable codonTable ) {
		super( freq );
		this.kappa = kappa;
		this.omega = omega;
		this.codonTable = codonTable;
		setParameters(new double[] {kappa,omega});
		showSE = false;
	}

	/**
	 * constructor 2 (universal codon table)
	 *
	 * @param omega N/S rate ratio
	 * @param kappa transition/transversion rate ratio
	 * @param freq codon frequencies
	 */
	public YangCodonModel( double omega, double kappa, double[] freq ) {
		this( omega, kappa, freq, CodonTableFactory.createUniversalTranslator() );
	}

	/**
	 * constructor 4 (universal codon table)
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 */
	public YangCodonModel( double[] params, double[] freq ) {
		this( params[0], params[1], freq, CodonTableFactory.createUniversalTranslator() );
	}

	/**
	 * constructor 3
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 * @param codonTable codon table
	 */
	public YangCodonModel( double[] params, double[] freq,
												 CodonTable codonTable ) {
		this( params[0], params[1], freq, codonTable );
	}

	// Get numerical code describing the model type
	public int getModelID() {
		return 0;
	}

	// interface Report

	public void report( PrintWriter out ) {
		out.println( "Model of substitution: YANG (Yang, ????)" );

		out.print( "Parameter kappa: " );
		format.displayDecimal( out, kappa, 2 );
		if( showSE ) {
			out.print( "  (S.E. " );
			format.displayDecimal( out, kappaSE, 2 );
			out.print( ")" );
		}
		out.println();

		out.print( "Parameter omega: " );
		format.displayDecimal( out, omega, 2 );
		if( showSE ) {
			out.print( "  (S.E. " );
			format.displayDecimal( out, omegaSE, 2 );
			out.print( ")" );
		}
		out.println();

		printFrequencies( out );
		printRatios( out );
	}

	// interface Parameterized

	public int getNumParameters() {
		return 2;
	}



	public void setParameterSE( double paramSE, int n ) {
		switch( n ) {
			case KAPPA_PARAMETER: {	kappaSE = paramSE; break; }
			case OMEGA_PARAMETER: {	omegaSE = paramSE; break; }
			default: throw new IllegalArgumentException();
		}
		showSE = true;
	}

	public final double getKappaLowerLimit() {	return MINIMUM_KAPPA; }
	public final double getOmegaLowerLimit() {	return MINIMUM_OMEGA; }
	public final double getKappaUpperLimit() {	return MAXIMUM_KAPPA; }
	public final double getOmegaUpperLimit() {  return MAXIMUM_OMEGA; }
	public final double getKappaDefaultValue() {return DEFAULT_KAPPA; }
	public final double getOmegaDefaultValue() {	return DEFAULT_OMEGA; }

	public final double getOmega() { return omega; }
	public final double getKappa() { return kappa; }
	public final void setKappaSE( double value ) { kappaSE = value; }
	public final void setOmegaSE( double value ) { omegaSE = value; }
	public final void setKappa( double value ) { setParameter(value, KAPPA_PARAMETER);  }
	public final void setOmega( double value ) { setParameter(value, OMEGA_PARAMETER); }

	public double getLowerLimit( int n ) { return n==KAPPA_PARAMETER ? MINIMUM_KAPPA : MINIMUM_OMEGA; }
	public double getUpperLimit( int n ) { return n==KAPPA_PARAMETER ? MAXIMUM_KAPPA : MAXIMUM_OMEGA; }
	public double getDefaultValue( int n ) { return n==KAPPA_PARAMETER ? DEFAULT_KAPPA : DEFAULT_OMEGA; }
	public String getParameterName( int i ) {
		switch( i ) {
			case KAPPA_PARAMETER:	{ return KAPPA; }
			case OMEGA_PARAMETER:	{ return OMEGA; }
			default:return UNKNOWN;
		}
	}
	public String getUniqueName() { 	return YANG_CODON_MODEL;	}

	// Make REV model
	protected void rebuildRateMatrix(double[][] rate, double[] parameters) {
		this.kappa = parameters[KAPPA_PARAMETER];
		this.omega = parameters[OMEGA_PARAMETER];
		int dimension = getDimension();
		int numRates = ( dimension*( dimension-1 ) )/2;
		rateMap = new byte[numRates];

//	NewArray(rateMap, char, numRates);

		int u, v, rateClass;
		char aa1, aa2;
		char[] codon1, codon2;

		for( u = 0; u<dimension; u++ ) {
			codon1 = Codons.getNucleotidesFromCodonIndex( u );

			for( v = u+1; v<dimension; v++ ) {
				codon2 = Codons.getNucleotidesFromCodonIndex( v );

				rateClass = -1;
				if( codon1[0]!=codon2[0] ) {
					if( ( codon1[0]=='A'&&codon2[0]=='G' )||
							( codon1[0]=='G'&&codon2[0]=='A' )|| // A <-> G
							( codon1[0]=='C'&&codon2[0]=='T' )||
							( codon1[0]=='T'&&codon2[0]=='C' ) ) { // C <-> T
						rateClass = 1; // Transition
					} else {
						rateClass = 2; // Transversion
					}
				}
				if( codon1[1]!=codon2[1] ) {
					if( rateClass==-1 ) {
						if( ( codon1[1]=='A'&&codon2[1]=='G' )||
								( codon1[1]=='G'&&codon2[1]=='A' )|| // A <-> G
								( codon1[1]=='C'&&codon2[1]=='T' )||
								( codon1[1]=='T'&&codon2[1]=='C' ) ) { // C <-> T
							rateClass = 1; // Transition
						} else {
							rateClass = 2; // Transversion
						}
					} else {
						rateClass = 0; // Codon changes at more than one position
					}
				}
				if( codon1[2]!=codon2[2] ) {
					if( rateClass==-1 ) {
						if( ( codon1[2]=='A'&&codon2[2]=='G' )||
								( codon1[2]=='G'&&codon2[2]=='A' )|| // A <-> G
								( codon1[2]=='C'&&codon2[2]=='T' )||
								( codon1[2]=='T'&&codon2[2]=='C' ) ) { // C <-> T
							rateClass = 1; // Transition
						} else {
							rateClass = 2; // Transversion
						}
					} else {
						rateClass = 0; // Codon changes at more than one position
					}
				}

				if( rateClass!=0 ) {
					aa1 = codonTable.getAminoAcidChar( codon1 );
					aa2 = codonTable.getAminoAcidChar( codon2 );
					if( aa1==AminoAcids.TERMINATE_CHARACTER||aa2==AminoAcids.TERMINATE_CHARACTER ) {
						rateClass = 0; // Can't change to a stop codon
					} else if( aa1!=aa2 ) {
						rateClass += 2; // Is a non-synonymous change
					}
				}

				switch( rateClass ) {
					case 0:
						rate[u][v] = 0.0; break; // codon changes in more than one codon position
					case 1:
						rate[u][v] = kappa; break; // synonymous transition
					case 2:
						rate[u][v] = 1.0; break; // synonymous transversion
					case 3:
						rate[u][v] = kappa*omega; break; // non-synonymous transition
					case 4:
					rate[u][v] = omega; break; // non-synonymous transversion
				}
				rate[v][u] = rate[u][v];
			}
			rate[u][u] = 0.0;
		}
	}
	/**
	 * Used by the more complex models to assist in adjust branch length scaling correctly
	 * @param kappa The kappa value
	 * @param omega The omega value
	 * @return The current expected number of substitutions per time unit
	 */
	private final double setParametersIncomplete( double kappa, double omega ) {
		parameterStore_[KAPPA_PARAMETER] = kappa;
		parameterStore_[OMEGA_PARAMETER] = omega;
		return setParametersNoScale(parameterStore_);
	}
	/**
	 * Finish the process started by setParametersIncomplete();
	 * @param substitutionScale The total number of expected number of substitutions per time unit
			*/
	private final void finishSetParameters( double substitutionScale ) {
		scale( substitutionScale );
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		report( new PrintWriter( sw ) );
		return sw.toString();
	}

// -==--=-=-=-=-=-=-==--=-==--=-=-=-==-=--=-=-==--=-=-==--=-=-=-=-=-=-=-=-==-=--==-=-=-=--=-=-=-==--==-
// -==--=-=-=-=-=-=-==--=-==--=-=-=-==-=--=-=-==--=-=-==--=-=-=-=-=-=-=-=-==-=--==-=-=-=--=-=-=-==--==-
// -==--=-=-=-=-=-=-==--=-==--=-=-=-==-=--=-=-==--=-=-==--=-=-=-=-=-=-=-=-==-=--==-=-=-=--=-=-=-==--==-

	//===================================
	//=========== Static classes ========
	//===================================

	/**
	 * A Utility class
	 */
	public static final class Utils {

		/**
		 * Probably of no use to anyone else (is used by internal code though)
		 */
		public static final YangCodonModel[] getCopy( YangCodonModel[] toCopy ) {
			if( toCopy==null ) {
				return null;
			}
			YangCodonModel[] copy = new YangCodonModel[toCopy.length];
			for( int i = 0; i<copy.length; i++ ) {
				copy[i] = ( YangCodonModel )toCopy[i].clone();
			}
			return copy;
		}
	}

// -==--=-=-=-=-=-=-==--=-==--=-=-=-==-=--=-=-==--=-=-==--=-=-=-=-=-=-=-=-==-=--==-=-=-=--=-=-=-==--==-

	//===================================
	//=========== SubstituionModels =====
	//===================================
	//NeilsonYang stuff
	/**
	 * A Substitution Model which can be used to implment the Postitive Selection (with out continuous rate stuff)
	 * Codon model of [1] which uses the weighted sum of a three base Codon model where
	 * omega=0, omega=1 and omega=free
	 * <br>
	 * [1] Nielsen, R., Yang Z., 1998  Likelihood Models for Detecting Positively Selected Amino Acid Sites and
	 * Applications to the HIV-1 Envelope Gene. Genetics <b>148:</b> 929-936.
	 */
	public static class SimplePositiveSelection extends PalObjectListener.EventGenerator implements SubstitutionModel {

		private static final double MINIMUM_PROPORTION = 0;
		private static final double MAXIMUM_PROPORTION = 1;

		private YangCodonModel[] baseMatrixes_;
		private double p0_ = 0.5, p1_ = 0.5, p2_ = 0.5;
		private double[] probabilities_;
		private transient boolean needsRebuild_ = true;

		private double kappa_;
		private double freeOmega_;

		private static final long serialVersionUID = -7826700615445839100L;
		private static final int NUMBER_OF_CLASSES = 3;
		private void writeObject( java.io.ObjectOutputStream out ) throws java.io.IOException {
			out.writeByte( 1 ); //Version number
			out.writeObject( baseMatrixes_ );
			out.writeDouble( p0_ );
			out.writeDouble( p1_ );
			out.writeDouble( p2_ );
			out.writeDouble( kappa_ );
			out.writeDouble( freeOmega_ );
		}

		private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
			byte version = in.readByte();
			switch( version ) {
				default: {
					this.probabilities_ = new double[NUMBER_OF_CLASSES];
					baseMatrixes_ = ( YangCodonModel[] )in.readObject();
					p0_ = in.readDouble();
					p1_ = in.readDouble();
					p2_ = in.readDouble();
					this.kappa_ = in.readDouble();
					this.freeOmega_ = in.readDouble();
					scheduleRebuild();
					break;
				}
			}
		}

		protected SimplePositiveSelection( SimplePositiveSelection toCopy ) {
			this.baseMatrixes_ = YangCodonModel.Utils.getCopy( toCopy.baseMatrixes_ );
			this.probabilities_ = new double[NUMBER_OF_CLASSES];
			this.p0_ = toCopy.p0_;
			this.p1_ = toCopy.p1_;
			this.freeOmega_ = toCopy.freeOmega_;
			this.kappa_ = toCopy.kappa_;
			scheduleRebuild();
		}

		public SimplePositiveSelection( CodonTable translator, double[] codonProbabilities, double startingKappa, double startingFreeOmega) {
			this( translator, codonProbabilities,  startingKappa,startingFreeOmega, 0.5, 0.5, 0.5 );
		}

		public SimplePositiveSelection( CodonTable translator, double[] codonProbabilities, double startingKappa, double startingFreeOmega, double p0, double p1 ) {
			this(translator, codonProbabilities, startingKappa, startingFreeOmega, p0, p1, (1-p0-p1));

		}
		public SimplePositiveSelection( CodonTable translator, double[] codonProbabilities, double startingKappa, double startingFreeOmega, double p0, double p1, double p2 ) {
			this.baseMatrixes_ = new YangCodonModel[NUMBER_OF_CLASSES];
			this.probabilities_ = new double[NUMBER_OF_CLASSES];
			setTransitionCategoryProbabilities( p0, p1, p2 );
			this.kappa_ = startingKappa;
			this.freeOmega_ = startingFreeOmega;
			for(int i = 0 ; i < NUMBER_OF_CLASSES ;i++) {
				//Initial values are not important as they will be reconstructed by rebuild();
				baseMatrixes_[i] = 	new YangCodonModel(1,1,codonProbabilities,translator);
			}
			scheduleRebuild();
		}

		public Object clone() {
			return new SimplePositiveSelection( this );
		}

		public SubstitutionModel getCopy() {
			return new SimplePositiveSelection( this );
		}

		private final void scheduleRebuild() {
			this.needsRebuild_ = true;
		}

		private final void check() {
			if( needsRebuild_ ) {
				rebuild();needsRebuild_ = false;
			}
		}
		private final void rebuild() {
			double total = p0_+p1_+p2_;
			if( total==0 ) {
				probabilities_[0] = 0.33; probabilities_[1] = 0.33; probabilities_[2] = 0.34;
			} else {
				probabilities_[0] = p0_/total; probabilities_[1] = p1_/total; probabilities_[2] = p2_/total;
			}
			//Speciallised scaling to make sure it's scaled to one substitution per time unit overall
			double x1 = baseMatrixes_[0].setParametersIncomplete( kappa_, 0 );
			double x2 = baseMatrixes_[1].setParametersIncomplete( kappa_, 1 );
			double x3 = baseMatrixes_[2].setParametersIncomplete( kappa_, freeOmega_ );

			double scale = x1*probabilities_[0]+probabilities_[1]*x2+probabilities_[2]*x3;

			baseMatrixes_[0].finishSetParameters( scale );
			baseMatrixes_[1].finishSetParameters( scale );
			baseMatrixes_[2].finishSetParameters( scale );
		}
		public DataType getDataType() {	return baseMatrixes_[0].getDataType();	}

		/**
		 * @return 3
		 */
		public int getNumberOfTransitionCategories() { return NUMBER_OF_CLASSES; 	}

		public double getTransitionCategoryProbability( int category ) {
			check();
			return probabilities_[category];
		}

		public double[] getTransitionCategoryProbabilities() {
			check();
			return probabilities_;
		}

		public double[] getEquilibriumFrequencies() {
			return baseMatrixes_[0].getEquilibriumFrequencies();
		}

		public void getTransitionProbabilities( double branchLength, double[][][] tableStore ) {
			check();
			for( int i = 0; i< NUMBER_OF_CLASSES; i++ ) {
				baseMatrixes_[i].setDistance( branchLength );
				baseMatrixes_[i].getTransitionProbabilities( tableStore[i] );
			}
		}

		/**
		 * Table is organized as [tree_group][from][to]
		 */
		public void getTransitionProbabilities( double branchLength, int category, double[][] tableStore ) {
			check();
			baseMatrixes_[category].setDistance( branchLength );
			baseMatrixes_[category].getTransitionProbabilities( tableStore );
		}

		/**
		 * Table is organized as [tree_group][to][from]
		 */
		public void getTransitionProbabilitiesTranspose( double branchLength, double[][][] tableStore ) {
			check();
			for( int i = 0; i< NUMBER_OF_CLASSES; i++ ) {
				baseMatrixes_[i].setDistanceTranspose( branchLength );
				baseMatrixes_[i].getTransitionProbabilities( tableStore[i] );
			}
		}

		/**
		 * Table is organized as [to][from]
		 */
		public void getTransitionProbabilitiesTranspose( double branchLength, int category, double[][] tableStore ) {
			check();
			baseMatrixes_[category].setDistanceTranspose( branchLength );
			baseMatrixes_[category].getTransitionProbabilities( tableStore );
		}
		/**
		 * We use three parameters instead of two to make opimisation easier (there are effectively only *two* parameters though)
		 * @param p0
		 * @param p1
		 * @param p2
		 */
		public final void setTransitionCategoryProbabilities( double p0, double p1, double p2 ) {
			this.p0_ = p0;	this.p1_ = p1;	this.p2_ = p2;	scheduleRebuild();
		}

		/**
		 * Five parameters, three proportions, kappa, omega.
		 * Even though the probabilities could be represented by two parameters we use three for ease of optimisation
		 */
		public int getNumParameters() {	return 5; 	}

		public void setParameter( double param, int n ) {
			switch( n ) {
				case 0: {	this.kappa_ = param;	break;		}
				case 1: { this.freeOmega_ = param; break; }
				case 2: {	this.p0_ = param; break; }
				case 3: {	this.p1_ = param; break; }
				default: { this.p2_ = param;break;
				}
			}
			scheduleRebuild();
			fireParametersChangedEvent();
		}

		public double getParameter( int n ) {
			switch( n ) {
				case 0: {	return kappa_; }
				case 1: {	return freeOmega_; }
				case 2: {	return p0_; }
				case 3: {	return p1_; }
				default: {return p2_; }
			}
		}

		public void setParameterSE( double paramSE, int n ) {
			System.out.println("Not implemented yet...");
		}

		public double getLowerLimit( int n ) {
			switch( n ) {
				case 0: {	return MINIMUM_KAPPA; }
				case 1: {	return MINIMUM_OMEGA; }
				default: { return MINIMUM_PROPORTION; }
			}
		}

		public double getUpperLimit( int n ) {
			switch( n ) {
				case 0: {	return MAXIMUM_KAPPA; }
				case 1: {	return MAXIMUM_OMEGA; }
				default: { return MAXIMUM_PROPORTION; }
			}
		}

		public double getDefaultValue( int n ) {
			switch( n ) {
				case 0: {	return DEFAULT_KAPPA; }
				case 1: {	return DEFAULT_OMEGA; }
				default: {	return 0.5; }
			}
		}
		public OrthogonalHints getOrthogonalHints() {	return null; }
		public String toString() {
			check();
			StringBuffer sb = new StringBuffer();
			sb.append( "Simple Positive Selection Model [1]\n" );
			sb.append( "p0 = "+probabilities_[0]+"\n" );
			sb.append( "p1 = "+probabilities_[1]+"\n" );
			sb.append( "p2 = "+probabilities_[2]+"\n" );
			sb.append( "Free Omega = "+freeOmega_+"\n" );
			sb.append( "Kappa = "+kappa_+"\n" );
			sb.append(
				"\n[1] Nielsen, R., Yang Z., 1998  Likelihood Models for Detecting Positively Selected "+
				"Amino Acid Sites and Applications to the HIV-1 Envelope Gene. Genetics 148: 929-936."
				);
			return sb.toString();
		}

		public void report( java.io.PrintWriter pw ) {	pw.print( toString() );	}
	}

// -=-=-=-==--=-=-=-=-==--==--=-=-=-=-==--==--==-=-==--=-=-=-=-=-==================-=-=-=-=-==--=-==--==-=--=-=-=-=-=-=

	/**
	 * A Substitution Model which can be used to implment the Neutral Model (with out continuous rate stuff)
	 * Codon model of [1] which uses the weighted sum of trwo base YangCodon models where
	 * omega=0, omega=1 repectively
	 * <br>
	 * [1] Nielsen, R., Yang Z., 1998  Likelihood Models for Detecting Positively Selected Amino Acid Sites and
	 * Applications to the HIV-1 Envelope Gene. Genetics <b>148:</b> 929-936.
	 */
	public static class SimpleNeutralSelection extends PalObjectListener.EventGenerator implements SubstitutionModel {
		public static final double P_UPPER_LIMIT = 1;
		public static final double P_LOWER_LIMIT = 0;
		public static final double P_DEFAULT_VALUE = 0.5;

		private YangCodonModel[] baseMatrixes_;
		private double p_ = 0.5;
		private double kappa_ = 2;
		private double[] probabilities_;
		private transient boolean needsRebuild_ = true;

		private SimpleNeutralSelection( SimpleNeutralSelection toCopy ) {
			this.baseMatrixes_ = YangCodonModel.Utils.getCopy( toCopy.baseMatrixes_ );
			this.p_ = toCopy.p_;
			this.kappa_ = toCopy.kappa_;
			this.probabilities_ = new double[2];
			scheduleRebuild();
		}

		public SimpleNeutralSelection( CodonTable translator, double[] codonProbabilities, double startingKappa ) {
			this( translator, codonProbabilities, startingKappa, 0.5 );
		}

		public SimpleNeutralSelection( CodonTable translator, double[] codonProbabilities, double startingKappa, double proportionZero ) {
			this.probabilities_ = new double[2];
			this.p_ = proportionZero;
			this.baseMatrixes_ = new YangCodonModel[] {
													 new YangCodonModel( 0, startingKappa, codonProbabilities, translator ),
													 new YangCodonModel( 1, startingKappa, codonProbabilities, translator )
			};
			this.kappa_ = startingKappa;
			scheduleRebuild();
		}

		private final void scheduleRebuild() {
			this.needsRebuild_ = true; }

		private final void check() {
			if( needsRebuild_ ) {
				rebuild();
				needsRebuild_ = false;
			}
		}

		private final void rebuild() {
			//Speciallised scaling to make sure it's scaled to one substitution per time unit
			probabilities_[0] = p_;
			probabilities_[1] = ( 1-p_ );
			double x1 = baseMatrixes_[0].setParametersIncomplete( kappa_, 0 );
			double x2 = baseMatrixes_[1].setParametersIncomplete( kappa_, 1 );
			double scale = x1*p_+( 1-p_ )*x2;
			baseMatrixes_[0].finishSetParameters( scale );
			baseMatrixes_[1].finishSetParameters( scale );
		}

		public Object clone() {
			return new SimpleNeutralSelection( this ); }

		public double[] getEquilibriumFrequencies() {
			return baseMatrixes_[0].getEquilibriumFrequencies(); }

		public SubstitutionModel getCopy() {
			return new SimpleNeutralSelection( this ); }

		public double[] getEquilibriumProbabilities() {
			return probabilities_; }

		public DataType getDataType() {
			return baseMatrixes_[0].getDataType(); }

		public int getNumberOfTransitionCategories() {
			return 2; }

		public double getTransitionCategoryProbability( int category ) {
			check(); return probabilities_[category]; }

		public double[] getTransitionCategoryProbabilities() {
			check(); return probabilities_; }

		/**
		 * Table is organized as [tree_group][from][to]
		 */
		public void getTransitionProbabilities( double branchLength, double[][][] tableStore ) {
			check();
			baseMatrixes_[0].setDistance( branchLength ); baseMatrixes_[0].getTransitionProbabilities( tableStore[0] );
			baseMatrixes_[1].setDistance( branchLength ); baseMatrixes_[1].getTransitionProbabilities( tableStore[1] );
		}

		public void getTransitionProbabilities( double branchLength, int category, double[][] tableStore ) {
			check();
			baseMatrixes_[category].setDistance( branchLength ); baseMatrixes_[category].getTransitionProbabilities( tableStore );
		}

		public void getTransitionProbabilitiesTranspose( double branchLength, double[][][] tableStore ) {
			check();
			baseMatrixes_[0].setDistanceTranspose( branchLength ); baseMatrixes_[0].getTransitionProbabilities( tableStore[0] );
			baseMatrixes_[1].setDistanceTranspose( branchLength ); baseMatrixes_[1].getTransitionProbabilities( tableStore[1] );

		}

		public void getTransitionProbabilitiesTranspose( double branchLength, int category, double[][] tableStore ) {
			check();
			baseMatrixes_[category].setDistanceTranspose( branchLength ); baseMatrixes_[category].getTransitionProbabilities( tableStore );
		}

		/**
		 * Two parameters, kappa, p,
		 */
		public int getNumParameters() {
			return 2; }

		public void setParameter( double param, int n ) {
			if( n==0 ) {
				kappa_ = param; } else {
				p_ = param; }
			scheduleRebuild();
			fireParametersChangedEvent();
		}

		public double getParameter( int n ) {
			if( n==0 ) {
				return kappa_; }
			return p_;
		}

		public void setParameterSE( double paramSE, int n ) {
			System.out.println( "Not implemented yet..." ); }

		public double getLowerLimit( int n ) {
			if( n==0 ) {
				return baseMatrixes_[0].getKappaLowerLimit(); }
			return P_LOWER_LIMIT;
		}

		public double getUpperLimit( int n ) {
			if( n==0 ) {
				return baseMatrixes_[0].getKappaUpperLimit(); }
			return P_UPPER_LIMIT;
		}

		public double getDefaultValue( int n ) {
			if( n==0 ) {
				return baseMatrixes_[0].getKappaUpperLimit(); }
			return P_DEFAULT_VALUE;
		}

		public OrthogonalHints getOrthogonalHints() {
			return null; }

		public String toString() {
			check();
			StringBuffer sb = new StringBuffer();
			sb.append( "Simple Neutral Model [1]\n" );
			sb.append( "p0 = "+probabilities_[0]+"\n" );
			sb.append( "p1 = "+probabilities_[1]+"\n" );
			sb.append( "Kappa = "+kappa_+"\n" );
			sb.append(
				"\n[1] Nielsen, R., Yang Z., 1998  Likelihood Models for Detecting Positively Selected "+
				"Amino Acid Sites and Applications to the HIV-1 Envelope Gene. Genetics 148: 929-936."
				);
			return sb.toString();
		}

		public void report( java.io.PrintWriter pw ) {
			java.text.MessageFormat mf;
			pw.print( toString() );
		}
	}
// -=-=-=-==--=-=-=-=-==--==--=-=-=-=-==--==--==-=-==--=-=-=-=-=-==================-=-=-=-=-==--=-==--==-=--=-=-=-=-=-=
	public static final MutableDouble createKappaStore(double initialValue) {
		return createKappaStore(initialValue,"Kappa");
	}
	public static final MutableDouble createKappaStore(double initialValue, String name) {
		return new MutableDouble(initialValue, DEFAULT_KAPPA,MINIMUM_KAPPA,MAXIMUM_KAPPA,name);
	}
	public static final MutableDouble createOmegaStore(double initialValue) {
		return createOmegaStore(initialValue,"Omega");
	}
	public static final MutableDouble createOmegaStore(double initialValue, String name) {
		return new MutableDouble(initialValue, DEFAULT_OMEGA,MINIMUM_OMEGA,MAXIMUM_OMEGA,name);
	}
}
