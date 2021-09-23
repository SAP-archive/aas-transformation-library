/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.placeholder.exceptions;

public class PlaceholderValueMissingException extends IllegalStateException {

    private static final long serialVersionUID = 8550109938648946314L;

    public PlaceholderValueMissingException(String placeholderName) {
        super("No value for placeholder '" + placeholderName + "' was given.");
    }
}
