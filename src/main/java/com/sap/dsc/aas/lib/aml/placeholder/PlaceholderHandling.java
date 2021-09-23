/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml.placeholder;

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.dsc.aas.lib.aml.placeholder.exceptions.PlaceholderValueMissingException;

import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.Deserializer;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.Serializer;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class PlaceholderHandling {

    /**
     * Replace all placeholders in an AssetAdministrationShellEnv.
     *
     * If no placeholder values are provided (null or empty), the same AssetAdministrationShellEnv will
     * be returned. If one placeholder value is provided, it is required, that all values will be
     * provided.
     *
     * @param assetAdministrationShellEnvironment The shell env to replace the placeholders
     * @param placeholders The placeholder values to replace
     * @return The shell env with replaced placeholders
     */
    public AssetAdministrationShellEnvironment replaceAllPlaceholders(
        AssetAdministrationShellEnvironment assetAdministrationShellEnvironment, Map<String, String> placeholders)
        throws SerializationException, DeserializationException {
        Serializer serializer = new JsonSerializer();
        Deserializer deserializer = new JsonDeserializer();

        if (placeholders == null || placeholders.isEmpty()) {
            return assetAdministrationShellEnvironment;
        }

        String aasJson = serializer.write(assetAdministrationShellEnvironment);
        aasJson = replaceAllPlaceholders(aasJson, placeholders);
        return deserializer.read(aasJson);
    }

    /**
     * Replaces all placeholders in a string. Placeholder format: ${{placeholderName}}
     *
     * Example: "The id is ${{placeholderName}}" will generate "The id is placerholderValue"
     *
     * @param assetShellAsString Asset Shell Env as String
     * @param placeholders The placeholder key value pairs
     * @return The string with replaced values
     * @throws PlaceholderValueMissingException Thrown when placeholder values are not provided
     */
    protected String replaceAllPlaceholders(String assetShellAsString, Map<String, String> placeholders) {
        Matcher matcher = Pattern.compile("\\$\\{\\{([a-zA-Z0-9\\_\\-]+)\\}\\}").matcher(assetShellAsString);

        return matcher.replaceAll(match -> matchPlaceholders(match, placeholders));
    }

    private String matchPlaceholders(MatchResult matchResult, Map<String, String> placeholders) {
        String placeholder = matchResult.group(1);
        String value;
        if ((value = placeholders.get(placeholder)) != null) {
            return value;
        } else {
            throw new PlaceholderValueMissingException(placeholder);
        }
    }
}
