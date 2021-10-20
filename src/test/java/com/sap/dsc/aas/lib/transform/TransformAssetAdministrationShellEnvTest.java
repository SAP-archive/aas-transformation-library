/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.dom4j.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.pojo.*;
import com.sap.dsc.aas.lib.exceptions.TransformationException;

import io.adminshell.aas.v3.model.*;

public class TransformAssetAdministrationShellEnvTest extends AbstractTransformerTest {

    private AssetAdministrationShellEnvTransformer classUnderTest;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.setAMLBindings();
        when(mockIdGenerator.generateId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn("1234");
        this.classUnderTest = new AssetAdministrationShellEnvTransformer(mockIdGenerator, mockPreconditionValidator);
    }

    @Test
    @DisplayName("Create a shell with references to asset and submodels")
    void createAssetAdministrationShell() throws TransformationException {
        String shellId = "1234";
        String assetGlobalIdIRI = "http://globalAssetId.com";

        ConfigReference configGlobalIdReference = new ConfigReference();
        configGlobalIdReference.setKeyType(KeyType.IRI);
        configGlobalIdReference.setKeyElement(KeyElements.ASSET);
        configGlobalIdReference.setValueId(assetGlobalIdIRI);

        ArrayList<Reference> submodels = new ArrayList<>();

        ConfigAssetShell configAssetShell = new ConfigAssetShell();
        configAssetShell.setIdGeneration(createSimpleIdGeneration(shellId));
        configAssetShell.setIdShortXPath("'Shell'");

        ConfigAssetInformation configAssetInformation = new ConfigAssetInformation();
        configAssetInformation.setKindTypeXPath("INSTANCE");
        configAssetInformation.setGlobalAssetIdReference(configGlobalIdReference);

        AssetAdministrationShell shell =
            classUnderTest.createAssetAdministrationShell(unitClass, configAssetShell, configAssetInformation, submodels);

        assertThat(shell).isNotNull();
        assertThat(shell.getIdentification().getIdentifier()).isEqualTo(shellId);
        assertThat(shell.getIdShort()).isEqualTo("Shell");
        assertThat(shell.getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue()).isEqualTo(ID_VALUE);
        assertThat(shell.getSubmodels()).isNotNull();
        assertThat(shell.getSubmodels()).isEmpty();
    }

    @Test
    @DisplayName("Create single shell env, containing shell, asset and asset related submodel")
    void createSingleAssetAdministrationShellEnv() throws TransformationException {
        AssetAdministrationShellEnvironment result = classUnderTest.createSingleAssetAdministrationShellEnv(unitClass, configMapping);

        assertThat(result).isNotNull();
        assertThat(result.getConceptDescriptions()).isNotNull();
        assertThat(result.getConceptDescriptions()).hasSize(0);
        assertThat(result.getAssetAdministrationShells()).isNotNull();
        assertThat(result.getAssetAdministrationShells()).hasSize(1);
        assertThat(result.getSubmodels()).isNotNull();
    }

    @Test
    @DisplayName("Create a flat shell env, containing all assets, shells and submodels, found in the AML")
    void createShellEnv() throws TransformationException {
        AssetAdministrationShellEnvironment result = classUnderTest.createShellEnv(document, mapping.getConfigMappings());

        assertThat(result).isNotNull();
        verify(mockPreconditionValidator, times(2))
            .validate(or(any(ConfigMapping.class), isNull()), or(anyList(), isNull()));
    }
}
