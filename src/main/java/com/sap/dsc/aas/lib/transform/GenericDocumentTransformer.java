/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import java.io.InputStream;

import javax.xml.XMLConstants;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;
import com.sap.dsc.aas.lib.transform.validation.SchemaValidator;

/**
 * Can transform any XML Document. Does not contain any validation despite parsing the generic XML
 * structure into a Document Object.
 *
 */
public class GenericDocumentTransformer extends DocumentTransformer {

    private PreconditionValidator preconditionValidator;

    public GenericDocumentTransformer() {
        this(new PreconditionValidator());
    }

    public GenericDocumentTransformer(PreconditionValidator validator) { // tests
        this.preconditionValidator = validator;
    }

    @Override
    public void validateDocument(Document document) throws TransformationException {
        return;
    }

    @Override
    public Document readXmlDocument(InputStream inStream) throws TransformationException {
        try {
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return reader.read(inStream);
        } catch (DocumentException | SAXException e) {
            throw new UnableToReadXmlException("Unable to Document.", e);
        }
    }

    @Override
    public SchemaValidator getSchemaValidator() {
        return new SchemaValidator(null) {
            @Override
            public void validate(Document document) throws TransformationException {
                return;
            }
        };
    }

    @Override
    protected void afterValidation(Document readXmlDocument, MappingSpecification mapping) {
        return;
    }
}
