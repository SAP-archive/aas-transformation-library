/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigTransformToAas;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigSubmodel;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;
import com.sap.dsc.aas.lib.aml.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.aml.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;

public class SubmodelTransformer extends AbstractTransformer {

    private SubmodelElementTransformer submodelElementTransformer;

    public SubmodelTransformer(IdGenerator idGenerator, PreconditionValidator preconditionValidator) {
        super(idGenerator, preconditionValidator);
        this.submodelElementTransformer = new SubmodelElementTransformer(this.idGenerator, preconditionValidator);
    }

    protected List<Submodel> createSubmodels(Node rootAssetNode, List<ConfigSubmodel> configSubmodels)
        throws TransformationException {
        List<Submodel> submodels = new ArrayList<>();

        for (ConfigSubmodel configSubmodel : configSubmodels) {
            List<Submodel> submodelsFromConfigSubmodel = createSubmodelsFromConfigSubmodel(rootAssetNode, configSubmodel);
            submodels.addAll(submodelsFromConfigSubmodel);
        }

        return submodels;
    }

    /**
     * One configSubmodel can result in several AAS submodels
     *
     * @param rootAssetNode The asset's root node
     * @param configSubmodel The config for the submodel
     * @return The list of generated submodels
     * @throws TransformationException If something goes wrong during transformation
     */
    protected List<Submodel> createSubmodelsFromConfigSubmodel(Node rootAssetNode, ConfigSubmodel configSubmodel)
        throws TransformationException {
        List<Node> submodelNodes = xPathHelper.getNodes(rootAssetNode, configSubmodel.getXPath());
        preconditionValidator.validate(configSubmodel, submodelNodes);

        List<Submodel> submodels = new ArrayList<>();
        for (Node submodelNode : submodelNodes) {
            submodels.add(createSubmodel(submodelNode, configSubmodel));
        }

        return submodels;
    }

    protected Submodel createSubmodel(Node submodelNode, ConfigSubmodel configSubmodel)
        throws TransformationException {
        String idShort = xPathHelper.getStringValue(submodelNode, configSubmodel.getIdShortXPath());

        ConfigReference configSemanticIdReference = configSubmodel.getSemanticIdReference();
        Reference semanticIdReference = null;
        if (configSemanticIdReference != null) {
            String semanticId = idGenerator.generateSemanticId(submodelNode, configSemanticIdReference.getIdGeneration());
            semanticIdReference =
                createReference(semanticId, configSemanticIdReference.getKeyElement(), configSemanticIdReference.getKeyType());
        }

        return new DefaultSubmodel.Builder()
            .idShort(idShort)
            .identification(createIdentifier(submodelNode, configSubmodel.getIdGeneration()))
            .submodelElements(submodelElementTransformer.createSubmodelElements(submodelNode, configSubmodel.getSubmodelElements()))
            .semanticId(semanticIdReference)
            .build();
    }
    
	@Override
	public AssetAdministrationShellEnvironment transform(InputStream amlStream, ConfigTransformToAas mapping)
			throws TransformationException {
		throw new UnsupportedOperationException();
	}

}
