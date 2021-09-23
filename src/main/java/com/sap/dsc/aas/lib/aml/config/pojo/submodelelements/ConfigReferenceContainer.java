/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;

public interface ConfigReferenceContainer {

    default ConfigReference getReference() {
        return null;
    }

    default List<ConfigReference> getReferences() {
        if (getReference() == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(getReference());
    }
}
