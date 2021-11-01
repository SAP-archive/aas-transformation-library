package com.sap.dsc.aas.lib.transform;

import com.sap.dsc.aas.lib.config.pojo.ConfigTransformToAas;
import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.transform.validation.SchemaValidator;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import org.dom4j.Document;

import java.io.InputStream;

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

	/**actual transformation done after a successful XML read and validation action
	 * 
	 * @param readXmlDocument
	 * @param mapping
	 * @return
	 * @throws TransformationException
	 */
	protected abstract AssetAdministrationShellEnvironment createShellEnv(Document readXmlDocument, ConfigTransformToAas mapping) throws TransformationException;

	/**
	 * Parses and XML Document from InputStream.
	 *
	 * Note that the input stream will be read and closed by this method.
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
	 * Validates a given XML file. We expect the XML file to be UTF-8 encoded.
	 *
	 *
	 * @param document read org.dom4j.Document
	 * @throws TransformationException If the input stream is not valid
	 */
	public void validateDocument(Document document) throws TransformationException {
		getSchemaValidator().validate(document);
	}

	public abstract SchemaValidator getSchemaValidator();

}
