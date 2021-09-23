/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.ArrayList;
import java.util.List;

import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;

public class ConfigOperation extends AbstractConfigSubmodelElement implements ConfigSubmodelElementContainer {

    private List<AbstractConfigSubmodelElement> inputVariables;
    private List<AbstractConfigSubmodelElement> outputVariables;
    private List<AbstractConfigSubmodelElement> inOutputVariables;

    public List<AbstractConfigSubmodelElement> getInputVariables() {
        return inputVariables;
    }

    public void setInputVariables(List<AbstractConfigSubmodelElement> inputVariables) {
        this.inputVariables = inputVariables;
    }

    public List<AbstractConfigSubmodelElement> getOutputVariables() {
        return outputVariables;
    }

    public void setOutputVariables(List<AbstractConfigSubmodelElement> outputVariables) {
        this.outputVariables = outputVariables;
    }

    public List<AbstractConfigSubmodelElement> getInOutputVariables() {
        return inOutputVariables;
    }

    public void setInOutputVariables(List<AbstractConfigSubmodelElement> inOutputVariables) {
        this.inOutputVariables = inOutputVariables;
    }

    @Override
    public List<AbstractConfigSubmodelElement> getSubmodelElements() {
        List<AbstractConfigSubmodelElement> allSubmodelElements = new ArrayList<>();
        if (getInputVariables() != null) {
            allSubmodelElements.addAll(getInputVariables());
        }
        if (getOutputVariables() != null) {
            allSubmodelElements.addAll(getOutputVariables());
        }
        if (getInOutputVariables() != null) {
            allSubmodelElements.addAll(getInOutputVariables());
        }
        return allSubmodelElements;
    }
}
