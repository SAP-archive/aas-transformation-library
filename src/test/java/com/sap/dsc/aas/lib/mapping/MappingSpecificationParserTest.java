/*
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved.

  SPDX-License-Identifier: Apache-2.0
 */
package com.sap.dsc.aas.lib.mapping;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sap.dsc.aas.lib.exceptions.InvalidBindingException;
import com.sap.dsc.aas.lib.mapping.model.LegacyTemplate;
import com.sap.dsc.aas.lib.mapping.model.Mapping;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.mapping.model.Template;

import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.SubmodelElement;

public class MappingSpecificationParserTest {

    private MappingSpecificationParser parser;

    @BeforeEach
    void setup() {
        parser = new MappingSpecificationParser();
    }

    @Test
    void loadFromFile() throws IOException {
        MappingSpecification result = parser.loadMappingSpecification("src/test/resources/mappings/simpleMapping.json");

        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        assertThat(result.getAasVersion()).isEqualTo("3.0RC01");
        assertThat(result.getAasEnvironmentMapping()).isNotNull();

        AssetAdministrationShellEnvironment mapping = result.getAasEnvironmentMapping();
        assertThat(mapping instanceof LegacyTemplate);
        assertThat(((LegacyTemplate) mapping.getAssetAdministrationShells().get(0).getAssetInformation()).getKindTypeXPath()).contains("Type");

        assertThat(mapping.getSubmodels()).hasSize(6);
        mapping.getSubmodels().forEach(s -> assertThat(s).isInstanceOf(Template.class));

        // currently one sub model has a bind specification
        assertThat(mapping.getSubmodels().stream().filter(s -> {
            return s.getSubmodelElements().stream()
                .filter(se -> ((Template) se).getBindSpecification() != null)
                .findFirst().isPresent();
        }).collect(Collectors.toSet())).hasSize(1);
    }

    @Test
    void loadFileWithInvalidBindings() throws IOException {
        JsonMappingException e = assertThrows(JsonMappingException.class, () ->
            parser.loadMappingSpecification("src/test/resources/mappings/invalidBindings.json"));
        Throwable cause = e.getCause();
        assertThat(cause instanceof InvalidBindingException);
        assertThat(((InvalidBindingException) cause).getFields()).hasSize(2);
    }
    
	@Test
	void minimalTemplateExample() throws IOException {
		MappingSpecification result = parser
				.loadMappingSpecification("src/test/resources/mappings/simpleMapping_min.json");

		assertThat(result).isNotNull();
		assertThat(result.getVersion()).isEqualTo("1.0.0");
		assertThat(result.getAasVersion()).isEqualTo("3.0RC01");
		assertThat(result.getAasEnvironmentMapping()).isNotNull();

		AssetAdministrationShellEnvironment mapping = result.getAasEnvironmentMapping();
		assertThat(mapping instanceof LegacyTemplate);
		assertThat(((LegacyTemplate) mapping.getAssetAdministrationShells().get(0).getAssetInformation()).getKindTypeXPath()).contains("Type");

		SubmodelElement submodelElement = mapping.getSubmodels().get(0).getSubmodelElements().get(0);
		Template temp = (Template) submodelElement;
		assertThat(temp.getBindSpecification().getBindings().keySet()).containsAtLeastElementsIn(new String[] {"idShort", "value", "mimeType"});
		// assertThat(temp.getBindSpecification().getBindings().get("value")).isEqualTo("caex:Attribute[@Name='refURI']/caex:Value");
	}

    @Test
    void expressionsExample() throws IOException {
        MappingSpecification result = parser
            .loadMappingSpecification("src/test/resources/mappings/simpleMapping_w_expressions.json");

		AssetAdministrationShellEnvironment mapping = result.getAasEnvironmentMapping();

        SubmodelElement submodelElement = mapping.getSubmodels().get(0).getSubmodelElements().get(0);
        Template temp = (Template) submodelElement;
        assertThat(temp.getBindSpecification().getBindings().keySet()).containsAtLeastElementsIn(new String[] {"idShort", "value", "mimeType"});
    }
}