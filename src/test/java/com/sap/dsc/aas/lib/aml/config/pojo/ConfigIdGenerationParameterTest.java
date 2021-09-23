/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dsc.aas.lib.aml.exceptions.AlreadyDefinedException;

public class ConfigIdGenerationParameterTest {

    private ConfigIdGenerationParameter classUnderTest;

    @BeforeEach
    void setup() {
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
