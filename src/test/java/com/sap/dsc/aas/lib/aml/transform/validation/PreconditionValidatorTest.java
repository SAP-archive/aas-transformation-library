/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
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

import com.sap.dsc.aas.lib.aml.config.pojo.Precondition;
import com.sap.dsc.aas.lib.aml.config.pojo.preconditions.PreconditionTypeForAll;
import com.sap.dsc.aas.lib.aml.config.pojo.preconditions.PreconditionTypeMatch;
import com.sap.dsc.aas.lib.aml.config.pojo.preconditions.PreconditionTypeRange;
import com.sap.dsc.aas.lib.aml.exceptions.PreconditionValidationException;
import com.sap.dsc.aas.lib.aml.transform.AbstractTransformerTest;

public class PreconditionValidatorTest extends AbstractTransformerTest {

    private final static String CONFIG_ELEMENT_ID = "configElementId";
    private PreconditionValidator classUnderTest;
    private Precondition precondition;

    private static Stream<Arguments> matchValues() {
        return Stream.of(
            arguments("Matches precondition pattern", "[a-z]*", "helloworld", false),
            arguments("Does not match precondition pattern", "[a-z]*", "Hello World!", true));
    }

    private static Stream<Arguments> rangeValues() {
        return Stream.of(
            arguments("Within min/max", 1, 1, 2, false),
            arguments("Within min/max", 2, 1, 2, false),
            arguments("Below min", 0, 1, 2, true),
            arguments("Above max", 3, 1, 2, true),
            arguments("Max is 0", 3, 1, 0, false));
    }

    @BeforeEach
    void setup() throws Exception {
        super.setUp();
        this.classUnderTest = new PreconditionValidator();
        this.precondition = new Precondition();
        precondition.setConfigElementId(CONFIG_ELEMENT_ID);
        precondition.setPreconditionName("myPreconditionName");
        precondition.setPreconditionDescription("Description of a precondition (unit test)");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matchValues")
    @DisplayName("Check if contents (value) of node matches a pattern")
    void validatePreconditionTypeMatch(String message, String pattern, String nodeValue, boolean throwsException) {
        PreconditionTypeMatch match = new PreconditionTypeMatch();
        match.setPattern(pattern);

        if (!throwsException) {
            assertDoesNotThrow(() -> this.classUnderTest.validatePreconditionTypeMatch(precondition, match, Arrays.asList(nodeValue)));
        } else {
            assertThrows(PreconditionValidationException.class,
                () -> this.classUnderTest.validatePreconditionTypeMatch(precondition, match, Arrays.asList(nodeValue)));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rangeValues")
    @DisplayName("Check if number of nodes lie within a range (forEach type)")
    void validatePreconditionTypeRange(String message, int numberNodes, int min, int max, boolean throwsException) {
        PreconditionTypeRange range = new PreconditionTypeRange();
        range.setMinimumNumber(min);
        range.setMaximumNumber(max);
        range.setErrorMessageOnFailure("Requires a value between min/max!");

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numberNodes; i++) {
            nodes.add(attribute);
        }

        if (!throwsException) {
            assertDoesNotThrow(() -> this.classUnderTest.validatePreconditionTypeRange(precondition, range, nodes));
        } else {
            assertThrows(PreconditionValidationException.class,
                () -> this.classUnderTest.validatePreconditionTypeRange(precondition, range, nodes));
        }
    }

    @Test
    @DisplayName("Validate different precondition types at the same time")
    void validateForEach() {
        PreconditionTypeRange range = new PreconditionTypeRange();
        range.setFromXPath(".");
        PreconditionTypeMatch match = new PreconditionTypeMatch();
        match.setFromXPath(".");
        match.setPattern(".*");

        precondition.setForEach(Arrays.asList(range, match));

        assertDoesNotThrow(() -> this.classUnderTest.validateForEach(precondition, unitClass));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rangeValues")
    @DisplayName("Check if number of nodes lie within a range (forAll type)")
    void validateForAll(String message, int numberNodes, int min, int max, boolean throwsException) {
        PreconditionTypeForAll forAll = new PreconditionTypeForAll();
        forAll.setMinimumNumber(min);
        forAll.setMaximumNumber(max);
        precondition.setForAll(forAll);

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numberNodes; i++) {
            nodes.add(attribute);
        }

        if (!throwsException) {
            assertDoesNotThrow(() -> this.classUnderTest.validateForAll(precondition, nodes));
        } else {
            assertThrows(PreconditionValidationException.class,
                () -> this.classUnderTest.validateForAll(precondition, nodes));
        }
    }

    @Test
    @DisplayName("Missing (null) values in the config should not lead to an exception")
    void validateWithNull() {
        assertDoesNotThrow(() -> this.classUnderTest.validateForAll(precondition, null));
        assertDoesNotThrow(() -> this.classUnderTest.validateForEach(precondition, null));
    }

}
