/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.ConfigLoader;
import com.sap.dsc.aas.lib.exceptions.AlreadyDefinedException;

public class AbstractConfigFromAttributeTest {

    @BeforeEach
    void setup() throws Exception {
		TestUtils.resetBindings();
    }
    
    @Test
    void invalidConfigXPathAlreadyDefined() {
        final AbstractConfigFromAttribute configObject1 = new AbstractConfigFromAttribute() {};
        configObject1.setAttributeName("IdentificationData");
        assertThrows(AlreadyDefinedException.class, () -> configObject1.setXPath(""));

        final AbstractConfigFromAttribute configObject2 = new AbstractConfigFromAttribute() {};
        configObject2.setXPath("caex:Attribute[@Name='IdentificationData']");
        assertThrows(AlreadyDefinedException.class, () -> configObject2.setAttributeName(""));
    }

    @Test
    void invalidSemanticIdAlreadyDefined() {
        final AbstractConfigFromAttribute configObject1 = new AbstractConfigFromAttribute() {};
        configObject1.setSemanticId(new ConfigReference());
        assertThrows(AlreadyDefinedException.class, () -> configObject1.setSemanticId(""));

        final AbstractConfigFromAttribute configObject2 = new AbstractConfigFromAttribute() {};
        configObject2.setSemanticId("MySemanticId");
        assertThrows(AlreadyDefinedException.class, () -> configObject2.setSemanticId(new ConfigReference()));
    }

    @Test
    void semanticId() {
        final AbstractConfigFromAttribute configObject1 = new AbstractConfigFromAttribute() {};
        ConfigReference semanticIdReference = new ConfigReference();
        semanticIdReference.setValueId("MySemanticId");
        configObject1.setSemanticId(semanticIdReference);

        final AbstractConfigFromAttribute configObject2 = new AbstractConfigFromAttribute() {};
        configObject2.setSemanticId("MySemanticId");

        assertEquals(configObject1.getSemanticIdReference().getIdGeneration().getParameters().get(0).getValueDefault(),
            configObject2.getSemanticIdReference().getIdGeneration().getParameters().get(0).getValueDefault());
    }

    @Test
    void loadFromString() throws JsonMappingException, JsonProcessingException {
        String expectedXPath = "caex:Attribute[@Name='IdentificationData']";
        String json = "{ \"from_xpath\" : \"" + expectedXPath + "\", \"idShort_xpath\": \"'Hello World'\" }";
        ObjectMapper objectMapper = new ObjectMapper();

        ConfigSubmodel submodel = objectMapper.readValue(json, ConfigSubmodel.class);
        assertEquals(expectedXPath, submodel.getXPath());
        assertEquals("'Hello World'", submodel.getIdShortXPath());

        String json2 = "{ \"from_attributeName\" : \"IdentificationData\" }";
        ConfigSubmodel submodel2 = objectMapper.readValue(json2, ConfigSubmodel.class);
        assertEquals(expectedXPath, submodel2.getXPath());
        assertEquals("@Name", submodel2.getIdShortXPath());
    }

}
