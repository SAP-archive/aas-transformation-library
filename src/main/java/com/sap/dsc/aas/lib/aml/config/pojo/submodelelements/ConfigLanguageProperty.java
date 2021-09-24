/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigLanguageProperty {

    private String langXPath;
    private String valueXPath;

    public ConfigLanguageProperty() {

    }

    @JsonCreator
    public ConfigLanguageProperty(@JsonProperty(required = true, value = "langXPath") String langXPath,
        @JsonProperty(required = true, value = "valueXPath") String valueXPath) {
        this.langXPath = langXPath;
        this.valueXPath = valueXPath;
    }

    public String getLangXPath() {
        return langXPath;
    }

    public void setLangXPath(String langXPath) {
        this.langXPath = langXPath;
    }

    public String getValueXPath() {
        return valueXPath;
    }

    public void setValueXPath(String valueXPath) {
        this.valueXPath = valueXPath;
    }
}
