/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;

public class ConfigRelationshipElement extends AbstractConfigSubmodelElement implements ConfigReferenceContainer {

    private ConfigReference first;
    private ConfigReference second;

    @JsonCreator
    public ConfigRelationshipElement(@JsonProperty(required = true, value = "first") ConfigReference first,
        @JsonProperty(required = true, value = "second") ConfigReference second) {
        this.first = first;
        this.second = second;
    }

    public ConfigReference getFirst() {
        return first;
    }

    public void setFirst(ConfigReference first) {
        this.first = first;
    }

    public ConfigReference getSecond() {
        return second;
    }

    public void setSecond(ConfigReference second) {
        this.second = second;
    }

    @Override
    public List<ConfigReference> getReferences() {
        return Arrays.asList(first, second);
    }
}
