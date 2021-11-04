package com.sap.dsc.aas.lib.expressions;

import com.sap.dsc.aas.lib.mapping.TransformationContext;

/**
 * Represents dynamic expressions within a template.
 */
public interface Expression {
    /**
     * Evaluates the expression using a thread-local context.
     *
     * @return computed result
     */
    Object evaluate(TransformationContext ctx);
}
