/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.dom4j.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.aml.exceptions.NoResultByXPathException;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;

import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.AssetKind;

public class TransformAssetInformationTest extends AbstractTransformerTest {

    private AssetInformationTransformer classUnderTest;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(mockIdGenerator.generateId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn(AbstractTransformerTest.ID_VALUE);
        when(mockIdGenerator.generateSemanticId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn("mySemanticId");
        this.classUnderTest = new AssetInformationTransformer(mockIdGenerator, mockPreconditionValidator);
    }

    @Test
    @DisplayName("Create an AssetInformation object")
    void createAssetInformation() {
        AssetInformation assetInformation = classUnderTest.createAssetInformation(unitClass, configAssetInformation);
        assertThat(assetInformation).isNotNull();
        assertThat(assetInformation.getAssetKind()).isEqualTo(AssetKind.TYPE);

        configAssetInformation.setKindTypeXPath("INSTANCE");

        assetInformation = classUnderTest.createAssetInformation(unitClass, configAssetInformation);

        assertThat(assetInformation).isNotNull();
        assertThat(assetInformation.getAssetKind()).isEqualTo(AssetKind.INSTANCE);
    }

    @Test
    @DisplayName("Get based on a XPath the asset kind type")
    void getAssetKindType() throws TransformationException {
        assertThat(classUnderTest.getAssetKindType(unitClass, "'TYPE'")).isEqualTo(Arrays.asList(AssetKind.TYPE));
        assertThat(classUnderTest.getAssetKindType(unitClass, "'INSTANCE'")).isEqualTo(Arrays.asList(AssetKind.INSTANCE));
        assertThrows(NoResultByXPathException.class, () -> classUnderTest.getAssetKindType(unitClass, "DoesNotExist"));
    }

}
