/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.config.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.exceptions.AlreadyDefinedException;

import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;

public class ConfigReference {
    private Map<String, Object> idGeneration;
    private KeyType keyType = KeyType.CUSTOM;
    private KeyElements keyElement;

    public Map<String, Object> getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(Map<String, Object> idGeneration) {
        this.idGeneration = idGeneration;
    }

    public void setValueId(@JsonProperty(required = true, value = "valueId") String valueId) {
        if (this.idGeneration != null) {
            throw new AlreadyDefinedException("idGeneration");
        }
        // do nothing
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public KeyElements getKeyElement() {
        return keyElement;
    }

    public void setKeyElement(KeyElements keyElement) {
        this.keyElement = keyElement;
    }
}
