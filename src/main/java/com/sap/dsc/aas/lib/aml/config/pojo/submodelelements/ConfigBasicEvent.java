/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;

public class ConfigBasicEvent extends AbstractConfigSubmodelElement implements ConfigReferenceContainer {

    private ConfigReference observed;

    public ConfigBasicEvent() {}

    @JsonCreator
    public ConfigBasicEvent(@JsonProperty(required = true, value = "observed") ConfigReference observed) {
        this.setObserved(observed);
    }

    public ConfigReference getObserved() {
        return observed;
    }

    public void setObserved(ConfigReference observed) {
        this.observed = observed;
    }

    @Override
    public ConfigReference getReference() {
        return getObserved();
    }
}
