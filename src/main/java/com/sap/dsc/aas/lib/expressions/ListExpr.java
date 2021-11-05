package com.sap.dsc.aas.lib.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.sap.dsc.aas.lib.mapping.TransformationContext;

/**
 * Represents a list constructor.
 */
public class ListExpr implements Expression {
	/**
	 * Expressions that are evaluated to compute
	 * the actual list elements.
	 */
	protected final Expression[] args;

	public ListExpr(List<Expression> args) {
		this(args.toArray(new Expression[args.size()]));
	}

	public ListExpr(Expression... args) {
		this.args = args;
	}

	public Expression[] getArgs() {
		return args;
	}

	@Override
	public Object evaluate(TransformationContext ctx) {
		List<Object> values = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i++) {
			Expression arg = args[i];
			values.add(arg.evaluate(ctx));
		}
		return values;
	}
}