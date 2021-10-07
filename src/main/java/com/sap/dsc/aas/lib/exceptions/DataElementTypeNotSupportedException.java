/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.exceptions;

import java.util.List;

public class DataElementTypeNotSupportedException extends TransformationException {

    private static final long serialVersionUID = 3383557852532635116L;

    public DataElementTypeNotSupportedException(List<String> nonDataTypeElements) {
        super("The following model types are not supported: " + String.join(",", nonDataTypeElements));
    }
}
