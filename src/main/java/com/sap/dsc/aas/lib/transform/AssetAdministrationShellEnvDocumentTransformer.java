/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dsc.aas.lib.mapping.AASMappingTransformer;
import com.sap.dsc.aas.lib.mapping.model.Mapping;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.expressions.Expression;
import com.sap.dsc.aas.lib.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;

public class AssetAdministrationShellEnvDocumentTransformer extends AbstractTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AssetAdministrationShellEnvDocumentTransformer(IdGenerator idGenerator,
			PreconditionValidator preconditionValidator) {
		super(idGenerator, preconditionValidator);
	}

	/**
	 * Map document based on the mapping configuration into one flat AAS env.
	 *
	 * @param document       The XML document
	 * @param configMappings The mapping configuration
	 * @return Flat AAS env
	 * @throws TransformationException If something goes wrong during transformation
	 */
	public AssetAdministrationShellEnvironment createShellEnv(Document document, MappingSpecification mappings)
			throws TransformationException {
		AssetAdministrationShellEnvironment shellEnv = new DefaultAssetAdministrationShellEnvironment();

		if (mappings == null) {
			return shellEnv;
		}

		LOGGER.info("Transforming {} configMappings...", mappings.getMappings().size());

		List<AssetAdministrationShellEnvironment> transformedEnvs = new AASMappingTransformer().transform(mappings, document);
		for (AssetAdministrationShellEnvironment envForFlattening : transformedEnvs) {
			shellEnv.getAssetAdministrationShells()
					.addAll(envForFlattening.getAssetAdministrationShells());
			shellEnv.getAssets().addAll(envForFlattening.getAssets());
			shellEnv.getSubmodels().addAll(envForFlattening.getSubmodels());
			shellEnv.getConceptDescriptions().addAll(envForFlattening.getConceptDescriptions());
		}

		return shellEnv;
	}

}
