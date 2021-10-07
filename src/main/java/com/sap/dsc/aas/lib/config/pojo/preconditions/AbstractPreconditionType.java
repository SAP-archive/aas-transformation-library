/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo.preconditions;

public abstract class AbstractPreconditionType {

    private String errorMessageOnFailure;

    public String getErrorMessageOnFailure() {
        return errorMessageOnFailure;
    }

    public void setErrorMessageOnFailure(String errorMessageOnFailure) {
        this.errorMessageOnFailure = errorMessageOnFailure;
    }

}
