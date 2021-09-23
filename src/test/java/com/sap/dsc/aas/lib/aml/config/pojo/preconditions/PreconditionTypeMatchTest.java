/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class PreconditionTypeMatchTest extends AbstractPreconditionTypeTest {

    private PreconditionTypeMatch classUnderTest;

    @BeforeEach
    void setup() {
        super.setup();
        classUnderTest = new PreconditionTypeMatch();
    }

    @Test
    void fromJsonStringValid() throws JsonMappingException, JsonProcessingException {
        String input = "{\"@type\":\"Match\", \"pattern\": \"[a-z]\", \"from_xpath\": \"Attribute[@Name='Model']/Value\"}";
        assertEquals(PreconditionTypeMatch.class, objectMapper.readValue(input, AbstractPreconditionTypeForEach.class).getClass());
    }

    @Test
    void fromJsonStringInvalid() {
        assertThrows(MismatchedInputException.class, () -> objectMapper.readValue("{}", AbstractPreconditionTypeForEach.class));
        assertThrows(MismatchedInputException.class,
            () -> objectMapper.readValue("{\"@type\":\"Match\"}", AbstractPreconditionTypeForEach.class));
        assertThrows(MismatchedInputException.class,
            () -> objectMapper.readValue("{\"@type\":\"Match\", \"pattern\": \"\"}", AbstractPreconditionTypeForEach.class));
        assertThrows(MismatchedInputException.class,
            () -> objectMapper.readValue("{\"@type\":\"Match\", \"from_xpath\": \"\"}", AbstractPreconditionTypeForEach.class));
    }

    @Test
    void invalidPattern() {
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.setPattern("[a-z"));
    }

}
