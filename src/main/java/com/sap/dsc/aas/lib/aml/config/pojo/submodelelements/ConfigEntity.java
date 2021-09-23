/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo.submodelelements;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfigSubmodelElement;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;
import io.adminshell.aas.v3.model.EntityType;

public class ConfigEntity extends AbstractConfigSubmodelElement implements ConfigReferenceContainer, ConfigSubmodelElementContainer {

    private EntityType entityType;
    private ConfigReference assetReference;
    private List<AbstractConfigSubmodelElement> statements;

    public ConfigEntity() {}

    @JsonCreator
    public ConfigEntity(@JsonProperty(required = true, value = "entityType") EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public ConfigReference getAssetReference() {
        return assetReference;
    }

    public void setAssetReference(ConfigReference assetReference) {
        this.assetReference = assetReference;
    }

    public List<AbstractConfigSubmodelElement> getStatements() {
        return statements;
    }

    public void setStatements(List<AbstractConfigSubmodelElement> statements) {
        this.statements = statements;
    }

    @Override
    public ConfigReference getReference() {
        return getAssetReference();
    }

    @Override
    public List<AbstractConfigSubmodelElement> getSubmodelElements() {
        return getStatements();
    }
}
