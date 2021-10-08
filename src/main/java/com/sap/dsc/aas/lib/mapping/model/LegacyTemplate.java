/*
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved.

  SPDX-License-Identifier: Apache-2.0
 */
package com.sap.dsc.aas.lib.mapping.model;

import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.config.pojo.ConfigReference;

/**
 * Includes the legacy config attributes that may be replaced by a new template DSL in the future.
 */
public interface LegacyTemplate extends Template {

    String getXPath();

    @JsonProperty("from_xpath")
    void setXPath(String xPath);

    @JsonProperty("from_attributeName")
    void setAttributeName(String attributeName);

    @JsonProperty("semanticId_str")
    void setSemanticIdFromString(String id);

    String getIdShortXPath();

    @JsonProperty("idShort_xpath")
    void setIdShortXPath(String idShortXPath);

    String getLangXPath();

    @JsonProperty("langXPath")
    void setLangXPath(String langXPath);

    String getValueXPath();

    @JsonProperty("valueXPath")
    void setValueXPath(String valueXPath);

    String getMinValueXPath();

    void setMinValueXPath(String minValueXPath);

    String getMaxValueXPath();

    void setMaxValueXPath(String maxValueXPath);

    String getMimeTypeXPath();

    void setMimeTypeXPath(String mimeTypeXPath);

    ConfigIdGeneration getIdGeneration();

    void setIdGeneration(ConfigIdGeneration idGeneration);

    KeyType getKeyType();

    void setKeyType(KeyType keyType);

    KeyElements getKeyElement();

    void setKeyElement(KeyElements keyElement);

    String getKindTypeXPath();

    @JsonProperty("kindType_xpath")
    void setKindTypeXPath(String kindTypeXPath);

    ConfigReference getGlobalAssetIdReference();

    @JsonProperty("globalAssetIdReference")
    void setGlobalAssetIdReference(ConfigReference globalAssetIdReference);

    void setValueId(String valueId);

    /**
     * Returns an (optional) Id that can be used to refer to an element of the config
     *
     * @return The config element's Id
     */
    String getConfigElementId();

    void setConfigElementId(String configElementId);
}
