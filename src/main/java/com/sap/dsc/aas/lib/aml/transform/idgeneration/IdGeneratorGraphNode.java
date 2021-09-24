/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
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
