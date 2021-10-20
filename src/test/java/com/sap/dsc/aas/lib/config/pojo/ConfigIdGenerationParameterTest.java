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
import com.sap.dsc.aas.lib.exceptions.AlreadyDefinedException;

public class ConfigIdGenerationParameterTest {

    private ConfigIdGenerationParameter classUnderTest;

    @BeforeEach
    void setup() throws Exception {
		TestUtils.resetBindings();
        this.classUnderTest = new ConfigIdGenerationParameter();
    }

    @Test
    void loadFromJson() throws JsonMappingException, JsonProcessingException {
        String json = "{ \"from_idGenerationName\" : \"myAssetIdGeneration\"}";
        ObjectMapper objectMapper = new ObjectMapper();

        ConfigIdGenerationParameter parameter = objectMapper.readValue(json, ConfigIdGenerationParameter.class);
        assertEquals("myAssetIdGeneration", parameter.getIdGenerationName());
        assertEquals(ConfigIdGenerationFinalizeFunction.CONCATENATE, parameter.getFinalizeFunction());
    }

    @Test
    void xPathAlreadySet() {
        this.classUnderTest.setXPath("/test");

        assertThrows(AlreadyDefinedException.class, () -> classUnderTest.setIdGenerationName(""));
    }

    @Test
    void idNameAlreadySet() {
        this.classUnderTest.setIdGenerationName("myAssetId");

        assertThrows(AlreadyDefinedException.class, () -> classUnderTest.setXPath(""));
    }

}
