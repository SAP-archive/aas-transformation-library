package com.sap.dsc.aas.lib.expressions;

/**
 * Represents a constant value.
 */
public class ConstantExpr implements Expression {
	/**
	 * The constant value.
	 */
	protected final Object value;

	public ConstantExpr(Object value) {
		this.value = value;
	}

	@Override
	public Object evaluate() {
		return value;
	}

	public Object getValue() {
		return value;
	}
}