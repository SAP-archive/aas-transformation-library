/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

public class PreconditionTypeForAll extends AbstractPreconditionType {

    public static final int DEFAULT_MINIMUM_NUMBER = 1;
    public static final int DEFAULT_MAXIMUM_NUMBER = 1;

    private int minimumNumber = DEFAULT_MINIMUM_NUMBER;
    private int maximumNumber = DEFAULT_MAXIMUM_NUMBER;

    public int getMaximumNumber() {
        return maximumNumber;
    }

    public void setMaximumNumber(int maximumNumber) {
        this.maximumNumber = maximumNumber;
    }

    public int getMinimumNumber() {
        return minimumNumber;
    }

    public void setMinimumNumber(int minimumNumber) {
        this.minimumNumber = minimumNumber;
    }
}
