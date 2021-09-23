/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform;

import java.util.*;
import java.util.stream.Collectors;

import org.dom4j.Node;
import org.dom4j.XPath;

import com.sap.dsc.aas.lib.aml.exceptions.NoResultByXPathException;

public class XPathHelper {

    /**
     * Based on the given XPath expression, get a list of {@link org.dom4j.Node}. Filtered for Namespace
     * CAEX.
     *
     * @param parentNode Base node to start the XPath search
     * @param xpathExpression The XPath expression to match
     * @return List of child nodes matching the expression
     */
    public List<Node> getNodes(Node parentNode, String xpathExpression) {
        return createXPath(parentNode, xpathExpression).selectNodes(parentNode);
    }

    public String getStringValueOrNull(Node node, String xPath) {
        Object result = createXPath(node, xPath).evaluate(node);
        if (result instanceof String) {
            return (String) result;
        }
        if (result instanceof Node) {
            return ((Node) result).getStringValue();
        }

        return null;
    }

    public String getStringValue(Node node, String xPath) throws NoResultByXPathException {
        String value = getStringValueOrNull(node, xPath);
        if (value != null) {
            return value;
        }

        throw new NoResultByXPathException(xPath);
    }

    public List<String> getStringValues(Node rootNode, String xPath) {
        Object result = createXPath(rootNode, xPath).evaluate(rootNode);
        if (result instanceof String) {
            return Collections.singletonList((String) result);
        }
        if (result instanceof Node) {
            return Collections.singletonList(((Node) result).getStringValue());
        }
        if (result instanceof Number) {
            return Collections.singletonList(String.valueOf(result));
        }
        // Per .evaluate documentation the result object is never null
        return ((List<?>) result).stream()
            .map(Node.class::cast)
            .map(Node::getStringValue)
            .collect(Collectors.toList());
    }

    /**
     * Create a XPath, with the CAEX namespace set.
     *
     * @param parentNode Base node to start the search
     * @param xpathExpression The XPath expression to match
     * @return The searched Node if found, otherwise null
     */
    protected XPath createXPath(Node parentNode, String xpathExpression) {
        XPath xpath = parentNode.createXPath(xpathExpression);

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("caex", "http://www.dke.de/CAEX");
        xpath.setNamespaceURIs(namespaces);

        return xpath;
    }

}
