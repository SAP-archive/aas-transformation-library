/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform.idgeneration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dom4j.Node;

import com.google.common.hash.Hashing;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGenerationFinalizeFunction;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGenerationParameter;
import com.sap.dsc.aas.lib.aml.exceptions.IdGenerationNameAlreadyDefinedException;
import com.sap.dsc.aas.lib.aml.exceptions.IdGenerationNameCyclicException;
import com.sap.dsc.aas.lib.aml.exceptions.IdGenerationNameDoesNotExistException;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;
import com.sap.dsc.aas.lib.aml.transform.XPathHelper;

public class IdGeneratorGraph {

    private Map<String, IdGeneratorGraphNode> nodesByName;
    private List<IdGeneratorGraphNode> nodesWithoutName;
    private Map<IdGeneratorGraphNode, String> idCache;
    private XPathHelper xPathHelper;

    public IdGeneratorGraph() {
        this.nodesByName = new HashMap<>();
        this.nodesWithoutName = new ArrayList<>();
        this.idCache = new HashMap<>();
        this.xPathHelper = new XPathHelper();
    }

    public void addGraphNode(Node xmlNode, ConfigIdGeneration idGeneration)
        throws IdGenerationNameAlreadyDefinedException {

        if (idGeneration == null) {
            return;
        }
        if (this.hasGraphNode(idGeneration.getIdGenerationName())) {
            throw new IdGenerationNameAlreadyDefinedException(idGeneration.getIdGenerationName());
        }
        IdGeneratorGraphNode graphNode = new IdGeneratorGraphNode(xmlNode, idGeneration);
        if (idGeneration.getIdGenerationName() != null) {
            this.nodesByName.put(idGeneration.getIdGenerationName(), graphNode);
        } else {
            this.nodesWithoutName.add(graphNode);
        }
    }

    /**
     * Returns whether the graph already includes a given idGenerationName.
     *
     * @param idGenerationName The name of the id generation function
     * @return True if the graph includes the idGenerationName
     */
    protected boolean hasGraphNode(String idGenerationName) {
        return nodesByName.containsKey(idGenerationName);
    }

    protected IdGeneratorGraphNode getGraphNode(String idGenerationName) {
        return nodesByName.get(idGenerationName);
    }

    protected IdGeneratorGraphNode getGraphNode(Node xmlNode, ConfigIdGeneration idGeneration) {

        return getAllNodesStream()
            .filter(graphNode -> graphNode.getIdGeneration() == idGeneration && graphNode.getXmlNode() == xmlNode)
            .findFirst().orElse(null);
    }

    public List<IdGeneratorGraphNode> getUnnamedGraphNodes() {
        return this.nodesWithoutName;
    }

    public Stream<IdGeneratorGraphNode> getAllNodesStream() {
        return Stream.concat(this.nodesByName.values().stream(), this.nodesWithoutName.stream());
    }

    /**
     * Check each parameter in each graph node: Do all references to idGenerationNames exist?
     *
     * @return Null if all idGenerationNames exist
     */
    protected String getMissingIdGenerationName() {
        return this.getAllNodesStream()
            .filter(graphNode -> graphNode.getIdGeneration().getParameters() != null)
            .flatMap(graphNode -> graphNode.getIdGeneration().getParameters().stream())
            .filter(parameter -> parameter.getIdGenerationName() != null)
            .filter(parameter -> !this.hasGraphNode(parameter.getIdGenerationName()))
            .map(ConfigIdGenerationParameter::getIdGenerationName)
            .findFirst().orElse(null);
    }

    protected void checkCyclicReferences(IdGeneratorGraphNode rootNode) throws IdGenerationNameCyclicException {
        checkCyclicReferences(rootNode, new ArrayList<>());
    }

    /**
     *
     * @param graphNode The starting graph node
     */
    private void checkCyclicReferences(IdGeneratorGraphNode graphNode,
        List<IdGeneratorGraphNode> graphNodeChainSoFar) throws IdGenerationNameCyclicException {

        if (graphNodeChainSoFar.contains(graphNode)) {
            // Add the name again at the end to make cycle more obvious
            graphNodeChainSoFar.add(graphNode);
            throw new IdGenerationNameCyclicException(graphNodeChainSoFar);
        }

        List<IdGeneratorGraphNode> graphNodeChain = new ArrayList<>(graphNodeChainSoFar);
        graphNodeChain.add(graphNode);

        List<IdGeneratorGraphNode> childGraphNodes = getReferencesForGraphNode(graphNode);
        for (IdGeneratorGraphNode childGraphNode : childGraphNodes) {
            checkCyclicReferences(childGraphNode, graphNodeChain);
        }
    }

    protected List<IdGeneratorGraphNode> getReferencesForGraphNode(IdGeneratorGraphNode graphNode) {
        List<String> idGenerationNames = this.getReferences(graphNode.getIdGeneration());
        return this.nodesByName.entrySet().stream()
            .filter(entry -> idGenerationNames.contains(entry.getKey()))
            .map(Entry::getValue)
            .collect(Collectors.toList());
    }

    /**
     * Given an idGeneration, what are the referred idGenerationNames?
     *
     * @param idGeneration The idGeneration function
     * @return Empty list if no other nodes are referred to
     */
    protected List<String> getReferences(ConfigIdGeneration idGeneration) {
        if (idGeneration.getParameters() == null) {
            return new ArrayList<>();
        }
        return idGeneration.getParameters().stream()
            .filter(parameter -> parameter.getIdGenerationName() != null)
            .map(ConfigIdGenerationParameter::getIdGenerationName)
            .collect(Collectors.toList());
    }

    /**
     * Validates the graph by checking whether all idGenerationNames exist and whether there are any
     * cyclic dependencies.
     *
     * @throws TransformationException If the graph is invalid (missing name, cyclic references)
     */
    protected void validateGraph() throws TransformationException {
        String missingIdGenerationName = this.getMissingIdGenerationName();
        if (missingIdGenerationName != null) {
            throw new IdGenerationNameDoesNotExistException(missingIdGenerationName);
        }
        for (IdGeneratorGraphNode node : this.getAllNodesStream().collect(Collectors.toList())) {
            checkCyclicReferences(node);
        }
    }

    /**
     * To generate an ID, an array of parameters is passed.
     *
     * This function resolves a single parameter (the outgoing edge in the graph). Note that this might
     * first require resolving other nodes recursively.
     *
     * @param xmlNode The node in the XML document
     * @param parameter The outgoing edge in the graph
     * @return The resolved Id
     */
    protected String resolveIdGenerationParameter(Node xmlNode, ConfigIdGenerationParameter parameter) {
        String value = null;
        if (parameter.getIdGenerationName() != null) {
            value = this.resolveGraphNode(this.getGraphNode(parameter.getIdGenerationName()), parameter.getFinalizeFunction());
        } else if (parameter.getXPath() != null) {
            value = xPathHelper.getStringValueOrNull(xmlNode, parameter.getXPath());
        }

        if (value == null) {
            value = parameter.getValueDefault();
        }
        return value;
    }

    protected String resolveGraphNode(IdGeneratorGraphNode graphNode) {
        if (graphNode != null)
            return this.resolveGraphNode(graphNode, graphNode.getIdGeneration().getFinalizeFunction());
        else {
            return null;
        }
    }

    private String resolveGraphNode(IdGeneratorGraphNode graphNode, ConfigIdGenerationFinalizeFunction finalizeFunction) {
        if (this.idCache.containsKey(graphNode)) {
            return this.finalizeId(this.idCache.get(graphNode), finalizeFunction);
        }

        String rawId = graphNode.getIdGeneration().getParameters().stream()
            .map(parameter -> resolveIdGenerationParameter(graphNode.getXmlNode(), parameter))
            .collect(Collectors.joining());
        this.idCache.put(graphNode, rawId);
        return this.finalizeId(rawId, finalizeFunction);
    }

    protected String finalizeId(String rawId, ConfigIdGenerationFinalizeFunction finalizeFunction) {
        switch (finalizeFunction) {
            case CONCATENATE_AND_HASH:
                return Hashing.sha256().hashString(rawId, StandardCharsets.UTF_8).toString();
            case CONCATENATE_AND_URL_ENCODE:
                return URLEncoder.encode(rawId, StandardCharsets.UTF_8).replace("+", "%20");
            case CONCATENATE:
            default:
                return rawId;
        }
    }

}
