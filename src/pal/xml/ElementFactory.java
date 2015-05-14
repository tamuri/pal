// ElementFactory.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.xml;

import org.w3c.dom.*;
import pal.coalescent.*;
import java.util.*;
import java.io.Reader;
import pal.util.*;
import pal.misc.Units;
import pal.tree.Tree;
import pal.tree.Node;
import pal.alignment.Alignment;
import pal.misc.Identifier;
import pal.misc.TimeOrderCharacterData;
import pal.mep.*;
import pal.substmodel.RateMatrix;
import pal.misc.Attribute;
import java.util.Enumeration;



/**
 * This class provides static methods for creating DOM Elements from PAL Objects.
 * Arguably each object in PAL should be allowed to generate a DOM Element
 * representation itself -- however I have decided to restrict the PAL's
 * dependence on the DOM specification to this package only.
 *
 * @author Alexei Drummond
 *
 * @version $Id: ElementFactory.java,v 1.9 2003/07/20 04:52:42 matt Exp $
 */
public class ElementFactory implements XMLConstants {

	public static Element createAlignmentElement(Alignment a, Document document) {
		Element alignmentNode = document.createElement(ALIGNMENT);
		alignmentNode.setAttribute(DATA_TYPE, a.getDataType().getDescription());
		alignmentNode.setAttribute(DATA_TYPE_ID, a.getDataType().getTypeID()+"");
		for (int i = 0; i < a.getSequenceCount(); i++) {
			alignmentNode.appendChild(createSequenceElement(a.getIdentifier(i), a.getAlignedSequenceString(i), document));
		}
		return alignmentNode;
	}

	/**
	 * @return a DOM element describing an attribute element.
	 */
	public static Element createAttributeElement(Attribute a, Document document) {
		Element attNode = document.createElement(ATTRIBUTE);

		Object value = a.getValue();
		String type = Attribute.STRING;
		if (value instanceof Double) { type = Attribute.DOUBLE; }
		if (value instanceof Float) { type = Attribute.FLOAT; }
		if (value instanceof Boolean) { type = Attribute.BOOLEAN; }
		if (value instanceof Integer) { type = Attribute.INTEGER; }

		attNode.setAttribute(NAME, a.getName());
		attNode.setAttribute(VALUE, value.toString());
		attNode.setAttribute(TYPE, type);

		return attNode;
	}

	/**
	 * Creates an XML element representing a demographic model.
	 */
	public static Element createDemographicModelElement(DemographicModel demo, Document document) {

		Element demoNode = document.createElement(DEMOGRAPHIC_MODEL);
		if (demo instanceof ConstExpGrowth) {
			demoNode.setAttribute(TYPE, CONST_EXP_GROWTH);
			ConstExpGrowth ceg = (ConstExpGrowth)demo;
			if (ceg.getParameterization() == ConstExpGrowth.ALPHA_PARAMETERIZATION) {
				demoNode.appendChild(createParameterElement(ALPHA,
					ceg.getAncestral(), document));
			} else {
				demoNode.appendChild(createParameterElement(ANCESTRAL_POP_SIZE,
					ceg.getAncestral(), document));
			}
		} else if (demo instanceof ExponentialGrowth) {
			demoNode.setAttribute(TYPE, EXPONENTIAL_GROWTH);
			demoNode.appendChild(createParameterElement(GROWTH_RATE,
					((ExponentialGrowth)demo).getGrowthRate(), document));
		} else if (demo instanceof ConstantPopulation) {
			demoNode.setAttribute(TYPE, CONSTANT_POPULATION);
			demoNode.appendChild(createParameterElement(POPULATION_SIZE,
				((ConstantPopulation)demo).getN0(), document));
		}
		demoNode.setAttribute(UNITS, getUnitString(demo.getUnits()));
		return demoNode;
	}

	public static Element createEdgeNodeElement(pal.tree.Node node, Document document) {
		Element edgeNode = document.createElement(EDGE);
		edgeNode.setAttribute(LENGTH, node.getBranchLength()+"");
		for (int i =0; i < node.getChildCount(); i++) {
			edgeNode.appendChild(createNodeElement(node.getChild(i), document, true));
		}
		return edgeNode;
	}

	/**
	 * Creates a DOM element associated with the given document representing
	 * the given equilibrium frequencies of a rate matrix.
	 */
	public static Element createFrequenciesElement(double[] frequencies, Document d) {
		Element freqNode = d.createElement(FREQUENCIES);
		String freqs = frequencies[0] + " ";
		for (int i =1; i < frequencies.length; i++) {
			freqs += " " + frequencies[i];
		}
		freqNode.appendChild(d.createTextNode(freqs));
		return freqNode;
	}

	/**
	 * Creates an XML element representing a mutation rate model.
	 */
	public static Element createMutationRateModelElement(MutationRateModel muModel, Document document) {

		Element muNode = document.createElement(MUTATION_RATE_MODEL);
		if (muModel instanceof SteppedMutationRate) {
			muNode.setAttribute(TYPE, STEPPED_MUTATION_RATE);
			SteppedMutationRate smr = (SteppedMutationRate)muModel;
			muNode.appendChild(createParameterElement(MUTATION_RATE, smr.getMus()[0], document));
			muNode.appendChild(createParameterElement(ANCESTRAL_MU_RATE, smr.getMus()[1], document));
			muNode.appendChild(createParameterElement(MU_STEP_TIME, smr.getMuChanges()[0], document));
		} else if (muModel instanceof ConstantMutationRate) {
			muNode.setAttribute(TYPE, CONSTANT_MUTATION_RATE);
			muNode.appendChild(createParameterElement(MUTATION_RATE,
				muModel.getMutationRate(0.0), document));
		}
		return muNode;
	}

	public static Element createNodeElement(pal.tree.Node node, Document document) {
		return createNodeElement(node, document, false);
	}

	public static Element createNodeElement(pal.tree.Node node, Document document, boolean includeEdges) {
		Element nodeNode = document.createElement(NODE);
		nodeNode.setAttribute(HEIGHT, node.getNodeHeight()+"");
		nodeNode.setAttribute(NAME, node.getIdentifier().getName());

		if (node instanceof pal.tree.AttributeNode) {
			pal.tree.AttributeNode attNode = (pal.tree.AttributeNode)node;
			Enumeration e = attNode.getAttributeNames();
			while ((e != null) && e.hasMoreElements()) {
				String name = (String)e.nextElement();
				Object value = attNode.getAttribute(name);
				nodeNode.appendChild(createAttributeElement(new Attribute(name, value), document));
			}
		}
		for (int i =0; i < node.getChildCount(); i++) {
			if (includeEdges) {
				nodeNode.appendChild(createEdgeNodeElement(node.getChild(i), document));
			} else {
				nodeNode.appendChild(createNodeElement(node.getChild(i), document));
			}
		}
		return nodeNode;
	}

	/**
	 * Creates an XML element representing a parameter.
	 */
	public static Element createParameterElement(String name, double value, Document document) {
		Element parameterNode = document.createElement(PARAMETER);
		parameterNode.setAttribute(NAME, name);
		parameterNode.setAttribute(VALUE, value+"");
		return parameterNode;
	}

	public static Element createRateMatrixElement(RateMatrix matrix, Document d) {
		Element matrixNode = d.createElement(RATE_MATRIX);
		matrixNode.setAttribute(MODEL, matrix.getUniqueName());
		matrixNode.setAttribute(DATA_TYPE, matrix.getDataType().getDescription());
		matrixNode.setAttribute(DATA_TYPE_ID, matrix.getDataType().getTypeID()+"");

		matrixNode.appendChild(createFrequenciesElement(matrix.getEquilibriumFrequencies(), d));
		for (int i =0 ; i < matrix.getNumParameters(); i++) {
			matrixNode.appendChild(
				createParameterElement(matrix.getParameterName(i), matrix.getParameter(i), d));
		}
		return matrixNode;
	}

	public static Element createSequenceElement(Identifier id, String sequence, Document document) {
		Element sequenceNode = document.createElement(SEQUENCE);
		sequenceNode.setAttribute(NAME, id.getName());
		sequenceNode.appendChild(document.createTextNode(sequence));
		return sequenceNode;
	}

	public static Element createTimeDataElement(TimeOrderCharacterData tocd, Document document) {
		Element timeDataNode = document.createElement(TIME_DATA);
		timeDataNode.setAttribute(UNITS, getUnitString(tocd.getUnits()));
		timeDataNode.setAttribute(ORIGIN, "0");
		timeDataNode.setAttribute(DIRECTION, BACKWARDS);
		for (int i =0; i < tocd.getIdCount(); i++) {
			timeDataNode.appendChild(
				createTimeElement(tocd.getIdentifier(i), tocd.getTime(i), document));
		}
		return timeDataNode;
	}

	public static Element createTreeElement(Tree tree, Document document, boolean includeEdges) {
		Element treeNode = document.createElement(TREE);
		treeNode.setAttribute(UNITS, getUnitString(tree.getUnits()));
		treeNode.appendChild(createNodeElement(tree.getRoot(), document, false));
		return treeNode;
	}


	// PRIVATE METHODS

	private static Element createTimeElement(Identifier id, double time, Document document) {
		Element timeNode = document.createElement(TIME);
		timeNode.setAttribute(VALUE, time+"");
		timeNode.appendChild(document.createTextNode(id.getName()));
		return timeNode;
	}

	/**
	 * Private method that converts a unit integer into a human-readable name.
	 */
	private static String getUnitString(int units) {
		switch (units) {
			case Units.GENERATIONS: return GENERATIONS;
			case Units.DAYS: return DAYS;
			case Units.MONTHS: return MONTHS;
			case Units.YEARS: return YEARS;
			case Units.EXPECTED_SUBSTITUTIONS: return MUTATIONS;
			default: return UNKNOWN;
		}
	}
}
