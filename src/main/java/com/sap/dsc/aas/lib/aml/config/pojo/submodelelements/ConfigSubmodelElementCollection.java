/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.List;

import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;

public class ConfigSubmodelElementCollection extends AbstractConfigSubmodelElement implements ConfigSubmodelElementContainer {

    /**
     * A "collection" submodel element may have other submodel elements as children
     */
    private List<AbstractConfigSubmodelElement> submodelElements;

    @Override
    public List<AbstractConfigSubmodelElement> getSubmodelElements() {
        return submodelElements;
    }

    public void setSubmodelElements(List<AbstractConfigSubmodelElement> submodelElements) {
        this.submodelElements = submodelElements;
    }
}
