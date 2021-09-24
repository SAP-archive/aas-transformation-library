/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.exceptions;

public class IdGenerationNameAlreadyDefinedException extends TransformationException {

    private static final long serialVersionUID = -7577154906841542931L;

    public IdGenerationNameAlreadyDefinedException(String idGenerationName) {
        super("An idGeneration function with the name '" + idGenerationName + "' already exists.");
    }

}
