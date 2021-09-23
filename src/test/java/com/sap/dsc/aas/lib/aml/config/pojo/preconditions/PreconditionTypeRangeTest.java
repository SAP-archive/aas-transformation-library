/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class PreconditionTypeRangeTest extends AbstractPreconditionTypeTest {

    private PreconditionTypeRange classUnderTest;

    @BeforeEach
    void setup() {
        super.setup();
        classUnderTest = new PreconditionTypeRange();
    }

    @Test
    void fromJsonStringValid() throws JsonMappingException, JsonProcessingException {
        String input = "{\"@type\":\"Range\", \"from_xpath\": \"Attribute[@Name='Model']/Value\"}";
        PreconditionTypeRange result = (PreconditionTypeRange) objectMapper.readValue(input, AbstractPreconditionTypeForEach.class);
        assertEquals(PreconditionTypeRange.DEFAULT_MINIMUM_NUMBER, result.getMinimumNumber());
        assertEquals(PreconditionTypeRange.DEFAULT_MAXIMUM_NUMBER, result.getMaximumNumber());

        input = "{\"@type\":\"Range\", \"from_xpath\": \"\", \"minimumNumber\": 0, \"maximumNumber\": 1}";
        result = (PreconditionTypeRange) objectMapper.readValue(input, AbstractPreconditionTypeForEach.class);
        assertEquals(0, result.getMinimumNumber());
        assertEquals(1, result.getMaximumNumber());
    }

    @Test
    void fromJsonStringInvalid() {
        assertThrows(MismatchedInputException.class, () -> objectMapper.readValue("{}", AbstractPreconditionTypeForEach.class));
    }

}
