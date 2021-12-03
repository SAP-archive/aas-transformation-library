/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import com.sap.dsc.aas.lib.aml.transform.AmlTransformer;
import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.mapping.MappingSpecificationParser;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;

import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.Deserializer;
import io.adminshell.aas.v3.dataformat.Serializer;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.BasicEvent;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.Capability;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;

public class TransformationIntegrationTest {

    public static final String CONFIG_JSON = "src/test/resources/config/simpleConfig.json";
    public static final String AML_INPUT = "src/test/resources/aml/full_AutomationComponent.aml";
    public static final String JSON_SCHEMA_AAS = "src/test/resources/schema/schema_v3.0_RC01.json";
    public static final String JSON_SCHEMA_PLAIN = "src/test/resources/schema/schema_2019-09.json";
    public static final String AAS_v3_JSON = "src/test/resources/aas/AASEnv_Test_JSON_v3.json";

    @BeforeEach
    protected void setUp() throws Exception {
        TestUtils.resetBindings();
    }

    AssetAdministrationShellEnvironment createShellEnv() throws IOException, TransformationException {
        InputStream amlInputStream = Files.newInputStream(Paths.get(AML_INPUT));
        AmlTransformer amlTransformer = new AmlTransformer();
        MappingSpecification mapping = new MappingSpecificationParser().loadMappingSpecification(CONFIG_JSON);
        return amlTransformer.transform(amlInputStream, mapping);
    }

    @Test
    void deserializeAASEnvV3() throws IOException, DeserializationException {

        Deserializer deserializer = new JsonDeserializer();

        AssetAdministrationShellEnvironment assetAdministrationShellEnvironment =
            deserializer.read(Files.newInputStream(Paths.get(AAS_v3_JSON)));

        assertThat(assetAdministrationShellEnvironment.getAssetAdministrationShells().get(0).getIdShort())
            .isEqualTo("TestAssetAdministrationShell");
    }

    @Test
    void ambientTemperatureRangeTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel generalTechnicalData = getSubmodel("GeneralTechnicalData", shellEnv);
        SubmodelElement ambientTemperature = getElement("AmbientTemperature", generalTechnicalData);

        assertThat(ambientTemperature).isNotNull();
        assertThat(ambientTemperature).isInstanceOf(Range.class);

        Range ambientTemperatureRange = (Range) ambientTemperature;

        assertThat(ambientTemperatureRange.getValueType()).isEqualTo("integer");
        assertThat(ambientTemperatureRange.getMin()).isEqualTo("-273");
        assertThat(ambientTemperatureRange.getMax()).isEqualTo("100");

        Reference reference = ambientTemperatureRange.getSemanticId();
        assertThat(reference).isNotNull();
        assertThat(reference.getKeys()).isNotNull();
        assertThat(reference.getKeys()).hasSize(1);

        Key semanticId = reference.getKeys().get(0);
        assertThat(semanticId.getValue()).isEqualTo("AmbientTemperature");
        assertThat(semanticId.getIdType()).isEqualTo(KeyType.IRI);
        assertThat(semanticId.getType()).isEqualTo(KeyElements.RANGE);
    }

    @Test
    void materialMultiLanguagePropertyTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel generalTechnicalData = getSubmodel("GeneralTechnicalData", shellEnv);
        SubmodelElement descriptionShort = getElement("Material", generalTechnicalData);

        assertThat(descriptionShort).isNotNull();
        assertThat(descriptionShort).isInstanceOf(MultiLanguageProperty.class);

        MultiLanguageProperty descriptionShortMultiLanguageProperty = (MultiLanguageProperty) descriptionShort;

        assertThat(descriptionShortMultiLanguageProperty.getValues()).isNotNull();
        assertThat(descriptionShortMultiLanguageProperty.getValues()).hasSize(2);
        for (LangString langString : descriptionShortMultiLanguageProperty.getValues()) {
            assertThat(langString.getLanguage()).isAnyOf("en", "de");
            assertThat(langString.getValue()).isAnyOf("English Test Material", "Test Material Deutsch");
        }
    }

    @Test
    void productDetailsDescriptionMultiLanguagePropertyTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel commercialData = getSubmodel("CommercialData", shellEnv);
        SubmodelElement descriptionShort = getSubmodelCollectionElement("ProductDetails", "DescriptionShort", commercialData);

        assertThat(descriptionShort).isNotNull();
        assertThat(descriptionShort).isInstanceOf(MultiLanguageProperty.class);

        MultiLanguageProperty descriptionShortMultiLanguageProperty = (MultiLanguageProperty) descriptionShort;

        assertThat(descriptionShortMultiLanguageProperty.getValues()).isNotNull();
        assertThat(descriptionShortMultiLanguageProperty.getValues()).hasSize(1);
        assertThat(descriptionShortMultiLanguageProperty.getValues().get(0).getLanguage()).isEqualTo("en");
        assertThat(descriptionShortMultiLanguageProperty.getValues().get(0).getValue()).isEqualTo("TestProductDetailsDescriptionShort");
    }

    @Test
    void packagingAndTransportationEntityTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel commercialData = getSubmodel("CommercialData", shellEnv);
        SubmodelElement packagingAndTransportation = getElement("PackagingAndTransportation", commercialData);

        assertThat(packagingAndTransportation).isNotNull();
        assertThat(packagingAndTransportation).isInstanceOf(Entity.class);

        Entity packagingAndTransportationEntity = (Entity) packagingAndTransportation;
        assertEquals(EntityType.SELF_MANAGED_ENTITY, packagingAndTransportationEntity.getEntityType());

        Reference reference = packagingAndTransportationEntity.getSemanticId();
        assertThat(reference).isNotNull();
        assertThat(reference.getKeys()).isNotNull();
        assertThat(reference.getKeys()).hasSize(1);

        Key semanticId = reference.getKeys().get(0);
        assertThat(semanticId.getValue()).isEqualTo("PackAndTransport");
        assertThat(semanticId.getIdType()).isEqualTo(KeyType.IRDI);
        assertThat(semanticId.getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);

        Reference assetReference = packagingAndTransportationEntity.getGlobalAssetId();
        assertThat(assetReference).isNotNull();
        assertThat(assetReference.getKeys()).isNotNull();

        Key assetKey = assetReference.getKeys().get(0);
        assertThat(assetKey.getIdType()).isEqualTo(KeyType.CUSTOM);
        assertThat(assetKey.getValue()).isEqualTo("AssetIdExtern");
        assertThat(assetKey.getType()).isEqualTo(KeyElements.ASSET);

        List<SubmodelElement> statements = packagingAndTransportationEntity.getStatements();
        assertThat(statements).isNotNull();
        assertThat(statements).hasSize(3);
    }

    @Test
    void operationAOperationTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel operations = getSubmodel("Operations", shellEnv);
        SubmodelElement operationA = getElement("OperationA", operations);

        assertThat(operationA).isNotNull();
        assertThat(operationA).isInstanceOf(Operation.class);

        Operation operation = (Operation) operationA;
        assertThat(operation.getInputVariables()).isNotNull();
        assertThat(operation.getInputVariables()).hasSize(3);
        assertThat(operation.getOutputVariables()).isNotNull();
        assertThat(operation.getOutputVariables()).hasSize(2);
        assertThat(operation.getInoutputVariables()).isNotNull();
        assertThat(operation.getInoutputVariables()).hasSize(1);
    }

    @Test
    void browsableCapabilityTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel identificationData = getSubmodel("submodelShortId1", shellEnv);
        SubmodelElement browsable = getElement("Browseable", identificationData);

        assertThat(browsable).isNotNull();
        assertThat(browsable).isInstanceOf(Capability.class);
    }

    @Test
    void sampleFileTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel documents = getSubmodel("Documents", shellEnv);
        SubmodelElement submodelElement = getElement("Betriebsanleitung", documents);

        assertThat(submodelElement).isNotNull();
        assertThat(submodelElement).isInstanceOf(File.class);

        File file = (File) submodelElement;

        assertThat(file.getMimeType()).isEqualTo("application/pdf");
        assertThat(file.getValue()).isEqualTo("manual/OI_wtt12l_en_de_fr_it_pt_es_zh_.pdf");
    }

    @Test
    void stepGeometryReferenceElementTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel documents = getSubmodel("Documents", shellEnv);
        SubmodelElement stepGeometry = getElement("STEPGeometry", documents);

        assertThat(stepGeometry).isNotNull();
        assertThat(stepGeometry).isInstanceOf(ReferenceElement.class);

        ReferenceElement stepGeometryReferenceElement = (ReferenceElement) stepGeometry;

        assertThat(stepGeometryReferenceElement.getValue()).isNotNull();
        assertThat(stepGeometryReferenceElement.getValue().getKeys()).isNotNull();
        assertThat(stepGeometryReferenceElement.getValue().getKeys()).hasSize(1);

        Key key = stepGeometryReferenceElement.getValue().getKeys().get(0);
        assertThat(key.getValue()).isEqualTo("MGFTT2-DFC-C_20200519_134129_7UszRjlaUUSGJwr_3pyZ3g");
        assertThat(key.getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);
    }

    @Test
    void relationshipElementTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        SubmodelElement relElement = getElement("relElement", getSubmodel("Operations", shellEnv));

        assertThat(relElement).isNotNull();
        assertThat(relElement).isInstanceOf(RelationshipElement.class);

        RelationshipElement relationshipElement = (RelationshipElement) relElement;

        assertThat(relationshipElement.getIdShort()).isEqualTo("relElement");
        assertThat(relationshipElement.getFirst()).isNotNull();
        assertThat(relationshipElement.getSecond()).isNotNull();

        Key firstKey = relationshipElement.getFirst().getKeys().get(0);
        assertThat(firstKey.getValue()).isEqualTo("FOO");
        assertThat(firstKey.getIdType()).isEqualTo(KeyType.IRI);
        assertThat(firstKey.getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);

        Key secondKey = relationshipElement.getSecond().getKeys().get(0);
        assertThat(secondKey.getValue()).isEqualTo("BAR");
        assertThat(secondKey.getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);
    }

    @Test
    void annotatedRelationshipElementTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel operations = getSubmodel("Operations", shellEnv);
        SubmodelElement annoRelElement = getElement("annoRelElement", operations);

        assertNotNull(annoRelElement);
        assertThat(annoRelElement).isInstanceOf(AnnotatedRelationshipElement.class);

        AnnotatedRelationshipElement annotatedRelationshipElement = (AnnotatedRelationshipElement) annoRelElement;

        assertThat(annotatedRelationshipElement.getIdShort()).isEqualTo("annoRelElement");
        assertThat(annotatedRelationshipElement.getAnnotations()).hasSize(5);

        assertThat(annotatedRelationshipElement.getFirst()).isNotNull();
        assertThat(annotatedRelationshipElement.getSecond()).isNotNull();

        Key firstKey = annotatedRelationshipElement.getFirst().getKeys().get(0);
        assertThat(firstKey.getValue()).isEqualTo("abc");
        assertThat(firstKey.getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);

        Key secondKey = annotatedRelationshipElement.getSecond().getKeys().get(0);
        assertThat(secondKey.getValue()).isEqualTo("def");
        assertThat(secondKey.getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);
    }

    SubmodelElement getSubmodelCollectionElement(String idShortCollection, String idShortElement, Submodel submodel) {
        SubmodelElementCollection collection = (SubmodelElementCollection) getElement(idShortCollection, submodel);
        assertThat(collection.getValues()).isNotNull();

        return collection.getValues().stream()
            .filter(submodelElement -> idShortElement.equals(submodelElement.getIdShort()))
            .findFirst()
            .orElseThrow(() -> new AssertionFailedError("SubmodelElement with IdShort '" + idShortElement + "' not found"));
    }

    @Test
    void sampleBlobTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel documents = getSubmodel("Documents", shellEnv);
        SubmodelElement submodelElement = getElement("BetriebsanleitungBIN", documents);

        assertThat(submodelElement).isNotNull();
        assertThat(submodelElement).isInstanceOf(Blob.class);

        Blob blob = (Blob) submodelElement;

        assertThat(blob.getMimeType()).isEqualTo("application/pdf");
        assertThat(blob.getValue()).isEqualTo("foo".getBytes());
    }

    @Test
    void sampleBasicEventTransformed() throws Exception {
        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        Submodel documents = getSubmodel("Documents", shellEnv);
        SubmodelElement basicEvent = getElement("SampleBasicEvent", documents);

        assertThat(basicEvent).isNotNull();
        assertThat(basicEvent).isInstanceOf(BasicEvent.class);

        BasicEvent aBasicEvent = (BasicEvent) basicEvent;

        Key key = aBasicEvent.getObserved().getKeys().get(0);
        assertThat(key.getIdType()).isEqualTo(KeyType.CUSTOM);
        assertThat(key.getValue()).isNotNull();
        assertThat(key.getType()).isEqualTo(KeyElements.BLOB);
    }

    @Test
    void validateTransformedAgainstAASJSONSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Serializer serializer = new JsonSerializer();

        JsonNode schemaNode = mapper.readTree(Files.newInputStream(Paths.get(JSON_SCHEMA_AAS)));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaNode)).getSchema(schemaNode);

        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        JsonNode jsonNode = mapper.readTree(serializer.write(shellEnv));

        Set<ValidationMessage> errors = schema.validate(jsonNode);
        if (errors.size() != 0) {
            errors.forEach(System.out::println);
        }
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void validateTransformedAgainstPlainJSONSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Serializer serializer = new JsonSerializer();

        JsonNode schemaNode = mapper.readTree(Files.newInputStream(Paths.get(JSON_SCHEMA_PLAIN)));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaNode)).getSchema(schemaNode);

        AssetAdministrationShellEnvironment shellEnv = createShellEnv();
        JsonNode jsonNode = mapper.readTree(serializer.write(shellEnv));

        Set<ValidationMessage> errors = schema.validate(jsonNode);
        if (errors.size() != 0) {
            errors.forEach(System.out::println);
        }
        assertThat(errors.size()).isEqualTo(0);
    }

    SubmodelElement getElement(String idShort, Submodel submodel) {
        assertThat(submodel.getSubmodelElements()).isNotNull();

        return submodel.getSubmodelElements().stream()
            .filter(submodelElement -> idShort.equals(submodelElement.getIdShort()))
            .findFirst()
            .orElseThrow(() -> new AssertionFailedError("SubmodelElement with IdShort '" + idShort + "' not found"));
    }

    Submodel getSubmodel(String idShort, AssetAdministrationShellEnvironment shellEnv) {
        assertNotNull(shellEnv.getSubmodels());

        return shellEnv.getSubmodels().stream()
            .filter(submodel -> idShort.equals(submodel.getIdShort()))
            .findFirst()
            .orElseThrow(() -> new AssertionFailedError("Submodel with IdShort '" + idShort + "' not found"));
    }
}
