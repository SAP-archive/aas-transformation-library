/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions;

public class AlreadyDefinedException extends InvalidConfigException {

    private static final long serialVersionUID = -1738880557850718362L;

    public AlreadyDefinedException(String fieldName) {
        super("Field '" + fieldName + "' already defined");
    }

    public AlreadyDefinedException(String fieldName, String value) {
        super("Field '" + fieldName + "' already defined: " + value);
    }

}
