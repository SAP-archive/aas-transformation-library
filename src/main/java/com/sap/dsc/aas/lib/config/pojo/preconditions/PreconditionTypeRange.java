/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo.preconditions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PreconditionTypeRange extends AbstractPreconditionTypeForEach {

    public static final int DEFAULT_MINIMUM_NUMBER = 1;
    public static final int DEFAULT_MAXIMUM_NUMBER = 1;

    private int minimumNumber = DEFAULT_MINIMUM_NUMBER;
    private int maximumNumber = DEFAULT_MAXIMUM_NUMBER;

    public PreconditionTypeRange() {}

    @JsonCreator
    public PreconditionTypeRange(@JsonProperty(value = "from_xpath", required = true) String fromXPath) {
        this.setFromXPath(fromXPath);
    }

    public int getMinimumNumber() {
        return minimumNumber;
    }

    public void setMinimumNumber(int minimumNumber) {
        this.minimumNumber = minimumNumber;
    }

    public int getMaximumNumber() {
        return maximumNumber;
    }

    public void setMaximumNumber(int maximumNumber) {
        this.maximumNumber = maximumNumber;
    }

}
