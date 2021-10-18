/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.placeholder;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.placeholder.exceptions.PlaceholderValueMissingException;

import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;

public class PlaceholderHandlingTest {

    private PlaceholderHandling classUnderTest;

    @BeforeEach
    void setup() throws Exception {
		TestUtils.resetBindings();
        this.classUnderTest = new PlaceholderHandling();
    }

    @Test
    void testReplacementWithoutPlaceholderValues() throws SerializationException, DeserializationException {
        AssetAdministrationShellEnvironment assetShellEnv = new DefaultAssetAdministrationShellEnvironment.Builder().build();
        AssetAdministrationShellEnvironment result = classUnderTest.replaceAllPlaceholders(assetShellEnv, null);

        assertThat(result).isEqualTo(assetShellEnv);
    }

    @Test
    void testReplacement() throws SerializationException, DeserializationException {
        Submodel submodel = new DefaultSubmodel.Builder()
            .idShort("${{placeholderName}}")
            .identification(createSampleIdentifier("identifier", IdentifierType.CUSTOM))
            .build();

        AssetAdministrationShellEnvironment assetShellEnv = new DefaultAssetAdministrationShellEnvironment.Builder()
            .submodels(Collections.singletonList(submodel))
            .build();

        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("placeholderName", "placeholderValue");

        AssetAdministrationShellEnvironment result = classUnderTest.replaceAllPlaceholders(assetShellEnv, placeholderValues);

        assertThat(result.getSubmodels().get(0).getIdShort()).isEqualTo("placeholderValue");
    }

    @Test
    void testReplacementWithMissingPlaceholders() {
        Submodel submodel1 = new DefaultSubmodel.Builder()
            .idShort("${{placeholderName1}}")
            .identification(createSampleIdentifier("identifier", IdentifierType.CUSTOM))
            .build();

        Submodel submodel2 = new DefaultSubmodel.Builder()
            .idShort("${{placeholderName2}}")
            .identification(createSampleIdentifier("identifier", IdentifierType.CUSTOM))
            .build();

        AssetAdministrationShellEnvironment assetShellEnv = new DefaultAssetAdministrationShellEnvironment.Builder()
            .submodels(Arrays.asList(submodel1, submodel2))
            .build();

        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("placeholderName1", "placeholderValue");
        assertThrows(PlaceholderValueMissingException.class, () -> classUnderTest.replaceAllPlaceholders(assetShellEnv, placeholderValues));
    }

    @Test
    void testReplacementWithMultiplePlaceholders() throws SerializationException, DeserializationException {
        Submodel submodel1 = new DefaultSubmodel.Builder()
            .idShort("${{placeholderName1}}")
            .identification(createSampleIdentifier("identifier", IdentifierType.CUSTOM))
            .build();

        Submodel submodel2 = new DefaultSubmodel.Builder()
            .idShort("${{placeholder-Name2}}")
            .identification(createSampleIdentifier("identifier", IdentifierType.CUSTOM))
            .build();

        Submodel submodel3 = new DefaultSubmodel.Builder()
            .idShort("${{placeholderName1}} and ${{placeholder-Name2}}")
            .identification(createSampleIdentifier("identifier", IdentifierType.CUSTOM))
            .build();

        Submodel submodel4 = new DefaultSubmodel.Builder()
            .idShort("${{}}")
            .identification(createSampleIdentifier("identifier", IdentifierType.CUSTOM))
            .build();

        AssetAdministrationShellEnvironment assetShellEnv = new DefaultAssetAdministrationShellEnvironment.Builder()
            .submodels(Arrays.asList(submodel1, submodel2, submodel3, submodel4))
            .build();

        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("placeholderName1", "placeholderValue1");
        placeholderValues.put("placeholder-Name2", "placeholderValue2");

        AssetAdministrationShellEnvironment result = classUnderTest.replaceAllPlaceholders(assetShellEnv, placeholderValues);

        assertThat(result.getSubmodels().get(0).getIdShort()).isEqualTo("placeholderValue1");
        assertThat(result.getSubmodels().get(1).getIdShort()).isEqualTo("placeholderValue2");
        assertThat(result.getSubmodels().get(2).getIdShort()).isEqualTo("placeholderValue1 and placeholderValue2");
        assertThat(result.getSubmodels().get(3).getIdShort()).isEqualTo("${{}}");
    }

    Identifier createSampleIdentifier(String id, IdentifierType idType) {
        return new DefaultIdentifier.Builder().idType(idType).identifier(id).build();
    }

}
