package com.sap.dsc.aas.lib.aml.transform;

import java.io.InputStream;

import org.dom4j.Document;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigTransformToAas;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;
import com.sap.dsc.aas.lib.aml.transform.validation.SchemaValidator;

import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public abstract class DocumentTransformer {

	/**
	 * Transforms an XML file to AAS. We expect the XML file to be UTF-8 encoded.
	 *
	 * @param inStream
	 * @param mapping
	 * @return
	 * @throws TransformationException
	 */
	public AssetAdministrationShellEnvironment transform(InputStream inStream, ConfigTransformToAas mapping)
			throws TransformationException {
		Document readXmlDocument = readXmlDocument(inStream);
		validateDocument(readXmlDocument);
		afterValidation(readXmlDocument, mapping);
		return createShellEnv(readXmlDocument, mapping);
	}

	/**
	 * Function called after document is validated and before shell environment gets created.
	 * 
	 * @param readXmlDocument
	 * @param mapping
	 */
	protected abstract void afterValidation(Document readXmlDocument, ConfigTransformToAas mapping);

	protected abstract AssetAdministrationShellEnvironment createShellEnv(Document readXmlDocument, ConfigTransformToAas mapping) throws TransformationException;

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
	 * @param inStream
	 * @return
	 * @throws TransformationException
	 */
	public abstract Document readXmlDocument(InputStream inStream) throws TransformationException;

	/**
	 * Validates a given AML file. We expect the AML file to be UTF-8 encoded.
	 *
	 * Note that the input stream will be read and closed by this method.
	 *
	 * @param document read org.dom4j.Document
	 * @throws TransformationException If the input stream is not valid
	 */
	public void validateDocument(Document document) throws TransformationException {
		getSchemaValidator().validate(document);
	}

	public abstract SchemaValidator getSchemaValidator();

}
