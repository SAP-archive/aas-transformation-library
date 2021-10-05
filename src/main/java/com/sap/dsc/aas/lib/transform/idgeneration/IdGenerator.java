/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform.idgeneration;

import java.util.List;

import org.dom4j.Node;

import com.sap.dsc.aas.lib.transform.XPathHelper;
import com.sap.dsc.aas.lib.config.pojo.*;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigReferenceContainer;
import com.sap.dsc.aas.lib.config.pojo.submodelelements.ConfigSubmodelElementContainer;
import com.sap.dsc.aas.lib.exceptions.TransformationException;

public class IdGenerator {//FIXME for which Ids? IdShort? Identifier?

    private IdGeneratorGraph graph;

    private XPathHelper xPathHelper;

    public IdGenerator() {
        this.xPathHelper = XPathHelper.getInstance();
    }

    public IdGeneratorGraph getGraph() {
        return this.graph;
    }

    public String generateId(Node node, ConfigIdGeneration idGeneration) {
        if (idGeneration == null || idGeneration.getParameters() == null) {
            return node.getName();
        }

        IdGeneratorGraphNode graphNode = this.graph.getGraphNode(node, idGeneration);
        return this.graph.resolveGraphNode(graphNode);
    }

    public String generateSemanticId(Node node, ConfigIdGeneration idGeneration) {
        if (idGeneration == null || idGeneration.getParameters() == null) {
            return null;
        }
        return generateId(node, idGeneration);
    }

    public void resetGraph() {
        graph = new IdGeneratorGraph();
    }

    /**
     * Prepare all graph nodes: This should happen before the actual transformation AML to AAS
     *
     * @param xmlRootNode The xml root node
     * @param configMappings The assets in the mapping file
     * @throws TransformationException If something in the idGeneration is invalid (missing names,
     *         duplicate names) or goes wrong
     */
    public void prepareGraph(Node xmlRootNode, List<ConfigMapping> configMappings) throws TransformationException {
        resetGraph();

        for (ConfigMapping configMapping : configMappings) {
            List<Node> rootMappingNodes = xPathHelper.getNodes(xmlRootNode, configMapping.getXPath());
            for (Node rootMappingNode : rootMappingNodes) {
                this.graph.addGraphNode(rootMappingNode, configMapping.getIdGeneration());//XMLElement to IDGen-Mapping? But why adding it multiple times?
                this.graph.addGraphNode(rootMappingNode, configMapping.getConfigAssetShell().getIdGeneration());
                this.graph.addGraphNode(rootMappingNode, configMapping.getConfigAssetInformation().getIdGeneration());
                this.graph.addGraphNode(rootMappingNode,
                    configMapping.getConfigAssetInformation().getGlobalAssetIdReference().getIdGeneration());

                addSubmodelsToGraph(configMapping, rootMappingNode);
            }
        }
        this.graph.validateGraph();
    }

    private void addSubmodelsToGraph(ConfigMapping configMapping, Node rootAssetNode) throws TransformationException {
        for (ConfigSubmodel configSubmodel : configMapping.getSubmodels()) {
            List<Node> rootSubmodelNodes = xPathHelper.getNodes(rootAssetNode, configSubmodel.getXPath());
            for (Node rootSubmodelNode : rootSubmodelNodes) {
                this.graph.addGraphNode(rootSubmodelNode, configSubmodel.getIdGeneration());
                this.graph.addGraphNode(rootSubmodelNode, configSubmodel.getSemanticIdReference().getIdGeneration());

                addSubmodelElementsToGraph(configSubmodel.getSubmodelElements(), rootSubmodelNode);
            }
        }
    }

    private void addSubmodelElementsToGraph(List<AbstractConfigSubmodelElement> submodelElements, Node rootSubmodelNode)
        throws TransformationException {
        if (submodelElements == null) {
            return;
        }

        for (AbstractConfigSubmodelElement configSubmodelElement : submodelElements) {
            List<Node> rootSubmodelElementNodes = xPathHelper.getNodes(rootSubmodelNode, configSubmodelElement.getXPath());
            for (Node rootSubmodelElementNode : rootSubmodelElementNodes) {
                if (configSubmodelElement.getSemanticIdReference() != null) {
                    this.graph.addGraphNode(rootSubmodelElementNode, configSubmodelElement.getSemanticIdReference().getIdGeneration());
                }

                if (configSubmodelElement instanceof ConfigSubmodelElementContainer) {
                    addSubmodelElementsToGraph(((ConfigSubmodelElementContainer) configSubmodelElement).getSubmodelElements(),
                        rootSubmodelElementNode);
                }

                if (configSubmodelElement instanceof ConfigReferenceContainer) {
                    addSubmodelElementReferencesToGraph((ConfigReferenceContainer) configSubmodelElement, rootSubmodelElementNode);
                }
            }
        }
    }

    private void addSubmodelElementReferencesToGraph(ConfigReferenceContainer configReferenceContainer, Node rootSubmodelElementNode)//FIXME SME implies IdShort?
        throws TransformationException {
        if (configReferenceContainer.getReferences() != null) {
            for (ConfigReference reference : configReferenceContainer.getReferences()) {
                this.graph.addGraphNode(rootSubmodelElementNode,
                    reference.getIdGeneration());
            }
        }
    }
}
