/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.List;

import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;

public class ConfigMultiLanguageProperty extends AbstractConfigSubmodelElement implements ConfigReferenceContainer, ConfigDataElement {

    public static final String DEFAULT_ATTRIBUTE_XPATH = "caex:Attribute[starts-with(@Name, 'aml-lang=')]";
    public static final String DEFAULT_VALUE_XPATH = "Value";

    private List<ConfigLanguageProperty> values;
    private ConfigReference valueId;

    public List<ConfigLanguageProperty> getValues() {
        return values;
    }

    public void setValues(List<ConfigLanguageProperty> values) {
        this.values = values;
    }

    public ConfigReference getValueIdReference() {
        return valueId;
    }

    public void setValueId(ConfigReference valueId) {
        this.valueId = valueId;
    }

    @Override
    public ConfigReference getReference() {
        return getValueIdReference();
    }
}
