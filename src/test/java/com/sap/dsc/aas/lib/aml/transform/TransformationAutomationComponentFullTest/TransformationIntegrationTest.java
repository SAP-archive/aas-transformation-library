/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform.TransformationAutomationComponentFullTest;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.aml.transform.AmlTransformer;
import com.sap.dsc.aas.lib.config.ConfigLoader;
import com.sap.dsc.aas.lib.config.pojo.ConfigTransformToAas;
import com.sap.dsc.aas.lib.exceptions.TransformationException;

import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.Serializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class TransformationIntegrationTest {

    public static final String AUTOMATION_COMPONENT_CONFIG_JSON = "src/test/resources/config/AutomationComponentConfig.json";
    public static final String AML_INPUT = "src/test/resources/aml/full_AutomationComponent.aml";
    public static final String JSON_SCHEMA_AAS = "src/test/resources/schema/schema_v3.0_RC01.json";
    public static final String JSON_SCHEMA_PLAIN = "src/test/resources/schema/schema_2019-09.json";
    public static final String AAS_v3_JSON = "src/test/resources/aas/AASEnv_Test_JSON_v3.json";
    private static AssetAdministrationShellEnvironment shellEnv;

	@BeforeEach
	protected void setUp() throws Exception {
		TestUtils.resetBindings();
	}
        
    @Test
    void validateTransformedAutomationConfigFullAgainstAASJSONSchema() throws IOException, SerializationException, TransformationException {

        InputStream amlInputStream = Files.newInputStream(Paths.get(AML_INPUT));

        AmlTransformer amlTransformer = new AmlTransformer();
        ConfigLoader configLoader = new ConfigLoader();

        ConfigTransformToAas config = configLoader.loadConfig(AUTOMATION_COMPONENT_CONFIG_JSON);

        shellEnv = amlTransformer.transform(amlInputStream, config);

        ObjectMapper mapper = new ObjectMapper();
        Serializer serializer = new JsonSerializer();

        JsonNode schemaNode = mapper.readTree(Files.newInputStream(Paths.get(JSON_SCHEMA_AAS)));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaNode)).getSchema(schemaNode);

        JsonNode jsonNode = mapper.readTree(serializer.write(shellEnv));

        Set<ValidationMessage> errors = schema.validate(jsonNode);
        if (errors.size() != 0) {
            errors.forEach(System.out::println);
        }
        assertThat(errors.size()).isEqualTo(0);
    }

}
