/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.transform;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;
import com.sap.dsc.aas.lib.aml.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.aml.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;

public abstract class AbstractTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected IdGenerator idGenerator;
    protected PreconditionValidator preconditionValidator;
    protected XPathHelper xPathHelper;

    public AbstractTransformer(IdGenerator idGenerator, PreconditionValidator preconditionValidator) {
        this.idGenerator = idGenerator;
        this.preconditionValidator = preconditionValidator;
        this.xPathHelper = new XPathHelper();
    }

    protected Reference createReferenceFromConfig(Node node, ConfigReference configReference) {
        String id = idGenerator.generateId(node, configReference.getIdGeneration());
        return createReference(id, configReference.getKeyElement(), configReference.getKeyType());
    }

    protected Reference createReference(String value, KeyElements keyElement, KeyType keyType) {
        Key key = new DefaultKey.Builder()
            .idType(keyType)
            .type(keyElement)
            .value(value)
            .build();

        return new DefaultReference.Builder()
            .keys(Arrays.asList(key))
            .build();
    }

    /**
     * Generates an id from a node and an idGeneration
     *
     * @param node The xml node
     * @param idGeneration The id generation function
     * @return The generated id
     */
    protected Identifier createIdentifier(Node node, ConfigIdGeneration idGeneration) {

        return new DefaultIdentifier.Builder()
            .idType(IdentifierType.CUSTOM)
            .identifier(this.idGenerator.generateId(node, idGeneration))
            .build();
    }

}
