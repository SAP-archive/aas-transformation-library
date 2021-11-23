/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.pojo.Precondition;
import com.sap.dsc.aas.lib.config.pojo.preconditions.PreconditionTypeForAll;
import com.sap.dsc.aas.lib.config.pojo.preconditions.PreconditionTypeRange;
import com.sap.dsc.aas.lib.exceptions.PreconditionValidationException;
import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.transform.AbstractTransformerTest;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;

class TransformAmlTest extends AbstractTransformerTest {

    private AmlTransformer classUnderTest;
    private InputStream amlInputStream;

    static Stream<Arguments> getValidatedVersionString() {
        return Stream.of(
            arguments("abc.0.1", "[Invalid version string provided]"),
            arguments("1.0.abc", "[Invalid version string provided]"),
            arguments("1.0.1abc", "[Invalid version string provided]"),
            arguments("1.0.abc1", "[Invalid version string provided]"),
            arguments("This is not a valid version", "[Invalid version string provided]"),
            arguments("1.hello.1", "[Invalid version string provided]"),
            arguments(null, "[No version provided]"),
            arguments("10.001.010", "10.001.010"),
            arguments("0.0.1", "0.0.1"),
            arguments("0.0.1", "0.0.1"),
            arguments("1.0", "1.0"));
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.setAMLBindings();

        this.classUnderTest = new AmlTransformer(this.mockPreconditionValidator);
        this.amlInputStream = Files.newInputStream(Paths.get("src/test/resources/aml/full_AutomationComponent.aml"));
    }

    @Test
    @DisplayName("Straight transform an AML document into AAS format")
    void transformAmlTest() throws TransformationException {
        AssetAdministrationShellEnvironment result = classUnderTest.transform(amlInputStream, mapping);
        assertNotNull(result);

        List<AssetAdministrationShell> shells = result.getAssetAdministrationShells();
        assertThat(shells).isNotNull();
        assertThat(shells).hasSize(1);
    }

    @Test
    @DisplayName("Test failure case, that the AML document can't read")
    void transformAmlWithEmptyAmlStream() {
        assertThrows(UnableToReadXmlException.class, () -> classUnderTest.transform(null, new MappingSpecification()));
    }

    @Test
    @DisplayName("Test reading an invalid XML file")
    void readInvalidXml() {
        String initialString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><UnclosedOpenTag>Text";
        InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
        assertThrows(UnableToReadXmlException.class, () -> classUnderTest.transform(inputStream, new MappingSpecification()));
    }

    @Test
    @DisplayName("Test reading an invalid AML file (but valid XML file)")
    void readInvalidAml() {
        String initialString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><CustomXmlElement>Text</CustomXmlElement>";
        InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
        assertThrows(UnableToReadXmlException.class, () -> classUnderTest.transform(inputStream, new MappingSpecification()));
    }

//    @Test
//    @DisplayName("Create identifier")
//    void createIdentifier() throws TransformationException {
//        Identifier result = ((AbstractTransformer) classUnderTest).createIdentifier(unitClass, createSimpleIdGeneration(ID_VALUE));
//        assertThat(result).isNotNull();
//        assertThat(result.getIdentifier()).isEqualTo(ID_VALUE);
//    }

    @Test
    @DisplayName("Test a precondition check which succeeds")
    void preconditionCheckSucceeds() throws TransformationException {
        this.classUnderTest = new AmlTransformer(new PreconditionValidator());

        Precondition precondition = new Precondition();
        precondition.setConfigElementId(DEFAULT_CONFIG_ELEMENT_ID);

        PreconditionTypeForAll forAll = new PreconditionTypeForAll();
        forAll.setMinimumNumber(1);
        forAll.setMaximumNumber(2);
        precondition.setForAll(forAll);

        PreconditionTypeRange range = new PreconditionTypeRange();
        range.setFromXPath("//caex:Attribute");
        range.setMinimumNumber(1);
        range.setMaximumNumber(0);
        precondition.setForEach(Arrays.asList(range));

        mapping.getHeader().setPreconditions(Arrays.asList(precondition));

        assertDoesNotThrow(() -> classUnderTest.transform(amlInputStream, mapping));
    }

    @Test
    @Disabled("Disabled until preconditions are supported")
    @DisplayName("Test a precondition check with forAll which fails")
    void preconditionCheckFailsForAll() throws TransformationException {
        this.classUnderTest = new AmlTransformer(new PreconditionValidator());

        Precondition precondition = new Precondition();
        precondition.setConfigElementId(DEFAULT_CONFIG_ELEMENT_ID);

        PreconditionTypeForAll forAll = new PreconditionTypeForAll();
        forAll.setMinimumNumber(4);
        forAll.setMaximumNumber(4);
        precondition.setForAll(forAll);

        mapping.getHeader().setPreconditions(Arrays.asList(precondition));

        assertThrows(PreconditionValidationException.class, () -> classUnderTest.transform(amlInputStream, mapping));
    }

    @Test
    @Disabled("Disabled until preconditions are supported")
    @DisplayName("Test a precondition check with forEach which fails")
    void preconditionCheckFailsForEachAll() throws TransformationException {
        this.classUnderTest = new AmlTransformer(new PreconditionValidator());

        Precondition precondition = new Precondition();
        precondition.setConfigElementId(DEFAULT_CONFIG_ELEMENT_ID);

        PreconditionTypeRange range = new PreconditionTypeRange();
        range.setFromXPath("//caex:Attribute");
        range.setMinimumNumber(1);
        range.setMaximumNumber(1);
        precondition.setForEach(Arrays.asList(range));

        mapping.getHeader().setPreconditions(Arrays.asList(precondition));

        assertThrows(PreconditionValidationException.class, () -> classUnderTest.transform(amlInputStream, mapping));
    }

    @ParameterizedTest(name = "Given version ''{0}'' expected result ''{1}''")
    @MethodSource
    @DisplayName("Test if the given version String is really a version number")
    void getValidatedVersionString(String version, String expectedVersion) {
        assertThat(classUnderTest.getValidatedVersionString(version)).isEqualTo(expectedVersion);
    }

}
