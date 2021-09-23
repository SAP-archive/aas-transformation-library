/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config;

import java.io.IOException;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigAmlToAas;

public class ConfigLoader {
    private final ObjectMapper objectMapper;

    public ConfigLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public ConfigAmlToAas loadConfig(String filePath) throws IOException {
        return objectMapper.readValue(Paths.get(filePath).toFile(), ConfigAmlToAas.class);
    }

}
