/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.transform;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Node;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigAssetInformation;
import com.sap.dsc.aas.lib.aml.exceptions.NoResultByXPathException;
import com.sap.dsc.aas.lib.aml.transform.idgeneration.IdGenerator;
import com.sap.dsc.aas.lib.aml.transform.validation.PreconditionValidator;

import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultAssetInformation;

public class AssetInformationTransformer extends AbstractTransformer {

    public AssetInformationTransformer(IdGenerator idGenerator, PreconditionValidator preconditionValidator) {
        super(idGenerator, preconditionValidator);
    }

    protected AssetInformation createAssetInformation(Node rootAssetNode, ConfigAssetInformation configMapping) {
        return new DefaultAssetInformation.Builder()
            .assetKind(AssetKind.valueOf(configMapping.getKindTypeXPath()))
            .globalAssetId(createReferenceFromConfig(rootAssetNode, configMapping.getGlobalAssetIdReference()))
            .build();
    }

    protected List<AssetKind> getAssetKindType(Node assetNode, String xPath) throws NoResultByXPathException {
        Object result = xPathHelper.createXPath(assetNode, xPath).evaluate(assetNode);
        if (result instanceof String) {
            return Arrays.asList(AssetKind.valueOf((String) result));
        }

        throw new NoResultByXPathException(xPath);
    }

}
