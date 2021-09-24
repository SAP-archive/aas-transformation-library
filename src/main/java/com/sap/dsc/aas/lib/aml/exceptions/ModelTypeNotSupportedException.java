/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.exceptions;

public class ModelTypeNotSupportedException extends TransformationException {

    private static final long serialVersionUID = 4712041471494868057L;

    public ModelTypeNotSupportedException(String name) {
        super("The model type '" + name + "' is not supported");
    }
}
