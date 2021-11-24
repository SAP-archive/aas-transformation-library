/*
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved.

  SPDX-License-Identifier: Apache-2.0
 */
package com.sap.dsc.aas.lib.mapping.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.config.pojo.Precondition;

public class Header extends TemplateSupport {

    protected Map<String, String> namespaces;
    private String version;
    private String aasVersion;
    private List<Precondition> preconditions;
    private List<Parameter> parameters = new ArrayList<>();

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

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    @JsonProperty("@namespaces")
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    @Override
    public void setBindSpecification(BindSpecification bindSpecification) {
        throw new UnsupportedOperationException("@bind ist not allowed in header");
    }

    public List<Precondition> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<Precondition> preconditions) {
        this.preconditions = preconditions;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @JsonProperty("@parameters")
    void setParameters(Map<String, String> parameterMap) {
        this.parameters.addAll(
            parameterMap.entrySet().stream().map(e -> new Parameter(e.getKey(), e.getValue())).collect(Collectors.toList()));
    }
}
