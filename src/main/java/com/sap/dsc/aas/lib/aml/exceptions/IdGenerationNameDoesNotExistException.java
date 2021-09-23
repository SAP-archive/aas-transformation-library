/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions;

public class IdGenerationNameDoesNotExistException extends TransformationException {

    private static final long serialVersionUID = -342822404066225810L;

    public IdGenerationNameDoesNotExistException(String idGenerationName) {
        super("The id generation function with idGenerationName '" + idGenerationName + "' does not exist.");
    }
}
