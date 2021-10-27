package com.sap.dsc.aas.lib.expressions;

/**
 * Represents dynamic expressions within a template.
 */
public interface Expression {
    /**
     * Evaluates the expression using a thread-local context.
     *
     * @return computed result
     */
    Object evaluate();
}
