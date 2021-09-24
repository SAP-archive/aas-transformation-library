/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;

public class ConfigRange extends AbstractConfigSubmodelElement implements ConfigDataElement {
    private String valueType;
    private String minValueXPath;
    private String maxValueXPath;

    public ConfigRange() {}

    @JsonCreator
    public ConfigRange(@JsonProperty(required = true, value = "valueType") String valueType) {
        this.valueType = valueType;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getMinValueXPath() {
        return minValueXPath;
    }

    public void setMinValueXPath(String minValueXPath) {
        this.minValueXPath = minValueXPath;
    }

    public String getMaxValueXPath() {
        return maxValueXPath;
    }

    public void setMaxValueXPath(String maxValueXPath) {
        this.maxValueXPath = maxValueXPath;
    }
}
