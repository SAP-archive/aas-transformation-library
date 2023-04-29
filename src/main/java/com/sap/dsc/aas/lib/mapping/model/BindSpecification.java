/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.mapping.model;

import java.util.HashMap;
import java.util.Map;

import com.sap.dsc.aas.lib.expressions.Expression;

public class BindSpecification {

	private Map<String, Expression> bindings = new HashMap<>();

    public Map<String, Expression> getBindings() {
        return bindings;
    }

    public void setBinding(String key, Expression value) {
        bindings.put(key, value);
    }
}
