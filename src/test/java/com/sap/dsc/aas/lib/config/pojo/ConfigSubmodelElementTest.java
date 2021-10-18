/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigAnnotatedRelationshipElement;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigBasicEvent;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigBlob;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigCapability;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigEntity;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigFile;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigMultiLanguageProperty;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigOperation;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigProperty;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigRange;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigReferenceElement;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigRelationshipElement;
import com.sap.dsc.aas.lib.exceptions.DataElementTypeNotSupportedException;

public class ConfigSubmodelElementTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
		TestUtils.resetBindings();
        objectMapper = new ObjectMapper();
    }

    static Stream<Arguments> listOfDataElements() {
        return Stream.of(
            arguments(false, Collections.singletonList(new ConfigProperty())),
            arguments(false, Arrays.asList(new ConfigFile(null), new ConfigBlob(null))),
            arguments(false, Arrays
                .asList(new ConfigFile(null), new ConfigBlob(null), new ConfigMultiLanguageProperty(), new ConfigRange(),
                    new ConfigProperty(null), new ConfigReferenceElement())),
            arguments(true, Collections.singletonList(new ConfigBasicEvent())),
            arguments(true, Arrays.asList(new ConfigOperation(), new ConfigBasicEvent())));
    }

    @ParameterizedTest
    @MethodSource("listOfDataElements")
    void setAnnotationsOnConfigAnnotatedRelationshipElement(boolean throwsException, List<AbstractConfigSubmodelElement> modelTypes) {
        ConfigAnnotatedRelationshipElement configElement = new ConfigAnnotatedRelationshipElement(null, null);

        if (throwsException) {
            assertThrows(DataElementTypeNotSupportedException.class, () -> configElement.setAnnotations(modelTypes));
        } else {
            assertDoesNotThrow(() -> configElement.setAnnotations(modelTypes));
        }
    }

    static Stream<Arguments> listOfConfigSubmodelElements() {
        return Stream.of(
            arguments("ConfigProperty",
                "\"@type\": \"Property\", \"valueType\": \"STRING\", \"valueDefault\": \"abc\", \"valueXPath\": \"abc\"",
                ConfigProperty.class),
            arguments("ConfigMultiLanguageProperty",
                "\"@type\": \"MultiLanguageProperty\", \"values\": [{ \"langXPath\": \"'en'\", \"valueXPath\": \"abc\"}]",
                ConfigMultiLanguageProperty.class),
            arguments("ConfigRange",
                "\"@type\": \"Range\", \"valueType\": \"INTEGER_MEASURE\", \"minValueXPath\": \"1\", \"maxValueXPath\": \"10\" ",
                ConfigRange.class),
            arguments("ConfigEntity",
                "\"@type\": \"Entity\", \"entityType\": \"CO_MANAGED_ENTITY\" ", ConfigEntity.class),
            arguments("ConfigReferenceElement",
                "\"@type\": \"ReferenceElement\", \"value\": { \"valueId\": \"SomeReference\"} ", ConfigReferenceElement.class),
            arguments("ConfigCapability",
                "\"@type\": \"Capability\", \"idShort_xpath\": \"'abc'\" ", ConfigCapability.class),
            arguments("ConfigAnnotatedRelationshipElement",
                "\"@type\": \"AnnotatedRelationshipElement\", \"idShort_xpath\": \"'abc'\", \"first\": { \"valueId\": \"firstReference\" }, \"second\": { \"valueId\": \"secondReference\" }",
                ConfigAnnotatedRelationshipElement.class),
            arguments("ConfigRelationshipElement",
                "\"@type\": \"RelationshipElement\", \"idShort_xpath\": \"'abc'\", \"first\": { \"valueId\": \"firstReference\" }, \"second\": { \"valueId\": \"secondReference\" }",
                ConfigRelationshipElement.class),
            arguments("ConfigBlob",
                "\"@type\": \"Blob\", \"mimeTypeXPath\": \"text/plain\", \"idShort_xpath\": \"'abc'\" ", ConfigBlob.class),
            arguments("ConfigOperation",
                "\"@type\": \"Operation\", \"idShort_xpath\": \"'abc'\" ", ConfigOperation.class),
            arguments("ConfigFile",
                "\"@type\": \"File\", \"mimeTypeXPath\": \"text/plain\", \"valueXPath\": \"file://test.txt\" ", ConfigFile.class));
    }

    @ParameterizedTest(name = "Based on the type, create a {0} object")
    @MethodSource("listOfConfigSubmodelElements")
    void createConfigProperty(String modelType, String submodelElementJson, Class<?> type) throws Exception {
        String json = createJson(submodelElementJson);

        ConfigSubmodel submodel = objectMapper.readValue(json, ConfigSubmodel.class);
        List<AbstractConfigSubmodelElement> elements = submodel.getSubmodelElements();

        assertNotNull(elements);
        assertThat(elements).hasSize(1);

        assertThat(elements.get(0)).isInstanceOf(type);
        assertThat(elements.get(0).getType()).isNotNull();
    }

    String createJson(String submodelElementPart) {
        return "{\n"
            + "   \"submodelElements\": [\n"
            + "       {\n"
            + submodelElementPart
            + "       }\n"
            + "    ]\n"
            + "}";
    }

}
