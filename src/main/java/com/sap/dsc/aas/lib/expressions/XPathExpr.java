package com.sap.dsc.aas.lib.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dom4j.Node;

import com.sap.dsc.aas.lib.mapping.TransformationContext;
import com.sap.dsc.aas.lib.transform.XPathHelper;

/**
 * Represents an operator that evaluates a specific XPath
 */
public class XPathExpr implements Expression {

	/**
	 * Expressions that are evaluated to compute the actual list elements.
	 */
	protected final List<Expression> args;

	public XPathExpr(List<Expression> args) {
		this.args = args;
	}

	@Override
	public List<Node> evaluate(TransformationContext ctx) {
		// evaluate multiple xpath expressions and create joined stream of all resulting
		// nodes
		return args.stream().map(arg -> arg.evaluate(ctx)).flatMap(value -> {
			if (value instanceof String && ctx.getContextItem().isPresent()
					&& ctx.getContextItem().get() instanceof Node) {
				// evaluate XPath against context node
				return XPathHelper.getInstance().getNodes((Node) ctx.getContextItem().get(), (String) value).stream();

			} else {
				// invalid XPath or no Node Context
				throw new IllegalArgumentException("Invalid XPath or no Node Context is given.");
			}
		}).collect(Collectors.toList());
	}

	@Override
	public String evaluateAsString(TransformationContext ctx) {
		Optional<String> xpath = args.stream().map(arg -> arg.evaluateAsString(ctx)).findFirst();
		if (xpath.isPresent() && ctx.getContextItem().isPresent() && ctx.getContextItem().get() instanceof Node) {
			return Objects.toString(
					XPathHelper.getInstance().getStringValueOrNull((Node) ctx.getContextItem().get(), xpath.get()));
		} else {
			return "";
		}
	}
}