package com.sap.dsc.aas.lib.mapping.jackson;

import io.adminshell.aas.v3.dataformat.json.modeltype.JsonTreeProcessor;
import io.adminshell.aas.v3.model.Submodel;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

public class SubmodelDeserializer extends JsonDeserializer<Submodel> {

    private static final String MODEL_TYPE = "modelType";
    private static final String MODEL_TYPE_NAME = "name";

    private final JsonDeserializer<?> delegate;

    public SubmodelDeserializer(JsonDeserializer<?> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Submodel deserialize(JsonParser jp, DeserializationContext dc)
        throws IOException, JsonProcessingException {
        JsonNode json = jp.readValueAsTree();

        JsonTreeProcessor.traverse(json, x -> {
            if (x.get(MODEL_TYPE) != null) {
                x.replace(MODEL_TYPE, x.get(MODEL_TYPE).get(MODEL_TYPE_NAME));
            }
        });

        JsonParser treeParser = new TreeTraversingParser(json, jp.getCodec());
        treeParser.nextToken();
        return (Submodel) delegate.deserialize(treeParser, dc);
    }
}