/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.mapping.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.config.pojo.ConfigPlaceholder;
import com.sap.dsc.aas.lib.config.pojo.Precondition;

import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class MappingSpecification {

    private AssetAdministrationShellEnvironment aasEnvironmentMapping;
    private Header header;

    public AssetAdministrationShellEnvironment getAasEnvironmentMapping() {
        return aasEnvironmentMapping;
    }

    public void setAasEnvironmentMapping(AssetAdministrationShellEnvironment mapping) {
        this.aasEnvironmentMapping = mapping;
    }

    public Header getHeader() {
        return header;
    }

    @JsonProperty("@header")
    public void setHeader(Header header) {
        this.header = header;
    }
}
