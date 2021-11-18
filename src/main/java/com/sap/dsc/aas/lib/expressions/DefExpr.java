package com.sap.dsc.aas.lib.expressions;

import com.sap.dsc.aas.lib.mapping.TransformationContext;

/**
 * Represents the value of a named definition.
 */
public class DefExpr implements Expression {
	/**
	 * The definition's name.
	 */
	protected final String name;

	public DefExpr(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public Object evaluate(TransformationContext ctx) {
		return ctx.getDefinitions().get(name).evaluate(ctx);
	}

	@Override
	public String evaluateAsString(TransformationContext ctx) {
		return ctx.getDefinitions().get(name).evaluateAsString(ctx);
	}
}