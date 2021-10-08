/*
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved.

  SPDX-License-Identifier: Apache-2.0
 */
package com.sap.dsc.aas.lib.mapping.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface for the configuration of AAS model templates.
 */
public interface Template {
	BindSpecification getBindSpecification();

	@JsonProperty("@bind")
	void setBindSpecification(BindSpecification bindSpecification);

	Map<String, Object> getVariables();

	@JsonProperty("@vars")
	void setVariables(Map<String, Object> variables);

	Map<String, Object> getDefinitions();

	@JsonProperty("@definitions")
	void setDefinitions(Map<String, Object> definitions);
}
