/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.exceptions.AlreadyDefinedException;

import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;

public abstract class AbstractConfigFromAttribute extends AbstractConfig {

    private String xPath;
    private ConfigReference semanticIdReference;

    public String getXPath() {
        return xPath;
    }

    @JsonProperty("from_xpath")
    public void setXPath(String xPath) {
        if (this.xPath != null) {
            throw new AlreadyDefinedException("from_xpath_builder");
        }
        this.xPath = xPath;
    }

    @JsonProperty("from_attributeName")
    public void setAttributeName(String attributeName) {
        if (this.xPath != null) {
            throw new AlreadyDefinedException("from_xpath", this.xPath);
        }
        this.xPath = "caex:Attribute[@Name='" + attributeName + "']";//FIXME
    }

    public ConfigReference getSemanticIdReference() {
        return semanticIdReference;
    }

    /**
     * Syntax sugar to allow defining semanticId (make config JSON more compact)
     *
     * @param id The hardcoded id value
     */
    @JsonProperty("semanticId")
    public void setSemanticId(String id) {
        if (this.semanticIdReference != null) {
            throw new AlreadyDefinedException("semanticId_ref");
        }
        this.semanticIdReference = new ConfigReference();
        this.semanticIdReference.setKeyType(KeyType.IRDI);
        this.semanticIdReference.setKeyElement(KeyElements.CONCEPT_DESCRIPTION);
        this.semanticIdReference.setValueId(id);
    }

    @JsonProperty("semanticId_ref")
    public void setSemanticId(ConfigReference semanticId) {
        if (this.semanticIdReference != null) {
            throw new AlreadyDefinedException("semanticId");
        }
        this.semanticIdReference = semanticId;
    }
}