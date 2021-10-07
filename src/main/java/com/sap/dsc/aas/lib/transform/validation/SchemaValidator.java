package com.sap.dsc.aas.lib.transform.validation;

import java.net.URL;

import org.dom4j.Document;

import com.sap.dsc.aas.lib.exceptions.TransformationException;

public abstract class SchemaValidator {

	private URL schemaUrl;

	public SchemaValidator(URL schemaUrl) {
		this.schemaUrl = schemaUrl;
	}

	public abstract void validate(Document document) throws TransformationException;

	public URL getSchemaURL() {
		return schemaUrl;
	}

}
