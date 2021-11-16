/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dom4j.Node;

import com.sap.dsc.aas.lib.mapping.model.Template;
import com.sap.dsc.aas.lib.transform.XPathHelper;
import com.sap.dsc.aas.lib.config.pojo.Precondition;
import com.sap.dsc.aas.lib.config.pojo.preconditions.AbstractPreconditionTypeForEach;
import com.sap.dsc.aas.lib.config.pojo.preconditions.PreconditionTypeMatch;
import com.sap.dsc.aas.lib.config.pojo.preconditions.PreconditionTypeRange;
import com.sap.dsc.aas.lib.exceptions.PreconditionValidationException;

public class PreconditionValidator {

    public static final String BUT_WAS = " but was ";
    private List<Precondition> preconditions;
    
    private XPathHelper xPathHelper = XPathHelper.getInstance();

    public List<Precondition> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<Precondition> preconditions) {
        this.preconditions = preconditions;
    }

    /**
     *
     * @param template A template to validate
     * @param matchingNodes Nodes matching this config element. For example, a configMapping matches a
     *        list of node in its from_xpath
     * @throws PreconditionValidationException If something goes wrong
     */
    public void validate(Template template, List<Node> matchingNodes) throws PreconditionValidationException {
        List<Precondition> matchingPreconditions = this.getPreconditions(/*template.getConfigElementId()*/null);
        for (Precondition precondition : matchingPreconditions) {
            this.validatePrecondition(precondition, matchingNodes);
        }
    }

    protected void validatePrecondition(Precondition precondition, List<Node> rootAssetNodes)
        throws PreconditionValidationException {

        this.validateForAll(precondition, rootAssetNodes);
        for (Node rootAssetNode : rootAssetNodes) {
            this.validateForEach(precondition, rootAssetNode);
        }
    }

    protected void validateForEach(Precondition precondition, Node rootNode)
        throws PreconditionValidationException {
        if (precondition.getForEach() == null) {
            return;
        }
        for (AbstractPreconditionTypeForEach forEach : precondition.getForEach()) {
            if (forEach instanceof PreconditionTypeRange) {
                List<Node> childNodes = xPathHelper.getNodes(rootNode, forEach.getFromXPath());
                this.validatePreconditionTypeRange(precondition, (PreconditionTypeRange) forEach, childNodes);
            } else if (forEach instanceof PreconditionTypeMatch) {
                List<String> stringValues = xPathHelper.getStringValues(rootNode, forEach.getFromXPath());
                this.validatePreconditionTypeMatch(precondition, (PreconditionTypeMatch) forEach, stringValues);
            }
        }
    }

    protected void validateForAll(Precondition precondition, List<Node> nodes)
        throws PreconditionValidationException {
        if (precondition.getForAll() == null) {
            return;
        }

        if (nodes.size() < precondition.getForAll().getMinimumNumber()) {
            throw new PreconditionValidationException(precondition, precondition.getForAll(),
                "Required minimum number of nodes in forAll is "
                    + precondition.getForAll().getMinimumNumber() + BUT_WAS + nodes.size());
        }
        if (nodes.size() > precondition.getForAll().getMaximumNumber() && precondition.getForAll().getMaximumNumber() > 0) {
            throw new PreconditionValidationException(precondition, precondition.getForAll(),
                "Maximum number of nodes returned in forAll is "
                    + precondition.getForAll().getMaximumNumber() + BUT_WAS + nodes.size());
        }
    }

    protected void validatePreconditionTypeRange(Precondition precondition, PreconditionTypeRange range, List<Node> childNodes)
        throws PreconditionValidationException {
        if (childNodes.size() < range.getMinimumNumber()) {
            throw new PreconditionValidationException(precondition, range,
                "Required minimum number of nodes returned by from_xpath='" + range.getFromXPath() + "' is "
                    + range.getMinimumNumber() + BUT_WAS + childNodes.size());
        }
        if (childNodes.size() > range.getMaximumNumber() && range.getMaximumNumber() > 0) {
            throw new PreconditionValidationException(precondition, range,
                "Maximum number of nodes returned by from_xpath='" + range.getFromXPath() + "' is "
                    + range.getMaximumNumber() + BUT_WAS + childNodes.size());
        }
    }

    protected void validatePreconditionTypeMatch(Precondition precondition, PreconditionTypeMatch forEach, List<String> stringValues)
        throws PreconditionValidationException {

        Pattern pattern = Pattern.compile(forEach.getPattern());
        for (String stringValue : stringValues) {
            Matcher matcher = pattern.matcher(stringValue);
            if (!matcher.matches()) {
                throw new PreconditionValidationException(precondition, forEach,
                    "Pattern " + forEach.getPattern() + " did not match the String " + stringValue);
            }
        }
    }

    protected List<Precondition> getPreconditions(String configElementId) {
        if (this.getPreconditions() == null || configElementId == null) {
            return new ArrayList<>();
        }
        return this.getPreconditions().stream()
            .filter(precondition -> configElementId.equals(precondition.getConfigElementId()))
            .collect(Collectors.toList());
    }

}
