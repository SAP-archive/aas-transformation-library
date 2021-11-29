# aas-transformation-library

[![Build Status](https://app.travis-ci.com/SAP/aas-transformation-library.svg?branch=main)](https://app.travis-ci.com/SAP/aas-transformation-library)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP/aas-transformation-library)](https://api.reuse.software/info/github.com/SAP/aas-transformation-library)

Transform XML-Formats
into [Asset Administration Shell (AAS)](https://www.plattform-i40.de/PI40/Redaktion/DE/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part_2_V1.html)
format and leverage special features for OPC UA and AutomationML.

- [aas-transformation-library](#aas-transformation-library)
  - [Documentation](#documentation)
  - [Requirements](#requirements)
  - [Local Usage](#local-usage)
  - [Validation](#validation)
    - [AML files](#aml-files)
    - [AMLX files](#amlx-files)
    - [OPC UA Nodeset files](#opc-ua-nodeset-files)
  - [Configuration](#configuration)
    - [Expressions](#expressions)
    - [Looping](#looping-with-foreach)
    - [Dynamic Evaluation](#dynamic-evaluation-with-bind)
    - [Defining Functions](#compound-expressions-with-definitions)
  - [Structure](#config-json-structure)
    - [Asset Administration Shell Environment](#asset-administration-shell-environment)
    - [Asset Administration Shells](#assetadministrationshells)
    - [Assets](#assets)
    - [Submodels](#submodels)
  - [Known Issues & Limitations](#known-issues--limitations)
  - [Upcoming Changes](#upcoming-changes)
  - [Contributing](#contributing)
  - [Code of Conduct](#code-of-conduct)
  - [To Do](#to-do)
  - [License](#license)

## Documentation

The library can be used twofold. Users can..

1. import the library in Java applications to transform models using the most relevant XML-based industrial standards
   into AAS objects.
2. use the library to build a [fat JAR](https://github.com/johnrengelman/shadow) to transform XML files into AAS JSON
   files locally.

In order to provide this functionality the library makes heavy use
of [XPath](https://www.w3schools.com/xml/xpath_intro.asp) to parse the source data modeling language. Currently, we
support XPath version 1.0 (which is supported by dom4j).

Users have to provide two inputs:

1. One XML file containing the source structure
2. One configuration ("config") file in JSON format, which details out which part of a given input corresponds to AAS
   objects (Examplary configuration files can be found under [_src/test/resources/config_](src/test/resources/config))

Depending on its usage the library will output one of the two:

* One AAS JSON file containing a serialized _AssetAdministrationShellEnvironment_ object
* One _AssetAdministrationShellEnvironment_ object (
  leveraging [admin-shell-io/java-model](https://github.com/admin-shell-io/java-model))

## Requirements

We rely on [SapMachine 11](https://sap.github.io/SapMachine/) and use [Gradle](https://gradle.org/).

## Local Usage

```sh
$ ./gradlew build

$ java -jar build/distributions/aas-transformation-library-shadow-0.0.1-SNAPSHOT.jar
usage: transform -a <AML_INPUT_FILE> | -amlx <AMLX_INPUT_FILE> | -p  -c
       <CONFIG_FILE> [-P <PLACEHOLDER_VALUES_JSON>]
Transform AutomationML file into an AAS structured file

 -a,--aml <AML_INPUT_FILE>                           AML input file
 -amlx,--amlx <AMLX_INPUT_FILE>                      AMLX input file
 -c,--config <CONFIG_FILE>                           Mapping config file
 -P,--placeholder-values <PLACEHOLDER_VALUES_JSON>   Map of placeholder
                                                     values in JSON format
 -p,--print-placeholders                             Print placeholders
                                                     with description

Missing required options: c, [-a AML input file, -amlx AMLX input file, -p
Print placeholders with description]

$ java -jar ./build/distributions/aas-transformation-library-shadow-0.0.1-SNAPSHOT.jar -c src/test/resources/config/simpleConfig.json -a src/test/resources/aml/full_AutomationComponent.aml
[main] INFO com.sap.dsc.aas.lib.aml.ConsoleApplication - Loaded config version 1.0.0, aas version 2.0.1
[main] INFO com.sap.dsc.aas.lib.aml.transform.AmlTransformer - Loaded config version 1.0.0, AAS version 2.0.1
[main] INFO com.sap.dsc.aas.lib.aml.transform.AssetAdministrationShellEnvTransformer - Transforming 1 config assets...
[main] INFO com.sap.dsc.aas.lib.aml.ConsoleApplication - Wrote AAS file to full_AutomationComponent.json

$  cd src/test/resources/amlx/minimal_AutomationMLComponent_WithDocuments

$ zip -r minimal_AutomationMLComponent_WithDocuments.amlx . -x "*.DS_Store"
adding: [Content_Types].xml (deflated 52%)
adding: _rels/ (stored 0%)
adding: _rels/.rels (deflated 68%)
adding: lib/ (stored 0%)
adding: lib/AutomationComponentLibrary_v1_0_0_Full_CAEX3_BETA.aml (deflated 85%)
adding: files/ (stored 0%)
adding: files/TestPDFDeviceManual.pdf (deflated 14%)
adding: files/TestTXTDeviceManual.txt (stored 0%)
adding: files/TestTXTWarranty.txt (stored 0%)
adding: CAEX_ClassModel_V.3.0.xsd (deflated 90%)
adding: minimal_AutomationMLComponent_WithDocuments.aml (deflated 80%)

$ cd ../../../../../

$ java -jar ./build/distributions/aas-transformation-library-shadow-0.0.1-SNAPSHOT.jar -c src/test/resources/config/simpleConfig.json -amlx src/test/resources/amlx/minimal_AutomationMLComponent_WithDocuments/minimal_AutomationMLComponent_WithDocuments.amlx
[main] INFO com.sap.dsc.aas.lib.aml.ConsoleApplication - Loaded config version 1.0.0, aas version 2.0.1
[main] INFO com.sap.dsc.aas.lib.aml.transform.AmlTransformer - Loaded config version 1.0.0, AAS version 2.0.1
[main] INFO com.sap.dsc.aas.lib.aml.transform.AssetAdministrationShellEnvTransformer - Transforming 1 config assets...
[main] INFO com.sap.dsc.aas.lib.aml.ConsoleApplication - Wrote AAS file to minimal_AutomationMLComponent_WithDocuments.json
Writing to: minimal_AutomationMLComponent_WithDocuments/files/TestTXTDeviceManual.txt
Writing to: minimal_AutomationMLComponent_WithDocuments/files/TestPDFDeviceManual.pdf
Writing to: minimal_AutomationMLComponent_WithDocuments/files/TestTXTWarranty.txt
```

## Validation

### AML files

AML file validation includes the following steps:

- Check that the AML file is a valid XML file
- Check that the AML file is valid according to
  the [CAEX 3.0 class model](https://github.com/br-iosb/aas-transformation-library/blob/main/src/main/resources/aml/CAEX_ClassModel_V.3.0.xsd)

### AMLX files

AMLX file validation includes the following steps:

- Check whether each document defined in */_rels/.rels* exists
- Check whether each file in the AMLX file (a ZIP archive) is defined in */_rels/.rels*
- Check that the root document exists
- Check that there is exactly one root document
- Check that the root document is a valid AML file

### OPC UA Nodeset files

The OPC UA Nodeset validation includes the following

- Check that the nodeset xml file is valid xml file.
- Check that the nodeset xml file is valid according
  to [UANodeSet.xsd](https://github.com/OPCFoundation/UA-Nodeset/blob/v1.04/Schema/UANodeSet.xsd) V1.04 schema.

Note : The  nodeset [EntType.xml](https://github.com/kannoth/aas-transformation-library/tree/main/src/test/resources/ua/EntType.xml)
is taken from OPC UA information models published by [Equinor](https://github.com/equinor/opc-ua-information-models/tree/test) and used as a test resource.

## Configuration

Asset Administration Shells are instantiated using the basic structure determined in the AAS-JSON specification.
However, there are a variety of tools that ease the writing process

### Expressions
Expressions are denoted by a `@` at the beginning of the json-key and signify a side-effect during runtime. While those
configurations with no expression in their context are just parsed as AAS-JSON, expressions are evaluated with their result
determining the structure of the resulting AAS objects.

### Looping with `@foreach`
On every level (except for the `aasEnvironmentMapping`) objects can be dynamically generated using this feature.
It evaluates the expression and builds objects according to the statements below - once for every returned value.
The syntax is as follows:

```json
{
  "@foreach": {
    "@xpath": "someXpath"
  },
  "xyz": "abc"
}
```
### Dynamic evaluation with `@bind`
Looping around the results of an expression would be obsolete if all resulting objects would hold the same values.
That's why the @bind Context allows to fill AAS-attributes with the result of an expression. It may only return a single
value that will be used. If I wanted to configure changing idShorts based on the iterator, it could look like this:

```json
{
  "@foreach": {
    "@xpath": "someXpathQuery"
  },
  "@bind": {
    "idShort": {
      "@xpath": "somePotentiallyRelativeXpathQuery"
    }
  }
}
```
###Compound Expressions with `@definitions`
Usually in the `@header` (but everywhere else is fine as well) the config can define more complex functions that will
then be called using the `@def`-key in the `@bind`-context


## config-JSON structure

The configuration file describes how the AAS file should be generated.
See [this config file](src/test/resources/config/simpleConfig.json) for an example.

### Asset Administration Shell Environment

The library generates a single AAS Environment for every config-file. The structure of the config-file is as follows.

```json
{
  "@header": {
    "aasVersion": "3.0RC01",
    "@namespaces": {},
    "@definitions": {},
    "@variables": {},
    "@parameters": {}
  },
  "aasEnvironmentMapping": [
    {
      "assetAdministrationShells": [],
      "assets": [],
      "submodels": [],
      "conceptDescriptions": []
    }
  ]
}
```

This structure looks familiar because everything in the "aasEnvironmentMapping" holds the same structure as the JSON-
serialization of AAS. This is supported both to reuse the syntax as well as improve maintainability and parsability of
the config-JSON files.

### AssetAdministrationShells

```json
{
  "@foreach": {
    "@xpath": "some-xPath-Query that may return multiple results"
  },
  "idShort": "someIdShort",
  "identification": {
    "idType": "Iri",
    "id": "someIri"
  },
  "assetInformation": {
    ...
  }
}
```

In the above case all AAS would hold the same idShort and identification which is semantically wrong but can be avoided
using the `@bind` operator. Please note that the AAS' `submodels:`-key holding references to submodel objects is omitted
in the config-file since its contents are generated automatically.

### Assets

Configuring Assets is optional in the transformation library, just like it's optional in the AAS in general.

```json
{
  "assets": [
    {
      "identification": {
        "id": "Iri",
        "idType": "SomeIri"
      }
    }
  ]
}
```

### Submodels

An AAS Submodel are generated in the `submodels`-section and use expressions the same way as explained before. An
example:

```json
{
  "submodels": [
    {
      "@foreach": {
        "@xpath": "caex:Attribute[@Name='CommercialData']/caex:Attribute[@Name='ManufacturerDetails']"
      },
      "semanticId": {
        "keys": [
          {
            "value": "https://admin-shell.io/zvei/nameplate/1/0/Nameplate",
            "idType": "Iri",
            "type": "Submodel"
          }
        ]
      },
      "identification": {
        "id": "_submodel1",
        "idType": "Custom"
      },
      "submodelElements": [
        {
          "semanticId": {
            "keys": [
              {
                "value": "0173-1#02-AAO677#002",
                "idType": "Irdi",
                "type": "SubmodelElement"
              }
            ]
          }
        }
      ]
    }
```

SubmodelElements are added into the submodels as defined by the json-spec.


## Known issues & limitations

Please see [Issues](https://github.com/SAP/aas-transformation-library/issues) list.

## Upcoming changes

Please refer to the Github issue board. For upcoming features check the "enhancement" label.

## Contributing

You are welcome to join forces with us in the quest to contribute to the Asset Administration Shell community! Simply
check our [Contribution Guidelines](CONTRIBUTING.md).

## Code of Conduct

Everyone participating in this joint project is welcome as long as our [Code of Conduct](CODE_OF_CONDUCT.md) is being
adhered to.

## To Do

Many improvements are coming! All tasks will be posted to our GitHub issue tracking system. As mentioned, some of the
improvements will mean breaking changes. While we strive to avoid doing so, we cannot guarantee this will not happen
before the first release.

## License

Copyright 2021 SAP SE or an SAP affiliate company and aas-transformation-library contributors. Please see
our [LICENSE](https://github.com/SAP/aas-transformation-library/blob/main/LICENSE) for copyright and license
information. Detailed information including third-party components and their licensing/copyright information is
available via the [REUSE tool](https://api.reuse.software/info/github.com/SAP/aas-transformation-library).
