/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PreconditionTypeRange.class, name = "Range"),
    @JsonSubTypes.Type(value = PreconditionTypeMatch.class, name = "Match")
})
public abstract class AbstractPreconditionTypeForEach extends AbstractPreconditionType {

    private String fromXPath;

    public String getFromXPath() {
        return fromXPath;
    }

    public void setFromXPath(String fromXPath) {
        this.fromXPath = fromXPath;
    }

}
