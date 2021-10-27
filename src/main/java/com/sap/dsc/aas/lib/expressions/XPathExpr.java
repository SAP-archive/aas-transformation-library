package com.sap.dsc.aas.lib.expressions;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.dom4j.Node;

import com.sap.dsc.aas.lib.transform.XPathHelper;

/**
 * Represents an operator that evaluates a specific XPath
 */
public class XPathExpr implements Expression {

    /**
     * Current thread-local context for XPath evaluation
     */
    static final ThreadLocal<Node> currentContext = new ThreadLocal<>();

    /**
     * Expressions that are evaluated to compute the actual list elements.
     */
    protected final List<Expression> args;

    public XPathExpr(List<Expression> args) {
        this.args = args;
    }

    /**
     * Execute a function while temporary changing the context node.
     *
     * @param context The new context node
     * @param func    The function to execute
     * @return The result value of the function
     */
    public static Object withContext(Node context, Supplier<Object> func) {
        Node last = currentContext.get();
        try {
            currentContext.set(context);
            return func.get();
        } finally {
            currentContext.set(last);
        }
    }

    @Override
    public Object evaluate() {
        // evaluate multiple xpath expressions and create joined stream of all resulting nodes
        return args.stream().map(arg -> arg.evaluate()).flatMap(value -> {
            if (value instanceof String) {
                // evaluate XPath against context node
                return XPathHelper.getInstance().getNodes(currentContext.get(), (String) value).stream();
            } else {
                // invalid XPath
                throw new IllegalArgumentException("Invalid XPath");
            }
        });
    }
}