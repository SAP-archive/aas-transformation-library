/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PreconditionTypeMatch extends AbstractPreconditionTypeForEach {

    private String pattern;

    public PreconditionTypeMatch() {}

    @JsonCreator
    public PreconditionTypeMatch(@JsonProperty(value = "from_xpath", required = true) String fromXPath,
        @JsonProperty(value = "pattern", required = true) String pattern) {
        this.setFromXPath(fromXPath);
        this.setPattern(pattern);
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        Pattern.compile(pattern);
        this.pattern = pattern;
    }

}
