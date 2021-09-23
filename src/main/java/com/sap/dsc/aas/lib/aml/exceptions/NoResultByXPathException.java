/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions;

public class NoResultByXPathException extends TransformationException {

    private static final long serialVersionUID = 5614753714042520764L;

    public NoResultByXPathException(String xpath) {
        super("Unable to find a single result for XPath " + xpath);
    }
}
