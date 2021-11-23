package com.sap.dsc.aas.lib.expressions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dom4j.Node;

import com.sap.dsc.aas.lib.mapping.TransformationContext;
import com.sap.dsc.aas.lib.transform.XPathHelper;

/**
 * Represents an operator that evaluates a specific XPath for Caex Attribute
 * Name
 */
public class CaexAttributeNameExpr implements Expression {

	protected final String attributeName;

	public CaexAttributeNameExpr(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public List<Node> evaluate(TransformationContext ctx) {
		if (ctx.getContextItem() instanceof Node) {
			return XPathHelper.getInstance().getNodes((Node) ctx.getContextItem(),
					"caex:Attribute[@Name='" + attributeName + "']");

		} else {
			throw new IllegalArgumentException("Invalid XPath or no Node Context is given.");
		}

	}

	@Override
	public String evaluateAsString(TransformationContext ctx) {
		if (ctx.getContextItem() instanceof Node) {
			return Objects.toString(XPathHelper.getInstance().getStringValueOrNull((Node) ctx.getContextItem(),
					"caex:Attribute[@Name='" + attributeName + "']"));
		} else {
			throw new IllegalArgumentException("Invalid XPath or no Node Context is given.");
		}
	}
}