/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.transform.validation;

import com.sap.dsc.aas.lib.aml.transform.XPathHelper;

public abstract class AbstractValidator {

    protected XPathHelper xPathHelper;

    public AbstractValidator() {
        this.xPathHelper = new XPathHelper();
    }

}
