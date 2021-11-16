/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;

public abstract class AbstractTransformerTest {

    public static final String ID_VALUE = "1234";
    public static final String VALUE_CONTENT = "MyValue";
    public static final String DEFAULT_VALUE_CONTENT = "OtherValue";
    public static final String DEFAULT_CONFIG_ELEMENT_ID = "myConfigElementId";

    protected Document document;
    protected Element unitClass;
    protected Element attribute;
    protected Element subAttribute;

    protected MappingSpecification mapping;

    protected PreconditionValidator mockPreconditionValidator;

    protected void setUp() throws Exception {
		TestUtils.resetBindings();
        // sample AML document
        this.document = createDocument();
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
