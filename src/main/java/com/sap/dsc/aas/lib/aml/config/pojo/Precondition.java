/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.preconditions.AbstractPreconditionTypeForEach;
import com.sap.dsc.aas.lib.aml.config.pojo.preconditions.PreconditionTypeForAll;

public class Precondition {

    private String preconditionName;
    private String preconditionDescription;
    private String configElementId;
    private PreconditionTypeForAll forAll;
    private List<AbstractPreconditionTypeForEach> forEach;

    public Precondition() {}

    @JsonCreator
    public Precondition(@JsonProperty(value = "configElementId", required = true) String configElementId) {
        this.configElementId = configElementId;
    }

    public String getPreconditionName() {
        return preconditionName;
    }

    public void setPreconditionName(String preconditionName) {
        this.preconditionName = preconditionName;
    }

    public String getPreconditionDescription() {
        return preconditionDescription;
    }

    public void setPreconditionDescription(String preconditionDescription) {
        this.preconditionDescription = preconditionDescription;
    }

    public String getConfigElementId() {
        return configElementId;
    }

    public void setConfigElementId(String configElementId) {
        this.configElementId = configElementId;
    }

    public PreconditionTypeForAll getForAll() {
        return forAll;
    }

    public void setForAll(PreconditionTypeForAll forAll) {
        this.forAll = forAll;
    }

    public List<AbstractPreconditionTypeForEach> getForEach() {
        return forEach;
    }

    public void setForEach(List<AbstractPreconditionTypeForEach> forEach) {
        this.forEach = forEach;
    }

}
