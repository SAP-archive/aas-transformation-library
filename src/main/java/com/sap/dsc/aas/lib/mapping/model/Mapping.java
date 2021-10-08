/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.mapping.model;

import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.Submodel;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGeneration;

public class Mapping extends TemplateSupport {

    private String XPath;
    private AssetAdministrationShell assetShell;
    private AssetInformation assetInformation;
    private List<Submodel> submodels;
    private ConfigIdGeneration idGeneration;

    public Mapping() {
        super();
        setTarget(this);
    }

    public String getXPath() {
        return XPath;
    }

    @JsonProperty("from_xpath")
    public void setXPath(String xPath) {
        this.XPath = xPath;
    }

    public AssetInformation getAssetInformation() {
        return assetInformation;
    }

    @JsonProperty("assetInformation")
    public void setAssetInformation(AssetInformation assetInformation) {
        this.assetInformation = assetInformation;
    }

    public List<Submodel> getSubmodels() {
        return submodels;
    }

    @JsonProperty("submodels")
    public void setSubmodels(List<Submodel> submodels) {
        this.submodels = submodels;
    }

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(ConfigIdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    public AssetAdministrationShell getAssetShell() {
        return assetShell;
    }

    @JsonProperty("assetShell")
    public void setAssetShell(AssetAdministrationShell assetShell) {
        this.assetShell = assetShell;
    }
}
