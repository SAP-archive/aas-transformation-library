package com.sap.dsc.aas.lib.transform;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.mapping.AASMappingTransformer;
import com.sap.dsc.aas.lib.mapping.MappingSpecificationParser;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
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
		//ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/foreachTest.json");

		//ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec);
		
		//ASSERT
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
		//ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/foreachTest2.json");

		//ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec);
		
		//ASSERT
		String singleIdentifier = transform.get(0).getSubmodels().get(0).getIdentification().getIdentifier();
		Assertions.assertEquals("https://test.org/transform_for_first_in_list", singleIdentifier);
	}
	
	@Test
	void testBindingsEvaluation() throws IOException {
		//ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/bindingsTest.json");
		
		//ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec);

		//ASSERT
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
		//ARRANGE
		MappingSpecification mapSpec = parser
				.loadMappingSpecification("src/test/resources/mappings/generic/bindingsTest_w_errors.json");
		
		//ACT
		List<AssetAdministrationShellEnvironment> transform = aasMappingTransformer.transform(mapSpec);

		//ASSERT
		Submodel submodel = transform.get(0).getSubmodels().get(0);
		Property prop = (Property) submodel.getSubmodelElements().get(1);
		KeyElements type = prop.getValueId().getKeys().get(0).getType();
		Assertions.assertNull(type);
	}

}
