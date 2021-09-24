/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;
import com.sap.dsc.aas.lib.aml.exceptions.DataElementTypeNotSupportedException;

public class ConfigAnnotatedRelationshipElement extends ConfigRelationshipElement implements ConfigSubmodelElementContainer {

    private List<AbstractConfigSubmodelElement> annotations = new ArrayList<>();

    @JsonCreator
    public ConfigAnnotatedRelationshipElement(@JsonProperty(required = true, value = "first") ConfigReference first,
        @JsonProperty(required = true, value = "second") ConfigReference second) {
        super(first, second);
    }

    public List<AbstractConfigSubmodelElement> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AbstractConfigSubmodelElement> annotations) throws DataElementTypeNotSupportedException {
        if (annotations != null) {
            List<String> nonDataTypeElements = annotations.stream()
                .filter(type -> !(type instanceof ConfigDataElement))
                .map(type -> type.getClass().getSimpleName())
                .collect(Collectors.toList());

            if (!nonDataTypeElements.isEmpty()) {
                throw new DataElementTypeNotSupportedException(nonDataTypeElements);
            }
        }
        this.annotations = annotations;
    }

    @Override
    public List<AbstractConfigSubmodelElement> getSubmodelElements() {
        return getAnnotations();
    }
}
