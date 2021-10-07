/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo.submodelelements;

import java.util.List;

import com.sap.dsc.aas.lib.config.pojo.AbstractConfigSubmodelElement;

public interface ConfigSubmodelElementContainer {

    List<AbstractConfigSubmodelElement> getSubmodelElements();
}
