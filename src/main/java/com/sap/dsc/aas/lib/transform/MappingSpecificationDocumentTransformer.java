/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.lang.invoke.MethodHandles;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.mapping.AASMappingTransformer;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;

public class MappingSpecificationDocumentTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public MappingSpecificationDocumentTransformer() {
	}

	/**
	 * Map document based on the mapping configuration into one flat AAS env.
	 *
	 * @param document The XML document
	 * @param mappings The mapping configuration
	 * @return Flat AAS env
	 * @throws TransformationException If something goes wrong during transformation
	 */
	public AssetAdministrationShellEnvironment createShellEnv(Document document, MappingSpecification mappings)
			throws TransformationException {
		if (mappings.getAasEnvironmentMapping() != null) {

			LOGGER.info("Transforming AAS Environment...");

			AssetAdministrationShellEnvironment transformedEnvironment = new AASMappingTransformer()
					.transform(mappings, document);

			return transformedEnvironment;
		} else {
			throw new IllegalArgumentException("No AAS Environment specified!");
		}
	}

}
