/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions;

public abstract class TransformationException extends Exception {

    private static final long serialVersionUID = -1635935178564746046L;

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
