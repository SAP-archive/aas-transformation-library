/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.exceptions;

public class UnableToReadAmlException extends TransformationException {

    private static final long serialVersionUID = 3909162316257201925L;

    public UnableToReadAmlException(String message) {
        super(message);
    }

    public UnableToReadAmlException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
