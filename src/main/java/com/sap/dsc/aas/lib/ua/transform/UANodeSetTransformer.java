/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.ua.transform;

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

import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.transform.DocumentTransformer;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;
import com.sap.dsc.aas.lib.transform.validation.SchemaValidator;
import com.sap.dsc.aas.lib.ua.transform.validation.UANodeSetSchemaValidator;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

// TODO write tests
public class UANodeSetTransformer extends DocumentTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SchemaValidator nodesetValidator;

    private PreconditionValidator preconditionValidator;

    public UANodeSetTransformer() {
        this(new PreconditionValidator());
        // FIXME check for needed namespace bindings
        // XPathHelper.getInstance().setNamespaceBinding(null, null);
    }

    public UANodeSetTransformer(PreconditionValidator validator) {// FIXME only used by tests
        this.preconditionValidator = validator;
        this.nodesetValidator = new UANodeSetSchemaValidator();
    }

    @Override
    public void validateDocument(Document document) throws TransformationException {
        this.nodesetValidator.validate(document);
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
            throw new UnableToReadXmlException("Unable to load UA NodeSet structure", e);
        }
    }

    @Override
    public SchemaValidator getSchemaValidator() {
        return this.nodesetValidator;
    }

    @Override
    protected void afterValidation(Document readXmlDocument, MappingSpecification mapping) {
        LOGGER.info("NodeSet validated.");
        BrowsepathXPathBuilder.updateInstance(readXmlDocument);
        LOGGER.info("BrowsepathBuilder set to validated NodeSet.");
    }

    @Override
    public AssetAdministrationShellEnvironment createShellEnv(Document validXmlDocument,
        MappingSpecification mapping, Map<String, String> initialVars) throws TransformationException {
        setNamespaces(mapping.getHeader().getNamespaces());
        preconditionValidator.setPreconditions(mapping.getHeader().getPreconditions());
        return super.createShellEnv(validXmlDocument, mapping, initialVars);
    }
}
