/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.dsc.aas.lib.transform.pojo.GenericSubmodelElementAttributes;
import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.pojo.AbstractConfig;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.config.pojo.ConfigReference;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.*;
import com.sap.dsc.aas.lib.exceptions.NoResultByXPathException;
import com.sap.dsc.aas.lib.exceptions.TransformationException;

import io.adminshell.aas.v3.model.*;

public class TransformSubmodelElementTest extends AbstractTransformerTest {

    private SubmodelElementTransformer classUnderTest;

    public TransformSubmodelElementTest() {}

    private static Stream<Arguments> configSubmodelElementValues() {
        // User passes xpath and default value
        // --> try with xpath, result is null use default value
        ConfigProperty xpathAndDefaultNoMatch = new ConfigProperty();
        xpathAndDefaultNoMatch.setValueDefault(AbstractTransformerTest.DEFAULT_VALUE);
        xpathAndDefaultNoMatch.setValueXPath("DoesNotExist");

        // User passes xpath and default value
        // --> try with xpath, result has value
        ConfigProperty xpathAndDefaultMatch = new ConfigProperty();
        xpathAndDefaultMatch.setValueDefault(AbstractTransformerTest.DEFAULT_VALUE);
        xpathAndDefaultMatch.setValueXPath("caex:DefaultValue");

        // User does NOT pass xpath, user passes default value
        // --> Use default value ("hardcoded")
        ConfigProperty noXpathAndOnlyDefault = new ConfigProperty();
        noXpathAndOnlyDefault.setValueDefault(AbstractTransformerTest.DEFAULT_VALUE);

        // User passes xpath, user does NOT pass default value
        // --> try with xpath, result is null
        ConfigProperty xpathAndNoDefaultNoMatch = new ConfigProperty();
        xpathAndNoDefaultNoMatch.setValueXPath("DoesNotExist");

        // User passes xpath, user does NOT pass default value
        // --> try with xpath, result has value
        ConfigProperty xpathAndNoDefaultMatch = new ConfigProperty();
        xpathAndNoDefaultMatch.setValueXPath("caex:DefaultValue");

        // User does NOT pass xpath, user does NOT pass default value
        // --> try with default xpath
        ConfigProperty noXpathAndNoDefaultMatch = new ConfigProperty();

        return Stream.of(
            arguments("Wrong XPath and Default set, expected default value", AbstractTransformerTest.DEFAULT_VALUE, xpathAndDefaultNoMatch),
            arguments("XPath and Default set, expected xpath result value", AbstractTransformerTest.DEFAULT_VALUE_CONTENT,
                xpathAndDefaultMatch),
            arguments("No XPath set and Default set, expected default value", AbstractTransformerTest.DEFAULT_VALUE, noXpathAndOnlyDefault),
            arguments("wrong XPath and no Default set, expected null value", null, xpathAndNoDefaultNoMatch),
            arguments("XPath and no Default set, expected result value", AbstractTransformerTest.DEFAULT_VALUE_CONTENT,
                xpathAndNoDefaultMatch),
            arguments("No XPath and no Default set, expected default xpath result value", AbstractTransformerTest.VALUE_CONTENT,
                noXpathAndNoDefaultMatch));
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.setAMLBindings();
        when(mockIdGenerator.generateId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn(ID_VALUE);
        when(mockIdGenerator.generateSemanticId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn("mySubmodelElementCollectionSemanticId");
        this.classUnderTest = new SubmodelElementTransformer(mockIdGenerator, mockPreconditionValidator);
    }

    @Test
    @DisplayName("Create a submodel element from a config submodel element")
    void createSubmodelElementProperty() throws TransformationException {

        // The submodelElement will be of type Property and will have a sample Attribute of type String
        // The subAttribute Node is found on the sample AML document that we created in the parent class
        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(subAttribute, configSubmodelElementProperty);

        assertThat(submodelElement).isNotNull();
        assertThat(submodelElement.getIdShort()).isEqualTo("MySubAttribute");
        assertThat(((Property) submodelElement).getValue()).isEqualTo(VALUE_CONTENT);
    }

    @Test
    @DisplayName("Create a submodel element with model type range from a config submodel element")
    void createSubmodelElementRange() throws TransformationException {
        ConfigRange configRange = new ConfigRange();

        configRange.setValueType("integer");
        configRange.setMinValueXPath("'1'");
        configRange.setMaxValueXPath("'10'");
        configRange.setIdShortXPath("'" + ID_VALUE + "'");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(subAttribute, configRange);

        assertThat(submodelElement).isNotNull();
        assertThat(submodelElement.getIdShort()).isEqualTo(ID_VALUE);
        assertThat(submodelElement).isInstanceOf(Range.class);

        Range range = (Range) submodelElement;

        assertThat(range.getMin()).isEqualTo("1");
        assertThat(range.getMax()).isEqualTo("10");
        assertThat(range.getValueType()).isEqualTo("integer");
    }

    @Test
    @DisplayName("Create a submodel element with model type entity from a config submodel element")
    void createSubmodelElementEntity() throws TransformationException {
        ConfigEntity configEntity = new ConfigEntity();

        ConfigReference configReference = new ConfigReference();
        configReference.setValueId("ABC");
        configReference.setKeyElement(KeyElements.ENTITY);
        configEntity.setAssetReference(configReference);
        configEntity.setEntityType(EntityType.SELF_MANAGED_ENTITY);
        configEntity.setStatements(configSubmodelElementCollection.getSubmodelElements());
        configEntity.setIdShortXPath("'" + ID_VALUE + "'");

        when(mockIdGenerator.generateId(any(), eq(configEntity.getAssetReference().getIdGeneration()))).thenReturn("ABC");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(attribute, configEntity);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(Entity.class);
        Entity entity = (Entity) submodelElement;
        assertEquals(EntityType.SELF_MANAGED_ENTITY, entity.getEntityType());
        assertThat(entity.getStatements()).hasSize(3);

        Key key = entity.getGlobalAssetId().getKeys().get(0);
        assertEquals("ABC", key.getValue());
        assertEquals(KeyType.CUSTOM, key.getIdType());
        assertEquals(KeyElements.ENTITY, key.getType());
    }

    @Test
    @DisplayName("Create a submodel element with model type operation from a config submodel element")
    void createSubmodelElementOperation() throws TransformationException {
        ConfigOperation configOperation = new ConfigOperation();

        configOperation.setInputVariables(configSubmodelElementCollection.getSubmodelElements());
        configOperation.setOutputVariables(configSubmodelElementCollection.getSubmodelElements());
        configOperation.setInOutputVariables(configSubmodelElementCollection.getSubmodelElements());
        configOperation.setIdShortXPath("'" + ID_VALUE + "'");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(attribute, configOperation);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(Operation.class);

        Operation operation = (Operation) submodelElement;
        assertThat(operation.getInputVariables()).isNotNull();
        assertThat(operation.getInputVariables()).hasSize(3);
        assertThat(operation.getOutputVariables()).isNotNull();
        assertThat(operation.getOutputVariables()).hasSize(3);
        assertThat(operation.getInoutputVariables()).isNotNull();
        assertThat(operation.getInoutputVariables()).hasSize(3);
    }

    @Test
    @DisplayName("Create a submodel element with model type conceptDescription from a config submodel element")
    void createSubmodelElementConceptDescription() throws TransformationException {
        ConfigReferenceElement configReferenceElement = new ConfigReferenceElement();

        ConfigReference configReference = new ConfigReference();
        configReference.setValueId("ABC");
        configReference.setKeyElement(KeyElements.CONCEPT_DESCRIPTION);
        configReferenceElement.setValue(configReference);
        configReferenceElement.setIdShortXPath("'" + ID_VALUE + "'");

        when(mockIdGenerator.generateId(any(), eq(configReferenceElement.getValue().getIdGeneration()))).thenReturn("ABC");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(attribute, configReferenceElement);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(ReferenceElement.class);
        ReferenceElement referenceElement = (ReferenceElement) submodelElement;

        assertNotNull(referenceElement.getValue());
        Key key = referenceElement.getValue().getKeys().get(0);
        assertEquals("ABC", key.getValue());
        assertEquals(KeyType.CUSTOM, key.getIdType());
        assertEquals(KeyElements.CONCEPT_DESCRIPTION, key.getType());
    }

    @Test
    @DisplayName("Create a submodel element with model type annotated relationship element from a config submodel element")
    void createSubmodelElementAnnotatedRelationshipElement() throws TransformationException {
        ConfigReference first = new ConfigReference();
        first.setValueId("ABC");
        first.setKeyElement(KeyElements.CONCEPT_DESCRIPTION);

        ConfigReference second = new ConfigReference();
        second.setValueId("DEF");
        second.setKeyElement(KeyElements.PROPERTY);

        ConfigAnnotatedRelationshipElement configAnnotatedRelationshipElement = new ConfigAnnotatedRelationshipElement(first, second);
        configAnnotatedRelationshipElement.setIdShortXPath("'" + ID_VALUE + "'");

        when(mockIdGenerator.generateId(any(), eq(configAnnotatedRelationshipElement.getFirst().getIdGeneration()))).thenReturn("ABC");
        when(mockIdGenerator.generateId(any(), eq(configAnnotatedRelationshipElement.getSecond().getIdGeneration()))).thenReturn("DEF");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(attribute, configAnnotatedRelationshipElement);

        assertThat(submodelElement).isNotNull();
        assertThat(submodelElement.getIdShort()).isEqualTo(ID_VALUE);
        assertThat(submodelElement).isInstanceOf(AnnotatedRelationshipElement.class);

        AnnotatedRelationshipElement annotatedRelationshipElement = (AnnotatedRelationshipElement) submodelElement;

        assertThat(annotatedRelationshipElement.getFirst()).isNotNull();
        assertThat(annotatedRelationshipElement.getSecond()).isNotNull();

        Key key = annotatedRelationshipElement.getFirst().getKeys().get(0);

        assertThat(key.getValue()).isEqualTo("ABC");
        assertThat(key.getIdType()).isEqualTo(KeyType.CUSTOM);
        assertThat(key.getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);

        key = annotatedRelationshipElement.getSecond().getKeys().get(0);

        assertThat(key.getValue()).isEqualTo("DEF");
        assertThat(key.getIdType()).isEqualTo(KeyType.CUSTOM);
        assertThat(key.getType()).isEqualTo(KeyElements.PROPERTY);
    }

    @Test
    @DisplayName("Create a submodel element with model type relationship element from a config submodel element")
    void createSubmodelElementRelationshipElement() throws TransformationException {
        ConfigReference first = new ConfigReference();
        first.setValueId("FOO");
        first.setKeyElement(KeyElements.PROPERTY);

        ConfigReference second = new ConfigReference();
        second.setValueId("BAR");
        second.setKeyElement(KeyElements.ENTITY);

        ConfigRelationshipElement configRelationshipElement = new ConfigRelationshipElement(first, second);
        configRelationshipElement.setIdShortXPath("'" + ID_VALUE + "'");

        when(mockIdGenerator.generateId(any(), eq(configRelationshipElement.getFirst().getIdGeneration())))
            .thenReturn("FOO");
        when(mockIdGenerator.generateId(any(), eq(configRelationshipElement.getSecond().getIdGeneration())))
            .thenReturn("BAR");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(attribute, configRelationshipElement);

        assertThat(submodelElement).isNotNull();
        assertThat(submodelElement.getIdShort()).isEqualTo(ID_VALUE);
        assertThat(submodelElement).isInstanceOf(RelationshipElement.class);

        RelationshipElement relationshipElement = (RelationshipElement) submodelElement;

        assertThat(relationshipElement.getFirst()).isNotNull();
        assertThat(relationshipElement.getSecond()).isNotNull();

        Key keyFirst = relationshipElement.getFirst().getKeys().get(0);

        assertThat(keyFirst.getValue()).isEqualTo("FOO");
        assertThat(keyFirst.getIdType()).isEqualTo(KeyType.CUSTOM);
        assertThat(keyFirst.getType()).isEqualTo(KeyElements.PROPERTY);

        Key keySecond = relationshipElement.getSecond().getKeys().get(0);

        assertThat(keySecond.getValue()).isEqualTo("BAR");
        assertThat(keySecond.getIdType()).isEqualTo(KeyType.CUSTOM);
        assertThat(keySecond.getType()).isEqualTo(KeyElements.ENTITY);
    }

    @Test
    @DisplayName("Create a submodel element with model type file from a config submodel element")
    void createSubmodelElementFile() throws TransformationException {
        ConfigFile configFile = new ConfigFile();

        configFile.setValueXPath("'file://test.txt'");
        configFile.setMimeTypeXPath("'text/plain'");
        configFile.setIdShortXPath("'" + ID_VALUE + "'");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(subAttribute, configFile);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(File.class);
        File file = (File) submodelElement;
        assertEquals("text/plain", file.getMimeType());
        assertEquals("file://test.txt", file.getValue());
    }

    @Test
    @DisplayName("Create a submodel element with model type blob from a config submodel element")
    void createSubmodelElementBlob() throws TransformationException {
        ConfigBlob configBlob = new ConfigBlob();

        configBlob.setValueXPath("'foo'");
        configBlob.setMimeTypeXPath("'text/plain'");
        configBlob.setIdShortXPath("'" + ID_VALUE + "'");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(subAttribute, configBlob);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(Blob.class);

        Blob blob = (Blob) submodelElement;

        assertEquals("text/plain", blob.getMimeType());
        assertThat(blob.getValue()).isEqualTo("foo".getBytes());
    }

    @Test
    @DisplayName("Create a submodel element with model type basic event from a config submodel element")
    void createSubmodelElementBasicEvent() throws TransformationException {
        ConfigBasicEvent configBasicEvent = new ConfigBasicEvent();

        ConfigReference configReference = new ConfigReference();
        configReference.setValueId("ABC");
        configReference.setKeyElement(KeyElements.BLOB);

        configBasicEvent.setObserved(configReference);
        configBasicEvent.setIdShortXPath("'" + ID_VALUE + "'");

        when(mockIdGenerator.generateId(any(), eq(configBasicEvent.getObserved().getIdGeneration()))).thenReturn("ABC");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(subAttribute, configBasicEvent);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(BasicEvent.class);
        BasicEvent basicEvent = (BasicEvent) submodelElement;

        assertNotNull(basicEvent.getObserved());

        Key key = basicEvent.getObserved().getKeys().get(0);
        assertEquals("ABC", key.getValue());
        assertEquals(KeyType.CUSTOM, key.getIdType());
        assertEquals(KeyElements.BLOB, key.getType());
    }

    @Test
    @DisplayName("Create a submodel element with model type capability from a config submodel element")
    void createSubmodelElementCapability() throws TransformationException {
        ConfigCapability configCapability = new ConfigCapability();

        configCapability.setIdShortXPath("'" + ID_VALUE + "'");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(subAttribute, configCapability);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(Capability.class);
    }

    @Test
    @DisplayName("Create a submodel element with model type multi language property from a config submodel element, with defined values")
    void createSubmodelElementFMultiLanguageProperty() throws TransformationException {
        ConfigMultiLanguageProperty configMultiLanguageProperty = new ConfigMultiLanguageProperty();

        List<ConfigLanguageProperty> languageProperties = Collections.singletonList(new ConfigLanguageProperty("'en_US'", "'Hello World'"));
        configMultiLanguageProperty.setValues(languageProperties);
        configMultiLanguageProperty.setIdShortXPath("'" + ID_VALUE + "'");

        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(subAttribute, configMultiLanguageProperty);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(MultiLanguageProperty.class);
        MultiLanguageProperty multiLanguageProperty = (MultiLanguageProperty) submodelElement;
        assertNotNull(multiLanguageProperty.getValues());
        assertThat(multiLanguageProperty.getValues()).hasSize(1);

        LangString langString = multiLanguageProperty.getValues().get(0);

        assertThat(langString.getLanguage()).isEqualTo("en_US");
        assertThat(langString.getValue()).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("Create a submodel element with model type multi language property from a config submodel element, with no values defined")
    void createSubmodelElementFMultiLanguagePropertyWithoutValuesSet() throws TransformationException {
        ConfigMultiLanguageProperty configMultiLanguageProperty = new ConfigMultiLanguageProperty();

        ConfigReference configReference = new ConfigReference();
        configReference.setValueId(ID_VALUE);
        configReference.setKeyElement(KeyElements.MULTI_LANGUAGE_PROPERTY);
        configMultiLanguageProperty.setValueId(configReference);
        configMultiLanguageProperty.setIdShortXPath("'" + ID_VALUE + "'");

        addLanguageElementsToAttribute();
        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(attribute, configMultiLanguageProperty);

        assertNotNull(submodelElement);
        assertEquals(ID_VALUE, submodelElement.getIdShort());
        assertThat(submodelElement).isInstanceOf(MultiLanguageProperty.class);
        MultiLanguageProperty multiLanguageProperty = (MultiLanguageProperty) submodelElement;
        assertNotNull(multiLanguageProperty.getValues());
        assertThat(multiLanguageProperty.getValues()).hasSize(2);

        for (LangString langString : multiLanguageProperty.getValues()) {
            assertThat(langString.getLanguage()).isAnyOf("en", "de");
            assertThat(langString.getValue()).isAnyOf("Hello World", "Hallo Welt");
        }

        assertNotNull(multiLanguageProperty.getValueId());

        Reference reference = multiLanguageProperty.getValueId();
        assertThat(reference.getKeys()).hasSize(1);
        assertNotNull(reference.getKeys());
        assertEquals(ID_VALUE, reference.getKeys().get(0).getValue());
    }

    @Test
    @DisplayName("Create a submodel element collection from a config submodel element")
    void createSubmodelElementCollection() throws TransformationException {
        SubmodelElement submodelElement = classUnderTest.createSubmodelElement(attribute, configSubmodelElementCollection);

        assertNotNull(submodelElement);

        SubmodelElementCollection submodelElementCollection = (SubmodelElementCollection) submodelElement;
        assertNotNull(submodelElementCollection.getValues());
        assertThat(submodelElementCollection.getValues()).hasSize(3);
        assertEquals("MyAttribute", submodelElement.getIdShort());

        configSubmodelElementCollection.setSemanticId("mySubmodelElementCollectionSemanticId");
        SubmodelElement submodelElement2 = classUnderTest.createSubmodelElement(attribute, configSubmodelElementCollection);
        assertEquals("mySubmodelElementCollectionSemanticId", submodelElement2.getSemanticId().getKeys().get(0).getValue());
    }

    @Test
    @DisplayName("Create list of submodel elements from a single configSubmodel")
    void createSubmodelElementsFromConfigSubmodelElement() throws TransformationException {
        List<SubmodelElement> submodelElements =
            classUnderTest.createSubmodelElementsFromConfig(attribute, configSubmodelElementMultiple);

        assertNotNull(submodelElements);
        assertThat(submodelElements).hasSize(2);

        verify(mockPreconditionValidator, times(1))
            .validate(or(any(AbstractConfig.class), isNull()), or(anyList(), isNull()));
    }

    @Test
    @DisplayName("Create a list of submodel elements from a list of a config submodel elements")
    void createSubmodelElements() throws TransformationException {
        List<SubmodelElement> submodelElements =
            classUnderTest.createSubmodelElements(attribute, Arrays.asList(configSubmodelElementProperty, configSubmodelElementMultiple));

        assertNotNull(submodelElements);
        assertThat(submodelElements).hasSize(3);
    }

    @Test
    void getSubmodelElementAttributes() throws NoResultByXPathException {
        GenericSubmodelElementAttributes submodelElementAttributes = classUnderTest
            .getSubmodelAttributes(subAttribute, configSubmodelElementProperty);
        assertNotNull(submodelElementAttributes);
        assertEquals("MySubAttribute", submodelElementAttributes.getIdShort());
        assertThat(submodelElementAttributes.getSemanticId()).isNull();

        configSubmodelElementProperty.setSemanticId("MySemanticId");
        when(mockIdGenerator.generateSemanticId(or(any(Node.class), isNull()), or(any(ConfigIdGeneration.class), isNull())))
            .thenReturn("MySemanticId");

        submodelElementAttributes = classUnderTest
            .getSubmodelAttributes(subAttribute, configSubmodelElementProperty);
        assertNotNull(submodelElementAttributes);
        assertEquals("MySubAttribute", submodelElementAttributes.getIdShort());
        assertThat(submodelElementAttributes.getSemanticId()).isInstanceOf(Reference.class);
        Reference semanticId = submodelElementAttributes.getSemanticId();
        assertThat(semanticId.getKeys()).isNotNull();
        assertThat(semanticId.getKeys()).hasSize(1);
        assertThat(semanticId.getKeys().get(0).getValue()).isEqualTo("MySemanticId");
        assertThat(semanticId.getKeys().get(0).getIdType()).isEqualTo(KeyType.IRDI);
        assertThat(semanticId.getKeys().get(0).getType()).isEqualTo(KeyElements.CONCEPT_DESCRIPTION);
    }

    @Test
    @DisplayName("Get language property values from AML node")
    void getLanguageProperties() {
        addLanguageElementsToAttribute();

        Map<String, String> result = classUnderTest.getLanguageProperties(attribute);

        assertNotNull(result);
        assertThat(result).hasSize(2);
        assertThat(result.keySet()).containsExactly("en", "de");
        assertThat(result.get("en")).isEqualTo("Hello World");
        assertThat(result.get("de")).isEqualTo("Hallo Welt");
    }

    @ParameterizedTest
    @DisplayName("Get language code out of language string")
    @CsvSource({"en-US,en", "en,en", "de-AT,de", "zh-Hant-TW,zh"})
    void getLanguageCode(String languageString, String languageCode) {
        String langCode = classUnderTest.getLanguageCode(languageString);

        assertEquals(languageCode, langCode);
    }

    private void addLanguageElementsToAttribute() {
        Element enValueElement = attribute.addElement("caex:Attribute");
        enValueElement.addAttribute("Name", "aml-lang=en-US");
        enValueElement.addElement("caex:Description").setText("This is an english text");
        enValueElement.addElement("caex:Value").setText("Hello World");

        Element deValueElement = attribute.addElement("caex:Attribute");
        deValueElement.addAttribute("Name", "aml-lang=de-DE");
        deValueElement.addElement("caex:Description").setText("Dies ist ein Text in Deutsch");
        deValueElement.addElement("caex:Value").setText("Hallo Welt");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("configSubmodelElementValues")
    @DisplayName("Get the value for a submodel element")
    void getSubmodelElementValue(String message, String expectedValue, ConfigProperty configProperty) {
        String value = classUnderTest.getSubmodelElementValue(subAttribute, configProperty);
        assertEquals(expectedValue, value, message);
    }

}
