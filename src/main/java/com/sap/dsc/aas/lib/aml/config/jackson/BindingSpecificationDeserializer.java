package com.sap.dsc.aas.lib.aml.config.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sap.dsc.aas.lib.mapping.model.BindSpecification;

public class BindingSpecificationDeserializer extends JsonDeserializer<BindSpecification> {

    public BindingSpecificationDeserializer() {
    }

    @Override
    public BindSpecification deserialize(JsonParser jp, DeserializationContext dc)
        throws IOException, JsonProcessingException {
        BindSpecification spec = new BindSpecification();

        JsonNode node = jp.getCodec().readTree(jp);
        node.fields().forEachRemaining(entry -> {
            // TODO add here logic to parse XPaths, BrowsePaths etc.
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            spec.setBinding(name, value.textValue());
        });
        return spec;
    }
}