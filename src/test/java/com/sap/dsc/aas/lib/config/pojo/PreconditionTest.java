/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sap.dsc.aas.lib.TestUtils;

public class PreconditionTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
		TestUtils.resetBindings();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void fromJsonString() throws JsonMappingException, JsonProcessingException {
        String input = "{\"configElementId\":\"myAssetConfig\", \"preconditionDescription\": \"myDescription\", "
            + "\"forEach\": [{\"from_xpath\": \"\", \"@type\": \"Range\"}]}";
        Precondition precondition = objectMapper.readValue(input, Precondition.class);
        assertEquals("myAssetConfig", precondition.getConfigElementId());
        assertEquals("myDescription", precondition.getPreconditionDescription());

        assertNull(precondition.getForAll());
        assertEquals(1, precondition.getForEach().size());

        assertThrows(MismatchedInputException.class, () -> objectMapper.readValue("{}", Precondition.class));
    }

}
