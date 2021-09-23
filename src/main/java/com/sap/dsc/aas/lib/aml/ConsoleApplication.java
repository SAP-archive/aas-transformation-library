/* Copyright (C)2021 SAP SE or an affiliate company. All rights reserved. */
package com.sap.dsc.aas.lib.aml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.*;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.dsc.aas.lib.aml.amlx.AmlxPackage;
import com.sap.dsc.aas.lib.aml.amlx.AmlxPackagePart;
import com.sap.dsc.aas.lib.aml.amlx.AmlxPackageReader;
import com.sap.dsc.aas.lib.aml.config.ConfigLoader;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigAmlToAas;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigPlaceholder;
import com.sap.dsc.aas.lib.aml.exceptions.InvalidConfigException;
import com.sap.dsc.aas.lib.aml.exceptions.TransformationException;
import com.sap.dsc.aas.lib.aml.placeholder.PlaceholderHandling;
import com.sap.dsc.aas.lib.aml.transform.AmlTransformer;

import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.Serializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class ConsoleApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ConfigLoader configLoader;
    private final AmlTransformer amlTransformer;
    private final AmlxPackageReader amlxPackageReader;

    private static final String OPTION_NAME_CONFIG = "config";
    private static final String OPTION_NAME_AML_INPUT_FILE = "aml";
    private static final String OPTION_NAME_AMLX_INPUT_FILE = "amlx";
    private static final String OPTION_NAME_PRINT_PLACEHOLDERS = "print-placeholders";
    private static final String OPTION_NAME_PLACEHOLDER_VALUES = "placeholder-values";

    public ConsoleApplication() {
        this.configLoader = new ConfigLoader();
        this.amlTransformer = new AmlTransformer();
        this.amlxPackageReader = new AmlxPackageReader();
    }

    protected ConfigAmlToAas loadConfig(String filePath) throws IOException {
        return this.configLoader.loadConfig(filePath);
    }

    protected void transformAml(CommandLine commandLine, String configFilePath, String amlFilePath, String aasOutputFileName)
        throws IOException, TransformationException {

        try (InputStream amlStream = Files.newInputStream(Paths.get(amlFilePath))) {
            this.transformAml(commandLine, configFilePath, amlStream, aasOutputFileName);
        }
    }

    protected void transformAml(CommandLine commandLine, String configFilePath, InputStream amlStream, String aasOutputFileName)
        throws IOException, TransformationException {

        ConfigAmlToAas config = this.loadConfig(configFilePath);
        LOGGER.info("Loaded config version {}, aas version {}", config.getVersion(), config.getAasVersion());

        AssetAdministrationShellEnvironment aasEnv = amlTransformer.transformAml(amlStream, config);

        if (commandLine.hasOption(OPTION_NAME_PLACEHOLDER_VALUES)) {
            LOGGER.info("Replacing placeholders in AAS env");
            aasEnv = replacePlaceholders(aasEnv, commandLine.getOptionValue(OPTION_NAME_PLACEHOLDER_VALUES));
        }

        if (aasEnv != null) {
            try {
                writeAasToFile(aasOutputFileName, aasEnv);
            } catch (SerializationException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void transformAmlx(CommandLine commandLine, String configFileName, String amlxInputFileName, String aasOutputFileName)
        throws TransformationException, IOException {

        AmlxPackage amlxPackage = amlxPackageReader.readAmlxPackage(Paths.get(amlxInputFileName).toFile());
        try (InputStream amlInputStream = amlxPackage.getRootAmlFile().getInputStream()) {
            transformAml(commandLine, configFileName, amlInputStream, aasOutputFileName);
        }

        // Copy documents from amlx
        String amlxDir = com.google.common.io.Files.getNameWithoutExtension(amlxInputFileName);

        for (AmlxPackagePart packagePart : amlxPackage.getNonAmlFiles()) {
            Path pathToFile = Paths.get(amlxDir, packagePart.getPathInAmlx());
            LOGGER.info("Writing to: {}", pathToFile);

            Files.createDirectories(pathToFile.getParent());
            try (InputStream packagePartStream = packagePart.getInputStream();
                OutputStream partOutputStream = Files.newOutputStream(pathToFile)) {
                IOUtil.copyCompletely(packagePartStream, partOutputStream);
            }
        }
    }

    protected void writeAasToFile(String aasOutputFileName, AssetAdministrationShellEnvironment aasEnv)
        throws IOException, SerializationException {
        try (OutputStream fileOutputStream = Files.newOutputStream(Paths.get(aasOutputFileName))) {
            Serializer serializer = new JsonSerializer();
            fileOutputStream.write(serializer.write(aasEnv).getBytes());
            fileOutputStream.flush();
            LOGGER.info("Wrote AAS file to {}", aasOutputFileName);
        }
    }

    protected String getAasOutputFileName(String inputFileName) {
        return com.google.common.io.Files.getNameWithoutExtension(inputFileName) + ".json";
    }

    protected void printPlaceholders(String configFileName) throws IOException {
        ConfigAmlToAas config = this.loadConfig(configFileName);
        LOGGER.info("Loaded config version {}, aas version {}", config.getVersion(), config.getAasVersion());
        List<ConfigPlaceholder> placeholders = config.getPlaceholders();

        LOGGER.info("Found {} placeholders:", placeholders.size());
        placeholders.forEach(placeholder -> LOGGER.info("{}: {}", placeholder.getName(), placeholder.getDescription()));
    }

    @SuppressWarnings("unchecked")
    protected AssetAdministrationShellEnvironment replacePlaceholders(AssetAdministrationShellEnvironment aasEnv, String placeholderValues)
        throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> placeholderMap = mapper.readValue(placeholderValues, Map.class);

        try {
            return new PlaceholderHandling().replaceAllPlaceholders(aasEnv, placeholderMap);
        } catch (SerializationException | DeserializationException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public static void main(String[] args) {
        final Options options = new Options();

        // Config file option
        options.addOption(Option.builder("c").desc("Mapping config file").longOpt(OPTION_NAME_CONFIG).hasArg()
            .argName("CONFIG_FILE").required().build());

        // Placeholder option
        options
            .addOption(Option.builder("P").desc("Map of placeholder values in JSON format")
                .longOpt(OPTION_NAME_PLACEHOLDER_VALUES).hasArg()
                .argName("PLACEHOLDER_VALUES_JSON").build());

        // Command option: AML input file or print placeholders (of config file)
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(true);
        optionGroup.addOption(Option.builder("a").desc("AML input file").longOpt(OPTION_NAME_AML_INPUT_FILE).hasArg()
            .argName("AML_INPUT_FILE").build());
        optionGroup.addOption(Option.builder("amlx").desc("AMLX input file").longOpt(OPTION_NAME_AMLX_INPUT_FILE).hasArg()
            .argName("AMLX_INPUT_FILE").build());
        optionGroup.addOption(Option.builder("p").desc("Print placeholders with description").longOpt(OPTION_NAME_PRINT_PLACEHOLDERS)
            .build());
        options.addOptionGroup(optionGroup);

        final CommandLineParser parser = new DefaultParser();
        try {
            ConsoleApplication application = new ConsoleApplication();

            final CommandLine commandLine = parser.parse(options, args);
            String configFileName = commandLine.getOptionValue(OPTION_NAME_CONFIG);

            if (commandLine.hasOption(OPTION_NAME_PRINT_PLACEHOLDERS)) {
                application.printPlaceholders(configFileName);
            } else if (commandLine.hasOption(OPTION_NAME_AMLX_INPUT_FILE)) {
                String amlxInputFileName = commandLine.getOptionValue(OPTION_NAME_AMLX_INPUT_FILE);
                String aasOutputFileName = application.getAasOutputFileName(amlxInputFileName);

                application.transformAmlx(commandLine, configFileName, amlxInputFileName, aasOutputFileName);
            } else {
                String amlInputFileName = commandLine.getOptionValue(OPTION_NAME_AML_INPUT_FILE);
                String aasOutputFileName = application.getAasOutputFileName(amlInputFileName);

                application.transformAml(commandLine, configFileName, amlInputFileName, aasOutputFileName);
            }
        } catch (final ParseException ex) {
            final String header = "Transform AutomationML file into an AAS structured file\n\n";
            final String footer = "\n" + ex.getMessage();

            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("transform", header, options, footer, true);
        } catch (IOException | TransformationException | InvalidConfigException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

}
