package com.sap.dsc.aas.lib.mapping.jackson;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dsc.aas.lib.expressions.Expression;
import com.sap.dsc.aas.lib.mapping.TransformationContext;

public class ExpressionWithDefault implements Expression {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Expression toWrap;
	private Expression defaultExpr;

	public ExpressionWithDefault(Expression toWrap, Expression defaultExpr) {
		this.toWrap = toWrap;
		this.defaultExpr = defaultExpr;
	}

	@Override
	public Object evaluate(TransformationContext ctx) {
		Object tryEvaluate = toWrap.evaluate(ctx);
		if (tryEvaluate == null || (tryEvaluate instanceof Collection && ((Collection) tryEvaluate).isEmpty())) {
			LOGGER.info("Result of expression {} is empty, using default {}...", toWrap, defaultExpr);
			return defaultExpr.evaluate(ctx);
		}
		return tryEvaluate;
	}

}
