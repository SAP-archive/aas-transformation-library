/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

import com.fasterxml.jackson.databind.ObjectMapper;

abstract class AbstractPreconditionTypeTest {

    protected ObjectMapper objectMapper;

    void setup() {
        this.objectMapper = new ObjectMapper();
    }

}
