/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.mapping.model;

import java.util.List;

import com.sap.dsc.aas.lib.config.pojo.ConfigPlaceholder;
import com.sap.dsc.aas.lib.config.pojo.Precondition;

import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class MappingSpecification {

    private String version;
    private String aasVersion;
    private AssetAdministrationShellEnvironment aasEnvironmentMapping;
    private List<Precondition> preconditions;
    private List<ConfigPlaceholder> placeholders;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAasVersion() {
        return aasVersion;
    }

    public void setAasVersion(String aasVersion) {
        this.aasVersion = aasVersion;
    }

    public AssetAdministrationShellEnvironment getAasEnvironmentMapping() {
        return aasEnvironmentMapping;
    }

    public void setAasEnvironmentMapping(AssetAdministrationShellEnvironment mapping) {
        this.aasEnvironmentMapping = mapping;
    }

    public List<Precondition> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<Precondition> preconditions) {
        this.preconditions = preconditions;
    }

    public List<ConfigPlaceholder> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List<ConfigPlaceholder> placeholders) {
        this.placeholders = placeholders;
    }
}
