package com.sap.dsc.aas.lib.transform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import io.adminshell.aas.v3.dataformat.Serializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.*;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.ConfigLoader;
import com.sap.dsc.aas.lib.config.pojo.ConfigTransformToAas;
import com.sap.dsc.aas.lib.exceptions.TransformationException;

class GenericDocumentTransformerTest {

	public static final String XML_INPUT = "src/test/resources/ua/aasfull.xml";
    public static final String JSON_CONFIG = "src/test/resources/ua/genericSampleConfig.json";

	private static final String TEST_SUBMODEL_1_IDENTIFIER = "test_https://acplt.org/Test_Submodel";

	private InputStream testInputStream;
	private DocumentTransformer transformer;
	private ConfigLoader configLoader;
	private ConfigTransformToAas config;
	AssetAdministrationShellEnvironment transform;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		TestUtils.resetBindings();
        testInputStream = Files.newInputStream(Paths.get(XML_INPUT));
		transformer = new GenericDocumentTransformer();
		configLoader= new ConfigLoader();
		config = configLoader.loadConfig(JSON_CONFIG);
		transform = transformer.transform(testInputStream, config);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testNsBindings() throws TransformationException, IOException {
		Assert.assertNotNull(transform);
		Assert.assertEquals(71, transform.getSubmodels().size());
	}

	@Test
	void assetAASIds(){
		for(AssetAdministrationShell aas : transform.getAssetAdministrationShells())
		{
			List<Key> globalAssetIds = aas.getAssetInformation().getGlobalAssetId().getKeys();
			Assert.assertEquals( AssetKind.TYPE, aas.getAssetInformation().getAssetKind());
			Assert.assertEquals( "shellId", aas.getIdentification().getIdentifier());
			Assert.assertEquals( IdentifierType.CUSTOM, aas.getIdentification().getIdType());
			Assert.assertEquals( "idshortAAS", aas.getIdShort());
			Assert.assertEquals(KeyElements.ASSET, globalAssetIds.get(0).getType());
			Assert.assertEquals(KeyType.CUSTOM, globalAssetIds.get(0).getIdType());
			Assert.assertEquals("assetInformationGlobalAssetIdReference", globalAssetIds.get(0).getValue());
		}
	}

	@Test
	void testSubmodelIds() throws IOException, TransformationException {
		for(Submodel submodel : transform.getSubmodels())
		{
			if(submodel.getIdentification().getIdentifier().equals(TEST_SUBMODEL_1_IDENTIFIER)) {
				List<Key> semanticIds = submodel.getSemanticId().getKeys();
				Assert.assertEquals("Submodel_20", submodel.getIdShort());
				Assert.assertEquals(KeyType.IRDI, semanticIds.get(0).getIdType());
				Assert.assertEquals(0, submodel.getSubmodelElements().size());
				Assert.assertEquals(1, semanticIds.size());
				Assert.assertEquals(KeyElements.CONCEPT_DESCRIPTION, semanticIds.get(0).getType());
				Assert.assertEquals("http://semanticid.com", semanticIds.get(0).getValue());
			}
		}
	}

}
