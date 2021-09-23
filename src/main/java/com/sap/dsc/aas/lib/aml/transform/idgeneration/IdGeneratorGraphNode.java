/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform.idgeneration;

import org.dom4j.Node;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;

public class IdGeneratorGraphNode {

    private final Node xmlNode;
    private final ConfigIdGeneration idGeneration;

    public IdGeneratorGraphNode(Node xmlNode, ConfigIdGeneration idGeneration) {
        this.xmlNode = xmlNode;
        this.idGeneration = idGeneration;
    }

    public Node getXmlNode() {
        return xmlNode;
    }

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

}
