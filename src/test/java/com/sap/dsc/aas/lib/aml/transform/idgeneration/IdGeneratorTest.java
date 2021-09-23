/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform.idgeneration;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGenerationParameter;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;
import com.sap.dsc.aas.lib.aml.transform.AbstractTransformerTest;

public class IdGeneratorTest extends AbstractTransformerTest {

    private IdGenerator classUnderTest;

    @BeforeEach
    void setup() throws Exception {
        this.classUnderTest = new IdGenerator();
        super.setUp();
    }

    @Test
    void testNoParameters() {
        String result = classUnderTest.generateId(subAttribute, new ConfigIdGeneration());
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Attribute");

        result = classUnderTest.generateId(subAttribute, null);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Attribute");
    }

    @Test
    @DisplayName("Create identifier with multiple parameters")
    void createIdentifierMultipleParameters() throws TransformationException {
        ConfigIdGeneration idGeneration = new ConfigIdGeneration();
        ConfigIdGenerationParameter param1 = new ConfigIdGenerationParameter();
        ConfigIdGenerationParameter param2 = new ConfigIdGenerationParameter();
        ConfigIdGenerationParameter param3 = new ConfigIdGenerationParameter();
        param1.setXPath("@ID");
        param2.setValueDefault("/");
        param3.setXPath("@Name");
        idGeneration.setParameters(Arrays.asList(param1, param2, param3));

        this.classUnderTest.resetGraph();
        this.classUnderTest.getGraph().addGraphNode(unitClass, idGeneration);
        String result = classUnderTest.generateId(unitClass, idGeneration);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("MyClassId/MyClass");
    }

}
