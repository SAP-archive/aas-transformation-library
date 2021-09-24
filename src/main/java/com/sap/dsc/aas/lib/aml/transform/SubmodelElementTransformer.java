/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache 2.0 
 */
package com.sap.dsc.aas.lib.aml.transform;

import java.util.*;
import java.util.stream.Collectors;

import org.dom4j.Element;
import org.dom4j.Node;

import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.*;
import com.sap.dsc.aas.lib.aml.exceptions.ModelTypeNotSupportedException;
import com.sap.dsc.aas.lib.aml.exceptions.NoResultByXPathException;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;
import com.sap.dsc.aas.lib.aml.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.aml.transform.pojo.GenericSubmodelElementAttributes;
import com.sap.dsc.aas.lib.aml.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.*;

public class SubmodelElementTransformer extends AbstractTransformer {

    public SubmodelElementTransformer(IdGenerator idGenerator, PreconditionValidator preconditionValidator) {
        super(idGenerator, preconditionValidator);
    }

    protected List<SubmodelElement> createSubmodelElements(Node submodelNode,
        List<AbstractConfigSubmodelElement> configSubmodelElements)
        throws TransformationException {
        List<SubmodelElement> submodelElements = new ArrayList<>();

        for (AbstractConfigSubmodelElement configSubmodelElement : configSubmodelElements) {
            List<SubmodelElement> submodelElementsFromConfig = createSubmodelElementsFromConfig(submodelNode, configSubmodelElement);
            submodelElements.addAll(submodelElementsFromConfig);
        }

        return submodelElements;
    }

    protected List<SubmodelElement> createSubmodelElementsFromConfig(Node submodelNode, AbstractConfigSubmodelElement configSubmodelElement)
        throws TransformationException {
        List<Node> submodelElementNodes = xPathHelper.getNodes(submodelNode, configSubmodelElement.getXPath());
        preconditionValidator.validate(configSubmodelElement, submodelElementNodes);

        List<SubmodelElement> submodelElements = new ArrayList<>();
        for (Node submodelElementNode : submodelElementNodes) {
            submodelElements.add(createSubmodelElement(submodelElementNode, configSubmodelElement));
        }

        return submodelElements;
    }

    protected SubmodelElement createSubmodelElement(Node submodelElementNode, AbstractConfigSubmodelElement configSubmodelElement)
        throws TransformationException {
        GenericSubmodelElementAttributes submodelElementAttributes = getSubmodelAttributes(submodelElementNode, configSubmodelElement);

        SubmodelElement submodelElement;
        switch (configSubmodelElement.getClass().getSimpleName()) {
            case "ConfigSubmodelElementCollection":
                submodelElement = createSubmodelElementCollection(submodelElementNode,
                    (ConfigSubmodelElementCollection) configSubmodelElement, submodelElementAttributes);
                break;
            case "ConfigProperty":
                submodelElement = createProperty(submodelElementNode, (ConfigProperty) configSubmodelElement, submodelElementAttributes);
                break;
            case "ConfigMultiLanguageProperty":
                submodelElement = createMultiLanguageProperty(submodelElementNode, (ConfigMultiLanguageProperty) configSubmodelElement,
                    submodelElementAttributes);
                break;
            case "ConfigRange":
                submodelElement = createRange(submodelElementNode, (ConfigRange) configSubmodelElement, submodelElementAttributes);
                break;
            case "ConfigEntity":
                submodelElement = createEntity(submodelElementNode, (ConfigEntity) configSubmodelElement, submodelElementAttributes);
                break;
            case "ConfigReferenceElement":
                submodelElement = createReferenceElement(submodelElementNode, (ConfigReferenceElement) configSubmodelElement,
                    submodelElementAttributes);
                break;
            case "ConfigAnnotatedRelationshipElement":
                submodelElement = createAnnotatedRelationshipElement(submodelElementNode,
                    (ConfigAnnotatedRelationshipElement) configSubmodelElement,
                    submodelElementAttributes);
                break;
            case "ConfigRelationshipElement":
                submodelElement = createRelationshipElement(submodelElementNode, (ConfigRelationshipElement) configSubmodelElement,
                    submodelElementAttributes);
                break;
            case "ConfigOperation":
                submodelElement = createOperation(submodelElementNode, (ConfigOperation) configSubmodelElement,
                    submodelElementAttributes);
                break;
            case "ConfigCapability":
                submodelElement = createCapability(submodelElementAttributes);
                break;
            case "ConfigFile":
                submodelElement = createFile(submodelElementNode, (ConfigFile) configSubmodelElement, submodelElementAttributes);
                break;
            case "ConfigBlob":
                submodelElement = createBlob(submodelElementNode, (ConfigBlob) configSubmodelElement, submodelElementAttributes);
                break;
            case "ConfigBasicEvent":
                submodelElement = createBasicEvent(submodelElementNode, (ConfigBasicEvent) configSubmodelElement,
                    submodelElementAttributes);
                break;
            default:
                throw new ModelTypeNotSupportedException(configSubmodelElement.getClass().getName());
        }
        return submodelElement;
    }

    protected String getSubmodelElementValue(Node submodelElementNode, ConfigProperty configProperty) {
        String value = null;
        if (configProperty.getValueXPath() != null) {
            value = xPathHelper.getStringValueOrNull(submodelElementNode, configProperty.getValueXPath());
        } else if (configProperty.getValueDefault() == null) {
            value = xPathHelper.getStringValueOrNull(submodelElementNode, configProperty.getDefaultValueXPath());
        }

        if (value == null) {
            value = configProperty.getValueDefault();
        }
        return value;
    }

    /**
     *
     * @param submodelElementCollectionNode Node of the submodel element collection
     * @param configSubmodelElementCollection Transformation config for the collection
     * @param submodelElementAttributes SubmodelElement generic attributes, like idShort
     * @return The created submodel element collection
     *
     * @throws TransformationException If something goes wrong during transformation
     */
    protected SubmodelElementCollection createSubmodelElementCollection(
        Node submodelElementCollectionNode, ConfigSubmodelElementCollection configSubmodelElementCollection,
        GenericSubmodelElementAttributes submodelElementAttributes)
        throws TransformationException {

        List<SubmodelElement> children = createSubmodelElements(submodelElementCollectionNode,
            configSubmodelElementCollection.getSubmodelElements());

        return new DefaultSubmodelElementCollection.Builder()
            .values(children)
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .allowDuplicates(true)
            .ordered(false)
            .build();
    }

    protected Property createProperty(Node submodelElementNode, ConfigProperty configProperty,
        GenericSubmodelElementAttributes submodelElementAttributes) {

        String value = getSubmodelElementValue(submodelElementNode, configProperty);
        Reference valueId = createReference(value, KeyElements.PROPERTY, KeyType.CUSTOM);

        return new DefaultProperty.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .value(value)
            .valueId(valueId)
            .valueType(configProperty.getValueType())
            .build();
    }

    protected MultiLanguageProperty createMultiLanguageProperty(Node submodelElementNode,
        ConfigMultiLanguageProperty configMultiLanguageProperty,
        GenericSubmodelElementAttributes submodelElementAttributes) throws NoResultByXPathException {

        List<LangString> values = new ArrayList<>();
        if (configMultiLanguageProperty.getValues() == null) {
            Map<String, String> languageValues = getLanguageProperties(submodelElementNode);
            languageValues.forEach((key, value) -> values.add(new LangString(value, key)));
        } else {
            for (ConfigLanguageProperty languageProperty : configMultiLanguageProperty.getValues()) {
                String lang = xPathHelper.getStringValue(submodelElementNode, languageProperty.getLangXPath());
                String langValue = xPathHelper.getStringValue(submodelElementNode, languageProperty.getValueXPath());
                LangString langString = new LangString(langValue, lang);

                values.add(langString);
            }
        }
        Reference valueId = null;
        if (configMultiLanguageProperty.getValueIdReference() != null) {
            valueId = createReferenceFromConfig(submodelElementNode, configMultiLanguageProperty.getValueIdReference());
        }

        return new DefaultMultiLanguageProperty.Builder()
            .values(values)
            .valueId(valueId)
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .build();
    }

    protected Map<String, String> getLanguageProperties(Node submodelElementNode) {
        List<Node> langNodes = xPathHelper.getNodes(submodelElementNode, ConfigMultiLanguageProperty.DEFAULT_ATTRIBUTE_XPATH);

        Map<String, String> languageValues = new HashMap<>();
        for (Node langNode : langNodes) {
            String lang = getLanguageCode(((Element) langNode).attribute("Name").getValue().substring(9));
            String value = ((Element) langNode).elementText(ConfigMultiLanguageProperty.DEFAULT_VALUE_XPATH);
            languageValues.put(lang, value);
        }

        return languageValues;
    }

    protected String getLanguageCode(String language) {
        Locale lang = Locale.forLanguageTag(language);

        return lang.getLanguage();
    }

    private AnnotatedRelationshipElement createAnnotatedRelationshipElement(Node submodelElementNode,
        ConfigAnnotatedRelationshipElement configAnnotatedRelationshipElement, GenericSubmodelElementAttributes submodelElementAttributes)
        throws TransformationException {

        Reference first = createReferenceFromConfig(submodelElementNode, configAnnotatedRelationshipElement.getFirst());
        Reference second = createReferenceFromConfig(submodelElementNode, configAnnotatedRelationshipElement.getSecond());

        List<DataElement> annotations = createSubmodelElements(submodelElementNode,
            configAnnotatedRelationshipElement.getAnnotations())
                .stream()
                .map(DataElement.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));

        return new DefaultAnnotatedRelationshipElement.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .first(first)
            .second(second)
            .annotations(annotations)
            .build();
    }

    private RelationshipElement createRelationshipElement(Node submodelElementNode,
        ConfigRelationshipElement configRelationshipElement, GenericSubmodelElementAttributes submodelElementAttributes) {

        Reference first = createReferenceFromConfig(submodelElementNode, configRelationshipElement.getFirst());
        Reference second = createReferenceFromConfig(submodelElementNode, configRelationshipElement.getSecond());

        return new DefaultRelationshipElement.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .first(first)
            .second(second)
            .build();
    }

    private Operation createOperation(Node submodelElementNode, ConfigOperation configOperation,
        GenericSubmodelElementAttributes submodelElementAttributes) throws TransformationException {

        List<OperationVariable> inputVariables = createOperationVariables(submodelElementNode,
            configOperation.getInputVariables());
        List<OperationVariable> outputVariables = createOperationVariables(submodelElementNode,
            configOperation.getOutputVariables());
        List<OperationVariable> inOutputVariables = createOperationVariables(submodelElementNode,
            configOperation.getInOutputVariables());

        return new DefaultOperation.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .inputVariables(inputVariables)
            .outputVariables(outputVariables)
            .inoutputVariables(inOutputVariables)
            .build();
    }

    private List<OperationVariable> createOperationVariables(Node submodelElementNode,
        List<AbstractConfigSubmodelElement> submodelElements) throws TransformationException {

        return createSubmodelElements(submodelElementNode, submodelElements)
            .stream()
            .map(s -> new DefaultOperationVariable.Builder().value(s).build())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private ReferenceElement createReferenceElement(Node submodelElementNode, ConfigReferenceElement configReferenceElement,
        GenericSubmodelElementAttributes submodelElementAttributes) {
        Reference reference = createReferenceFromConfig(submodelElementNode, configReferenceElement.getReference());

        return new DefaultReferenceElement.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .value(reference)
            .build();
    }

    protected Range createRange(Node submodelElementNode, ConfigRange configRange,
        GenericSubmodelElementAttributes submodelElementAttributes) {

        String min = xPathHelper.getStringValueOrNull(submodelElementNode, configRange.getMinValueXPath());
        String max = xPathHelper.getStringValueOrNull(submodelElementNode, configRange.getMaxValueXPath());
        String valueType = configRange.getValueType();

        return new DefaultRange.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .min(min)
            .max(max)
            .valueType(valueType)
            .build();
    }

    private Entity createEntity(Node submodelElementNode, ConfigEntity configEntity,
        GenericSubmodelElementAttributes submodelElementAttributes) throws TransformationException {
        Reference assetReference = createReferenceFromConfig(submodelElementNode, configEntity.getAssetReference());
        List<SubmodelElement> statements = createSubmodelElements(submodelElementNode, configEntity.getStatements());

        return new DefaultEntity.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .entityType(configEntity.getEntityType())
            .statements(statements)
            .globalAssetId(assetReference)
            .build();
    }

    private Capability createCapability(GenericSubmodelElementAttributes submodelElementAttributes) {
        return new DefaultCapability.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .build();
    }

    private File createFile(Node submodelElementNode, ConfigFile configFile,
        GenericSubmodelElementAttributes submodelElementAttributes)
        throws NoResultByXPathException {
        String mimeType = xPathHelper.getStringValue(submodelElementNode, configFile.getMimeTypeXPath());
        String value = xPathHelper.getStringValueOrNull(submodelElementNode, configFile.getValueXPath());

        return new DefaultFile.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .value(value)
            .mimeType(mimeType)
            .build();
    }

    private SubmodelElement createBlob(Node submodelElementNode, ConfigBlob configBlob,
        GenericSubmodelElementAttributes submodelElementAttributes)
        throws NoResultByXPathException {
        String mimeType = xPathHelper.getStringValue(submodelElementNode, configBlob.getMimeTypeXPath());
        byte[] value = xPathHelper.getStringValueOrNull(submodelElementNode, configBlob.getValueXPath()).getBytes();

        return new DefaultBlob.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .value(value)
            .mimeType(mimeType)
            .build();
    }

    private BasicEvent createBasicEvent(Node submodelElementNode, ConfigBasicEvent configBasicEvent,
        GenericSubmodelElementAttributes submodelElementAttributes) {

        Reference reference = createReferenceFromConfig(submodelElementNode, configBasicEvent.getObserved());

        return new DefaultBasicEvent.Builder()
            .idShort(submodelElementAttributes.getIdShort())
            .semanticId(submodelElementAttributes.getSemanticId())
            .kind(submodelElementAttributes.getKind())
            .embeddedDataSpecifications(submodelElementAttributes.getEmbeddedDataSpecifications())
            .qualifiers(submodelElementAttributes.getQualifiers())
            .descriptions(submodelElementAttributes.getDescriptions())
            .category(submodelElementAttributes.getCategory())
            .observed(reference)
            .build();
    }

    protected GenericSubmodelElementAttributes getSubmodelAttributes(Node submodelElementNode,
        AbstractConfigSubmodelElement configSubmodelElement)
        throws NoResultByXPathException {
        GenericSubmodelElementAttributes submodelElementAttributes = new GenericSubmodelElementAttributes();

        String idShort = xPathHelper.getStringValue(submodelElementNode, configSubmodelElement.getIdShortXPath());

        ConfigReference configSemanticIdReference = configSubmodelElement.getSemanticIdReference();
        Reference semanticIdReference = null;
        if (configSemanticIdReference != null) {
            String semanticId = idGenerator.generateSemanticId(submodelElementNode, configSemanticIdReference.getIdGeneration());
            semanticIdReference =
                createReference(semanticId, configSemanticIdReference.getKeyElement(), configSemanticIdReference.getKeyType());
        }

        submodelElementAttributes.setIdShort(idShort);
        submodelElementAttributes.setSemanticId(semanticIdReference);
        submodelElementAttributes.setCategory(configSubmodelElement.getCategory());
        submodelElementAttributes.setKind(configSubmodelElement.getKind());

        return submodelElementAttributes;
    }
}
