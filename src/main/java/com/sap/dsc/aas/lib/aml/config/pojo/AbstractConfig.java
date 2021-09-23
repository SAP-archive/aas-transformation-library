/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractConfig {

    private String idShortXPath = "@Name";
    private String configElementId;

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

}
