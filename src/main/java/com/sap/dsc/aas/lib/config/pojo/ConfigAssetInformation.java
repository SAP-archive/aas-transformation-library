/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.exceptions.AlreadyDefinedException;

public class ConfigAssetInformation extends AbstractConfig {
    private String kindTypeXPath = "TYPE";
    private ConfigIdGeneration idGeneration;

    private ConfigReference globalAssetIdReference;

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(ConfigIdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    public String getKindTypeXPath() {
        return kindTypeXPath;
    }

    @JsonProperty("kindType_xpath")
    public void setKindTypeXPath(String kindTypeXPath) {
        this.kindTypeXPath = kindTypeXPath;
    }

    public ConfigReference getGlobalAssetIdReference() {
        return globalAssetIdReference;
    }

    @JsonProperty("globalAssetIdReference")
    public void setGlobalAssetIdReference(ConfigReference globalAssetIdReference) {
        if (this.globalAssetIdReference != null) {
            throw new AlreadyDefinedException("globalAssetIdReference");
        }
        this.globalAssetIdReference = globalAssetIdReference;
    }

}
