/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.xml.XMLConstants;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigTransformToAas;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigMapping;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;
import com.sap.dsc.aas.lib.aml.exceptions.UnableToReadAmlException;
import com.sap.dsc.aas.lib.aml.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.aml.transform.validation.SchemaValidator;
import com.sap.dsc.aas.lib.aml.transform.validation.AbstractValidator;
import com.sap.dsc.aas.lib.aml.transform.validation.AmlSchemaValidator;
import com.sap.dsc.aas.lib.aml.transform.validation.PreconditionValidator;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class AmlTransformer extends AbstractTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private AssetAdministrationShellEnvTransformer assetAdministrationShellEnvTransformer;
    private SchemaValidator amlValidator;

    public AmlTransformer() {
        this(new IdGenerator(), new PreconditionValidator());
    }

    public AmlTransformer(IdGenerator idGenerator, PreconditionValidator validator) {
        super(idGenerator, validator);
        this.assetAdministrationShellEnvTransformer = new AssetAdministrationShellEnvTransformer(idGenerator, preconditionValidator);
        this.amlValidator = new AmlSchemaValidator();
    }

    /**
     * Transforms an AML file to AAS. We expect the AML file to be UTF-8 encoded.
     *
     * @param amlStream
     * @param mapping
     * @return
     * @throws TransformationException
     */
	@Override
    public AssetAdministrationShellEnvironment transform(InputStream amlStream, ConfigTransformToAas mapping)
        throws TransformationException {

        Document document = getXmlDocument(amlStream);
        this.amlValidator.validate(document);

        List<ConfigMapping> configMappings = mapping.getConfigMappings();

        LOGGER.info("Loaded config version {}, AAS version {}",
            getValidatedVersionString(mapping.getVersion()),
            getValidatedVersionString(mapping.getAasVersion()));

        this.preconditionValidator.setPreconditions(mapping.getPreconditions());
        this.idGenerator.prepareGraph(document, configMappings);
        return assetAdministrationShellEnvTransformer.createShellEnv(document, configMappings);
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

    /**
     * Validates a given AML file. We expect the AML file to be UTF-8 encoded.
     *
     * Note that the input stream will be read and closed by this method.
     *
     * @param amlStream File input stream
     * @throws TransformationException If the input stream is not valid AML
     */
    public void validateAml(InputStream amlStream) throws TransformationException {
        Document document = getXmlDocument(amlStream);
        this.amlValidator.validate(document);
    }

    /**
     * Parses and validates XML Document from InputStream.
     *
     * The SAXReader is unable to parse the XML file if it uses the wrong encoding to read an input
     * stream. We expect files in UTF-8 format only. Otherwise SAXReader relies on the System Charset,
     * which i.e. in the case of Docker containers (sapmachine:11) can be: US-ASCII if the Locale is not
     * set and `locale` output is: LANG= LANGUAGE= LC_CTYPE="POSIX" LC_NUMERIC="POSIX" LC_TIME="POSIX"
     * LC_COLLATE="POSIX" LC_MONETARY="POSIX" LC_MESSAGES="POSIX" LC_PAPER="POSIX" LC_NAME="POSIX"
     * LC_ADDRESS="POSIX" LC_TELEPHONE="POSIX" LC_MEASUREMENT="POSIX" LC_IDENTIFICATION="POSIX" LC_ALL=
     *
     * @param amlStream
     * @return
     * @throws TransformationException
     */
    public Document getXmlDocument(InputStream amlStream) throws TransformationException {
        try {
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return reader.read(amlStream);
        } catch (DocumentException | SAXException e) {
            throw new UnableToReadAmlException("Unable to load AML structure", e);
        }
    }
}
