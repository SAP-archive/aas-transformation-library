/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;

public class ConfigReferenceElement extends AbstractConfigSubmodelElement implements ConfigReferenceContainer, ConfigDataElement {

    private ConfigReference value;

    public ConfigReference getValue() {
        return value;
    }

    public void setValue(ConfigReference value) {
        this.value = value;
    }

    @Override
    public ConfigReference getReference() {
        return getValue();
    }
}
