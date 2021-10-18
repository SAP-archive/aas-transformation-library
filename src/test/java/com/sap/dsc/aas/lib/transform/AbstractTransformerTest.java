/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dom4j.*;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.config.pojo.*;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigProperty;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigSubmodelElementCollection;
import com.sap.dsc.aas.lib.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;

public abstract class AbstractTransformerTest {

    public static final String ID_VALUE = "1234";
    public static final String DEFAULT_VALUE = "Default Value Set";
    public static final String VALUE_CONTENT = "MyValue";
    public static final String DEFAULT_VALUE_CONTENT = "OtherValue";
    public static final String DEFAULT_CONFIG_ELEMENT_ID = "myConfigElementId";

    protected ConfigMapping configMapping;
    protected ConfigMapping configMapping2;
    protected ConfigSubmodel configSubmodel;

    protected ConfigSubmodel configSubmodelMultipleAttributes;
    protected ConfigAssetInformation configAssetInformation;

    protected ConfigProperty configSubmodelElementProperty;
    protected ConfigProperty configSubmodelElementMultiple;
    protected ConfigSubmodelElementCollection configSubmodelElementCollection;
    protected Document document;
    protected Element unitClass;
    protected Element attribute;
    protected Element subAttribute;
    protected ConfigTransformToAas mapping;

    protected IdGenerator mockIdGenerator;
    protected PreconditionValidator mockPreconditionValidator;

    protected void setUp() throws Exception {
		TestUtils.resetBindings();
        // sample configuration
        this.mapping = createDefaultMappingConfig();
        // sample AML document
        this.document = createDocument();
        this.mockIdGenerator = mock(IdGenerator.class);
        this.mockPreconditionValidator = mock(PreconditionValidator.class);
    }

    protected ConfigIdGeneration createSimpleIdGeneration(String idValue) {
        ConfigIdGeneration idGeneration = new ConfigIdGeneration();
        ConfigIdGenerationParameter parameter = new ConfigIdGenerationParameter();
        parameter.setValueDefault(idValue);
        idGeneration.setParameters(Arrays.asList(parameter));
        return idGeneration;
    }

    protected ConfigTransformToAas createDefaultMappingConfig() {
        ConfigAssetShell configAssetShell = new ConfigAssetShell();
        configAssetShell.setIdGeneration(createSimpleIdGeneration("5678"));
        configAssetShell.setIdShortXPath("'ShellIdShort'");

        ConfigTransformToAas configAmlToAas = new ConfigTransformToAas();

        // higher level object
        configMapping = new ConfigMapping();

        configMapping.setXPath(
            "//caex:SystemUnitClass[caex:SupportedRoleClass/@RefRoleClassPath='AutomationMLComponentStandardRCL/AutomationComponent']");
        configMapping.setIdShortXPath("@Name");
        configMapping.setIdGeneration(createSimpleIdGeneration(ID_VALUE));
        configMapping.setConfigElementId(DEFAULT_CONFIG_ELEMENT_ID);

        configSubmodel = new ConfigSubmodel();
        configSubmodel.setXPath("caex:Attribute[@Name='MyAttribute']");
        configSubmodel.setSemanticId("mySemanticId");
        configSubmodel.setIdGeneration(createSimpleIdGeneration(ID_VALUE + "_submodel"));

        configAssetInformation = new ConfigAssetInformation();
        ConfigReference globalIdReference = new ConfigReference();
        globalIdReference.setValueId("SAMPLE_ID_MUST_COME_FROM_XPATH");
        globalIdReference.setKeyElement(KeyElements.ASSET);
        globalIdReference.setKeyType(KeyType.CUSTOM);
        configAssetInformation.setGlobalAssetIdReference(globalIdReference);

        configSubmodelElementProperty = new ConfigProperty();
        configSubmodelElementProperty.setAttributeName("MySubAttribute");
        configSubmodelElementProperty.setValueType("String");

        configSubmodel.setSubmodelElements(Collections.singletonList(configSubmodelElementProperty));

        configSubmodelMultipleAttributes = new ConfigSubmodel();
        configSubmodelMultipleAttributes.setXPath("caex:Attribute");

        configSubmodelElementMultiple = new ConfigProperty();
        configSubmodelElementMultiple.setXPath("caex:Attribute");
        configSubmodelElementMultiple.setValueType("String");
        configSubmodelMultipleAttributes.setSubmodelElements(Collections.singletonList(configSubmodelElementMultiple));

        configSubmodelElementCollection = new ConfigSubmodelElementCollection();
        configSubmodelElementCollection.setXPath("");
        configSubmodelElementCollection.setSubmodelElements(Arrays.asList(configSubmodelElementProperty, configSubmodelElementMultiple));

        configMapping2 = new ConfigMapping();
        configMapping2.setXPath("//caex:SystemUnitClass[caex:SupportedRoleClass/@RefRoleClassPath='AutomationMLStandardRCL/Asset']");
        configMapping2.setIdShortXPath("@Name");
        configMapping2.setIdGeneration(createSimpleIdGeneration("5678"));

        configMapping.setConfigAssetShell(configAssetShell);
        configMapping.setSubmodels(Arrays.asList(configSubmodel, configSubmodelMultipleAttributes));
        configMapping.setConfigAssetInformation(configAssetInformation);

        List<ConfigMapping> configMappingList = new ArrayList<>();
        configMappingList.add(configMapping);
        configMappingList.add(configMapping2);

        configAmlToAas.setConfigMappings(configMappingList);

        return configAmlToAas;
    }

    // Create a sample AML document (xml) for use in testing
    protected Document createDocument() {
        Document document = DocumentHelper.createDocument();

        Namespace caexNs = new Namespace("caex", "http://www.dke.de/CAEX");

        Element rootNode = document.addElement(new QName("CAEXFile", caexNs));
        Element unitLib = rootNode.addElement(new QName("SystemUnitClassLib", caexNs))
            .addAttribute("Name", "SystemUnitClassLib");
        // Create a system unit class representing one asset
        unitClass = unitLib.addElement(new QName("SystemUnitClass", caexNs))
            .addAttribute("Name", "MyClass")
            .addAttribute("ID", "MyClassId");
        unitClass.addElement(new QName("SupportedRoleClass", caexNs))
            .addAttribute("RefRoleClassPath", "AutomationMLComponentStandardRCL/AutomationComponent");

        // Create an attribute representing a submodel
        attribute = unitClass.addElement(new QName("Attribute", caexNs))
            .addAttribute("Name", "MyAttribute");
        unitClass.addElement(new QName("Attribute", caexNs))
            .addAttribute("Name", "MyAttribute2");

        // Create an attribute representing a submodel element
        attribute.addElement(new QName("Attribute", caexNs))
            .addAttribute("Name", "MyFirstNotSearchedSubAttribute")
            .addElement(new QName("Value", caexNs)).addText("Not Searched");
        subAttribute = attribute.addElement(new QName("Attribute", caexNs))
            .addAttribute("Name", "MySubAttribute");
        subAttribute.addElement(new QName("Value", caexNs))
            .addText(VALUE_CONTENT);
        subAttribute.addElement(new QName("DefaultValue", caexNs))
            .addText(DEFAULT_VALUE_CONTENT);

        unitLib.addElement(new QName("SystemUnitClass", caexNs))
            .addAttribute("Name", "SecondComponent")
            .addAttribute("ID", "SecondComponentId")
            .addElement(new QName("SupportedRoleClass", caexNs))
            .addAttribute("RefRoleClassPath", "AutomationMLComponentStandardRCL/AutomationComponent");

        unitLib.addElement(new QName("SystemUnitClass", caexNs))
            .addAttribute("Name", "ThirdAssetShortId")
            .addAttribute("ID", "ThirdAssetId")
            .addElement(new QName("SupportedRoleClass", caexNs))
            .addAttribute("RefRoleClassPath", "AutomationMLStandardRCL/Asset");

        return document;
    }

}
