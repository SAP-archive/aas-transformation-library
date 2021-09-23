/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform.validation;

import com.sap.dsc.aas.lib.aml.transform.XPathHelper;

public abstract class AbstractValidator {

    protected XPathHelper xPathHelper;

    public AbstractValidator() {
        this.xPathHelper = new XPathHelper();
    }

}
