/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.config.pojo.ConfigSubmodel;
import com.sap.dsc.aas.lib.exceptions.TransformationException;

import io.adminshell.aas.v3.model.Submodel;

public class TransformSubmodelTest extends AbstractTransformerTest {

    private SubmodelTransformer classUnderTest;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.setAMLBindings();
        when(mockIdGenerator.generateId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn(AbstractTransformerTest.ID_VALUE + "_submodel");
        when(mockIdGenerator.generateSemanticId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn("mySemanticId");

        this.classUnderTest = new SubmodelTransformer(mockIdGenerator, mockPreconditionValidator);
    }

    @Test
    @DisplayName("Create single submodel")
    void createSingleSubmodel() throws TransformationException, URISyntaxException {
        Submodel submodel = classUnderTest.createSubmodel(attribute, configSubmodel);

        assertNotNull(submodel);
        assertEquals("MyAttribute", submodel.getIdShort());
        assertNotNull(submodel.getSemanticId());
        assertEquals("mySemanticId", submodel.getSemanticId().getKeys().get(0).getValue());
        assertEquals(AbstractTransformerTest.ID_VALUE + "_submodel", submodel.getIdentification().getIdentifier());
    }

    @Test
    @DisplayName("Create list of submodels from a single configSubmodel")
    void createSubmodelsFromConfigSubmodel() throws TransformationException {
        List<Submodel> submodels = classUnderTest.createSubmodelsFromConfigSubmodel(unitClass, configSubmodel);

        assertNotNull(submodels);
        assertThat(submodels).hasSize(1);

        verify(mockPreconditionValidator, times(1))
            .validate(or(any(ConfigSubmodel.class), isNull()), or(anyList(), isNull()));

        submodels = classUnderTest.createSubmodelsFromConfigSubmodel(unitClass, configSubmodelMultipleAttributes);

        assertNotNull(submodels);
        assertThat(submodels).hasSize(2);

        verify(mockPreconditionValidator, times(2))
            .validate(or(any(ConfigSubmodel.class), isNull()), or(anyList(), isNull()));
    }

    @Test
    @DisplayName("Create list of submodels from list of config submodels")
    void createSubmodels() throws TransformationException {
        List<Submodel> submodels =
            classUnderTest.createSubmodels(unitClass, Arrays.asList(configSubmodel, configSubmodelMultipleAttributes));

        assertNotNull(submodels);
        assertThat(submodels).hasSize(3);

        verify(mockPreconditionValidator, times(2))
            .validate(or(any(ConfigSubmodel.class), isNull()), or(anyList(), isNull()));
    }

}
