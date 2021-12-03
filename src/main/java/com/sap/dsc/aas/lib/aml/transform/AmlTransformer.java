/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.xml.XMLConstants;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sap.dsc.aas.lib.aml.transform.validation.AmlSchemaValidator;
import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.transform.DocumentTransformer;
import com.sap.dsc.aas.lib.transform.XPathHelper;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;
import com.sap.dsc.aas.lib.transform.validation.SchemaValidator;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class AmlTransformer extends DocumentTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SchemaValidator amlValidator;

    private PreconditionValidator preconditionValidator;

    public AmlTransformer() {
        this(new PreconditionValidator());
        XPathHelper.getInstance().setNamespaceBinding("caex", "http://www.dke.de/CAEX");
    }

    public AmlTransformer(PreconditionValidator validator) {// FIXME only used by tests
        this.preconditionValidator = validator;
        this.amlValidator = new AmlSchemaValidator();
    }

    @Override
    public void validateDocument(Document document) throws TransformationException {
        this.amlValidator.validate(document);
    }

    @Override
    public Document readXmlDocument(InputStream amlStream) throws TransformationException {
        try {
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return reader.read(amlStream);
        } catch (DocumentException | SAXException e) {
            throw new UnableToReadXmlException("Unable to load AML structure", e);
        }
    }

    @Override
    public SchemaValidator getSchemaValidator() {
        return this.amlValidator;
    }

    @Override
    protected void afterValidation(Document readXmlDocument, MappingSpecification mapping) {
        LOGGER.info("Loaded config version {}, AAS version {}",
            getValidatedVersionString(mapping.getHeader().getVersion()),
            getValidatedVersionString(mapping.getHeader().getAasVersion()));
    }

    protected String getValidatedVersionString(String version) {
        if (version == null) {
            return "[No version provided]";
        }
        if (version.matches("[0-9]+(\\.[0-9]+){1,2}")) {
            return version;
        }
        return "[Invalid version string provided]";
    }


}
