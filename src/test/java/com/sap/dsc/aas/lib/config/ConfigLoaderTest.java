/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.pojo.ConfigMapping;
import com.sap.dsc.aas.lib.config.pojo.ConfigTransformToAas;

public class ConfigLoaderTest {

    public static final String PATH_SIMPLE_CONFIG = "src/test/resources/config/simpleConfig.json";
    public static final String PATH_CONFIG_NSBINDING = "src/test/resources/config/nsbindingConfig.json";
    private ConfigLoader classUnderTest;

    @BeforeEach
    void setup() throws Exception {
		TestUtils.resetBindings();
        classUnderTest = new ConfigLoader();
    }

    @Test
    void loadFromFile() throws IOException {
        ConfigTransformToAas result = classUnderTest.loadConfig(PATH_SIMPLE_CONFIG);

        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        assertThat(result.getAasVersion()).isEqualTo("3.0RC01");
        assertThat(result.getConfigMappings()).isNotEmpty();

        ConfigMapping configMapping = result.getConfigMappings().get(0);
        assertThat(configMapping.getConfigAssetInformation().getKindTypeXPath()).contains("TYPE");
        assertThat(configMapping.getSubmodels()).hasSize(6);
    }

    @Test
    void loadNonexistentFile() {
        assertThrows(IOException.class, () -> classUnderTest.loadConfig("src/test/resources/config/doesNotExist.json"));
    }
    
    @Test
    void loadWithNSBindings() throws IOException {
        ConfigTransformToAas result = classUnderTest.loadConfig(PATH_CONFIG_NSBINDING);

        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        assertThat(result.getAasVersion()).isEqualTo("3.0RC01");
        assertThat(result.getNamespaceBindings().size()).isEqualTo(2);
    }
}
