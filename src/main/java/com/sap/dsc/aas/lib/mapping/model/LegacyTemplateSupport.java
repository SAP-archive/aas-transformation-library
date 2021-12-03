/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.mapping.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.exceptions.AlreadyDefinedException;

import io.adminshell.aas.v3.model.HasSemantics;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;

public class LegacyTemplateSupport extends TemplateSupport implements LegacyTemplate {

    private String kindTypeXPath = "TYPE";
    private String xPath, langXPath, valueXPath, minValueXPath, maxValueXPath, mimeTypeXPath;
    private Map<String, Object> idGeneration;
    private KeyType keyType;
    private KeyElements keyElement;
    private String idShortXPath = "@Name";
    private String configElementId;

    public LegacyTemplateSupport() {
        super();
    }

    public LegacyTemplateSupport(Object target) {
        super(target);
    }

    @Override
    public String getXPath() {
        return xPath;
    }

    @Override
    public void setXPath(String xPath) {
        if (this.xPath != null) {
            throw new AlreadyDefinedException("from_attributeName");
        }
        this.xPath = xPath;
    }

    @Override
    public void setAttributeName(String attributeName) {
        if (this.xPath != null) {
            throw new AlreadyDefinedException("from_xpath", this.xPath);
        }
        this.xPath = "caex:Attribute[@Name='" + attributeName + "']";
    }

    /**
     * Syntax sugar to allow defining semanticId (make config JSON more compact)
     *
     * @param id The hardcoded id value
     */
    @Override
    public void setSemanticIdFromString(String id) {
        if (((HasSemantics) getTarget()).getSemanticId() != null) {
            throw new AlreadyDefinedException("semanticId");
        }
        List<Key> keys = new ArrayList<>();
        Key key = new DefaultKey();
        key.setIdType(KeyType.IRDI);
        key.setType(KeyElements.CONCEPT_DESCRIPTION);
        key.setValue(id);
        keys.add(key);
        Reference semanticId = new DefaultReference();
        semanticId.setKeys(keys);
        ((HasSemantics) getTarget()).setSemanticId(semanticId);
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

    public String getMimeTypeXPath() {
        return mimeTypeXPath;
    }

    public void setMimeTypeXPath(String mimeTypeXPath) {
        this.mimeTypeXPath = mimeTypeXPath;
    }

    public Map<String, Object> getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(Map<String, Object> idGeneration) {
        this.idGeneration = idGeneration;
    }

    public String getIdShortXPath() {
        return idShortXPath;
    }

    @JsonProperty("idShort_xpath")
    public void setIdShortXPath(String idShortXPath) {
        this.idShortXPath = idShortXPath;
    }

    /**
     * Returns an (optional) Id that can be used to refer to an element of the config
     *
     * @return The config element's Id
     */
    public String getConfigElementId() {
        return configElementId;
    }

    public void setConfigElementId(String configElementId) {
        this.configElementId = configElementId;
    }

    public void setValueId(String valueId) {
        if (this.idGeneration != null) {
            throw new AlreadyDefinedException("idGeneration");
        }
        // do nothing
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public KeyElements getKeyElement() {
        return keyElement;
    }

    public void setKeyElement(KeyElements keyElement) {
        this.keyElement = keyElement;
    }

    @Override
    public String getKindTypeXPath() {
        return kindTypeXPath;
    }

    @Override
    public void setKindTypeXPath(String kindTypeXPath) {
        this.kindTypeXPath = kindTypeXPath;
    }

}
