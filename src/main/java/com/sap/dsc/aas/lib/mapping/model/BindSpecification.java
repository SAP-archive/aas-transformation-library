/*
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved.

  SPDX-License-Identifier: Apache-2.0
 */
package com.sap.dsc.aas.lib.mapping.model;

import java.util.HashMap;
import java.util.Map;

public class BindSpecification {

    protected Map<String, Object> bindings = new HashMap<>();

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBinding(String key, Object value) {
        bindings.put(key, value);
    }
}
