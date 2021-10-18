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
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sap.dsc.aas.lib.TestUtils;

public class ConfigPlaceholderTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
		TestUtils.resetBindings();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void fromJsonString() throws JsonMappingException, JsonProcessingException {
        String input = "{\"name\": \"placeholderName\", \"description\": \"ui text\"}";

        ConfigPlaceholder configPlaceholder = objectMapper.readValue(input, ConfigPlaceholder.class);
        assertEquals("placeholderName", configPlaceholder.getName());
        assertEquals("ui text", configPlaceholder.getDescription());

        assertThrows(MismatchedInputException.class, () -> objectMapper.readValue("{}", ConfigPlaceholder.class));
    }

}
