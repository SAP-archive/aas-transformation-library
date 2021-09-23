/* Copyright (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. */
package com.sap.dsc.aas.lib.aml.config.pojo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigAnnotatedRelationshipElement;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigBasicEvent;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigBlob;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigCapability;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigEntity;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigFile;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigMultiLanguageProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigOperation;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigRange;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigReferenceElement;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigRelationshipElement;
import com.sap.dsc.aas.lib.aml.config.pojo.submodelelements.ConfigSubmodelElementCollection;
import io.adminshell.aas.v3.model.ModelingKind;

@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConfigProperty.class, name = "Property"),
    @JsonSubTypes.Type(value = ConfigRange.class, name = "Range"),
    @JsonSubTypes.Type(value = ConfigEntity.class, name = "Entity"),
    @JsonSubTypes.Type(value = ConfigReferenceElement.class, name = "ReferenceElement"),
    @JsonSubTypes.Type(value = ConfigMultiLanguageProperty.class, name = "MultiLanguageProperty"),
    @JsonSubTypes.Type(value = ConfigCapability.class, name = "Capability"),
    @JsonSubTypes.Type(value = ConfigOperation.class, name = "Operation"),
    @JsonSubTypes.Type(value = ConfigAnnotatedRelationshipElement.class, name = "AnnotatedRelationshipElement"),
    @JsonSubTypes.Type(value = ConfigRelationshipElement.class, name = "RelationshipElement"),
    @JsonSubTypes.Type(value = ConfigFile.class, name = "File"),
    @JsonSubTypes.Type(value = ConfigBlob.class, name = "Blob"),
    @JsonSubTypes.Type(value = ConfigBasicEvent.class, name = "BasicEvent"),
    @JsonSubTypes.Type(value = ConfigSubmodelElementCollection.class, name = "SubmodelElementCollection")
})
public abstract class AbstractConfigSubmodelElement extends AbstractConfigFromAttribute {

    private String category = "PARAMETER";
    private ModelingKind kind = ModelingKind.INSTANCE;

    public String getType() {
        return this.getClass().getSimpleName().substring(6);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ModelingKind getKind() {
        return kind;
    }

    public void setKind(ModelingKind kind) {
        this.kind = kind;
    }

}
