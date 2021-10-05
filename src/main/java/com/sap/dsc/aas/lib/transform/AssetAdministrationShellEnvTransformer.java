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

import com.sap.dsc.aas.lib.config.pojo.ConfigAssetInformation;
import com.sap.dsc.aas.lib.config.pojo.ConfigAssetShell;
import com.sap.dsc.aas.lib.config.pojo.ConfigMapping;
import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;

public class AssetAdministrationShellEnvTransformer extends AbstractTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SubmodelTransformer submodelTransformer;
    private final AssetInformationTransformer assetInformationTransformer;

    public AssetAdministrationShellEnvTransformer(IdGenerator idGenerator, PreconditionValidator preconditionValidator) {
        super(idGenerator, preconditionValidator);
        this.submodelTransformer = new SubmodelTransformer(this.idGenerator, preconditionValidator);
        this.assetInformationTransformer = new AssetInformationTransformer(this.idGenerator, preconditionValidator);
    }

    /**
     * Map AML document based on the mapping configuration into one flat AAS env.
     *
     * @param document The AML document
     * @param configMappings The mapping configuration
     * @return Flat AAS env
     * @throws TransformationException If something goes wrong during transformation
     */
    public AssetAdministrationShellEnvironment createShellEnv(Document document, List<ConfigMapping> configMappings)
        throws TransformationException {
        AssetAdministrationShellEnvironment shellEnv = new DefaultAssetAdministrationShellEnvironment.Builder()
            .assetAdministrationShells(new ArrayList<>())
            .submodels(new ArrayList<>())
            .conceptDescriptions(new ArrayList<>())
            .build();

        if (configMappings == null) {
            return shellEnv;
        }

        LOGGER.info("Transforming {} configMappings...", configMappings.size());

        for (ConfigMapping configMapping : configMappings) {
            List<AssetAdministrationShellEnvironment> shellEnvs = createAssetAdministrationShellEnv(document, configMapping);

            for (AssetAdministrationShellEnvironment env : shellEnvs) {
                shellEnv.getSubmodels().addAll(env.getSubmodels());
                shellEnv.getAssetAdministrationShells().addAll(env.getAssetAdministrationShells());
                shellEnv.getConceptDescriptions().addAll(env.getConceptDescriptions());
            }
        }

        return shellEnv;
    }

    /**
     * The XPath expression in config asset may evaluate multiple XML nodes, this results in multiple
     * assets.
     *
     * @param node XML Root node
     * @param configMapping Config mapping
     * @return List of converted assets
     * @throws TransformationException If something goes wrong during transformation
     */
    protected List<AssetAdministrationShellEnvironment> createAssetAdministrationShellEnv(Node node, ConfigMapping configMapping)
        throws TransformationException {
        List<AssetAdministrationShellEnvironment> shellEnvs = new ArrayList<>();

        List<Node> rootAssetNodes = xPathHelper.getNodes(node, configMapping.getXPath());
        preconditionValidator.validate(configMapping, rootAssetNodes);
        for (Node rootAssetNode : rootAssetNodes) {
            shellEnvs.add(createSingleAssetAdministrationShellEnv(rootAssetNode, configMapping));
        }

        return shellEnvs;
    }

    /**
     * @param rootAssetNode XML node of the asset
     * @param configMapping configMapping
     * @return Single converted asset
     * @throws TransformationException If something goes wrong during transformation
     */
    protected AssetAdministrationShellEnvironment createSingleAssetAdministrationShellEnv(Node rootAssetNode, ConfigMapping configMapping)
        throws TransformationException {

        List<Submodel> submodels = new ArrayList<>();
        if (configMapping.getSubmodels() != null) {
            submodels = submodelTransformer.createSubmodels(rootAssetNode, configMapping.getSubmodels());
        }

        List<AssetAdministrationShell> assetShells = new ArrayList<>();
        List<Reference> submodelReferences = submodels.stream()
            .map(submodel -> createReference(submodel.getIdentification().getIdentifier(), KeyElements.SUBMODEL, KeyType.CUSTOM))
            .collect(Collectors.toCollection(ArrayList::new));
        if (configMapping.getConfigAssetShell() != null) {
            assetShells.add(createAssetAdministrationShell(rootAssetNode, configMapping.getConfigAssetShell(),
                configMapping.getConfigAssetInformation(), submodelReferences));
        }

        List<ConceptDescription> descriptions = new ArrayList<>();

        return new DefaultAssetAdministrationShellEnvironment.Builder()
            .assetAdministrationShells(assetShells)
            .submodels(submodels)
            .conceptDescriptions(descriptions)
            .build();
    }

    protected AssetAdministrationShell createAssetAdministrationShell(Node assetNode, ConfigAssetShell configAssetShell,
        ConfigAssetInformation configAssetInformation,
        List<Reference> submodels) throws TransformationException {
        String idShort = xPathHelper.getStringValue(assetNode, configAssetShell.getIdShortXPath());

        return new DefaultAssetAdministrationShell.Builder()
            .idShort(idShort)
            .identification(createIdentifier(assetNode, configAssetShell.getIdGeneration()))
            .assetInformation(assetInformationTransformer.createAssetInformation(assetNode, configAssetInformation))
            .submodels(submodels)
            .build();
    }
    
}
