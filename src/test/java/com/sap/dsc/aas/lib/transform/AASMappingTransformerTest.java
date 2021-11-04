package com.sap.dsc.aas.lib.transform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.XMLConstants;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.mapping.AASMappingTransformer;
import com.sap.dsc.aas.lib.mapping.MappingSpecificationParser;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;

import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;

public class AASMappingTransformerTest {

	private MappingSpecificationParser parser;
	private AASMappingTransformer aasMappingTransformer;

	@BeforeEach
	void setup() {
		parser = new MappingSpecificationParser();
		aasMappingTransformer = new AASMappingTransformer();
	}

	@Test
	void testNestedForeachEvaluations() throws IOException {
		// ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/foreachTest.json");

		// ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec, null);

		// ASSERT
		Assertions.assertEquals(1, transform.size());
		List<Submodel> transformedSubmodels = transform.get(0).getSubmodels();
		Assertions.assertEquals(3, transformedSubmodels.size());
		Assertions.assertNull(transformedSubmodels.get(0).getIdentification());
		List<SubmodelElement> submodelElements = transformedSubmodels.get(0).getSubmodelElements();
		Assertions.assertEquals(3, submodelElements.size());
		Assertions.assertEquals(2, submodelElements.stream().filter(File.class::isInstance).count());
		Assertions.assertEquals(1,
				submodelElements.stream().filter(SubmodelElementCollection.class::isInstance).count());
		SubmodelElementCollection SMEC = (SubmodelElementCollection) submodelElements.stream()
				.filter(SubmodelElementCollection.class::isInstance).findFirst().get();
		Assertions.assertEquals(3,
				SMEC.getValues().stream().filter(io.adminshell.aas.v3.model.Property.class::isInstance).count());
		Assertions.assertEquals(0, SMEC.getValues().stream()
				.filter(io.adminshell.aas.v3.model.MultiLanguageProperty.class::isInstance).count());
	}

	@Test
	void testForeachEvaluationsTargetNotAList() throws IOException {
		// ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/foreachTest2.json");

		// ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec, null);

		// ASSERT
		String singleIdentifier = transform.get(0).getSubmodels().get(0).getIdentification().getIdentifier();
		Assertions.assertEquals("https://test.org/transform_for_first_in_list", singleIdentifier);
	}

	@Test
	void testBindingsEvaluation() throws IOException {
		// ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/bindingsTest.json");

		// ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec, null);

		// ASSERT
		Submodel submodel = transform.get(0).getSubmodels().get(0);
		String identifier = submodel.getIdentification().getIdentifier();
		Assertions.assertEquals("https://test.org/id_via_bind", identifier);

		String fileIdShort = submodel.getSubmodelElements().get(0).getIdShort();
		Assertions.assertEquals("idshort_via_bind", fileIdShort);

		Property prop = (Property) submodel.getSubmodelElements().get(1);
		KeyElements type = prop.getValueId().getKeys().get(0).getType();
		Assertions.assertEquals(KeyElements.GLOBAL_REFERENCE, type);
	}

	@Test
	void testBindingsEvaluationWithInvalidBindings() throws IOException {
		// ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/bindingsTest_w_errors.json");

		// ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec, null);

		// ASSERT
		Submodel submodel = transform.get(0).getSubmodels().get(0);
		Property prop = (Property) submodel.getSubmodelElements().get(1);
		KeyElements type = prop.getValueId().getKeys().get(0).getType();
		Assertions.assertNull(type);
	}

	@Test
	void testXpathTransformation() throws Exception {
		// ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/genericXpathTest.json");

		Document testDoc = null;
		try (InputStream testResource = Files
				.newInputStream(Paths.get("src/test/resources/mappings/generic/generic.xml"))) {

			SAXReader reader = new SAXReader();
			reader.setEncoding("UTF-8");
			reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			testDoc = reader.read(testResource);
		}

		XPathHelper.getInstance().setNamespaceBinding("ns", "http://ns.org/");
		// ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec, testDoc);
		
		// ASSERT
		System.out.println(new JsonSerializer().write(transform.get(0)));
		
		
		Assertions.assertTrue(transform.get(0).getSubmodels().size() == 2);
	}

}
