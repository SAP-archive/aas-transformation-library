/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import com.sap.dsc.aas.lib.aml.transform.idgeneration.IdGeneratorGraphNode;

public class IdGenerationNameCyclicException extends TransformationException {

    private static final long serialVersionUID = 5331112972653896011L;

    public IdGenerationNameCyclicException(List<IdGeneratorGraphNode> graphNodeChain) {
        super("Cyclic idGenerationName references detected: " + graphNodeChain.stream()
            .map(graphNode -> graphNode.getIdGeneration().getIdGenerationName())
            .collect(Collectors.joining("->")));
    }

}
