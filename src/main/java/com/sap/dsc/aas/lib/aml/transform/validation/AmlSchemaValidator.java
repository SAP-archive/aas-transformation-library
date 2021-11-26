/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform.validation;

import java.lang.invoke.MethodHandles;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.transform.validation.SchemaValidator;

public class AmlSchemaValidator extends SchemaValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String PATH_CAEX_XSD = "aml/CAEX_ClassModel_V.3.0.xsd";

    public AmlSchemaValidator() {
        super(Thread.currentThread().getContextClassLoader().getResource(PATH_CAEX_XSD));
    }

    @Override
    public void validate(Document document) throws UnableToReadXmlException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Validating AML input...");
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(getSchemaURL());

            Validator validator = schema.newValidator();
            // Prevent allowing external entities in untrusted documents to be processed
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            validator.validate(new DocumentSource(document));
        } catch (Exception e) {
            throw new UnableToReadXmlException("Error during AML validation", e);
        }
    }

}
