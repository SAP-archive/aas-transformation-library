/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config;

import java.io.IOException;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dsc.aas.lib.config.pojo.ConfigTransformToAas;

public class ConfigLoader {
    private final ObjectMapper objectMapper;

    public ConfigLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public ConfigTransformToAas loadConfig(String filePath) throws IOException {
        return objectMapper.readValue(Paths.get(filePath).toFile(), ConfigTransformToAas.class);
    }

}
