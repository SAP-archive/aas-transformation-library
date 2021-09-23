/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;

public class ConfigProperty extends AbstractConfigSubmodelElement implements ConfigDataElement {

    private String valueType;
    private String valueXPath;
    private String valueDefault;
    private static final String DEFAULT_VALUE_XPATH = "caex:Value";

    public ConfigProperty() {}

    @JsonCreator
    public ConfigProperty(@JsonProperty(required = true, value = "valueType") String valueType) {
        this.valueType = valueType;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getValueXPath() {
        return valueXPath;
    }

    public String getDefaultValueXPath() {
        return DEFAULT_VALUE_XPATH;
    }

    public void setValueXPath(String valueXPath) {
        this.valueXPath = valueXPath;
    }

    public String getValueDefault() {
        return valueDefault;
    }

    public void setValueDefault(String valueDefault) {
        this.valueDefault = valueDefault;
    }
}
