/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions;

public class MissingConfigException extends InvalidConfigException {

    private static final long serialVersionUID = 8024812952881072036L;

    public MissingConfigException(String message) {
        super(message);
    }

}
