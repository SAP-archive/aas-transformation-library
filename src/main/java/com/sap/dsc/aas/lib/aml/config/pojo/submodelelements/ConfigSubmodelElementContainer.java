/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.List;

import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;

public interface ConfigSubmodelElementContainer {

    List<AbstractConfigSubmodelElement> getSubmodelElements();
}
