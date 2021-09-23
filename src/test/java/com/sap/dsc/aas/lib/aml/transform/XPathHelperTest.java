/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.dsc.aas.lib.aml.exceptions.NoResultByXPathException;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;

public class XPathHelperTest extends AbstractTransformerTest {

    private XPathHelper classUnderTest;

    private static Stream<Arguments> stringValues() {
        return Stream.of(
            arguments("'Hello world'", Arrays.asList("Hello world")),
            arguments("//caex:DoesNotExist", Arrays.asList()),
            arguments("//caex:Attribute[@Name='MySubAttribute']//caex:Value", Arrays.asList("MyValue")),
            arguments("//caex:Attribute//caex:Value", Arrays.asList("MyValue", "Not Searched")),
            arguments("//caex:Attribute//caex:Value", Arrays.asList("MyValue", "Not Searched")),
            arguments("//caex:Attribute", Arrays.asList("Not SearchedMyValueOtherValue", "Not Searched", "MyValueOtherValue", "")),
            arguments("count(//caex:Value)", Arrays.asList("2.0")),
            arguments("//caex:Value", Arrays.asList("MyValue", "Not Searched")));
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        this.classUnderTest = new XPathHelper();
        super.setUp();
    }

    @Test
    @DisplayName("Get a string value based on a given XPath")
    void getStringValue() throws TransformationException {
        assertEquals("MyAttribute", classUnderTest.getStringValue(attribute, "@Name"));
        assertEquals("Hello World", classUnderTest.getStringValue(attribute, "'Hello World'"));
        assertThrows(NoResultByXPathException.class, () -> classUnderTest.getStringValue(attribute, "DoesNotExist"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("stringValues")
    @DisplayName("Get multiple string values based on a given XPath")
    void getStringValues(String xPath, List<String> expectedValues) {
        assertThat(classUnderTest.getStringValues(unitClass, xPath)).containsExactlyElementsIn(expectedValues);
    }

}
