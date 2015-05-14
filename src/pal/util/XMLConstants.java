// XMLConstants.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.util;

/**
 * constants (strings and numbers) relating to reading and writing XML.
 * These constants should avoid the use of &lt; and &gt; and other
 * special XML characters as much as possible.
 *
 * @author Alexei Drummond
 * @version $Id: XMLConstants.java,v 1.11 2002/06/03 09:18:04 alexi Exp $
 */
public interface XMLConstants {
	
	// NUMBERS

	int SPACES_PER_LEVEL = 4;
	
	// TAG NAMES
	
	String DEMOGRAPHIC_MODEL = "demographicmodel";
	String MUTATION_RATE_MODEL = "mutationratemodel";
	String RATE_MATRIX = "ratematrix";
	String TREE = "tree";
	String FREQUENCIES = "frequencies";
	String PARAMETER = "parameter";
	String NODE = "node";
	String EDGE = "edge";
	String ALIGNMENT = "alignment";
	String SEQUENCE = "sequence";
	String TIME = "time";
	String TIME_DATA = "timedata";
	String ATTRIBUTE = "att";
		
	// ATTRIBUTE NAMES
	
	String TYPE = "type";
	String NAME = "name";
	String VALUE = "value";
	String UNITS = "units";
	String MODEL = "model";
	String ID = "id";
	String HEIGHT = "height";
	String LENGTH = "length";
	String DATA_TYPE = "datatype";
	String DATA_TYPE_ID = "datatypeid";
	String DIRECTION = "direction";
	String ORIGIN = "origin";
	String MISSING = "missing";
	
	// ATTRIBUTE VALUES
	
	String COALESCENT = "coalescent";
	
	/** constant population demographic model type */
	String CONSTANT_POPULATION = "constant";
	String EXPONENTIAL_GROWTH = "exponential";
	String CONST_EXP_GROWTH = "constexp";
	String EXPANDING_POPULATION = "expanding";
	String CONST_EXP_CONST = "constexpconst";
	
	String CONSTANT_MUTATION_RATE = "constant";
	String STEPPED_MUTATION_RATE = "stepped";
	
	String UNIFORM = "uniform";

	String MUTATION_RATE = "current mutation rate";
	String ANCESTRAL_MU_RATE = "ancestral mutation rate";
	String MU_STEP_TIME = "step time";

	String POPULATION_SIZE = "current population size";
	String GROWTH_RATE = "growth rate";
	String ALPHA = "alpha";
	String ANCESTRAL_POP_SIZE = "ancestral population size";
	String CURRENT_POP_SIZE_DURATION = "tx";
	String GROWTH_PHASE_DURATION = "lx";
	
	
	String GENERATIONS = "generations";
	String DAYS = "days";
	String MONTHS = "months";
	String YEARS = "years";
	String MUTATIONS = "mutations";

	String TWO_STATE = "binary";
	String JC = "JC";
	String F81 = "F81";
	String F84 = "F84";
	String HKY = "HKY";
	String GTR = "GTR";
	String TN = "Tamura-Nei";
	String WAG = "WAG";
	String JTT = "JTT";
	String VT = "VT";
	String CPREV = "CPREV";
	String BLOSUM62 = "BLOSUM62";
	String MTREV24 = "MTREV24";
	String DAYHOFF = "Dayhoff";
	String YANG_CODON_MODEL = "Yang codon model";

	String A_TO_C = "A-C";
	String A_TO_G = "A-G";
	String A_TO_T = "A-T";
	String C_TO_G = "C-G";
	String C_TO_T = "C-T";
	String G_TO_T = "G-T";
	String KAPPA = "kappa";
	String OMEGA = "omega";
	String PYRIMIDINE_PURINE_RATIO = "prymidine/purine transition ratio";
	String TS_TV_RATIO = "transition/transversion ratio";
	
	String UNKNOWN = "unknown";
	String BACKWARDS = "backwards";

	// ***************************************************************************
	// RATE DISTRIBUTION CONSTANTS 
	// ***************************************************************************
	
	String UNIFORM_RATE_DISTRIBUTION = "uniform";
	String GAMMA_DISTRIBUTION = "gamma";
	String RATE_DISTRIBUTION = "ratedistribution";
	String NUMBER_CATEGORIES = "ncat";
	String GAMMA_ALPHA = "shape";
	
}
