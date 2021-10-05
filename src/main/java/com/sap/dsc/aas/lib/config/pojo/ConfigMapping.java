/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigMapping extends AbstractConfig {
    private String XPath;
    private ConfigAssetShell configAssetShell;
    private ConfigAssetInformation configAssetInformation;
    private List<ConfigSubmodel> submodels;
    private ConfigIdGeneration idGeneration;

    public String getXPath() {
        return XPath;
    }

    @JsonProperty("from_xpath")
    public void setXPath(String xPath) {
        this.XPath = xPath;
    }

    public ConfigAssetInformation getConfigAssetInformation() {
        return configAssetInformation;
    }

    @JsonProperty("assetInformation")
    public void setConfigAssetInformation(ConfigAssetInformation configAssetInformation) {
        this.configAssetInformation = configAssetInformation;
    }

    public List<ConfigSubmodel> getSubmodels() {
        return submodels;
    }

    @JsonProperty("submodels")
    public void setSubmodels(List<ConfigSubmodel> submodels) {
        this.submodels = submodels;
    }

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(ConfigIdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    public ConfigAssetShell getConfigAssetShell() {
        return configAssetShell;
    }

    @JsonProperty("assetShell")
    public void setConfigAssetShell(ConfigAssetShell configAssetShell) {
        this.configAssetShell = configAssetShell;
    }
}
