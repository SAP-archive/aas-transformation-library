/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform.pojo;

import java.util.List;

import io.adminshell.aas.v3.model.*;

public class GenericSubmodelElementAttributes {

    private ModelingKind kind;
    private String category;
    private List<LangString> descriptions;
    private List<Constraint> qualifiers;
    private List<EmbeddedDataSpecification> embeddedDataSpecifications;
    private Reference parent;
    private String idShort;
    private Reference semanticId;

    public ModelingKind getKind() {
        return kind;
    }

    public void setKind(ModelingKind kind) {
        this.kind = kind;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LangString> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<LangString> descriptions) {
        this.descriptions = descriptions;
    }

    public List<Constraint> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(List<Constraint> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public List<EmbeddedDataSpecification> getEmbeddedDataSpecifications() {
        return embeddedDataSpecifications;
    }

    public void setEmbeddedDataSpecifications(List<EmbeddedDataSpecification> embeddedDataSpecifications) {
        this.embeddedDataSpecifications = embeddedDataSpecifications;
    }

    public Reference getParent() {
        return parent;
    }

    public void setParent(Reference parent) {
        this.parent = parent;
    }

    public String getIdShort() {
        return idShort;
    }

    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }

    public Reference getSemanticId() {
        return semanticId;
    }

    public void setSemanticId(Reference semanticId) {
        this.semanticId = semanticId;
    }
}
