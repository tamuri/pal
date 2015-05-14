// ElementParser.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.xml;

import org.w3c.dom.*;
import pal.coalescent.ConstExpGrowth;
import java.util.*;
import java.io.Reader;
import pal.util.*;
import pal.misc.*;
import pal.alignment.*;
import pal.datatype.*;
import pal.tree.AttributeNode;



/**
 * This class provides static methods for parsing PAL objects
 * from DOM Elements. Methods are ordered in public, protected, private and
 * alphabetic within each group.
 *
 * @version $Id: ElementParser.java,v 1.14 2003/08/02 01:15:14 matt Exp $
 *
 * @author Alexei Drummond
 */
public class ElementParser implements XMLConstants {

	/**
	 * @return the first child element of the given name.
	 */
	public static Element getFirstByName(Element parent, String name) {
		NodeList nodes = parent.getElementsByTagName(name);
		if (nodes.getLength() > 0) {
			return (Element)nodes.item(0);
		} else return null;
	}

	/**
	 * Parses an alignment element and returns an alignment object.
	 */
	public static Alignment parseAlignmentElement(Element e) throws XmlParseException {
		pal.alignment.Alignment alignment = null;
		pal.datatype.DataType dataType = Nucleotides.DEFAULT_INSTANCE;
		String gaps = "-";
		validateTagName(e, ALIGNMENT);

		if (hasAttribute(e, MISSING)) {gaps = e.getAttribute(MISSING);}

		if (hasAttribute(e, DATA_TYPE_ID)) {
			String dataTypeId = e.getAttribute(DATA_TYPE_ID);
			dataType = DataType.Utils.getInstance(Integer.parseInt(dataTypeId));
		} else if (hasAttribute(e, DATA_TYPE)) {
			String dataTypeStr = e.getAttribute(DATA_TYPE);
			if (dataTypeStr.equals(DataType.NUCLEOTIDE_DESCRIPTION)) {
				dataType = Nucleotides.DEFAULT_INSTANCE;
			} else if (dataTypeStr.equals(DataType.AMINO_ACID_DESCRIPTION)) {
				dataType = AminoAcids.DEFAULT_INSTANCE;
			} else if (dataTypeStr.equals(DataType.CODON_DESCRIPTION)) {
				dataType = new Codons();
			} else if (dataTypeStr.equals(DataType.TWO_STATE_DESCRIPTION)) {
				dataType = new TwoStates();
			}
		}

		NodeList nodes = e.getElementsByTagName(SEQUENCE);
		String[] sequences = new String[nodes.getLength()];
		String[] names = new String[nodes.getLength()];
		for (int i = 0; i < sequences.length; i++) {
			Element sequence = (Element)nodes.item(i);
			names[i] = getNameAttr(sequence);
			sequences[i] = "";
			NodeList seqs = sequence.getChildNodes();
			for (int j = 0; j < seqs.getLength(); j++) {
				if (seqs.item(j) instanceof Text) {
					sequences[i] += ((Text)seqs.item(j)).getNodeValue();
				}
			}

		}
		alignment = new SimpleAlignment(new SimpleIdGroup(names), sequences, gaps,dataType);

		return alignment;
	}

	/**
	 * parses an attribute element.
	 */
	public static Attribute parseAttributeElement(Element e)  throws pal.xml.XmlParseException {
		String name = null;
		String value = null;
		String type = null;
		validateTagName(e, ATTRIBUTE);

		if (hasAttribute(e, NAME)) {
			name = e.getAttribute(NAME);
		} else throw new XmlParseException(ATTRIBUTE + " tags require a name attribute!");

		if (hasAttribute(e, VALUE)) {
			value = e.getAttribute(VALUE);
		} else throw new XmlParseException(ATTRIBUTE + " tags require a value attribute!");

		if (hasAttribute(e, TYPE)) {
			type = e.getAttribute(TYPE);
		}

		return new Attribute(name, value, type);
	}

	/**
	 * Parses an element from an DOM document into a DemographicModel. Recognises
	 * ConstantPopulation, ExponentialGrowth, ConstExpGrowth.
	 */
	public static pal.coalescent.DemographicModel parseDemographicModel(Element e) throws XmlParseException {
		pal.coalescent.ConstantPopulation model = null;
		int units = pal.misc.Units.GENERATIONS;
		double growthParam = 0.0;
		double populationSize = 1.0;
		double ancestral = 0.0;
		double tx = 0.0;
		int parameterization = ConstExpGrowth.ALPHA_PARAMETERIZATION;

		validateTagName(e, DEMOGRAPHIC_MODEL);
		units = getUnitsAttr(e);
		NodeList nodes = e.getElementsByTagName(PARAMETER);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element param = (Element)nodes.item(i);
			String name = getNameAttr(param);
			if (name.equals(POPULATION_SIZE)) { populationSize = getDoubleValue(param);}
			else if (name.equals(GROWTH_RATE)) { growthParam = getDoubleValue(param); }
			else if (name.equals(ALPHA)) { ancestral = getDoubleValue(param); }
			else if (name.equals(ANCESTRAL_POP_SIZE)) {
				ancestral = getDoubleValue(param);
				parameterization = parameterization | ConstExpGrowth.N1_PARAMETERIZATION;
			}
			else if (name.equals(CURRENT_POP_SIZE_DURATION)) {
				tx = getDoubleValue(param);
			}
			else if (name.equals(GROWTH_PHASE_DURATION)) {
				growthParam = getDoubleValue(param);
				System.out.println("Found LX=" + growthParam);
				parameterization = parameterization | ConstExpGrowth.LX_PARAMETERIZATION;
			}
		}

		String type = e.getAttribute(TYPE);
		if (type.equals(CONSTANT_POPULATION)) {
			model = new pal.coalescent.ConstantPopulation(populationSize, units);
		} else if (type.equals(EXPONENTIAL_GROWTH)) {
			model = new pal.coalescent.ExponentialGrowth(populationSize, growthParam, units);
		} else if (type.toLowerCase().equals(CONST_EXP_GROWTH)) {
			model = new pal.coalescent.ConstExpGrowth(
				populationSize, growthParam, ancestral, units, parameterization);
		} else if (type.toLowerCase().equals(CONST_EXP_CONST)) {
			model = new pal.coalescent.ConstExpConst(
				populationSize, growthParam, ancestral, tx, units, parameterization);

		} else if (type.toLowerCase().equals(EXPANDING_POPULATION)) {
			//ExpandingPopulation must have alpha parameterization!!
			if ((parameterization & ConstExpGrowth.N1_PARAMETERIZATION) > 0) {
				ancestral = ancestral / populationSize;
			}
			model = new pal.coalescent.ExpandingPopulation(
				populationSize, growthParam, ancestral, units);

		}
		return model;
	}

	/**
	 * @return a tree node parsed from an XML element.
	 */
	public static pal.tree.Node parseEdgeNodeElement(Element e) throws XmlParseException {
		pal.tree.Node node = null;
		validateTagName(e, EDGE);
		node = pal.tree.NodeFactory.createNode();

		if (hasAttribute(e, LENGTH)) {
			node.setBranchLength(Double.parseDouble(e.getAttribute(LENGTH)));
		}
		NodeList nodes = e.getChildNodes();
		int nodeCount = 0;
		for (int i =0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				Element element = (Element)nodes.item(i);
				if (element.getTagName().equals(NODE)) {
					if (nodeCount > 0) {
						throw new RuntimeException("Each edge should contain only 1 node!!");
					}
					parseNodeElement((Element)nodes.item(0), node);
					nodeCount += 1;
				}
			}
		}

		return node;
	}


	/**
	 * reads XML format of frequencies. <BR>
	 * e.g &lt;frequencies&gt;0.19 0.31 0.16 0.34 &lt;/frequencies&gt;.
	 * @returns an array of double representing the equilibrium base frequencies.
	 */
	public static final double[] parseFrequencies(Element element) throws XmlParseException {
		Vector freqs = new Vector();
		validateTagName(element, FREQUENCIES);

		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				String text = node.getNodeValue();
				StringTokenizer tokens = new StringTokenizer(text);
				while (tokens.hasMoreElements()) {
					String token = (String)tokens.nextElement();
					freqs.addElement(new Double(token));
				}
			}
		}
		double[] frequencies = new double[freqs.size()];
		for (int i =0 ; i < frequencies.length; i++) {
			frequencies[i] = ((Double)freqs.elementAt(i)).doubleValue();
		}
		return frequencies;
	}

	/**
	 * Reads a mutation rate model from a DOM Document element.
	 */
	public static pal.mep.MutationRateModel parseMutationRateModel(Element e) throws XmlParseException {
		pal.mep.MutationRateModel model = null;
		int units = pal.misc.Units.GENERATIONS;
		double mutationRate = 1.0;
		double stepTime = Double.MAX_VALUE;
		double ancestralRate = 0.0;
		validateTagName(e, MUTATION_RATE_MODEL);

		units = getUnitsAttr(e);
		if (units == pal.misc.Units.EXPECTED_SUBSTITUTIONS) {
			throw new RuntimeException("mutations rate can't be in mutation units!");
		}
		NodeList nodes = e.getElementsByTagName(PARAMETER);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element param = (Element)nodes.item(i);
			String name = getNameAttr(param);
			if (name.equals(MUTATION_RATE)) { mutationRate = getDoubleValue(param);}
			else if (name.equals(MU_STEP_TIME)) { stepTime = getDoubleValue(param); }
			else if (name.equals(ANCESTRAL_MU_RATE)) {
				ancestralRate = getDoubleValue(param);
			}
		}
		String type = e.getAttribute(TYPE);
		if (type.equals(CONSTANT_MUTATION_RATE)) {
			model = new pal.mep.ConstantMutationRate(mutationRate, units,1000);
		} else if (type.equals(STEPPED_MUTATION_RATE)) {
			double[] rates = new double[] {mutationRate, ancestralRate};
			double[] steps = new double[] {stepTime};
			model = new pal.mep.SteppedMutationRate(rates, steps, units,1000);
		}

		return model;
	}

	/**
	 * @return a tree node parsed from an XML element.
	 */
	public static pal.tree.Node parseNodeElement(Element e) throws XmlParseException {
		pal.tree.Node node = pal.tree.NodeFactory.createNode();
		parseNodeElement(e, node);
		return node;
	}

	/**
	 * Reads a rate matrix from a DOM Document element. Reads JC, F81, HKY, GTR
	 */
	public static pal.substmodel.RateMatrix parseRateMatrix(Element e) throws XmlParseException {
		return parseRateMatrix(e, null);
	}

	public static pal.substmodel.RateDistribution parseRateDistribution(Element e) throws XmlParseException {

		validateTagName(e, RATE_DISTRIBUTION);
		String type = e.getAttribute(TYPE);
		if (type.equals(UNIFORM_RATE_DISTRIBUTION)) {
			return new pal.substmodel.UniformRate();
		} else if (type.equals(GAMMA_DISTRIBUTION)) {
			double alpha = 1.0;
			int ncat = 4;
			NodeList nodes = e.getElementsByTagName(PARAMETER);
			System.out.println("Found " + nodes.getLength() + " parameters in rate distribution");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element param = (Element)nodes.item(i);
				String name = getNameAttr(param);
				if (name.equals(GAMMA_ALPHA)) {
					alpha = getDoubleValue(param);
					System.out.println("Found alpha=" + alpha);
				}
				if (name.equals(NUMBER_CATEGORIES)) {
					ncat = getIntegerValue(param);
					System.out.println("Found ncats=" + ncat);
				}
			}
			return new pal.substmodel.GammaRates(ncat, alpha);
		} else throw new XmlParseException("Unrecognized rate distribution type! Should be one of\n'" +
				UNIFORM_RATE_DISTRIBUTION + "', '" + GAMMA_DISTRIBUTION +"'." );
	}

	/**
	 * Reads a rate matrix from a DOM Document element. Reads JC, F81, HKY, GTR
	 */
	protected static pal.substmodel.RateMatrix parseRateMatrix(Element e, Alignment a) throws XmlParseException {
		pal.substmodel.RateMatrix rateMatrix = null;
		double[] frequencies = null;

		validateTagName(e, RATE_MATRIX);

		String type = e.getAttribute(MODEL);
		Element freqElement = getFirstByName(e, FREQUENCIES);

		if (type.equals(JC)) {
			if (freqElement != null) {
				throw new XmlParseException("Frequency sub-element not allowed in JC model!");
			}
			return new pal.substmodel.F81(new double[] {0.25, 0.25, 0.25, 0.25});
		}

		if (freqElement != null) {
			frequencies = parseFrequencies(freqElement);
		} else if (a != null) {
			frequencies = AlignmentUtils.estimateFrequencies(a);
		} else throw new XmlParseException("Must have either frequency element or an associated alignment!");

		if (type.equals(F81)) {
			rateMatrix = new pal.substmodel.F81(frequencies);
		} else if (type.equals(F84)) {
			rateMatrix = new pal.substmodel.F84(1.0, frequencies);
		} else if (type.equals(HKY)) {
			rateMatrix = new pal.substmodel.HKY(1.0, frequencies);
		} else if (type.equals(GTR)) {
			rateMatrix = new pal.substmodel.GTR(1.0, 1.0, 1.0, 1.0, 1.0, frequencies);
		} else {
			throw new XmlParseException("rate matrix model '" + type + "' unexpected!");
		}
		NodeList nodes = e.getElementsByTagName(PARAMETER);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element param = (Element)nodes.item(i);
			String name = getNameAttr(param);
			if (name.equals(KAPPA)) {
				rateMatrix.setParameter(getDoubleValue(param), 0);
			} else if (name.equals(TS_TV_RATIO)) {
				rateMatrix.setParameter(getDoubleValue(param), 0);
			} else if (name.equals(A_TO_C)) {
				rateMatrix.setParameter(getDoubleValue(param), 0);
			} else if (name.equals(A_TO_G)) {
				rateMatrix.setParameter(getDoubleValue(param), 1);
			} else if (name.equals(A_TO_T)) {
				rateMatrix.setParameter(getDoubleValue(param), 2);
			} else if (name.equals(C_TO_G)) {
				rateMatrix.setParameter(getDoubleValue(param), 3);
			} else if (name.equals(C_TO_T)) {
				rateMatrix.setParameter(getDoubleValue(param), 4);
			} else if (name.equals(G_TO_T)) {
				rateMatrix.setParameter(getDoubleValue(param), 5);
			} else {
				throw new XmlParseException("rate matrix parameter '" + name + "' unexpected!");
			}
		}
		return rateMatrix;
	}

	/**
	 * @return a time data object based on the given XML element.
	 */
	public static pal.misc.TimeOrderCharacterData parseTimeDataElement(Element e) throws XmlParseException {
		pal.misc.TimeOrderCharacterData tocd = null;
		int units = pal.misc.Units.GENERATIONS;
		validateTagName(e, TIME_DATA);

		units = getUnitsAttr(e);
		NodeList nodes = e.getElementsByTagName(TIME);
		Vector names = new Vector();
		Vector times = new Vector();

		for (int i = 0; i < nodes.getLength(); i++) {
			Element timeElement = (Element)nodes.item(i);
			Double time = new Double(timeElement.getAttribute(VALUE));
			NodeList children = timeElement.getChildNodes();
			if (children.item(0) instanceof Text) {
				StringTokenizer tokens = new StringTokenizer(children.item(0).getNodeValue());
				while (tokens.hasMoreTokens()) {
					names.addElement(tokens.nextToken());
					times.addElement(time);
				}
			} else throw new XmlParseException("Non-text node found in time element!");
		}
		String[] nameArray = new String[names.size()];
		double[] timeArray = new double[names.size()];
		for (int i =0 ; i < nameArray.length; i++) {
			nameArray[i] = (String)names.elementAt(i);
			timeArray[i] = ((Double)times.elementAt(i)).doubleValue();
		}

		tocd = new TimeOrderCharacterData(new SimpleIdGroup(nameArray), units);
		tocd.setTimes(timeArray, units);

		return tocd;
	}

	/**
	 * @return a tree object based on the XML element it was passed.
	 */
	public static pal.tree.Tree parseTreeElement(Element e) throws XmlParseException {
		int units = pal.misc.Units.GENERATIONS;
		validateTagName(e, TREE);
		units = getUnitsAttr(e);
		NodeList nodes = e.getElementsByTagName(NODE);

		// TODO
		// instead of getting all subelements named node,
		// only the direct children of the tree element
		// should be interrogated! This will allow
		// for better error detection.

		pal.tree.Node root = parseNodeElement((Element)nodes.item(0));
		if (root.getNodeHeight() == 0.0) {
			pal.tree.NodeUtils.lengths2Heights(root);
		} else {
			pal.tree.NodeUtils.heights2Lengths(root);
		}
		pal.tree.SimpleTree tree = new pal.tree.SimpleTree(root);
		tree.setUnits(units);
		return tree;
	}

	/**
	 * Throws a runtime exception if the element does not have
	 * the given name.
	 */
	public static void validateTagName(Element e, String name) throws XmlParseException {
		if (!e.getTagName().equals(name)) {
			throw new XmlParseException("Wrong tag name! Expected " + name + ", found " + e.getTagName() + ".");
		}
	}

	// PROTECTED METHODS

	protected static double getDoubleValue(Element e) {
		return Double.parseDouble(e.getAttribute(VALUE));
	}

	protected static int getIntegerValue(Element e) {
		return Integer.parseInt(e.getAttribute(VALUE));
	}

	protected static String getNameAttr(Element e) {
		return e.getAttribute(NAME);
	}

	protected static int getUnitsAttr(Element e) {

		int units = pal.misc.Units.GENERATIONS;
		if (hasAttribute(e, UNITS)) {
			String unitsAttr = e.getAttribute(UNITS);
			if (unitsAttr.equals(YEARS)) { units = pal.misc.Units.YEARS;}
			else if (unitsAttr.equals(MONTHS)) { units = pal.misc.Units.MONTHS;}
			else if (unitsAttr.equals(DAYS)) { units = pal.misc.Units.DAYS;}
			else if (unitsAttr.equals(MUTATIONS)) { units = pal.misc.Units.EXPECTED_SUBSTITUTIONS;}
		}
		return units;
	}

	/**
	 * This method allows the removeal of e.hasAttribute which is DOM Level 2.
	 * I am trying to keep compliant with DOM level 1 for now.
	 */

	protected static final boolean hasAttribute(Element e, String name) {
		String attr = e.getAttribute(name);
		return ((attr != null) && !attr.equals(""));
	}


	// PRIVATE METHODS

	private static void parseNodeElement(Element e, pal.tree.Node node) throws XmlParseException {

		validateTagName(e, NODE);
		if (hasAttribute(e, HEIGHT)) {
			node.setNodeHeight(Double.parseDouble(e.getAttribute(HEIGHT)));
		}

		if (hasAttribute(e, NAME)) {
			node.setIdentifier(new Identifier(e.getAttribute(NAME)));
		}

		NodeList nodes = e.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				Element child = (Element)nodes.item(i);
				if (child.getTagName().equals(NODE)) {
					node.addChild(parseNodeElement((Element)nodes.item(i)));
				} else if (child.getTagName().equals(EDGE)) {
					node.addChild(parseEdgeNodeElement((Element)nodes.item(i)));
				} else if (child.getTagName().equals(ATTRIBUTE)) {
					if (node instanceof AttributeNode) {
						Attribute a = parseAttributeElement(child);
						((AttributeNode)node).setAttribute(a.getName(), a.getValue());
					}
				}
			}
		}
	}
}
