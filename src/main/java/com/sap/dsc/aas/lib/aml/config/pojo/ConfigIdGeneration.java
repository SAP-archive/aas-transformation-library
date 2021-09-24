/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo;

import java.util.List;

import com.sap.dsc.aas.lib.aml.exceptions.MissingConfigException;

public class ConfigIdGeneration {

    private List<ConfigIdGenerationParameter> parameters;
    private String idGenerationName;
    private ConfigIdGenerationFinalizeFunction finalizeFunction = ConfigIdGenerationFinalizeFunction.CONCATENATE;

    public List<ConfigIdGenerationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ConfigIdGenerationParameter> parameters) {
        if (parameters.isEmpty()) {
            throw new MissingConfigException("Pass at least one parameter for idGeneration.");
        }
        this.parameters = parameters;
    }

    public String getIdGenerationName() {
        return idGenerationName;
    }

    public void setIdGenerationName(String idName) {
        this.idGenerationName = idName;
    }

    public ConfigIdGenerationFinalizeFunction getFinalizeFunction() {
        return finalizeFunction;
    }

    public void setFinalizeFunction(String finalizeFunction) {
        this.finalizeFunction = ConfigIdGenerationFinalizeFunction.valueOf(finalizeFunction.toUpperCase());
    }

}
