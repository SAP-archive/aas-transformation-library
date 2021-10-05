/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.exceptions;

public class MissingConfigException extends InvalidConfigException {

    private static final long serialVersionUID = 8024812952881072036L;

    public MissingConfigException(String message) {
        super(message);
    }

}
