/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions.amlx;

import com.sap.dsc.aas.lib.aml.exceptions.ValidationException;

public class AmlxValidationException extends ValidationException {

    private static final long serialVersionUID = 496721678881430420L;

    public AmlxValidationException(final String message) {
        super("Validation error for amlx file: " + message);
    }

    public AmlxValidationException(final String message, final Throwable rootCause) {
        super("Validation error for amlx file: " + message, rootCause);
    }

}
