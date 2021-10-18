package com.sap.dsc.aas.lib.transform;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.config.ConfigLoader;
import com.sap.dsc.aas.lib.config.pojo.ConfigMapping;
import com.sap.dsc.aas.lib.config.pojo.ConfigTransformToAas;
import com.sap.dsc.aas.lib.exceptions.TransformationException;

import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

class GenericDocumentTransformerTest {

	public static final String XML_INPUT = "src/test/resources/ua/aasfull.xml";
    public static final String JSON_CONFIG = "src/test/resources/ua/genericSampleConfig.json";

	
	private InputStream testInputStream;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
        testInputStream = Files.newInputStream(Paths.get(XML_INPUT));
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testNsBindings() throws TransformationException, IOException {
		DocumentTransformer transformer = new GenericDocumentTransformer();
		
        ConfigLoader configLoader = new ConfigLoader();
        ConfigTransformToAas config = configLoader.loadConfig(JSON_CONFIG);
		
		AssetAdministrationShellEnvironment transform = transformer.transform(testInputStream, config);
		Assert.assertNotNull(transform);
	}

}
