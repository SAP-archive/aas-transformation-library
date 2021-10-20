/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform.idgeneration;

import static com.google.common.truth.Truth.assertThat;
import static com.sap.dsc.aas.lib.config.pojo.ConfigIdGenerationFinalizeFunction.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.dom4j.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.hash.Hashing;
import com.sap.dsc.aas.lib.transform.AbstractTransformerTest;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGenerationFinalizeFunction;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGenerationParameter;
import com.sap.dsc.aas.lib.exceptions.IdGenerationNameAlreadyDefinedException;
import com.sap.dsc.aas.lib.exceptions.IdGenerationNameCyclicException;
import com.sap.dsc.aas.lib.exceptions.IdGenerationNameDoesNotExistException;

public class IdGeneratorGraphTest extends AbstractTransformerTest {

    private static final String ID_GENERATION_NAME_1 = "myIdGenerationName1";
    private static final String ID_GENERATION_NAME_2 = "myIdGenerationName2";
    private static final String ID_VALUE_NODE_2 = "myIdNode2";
    private static final String ID_GENERATION_NAME_3 = "myIdGenerationName3";
    private IdGeneratorGraph classUnderTest;
    private Node node;
    private List<ConfigIdGeneration> idGenerations;
    private IdGeneratorGraphNode graphNode1;
    private IdGeneratorGraphNode graphNode2;
    private IdGeneratorGraphNode graphNode3;

    private static Stream<Arguments> configIdGenerationParameterValues() {
        // User passes xpath and default value
        // --> try with xpath, result is null use default value
        ConfigIdGenerationParameter xpathAndDefaultNoMatch = new ConfigIdGenerationParameter();
        xpathAndDefaultNoMatch.setValueDefault(DEFAULT_VALUE);
        xpathAndDefaultNoMatch.setXPath("DoesNotExist");

        // User passes xpath and default value
        // --> try with xpath, result has value
        ConfigIdGenerationParameter xpathAndDefaultMatch = new ConfigIdGenerationParameter();
        xpathAndDefaultMatch.setValueDefault(DEFAULT_VALUE);
        xpathAndDefaultMatch.setXPath("@ID");

        // User does NOT pass xpath, user passes default value
        // --> Use default value ("hardcoded")
        ConfigIdGenerationParameter noXpathAndOnlyDefault = new ConfigIdGenerationParameter();
        noXpathAndOnlyDefault.setValueDefault(DEFAULT_VALUE);

        // User passes xpath, user does NOT pass default value
        // --> try with xpath, result is null
        ConfigIdGenerationParameter xpathAndNoDefaultNoMatch = new ConfigIdGenerationParameter();
        xpathAndNoDefaultNoMatch.setXPath("DoesNotExist");

        // User passes xpath, user does NOT pass default value
        // --> try with xpath, result has value
        ConfigIdGenerationParameter xpathAndNoDefaultMatch = new ConfigIdGenerationParameter();
        xpathAndNoDefaultMatch.setXPath("@ID");

        return Stream.of(
            arguments("Wrong XPath and Default set, expected default value", DEFAULT_VALUE, xpathAndDefaultNoMatch),
            arguments("XPath and Default set, expected xpath result value", "MyClassId", xpathAndDefaultMatch),
            arguments("No XPath set and Default set, expected default value", DEFAULT_VALUE, noXpathAndOnlyDefault),
            arguments("Wrong XPath and no Default set, expected null value", null, xpathAndNoDefaultNoMatch),
            arguments("XPath and no Default set, expected result value", "MyClassId", xpathAndNoDefaultMatch));
    }

    private static Stream<Arguments> argsResolveIdParameterWithReferenceAndFinalizeFunctions() {
        IdGeneratorGraph graph = new IdGeneratorGraph();
        String initial = "TestManufacturer/TestModel";
        String hashedIdGeneration1 = graph.finalizeId(initial, CONCATENATE_AND_HASH);

        return Stream.of(
            arguments("Hash idGeneration1 but use unhashed in reference", CONCATENATE_AND_HASH, CONCATENATE,
                hashedIdGeneration1, initial, initial + "/Submodel"),
            arguments("Unhashed idGeneration1 but use hashed in reference", CONCATENATE, CONCATENATE_AND_HASH,
                initial, hashedIdGeneration1, hashedIdGeneration1 + "/Submodel"));
    }

    private static Stream<Arguments> argsFinalizeId() {
        return Stream.of(
            arguments("Concatenate", ID_VALUE, ID_VALUE, CONCATENATE),
            arguments("Hashed", ID_VALUE, Hashing.sha256().hashString(ID_VALUE, StandardCharsets.UTF_8).toString(), CONCATENATE_AND_HASH),
            arguments("Url encoded", "hello world", "hello%20world", CONCATENATE_AND_URL_ENCODE),
            arguments("Url encoded", "hello/world ", "hello%2Fworld%20", CONCATENATE_AND_URL_ENCODE),
            arguments("Url encoded", "hello$world?", "hello%24world%3F", CONCATENATE_AND_URL_ENCODE),
            arguments("Url encoded", "hello&world+", "hello%26world%2B", CONCATENATE_AND_URL_ENCODE));
    }

    @BeforeEach
    void setup() throws Exception {
    	super.setUp();
        this.classUnderTest = new IdGeneratorGraph();
        this.node = mock(Node.class);
        this.idGenerations = createValidIdGenerations();
        for (ConfigIdGeneration idGeneration : this.idGenerations) {
            this.classUnderTest.addGraphNode(node, idGeneration);
        }
        this.graphNode1 = this.classUnderTest.getGraphNode(ID_GENERATION_NAME_1);
        this.graphNode2 = this.classUnderTest.getGraphNode(ID_GENERATION_NAME_2);
        this.graphNode3 = this.classUnderTest.getUnnamedGraphNodes().get(0);
    }

    private ConfigIdGeneration createIdGeneration(String idGenerationName) {
        ConfigIdGeneration idGeneration = new ConfigIdGeneration();
        idGeneration.setIdGenerationName(idGenerationName);
        idGeneration.setFinalizeFunction(ConfigIdGenerationFinalizeFunction.CONCATENATE.toString());
        return idGeneration;
    }

    private ConfigIdGeneration createIdGenerationWithReference(String idGenerationName, String referenceIdGenerationName) {
        ConfigIdGeneration idGeneration = createIdGeneration(idGenerationName);
        ConfigIdGenerationParameter parameter = createParameterWithReference(referenceIdGenerationName);
        idGeneration.setParameters(Arrays.asList(parameter));
        return idGeneration;
    }

    private ConfigIdGenerationParameter createParameterWithReference(String referenceIdGenerationName) {
        ConfigIdGenerationParameter parameter = new ConfigIdGenerationParameter();
        parameter.setIdGenerationName(referenceIdGenerationName);
        return parameter;
    }

    private ConfigIdGenerationParameter createParameterWithValue(String defaultValue) {
        ConfigIdGenerationParameter parameter = new ConfigIdGenerationParameter();
        parameter.setValueDefault(defaultValue);
        return parameter;
    }

    private ConfigIdGenerationParameter createInvalidParameterWithReference() {
        ConfigIdGenerationParameter parameter = new ConfigIdGenerationParameter();
        parameter.setIdGenerationName("DoesNotExist");
        return parameter;
    }

    /**
     * Returns a test graph with the following nodes and edges:
     *
     * node1 -> node2
     *
     * node2 -> "myIdNode2"
     *
     * node3 -> node1
     *
     * node3 -> "/"
     *
     * node3 -> node2
     *
     * @return
     */
    private List<ConfigIdGeneration> createValidIdGenerations() {
        ConfigIdGeneration idGeneration1 = createIdGenerationWithReference(ID_GENERATION_NAME_1, ID_GENERATION_NAME_2);

        ConfigIdGeneration idGeneration2 = createIdGeneration(ID_GENERATION_NAME_2);
        ConfigIdGenerationParameter parameter2_1 = createParameterWithValue(ID_VALUE_NODE_2);
        idGeneration2.setParameters(Arrays.asList(parameter2_1));

        ConfigIdGeneration idGeneration3 = createIdGeneration(null);
        ConfigIdGenerationParameter parameter3_1 = createParameterWithReference(ID_GENERATION_NAME_1);
        ConfigIdGenerationParameter parameter3_2 = createParameterWithValue("/");
        ConfigIdGenerationParameter parameter3_3 = createParameterWithReference(ID_GENERATION_NAME_2);
        idGeneration3.setParameters(Arrays.asList(parameter3_1, parameter3_2, parameter3_3));

        return Arrays.asList(idGeneration1, idGeneration2, idGeneration3);
    }

    @Test
    void hasGraphNode() {
        assertTrue(this.classUnderTest.hasGraphNode(ID_GENERATION_NAME_1));
        assertFalse(this.classUnderTest.hasGraphNode("doesNotExist"));
    }

    @Test
    void getReferencesReturnsCorrectList() {
        assertThat(this.classUnderTest.getReferences(this.idGenerations.get(0)))
            .containsExactly("myIdGenerationName2");
        assertThat(this.classUnderTest.getReferences(this.idGenerations.get(1)))
            .isEmpty();
        assertThat(this.classUnderTest.getReferences(this.idGenerations.get(2)))
            .containsExactly("myIdGenerationName1", "myIdGenerationName2");
    }

    @Test
    void getReferencesForGraphNodeReturnsCorrectList() {
        assertThat(this.classUnderTest.getReferencesForGraphNode(this.graphNode1))
            .hasSize(1);
        assertThat(this.classUnderTest.getReferencesForGraphNode(this.graphNode2))
            .hasSize(0);
        assertThat(this.classUnderTest.getReferencesForGraphNode(this.graphNode3))
            .hasSize(2);
    }

    @Test
    void idGenerationNameAlreadyExists() {
        assertThrows(IdGenerationNameAlreadyDefinedException.class,
            () -> this.classUnderTest.addGraphNode(node, createIdGeneration("myIdGenerationName1")));
    }

    @Test
    void allIdGenerationNamesExist() {
        assertNull(this.classUnderTest.getMissingIdGenerationName());

        // first idGeneration has a name
        idGenerations.get(0).setParameters(Arrays.asList(createInvalidParameterWithReference()));
        assertEquals("DoesNotExist", this.classUnderTest.getMissingIdGenerationName());

        // third idGeneration does not have a name
        idGenerations.get(2).setParameters(Arrays.asList(createInvalidParameterWithReference()));
        assertEquals("DoesNotExist", this.classUnderTest.getMissingIdGenerationName());
    }

    @Test
    void validateGraphMissingName() {
        assertDoesNotThrow(() -> classUnderTest.validateGraph());

        idGenerations.get(0).setParameters(Arrays.asList(createInvalidParameterWithReference()));
        assertThrows(IdGenerationNameDoesNotExistException.class, () -> classUnderTest.validateGraph());
    }

    @Test
    void validCheckCyclicReferences() {
        assertDoesNotThrow(() -> classUnderTest.checkCyclicReferences(graphNode1));
        assertDoesNotThrow(() -> classUnderTest.checkCyclicReferences(graphNode2));
        assertDoesNotThrow(() -> classUnderTest.checkCyclicReferences(graphNode3));
    }

    @Test
    void invalidCheckCyclicReferences() throws IdGenerationNameAlreadyDefinedException {
        ConfigIdGeneration idGeneration1 = createIdGenerationWithReference(ID_GENERATION_NAME_1, ID_GENERATION_NAME_2);
        ConfigIdGeneration idGeneration2 = createIdGenerationWithReference(ID_GENERATION_NAME_2, ID_GENERATION_NAME_1);
        ConfigIdGeneration idGeneration3 = createIdGeneration(ID_GENERATION_NAME_3);

        IdGeneratorGraph graph = new IdGeneratorGraph();
        graph.addGraphNode(node, idGeneration1);
        graph.addGraphNode(node, idGeneration2);
        graph.addGraphNode(node, idGeneration3);

        assertThrows(IdGenerationNameCyclicException.class,
            () -> graph.checkCyclicReferences(graph.getGraphNode(ID_GENERATION_NAME_1)));
        assertThrows(IdGenerationNameCyclicException.class,
            () -> graph.checkCyclicReferences(graph.getGraphNode(ID_GENERATION_NAME_2)));
        assertDoesNotThrow(() -> graph.checkCyclicReferences(graph.getGraphNode(ID_GENERATION_NAME_3)));

        assertThrows(IdGenerationNameCyclicException.class,
            () -> graph.validateGraph());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("configIdGenerationParameterValues")
    @DisplayName("Resolve id parameter in the id generation function")
    void resolveIdParameter(String message, String expectedValue, ConfigIdGenerationParameter parameter) {
        String result = classUnderTest.resolveIdGenerationParameter(unitClass, parameter);
        assertEquals(expectedValue, result, message);
    }

    @Test
    @DisplayName("Resolve id parameter in the id generation function with a reference to another function")
    void resolveIdParameterWithReference() {
        ConfigIdGenerationParameter parameter = createParameterWithReference(ID_GENERATION_NAME_2);
        assertEquals(ID_VALUE_NODE_2, classUnderTest.resolveIdGenerationParameter(null, parameter));

        parameter.setFinalizeFunction(CONCATENATE_AND_HASH.toString());
        assertEquals(classUnderTest.finalizeId(ID_VALUE_NODE_2, CONCATENATE_AND_HASH),
            classUnderTest.resolveIdGenerationParameter(null, parameter));
    }

    @Test
    @DisplayName("Resolve ids in graph nodes")
    void resolveGraphNode() {
        assertEquals(ID_VALUE_NODE_2, classUnderTest.resolveGraphNode(this.graphNode1));
        assertEquals(ID_VALUE_NODE_2, classUnderTest.resolveGraphNode(this.graphNode2));
        assertEquals(ID_VALUE_NODE_2 + "/" + ID_VALUE_NODE_2, classUnderTest.resolveGraphNode(this.graphNode3));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("argsResolveIdParameterWithReferenceAndFinalizeFunctions")
    @DisplayName("Resolve id parameter in the id generation function but differing finalize functions")
    void resolveIdParameterWithReferenceAndFinalizeFunctions(
        String message, ConfigIdGenerationFinalizeFunction idGeneration1Function,
        ConfigIdGenerationFinalizeFunction referenceToIdGeneration1Function,
        String expectedResolveIdGeneration1, String expectedResolveReferenceIdGeneration1,
        String expectedResolveIdGeneration2)
        throws IdGenerationNameAlreadyDefinedException {

        ConfigIdGeneration idGeneration1 = createIdGeneration(ID_GENERATION_NAME_1);
        idGeneration1.setFinalizeFunction(idGeneration1Function.toString());
        idGeneration1.setParameters(Arrays.asList(createParameterWithValue("TestManufacturer"),
            createParameterWithValue("/"),
            createParameterWithValue("TestModel")));

        ConfigIdGeneration idGeneration2 = createIdGeneration(ID_GENERATION_NAME_2);
        idGeneration2.setFinalizeFunction(CONCATENATE.toString());
        ConfigIdGenerationParameter param2_1 = createParameterWithReference(ID_GENERATION_NAME_1);
        param2_1.setFinalizeFunction(referenceToIdGeneration1Function.toString());
        ConfigIdGenerationParameter param2_2 = createParameterWithValue("/Submodel");

        idGeneration2.setParameters(Arrays.asList(param2_1, param2_2));

        IdGeneratorGraph graph = new IdGeneratorGraph();
        graph.addGraphNode(node, idGeneration1);
        graph.addGraphNode(node, idGeneration2);

        assertEquals(expectedResolveIdGeneration1, graph.resolveGraphNode(graph.getGraphNode(ID_GENERATION_NAME_1)));
        assertEquals(expectedResolveReferenceIdGeneration1, graph.resolveIdGenerationParameter(null, param2_1));
        assertEquals(expectedResolveIdGeneration2, graph.resolveGraphNode(graph.getGraphNode(ID_GENERATION_NAME_2)));
    }

    @ParameterizedTest(name = "{0}: \"{1}\" -> \"{2}")
    @MethodSource("argsFinalizeId")
    @DisplayName("Output of finalize functions")
    void finalizeId(String message, String rawId, String expectedId, ConfigIdGenerationFinalizeFunction finalizeFunction) {
        assertEquals(expectedId, classUnderTest.finalizeId(rawId, finalizeFunction));
    }

}
