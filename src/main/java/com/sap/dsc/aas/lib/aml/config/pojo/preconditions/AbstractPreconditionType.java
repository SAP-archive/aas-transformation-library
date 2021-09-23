/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

public abstract class AbstractPreconditionType {

    private String errorMessageOnFailure;

    public String getErrorMessageOnFailure() {
        return errorMessageOnFailure;
    }

    public void setErrorMessageOnFailure(String errorMessageOnFailure) {
        this.errorMessageOnFailure = errorMessageOnFailure;
    }

}
