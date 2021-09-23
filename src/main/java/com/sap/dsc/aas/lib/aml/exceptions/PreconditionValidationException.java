/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions;

import com.sap.dsc.aas.lib.aml.config.pojo.Precondition;
import com.sap.dsc.aas.lib.aml.config.pojo.preconditions.AbstractPreconditionType;

public class PreconditionValidationException extends ValidationException {

    private static final long serialVersionUID = 3422964352802680384L;

    public PreconditionValidationException(Precondition precondition, AbstractPreconditionType failedPreconditionType,
        String detailedErrorMessage) {
        super("Precondition '" + precondition.getPreconditionName() + "' failed: " +
            failedPreconditionType.getErrorMessageOnFailure() + " " + detailedErrorMessage);
    }

}
