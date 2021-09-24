/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo;

import java.util.List;

public class ConfigSubmodel extends AbstractConfigFromAttribute {

    private ConfigIdGeneration idGeneration;
    private List<AbstractConfigSubmodelElement> submodelElements;

    public List<AbstractConfigSubmodelElement> getSubmodelElements() {
        return submodelElements;
    }

    public void setSubmodelElements(List<AbstractConfigSubmodelElement> submodelElements) {
        this.submodelElements = submodelElements;
    }

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(ConfigIdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }
}
