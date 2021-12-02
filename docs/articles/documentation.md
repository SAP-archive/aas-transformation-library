# Documentation

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
### Compound Expressions with `@definitions`
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


## Features
As mentioned in the intro, the library can process a variety of xml files. Since some aspects are only viable for a given
xml schema, files are validated beforehand if they shall be transformed by the [AmlTransformer](https://github.com/admin-shell-io/aas-transformation-library/blob/main/src/main/java/com/sap/dsc/aas/lib/aml/transform/AmlTransformer.java)
or the [UANodeSetTransformer.](https://github.com/admin-shell-io/aas-transformation-library/blob/main/src/main/java/com/sap/dsc/aas/lib/ua/transform/UANodeSetTransformer.java)
That's why the following steps are undertaken:
### AML files

AML file validation includes the following steps (cf. [_AmlValidator.java_](https://github.com/admin-shell-io/aas-transformation-library/blob/main/src/main/java/com/sap/dsc/aas/lib/aml/transform/validation/AmlValidator.java)):
- Check that the AML file is a valid XML file
- Check that the AML file is valid according to the [CAEX 3.0 class model](https://github.com/admin-shell-io/aas-transformation-library/blob/main/src/main/resources/aml/CAEX_ClassModel_V.3.0.xsd)

### AMLX files
AMLX file validation includes the following steps (cf. [_AmlxValidator.java_](https://github.com/br-iosb/aas-transformation-library/blob/main/src/main/java/com/sap/dsc/aas/lib/aml/amlx/AmlxValidator.java)):
- Check whether each document defined in */_rels/.rels* exists
- Check whether each file in the AMLX file (a ZIP archive) is defined in */_rels/.rels*
- Check that the root document exists
- Check that there is exactly one root document
- Check that the root document is a valid AML file

### OPC UA Nodeset files
The OPC UA Nodeset validation includes the following (cf.
[_UANodeSetSchemaValidator.java_](https://github.com/admin-shell-io/aas-transformation-library/blob/main/src/main/java/com/sap/dsc/aas/lib/ua/transform/validation/UANodeSetSchemaValidator.java)):

- Check that the nodeset xml file is valid xml file.
- Check that the nodeset xml file is valid according
  to [UANodeSet.xsd](https://github.com/OPCFoundation/UA-Nodeset/blob/v1.04/Schema/UANodeSet.xsd) V1.04 schema.

__Please note__: The nodeset [EntType.xml](https://github.com/admin-shell-io/aas-transformation-library/tree/main/src/test/resources/ua/EntType.xml)
is taken from OPC UA information models published by [Equinor](https://github.com/equinor/opc-ua-information-models/tree/test).