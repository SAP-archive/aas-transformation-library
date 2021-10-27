package com.sap.dsc.aas.lib.expressions;

/**
 * Represents the value of a named variable.
 */
public class VarExpr implements Expression {
	/**
	 * The variable's name.
	 */
	protected final String name;

	public VarExpr(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public Object evaluate() {
		return Expressions.getVar(name);
	}
}