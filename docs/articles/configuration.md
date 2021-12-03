# Configuration

Asset Administration Shells are instantiated using the basic structure determined in the AAS-JSON specification.
However, there are a variety of tools that ease the writing process.

## Expressions

Expressions are bottom-level-objects denoted by a `@` at the beginning of the json-key and signify a side-effect during
runtime. While those configurations with no expression in their context are just parsed as AAS-JSON, expressions are
evaluated with their result determining the structure of the resulting AAS objects. Examples include:

- `@xpath` for evaluation of xPath-Queries
- `@caexAttributeName` to fetch the attribute name of an element in a AutomationML file. Takes a string.
- `@uaBrowsePath` gets a Node's NodeId by its BrowsePath from a OPC UA nodeset file. Takes a list
  of [BrowseNames](https://reference.opcfoundation.org/Core/docs/Part3/5.2.4/)
  connected by hierarchical ReferenceTypes. This is called the `BrowsePath` in OPC UA.
- `@uaChildren` takes a BrowsePath (see above) and returns all Nodes that are connected to this node via a hierarchical
  ReferenceType.
- Several basic mathematical and boolean operations such as `@plus`,`@times`,`@max`,`@negate` or `@and`.

Please note that Expressions can only be called from within a `@bind`- or `@foreach`-context.

## Looping with `@foreach`

On every level (except for the `aasEnvironmentMapping`) objects can be dynamically generated using this feature. It
evaluates the expression and builds objects according to the statements below - once for every returned value. The
syntax is as follows:

```json
{
  "@foreach": {
    "@xpath": "someXpath"
  },
  "xyz": "abc"
}
```

## Dynamic evaluation with `@bind`

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

## Pre-defined Expressions with `@definitions`

Usually in the `@header` (but everywhere else is fine as well), the config can define more complex functions that will
then be called using the `@def`-key in the `@bind`-context.In the example below, the `exampleFunction` is called to
assign an id to an AAS.

```json
{
  "@headers": {
    "@definitions": {
      "exampleFunction": {
        "@concatenate": [
          {
            "@xpath": "someXpath"
          },
          "/",
          {
            "@xpath": "someOtherXpath"
          }
        ]
      }
    }
  },
  "aasEnvironmentMapping": {
    ...,
    "assetAdministrationShells": {
      "identification": {
        "@bind": {
          "id": {
            "@def": "exampleFunction"
          }
        }
      }
    }
  }
}
```

# Structure of the config-json file

The configuration file describes how the AAS file should be generated.
See [this config file](src/test/resources/config/simpleConfig.json) for an example.

## `@header`

The header sets a couple of circumstances for the transformation.

- `aasVersion` indicates the version of the meta-model that a given config-file is created for. This matters since the
  objects and their json-serialization vary greatly with each version.
- `@definitions` defines that name and content of the pre-defined expressions (see above) that can later be called
  with `@def`
- `@variables` allows the user to define values that can be bound to a variable, updated with another `@variables`
  statement and dereferenced by `@var`, each in a `@bind`-context
- `@namespaces` passes namespaces that the DOM4J xPath-Engine needs to successfully evaluate queries on a given XML
  document.
- `@parameters` are fixed value variable that will be replaced after the whole transformation and can be set by the
  caller during runtime. They are dereferenced by `@var` as well.

## Building Asset Administration Shell Environments with the `aasEnvironmentMapping`

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
in the config-file since its contents (References to Submodels in the `"submodels":[]` section are generated
automatically.

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
  ]
}
```

SubmodelElements are added into the submodels as defined by the json-spec.

### Concept Descriptions

Concept Descriptions are supported and can be built using the same toolbox of expressions, definitions, variables and
control statements like @bind and @foreach.