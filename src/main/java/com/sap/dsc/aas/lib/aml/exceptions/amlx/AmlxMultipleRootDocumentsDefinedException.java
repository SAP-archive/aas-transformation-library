/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.exceptions.amlx;

public class AmlxMultipleRootDocumentsDefinedException extends AmlxValidationException {

    private static final long serialVersionUID = 8010818651610754595L;

    public AmlxMultipleRootDocumentsDefinedException() {
        super("The .rels file in the container has multiple <Relationship> of type RootDocument");
    }

}
