package com.sap.dsc.aas.lib.aml.transform.validation;

import java.net.URL;

import org.dom4j.Document;

import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;

public abstract class SchemaValidator {

	private URL schemaUrl;

	public SchemaValidator(URL schemaUrl) {
		this.schemaUrl = schemaUrl;
	}

	public abstract void validate(Document document) throws TransformationException;

	protected URL getSchemaURL() {
		return schemaUrl;
	}

}
