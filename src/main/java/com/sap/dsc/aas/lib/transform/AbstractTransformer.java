/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.transform;

import java.util.Arrays;

import org.dom4j.Node;
import com.sap.dsc.aas.lib.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.config.pojo.ConfigReference;
import com.sap.dsc.aas.lib.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;

public abstract class AbstractTransformer {

    protected IdGenerator idGenerator;
    protected PreconditionValidator preconditionValidator;
    protected XPathHelper xPathHelper;

    public AbstractTransformer(IdGenerator idGenerator, PreconditionValidator preconditionValidator) {
        this.idGenerator = idGenerator;
        this.preconditionValidator = preconditionValidator;
        this.xPathHelper = XPathHelper.getInstance();
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
