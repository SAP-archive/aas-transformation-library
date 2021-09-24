# aas-transformation-library

Transform [AutomationML (AML)](https://www.automationml.org/) content into [Asset Administration Shell (AAS)](https://www.plattform-i40.de/PI40/Redaktion/DE/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part_2_V1.html) format.

- [aas-transformation-library](#aas-transformation-library)
  - [Documentation](#documentation)
  - [Requirements](#requirements)
  - [Local Usage](#local-usage)
  - [Validation](#validation)
    - [AML files](#aml-files)
    - [AMLX files](#amlx-files)
  - [Configuration](#configuration)
    - [Asset](#asset)
    - [Asset Information](#asset-information)
    - [Asset Shell](#asset-shell)
    - [Submodel](#submodel)
    - [Submodel Elements](#submodel-elements)
      - [Blob](#blob)
      - [File](#file)
      - [Entity](#entity)
      - [MultiLanguageProperty](#multilanguageproperty)
      - [Range](#range)
      - [Capability](#capability)
      - [BasicEvent](#basicevent)
      - [Operation](#operation)
      - [Property](#property)
      - [ReferenceElement](#referenceelement)
      - [RelationshipElement](#relationshipelement)
      - [AnnotatedRelationshipElement](#annotatedrelationshipelement)
      - [SubmodelElementCollection](#submodelelementcollection)
  - [Known Issues & Limitations](#known-issues--limitations)
  - [Upcoming Changes](#upcoming-changes)
  - [Contributing](#contributing)
  - [Code of Conduct](#code-of-conduct)
  - [To Do](#to-do)
  - [License](#license)

## Documentation
The library can be used twofold. Users can..
1. import the library in Java applications to transform AML inputs into AAS objects.
2. use the library to build a [fat JAR](https://github.com/johnrengelman/shadow) to transform AML files into AAS JSON files locally.

In order to provide this functionality the library makes heavy use of [XPath](https://www.w3schools.com/xml/xpath_intro.asp) to parse the XML based object-oriented data modeling language. Currently, we support XPath version 1.0 (which is supported by dom4j).

Users have to provide two inputs:
1. One AML or AMLX file
2. One configuration ("config") file in JSON format, which details out which part of a given AutomationML input corresponds to AAS fields (Examplary configuration files can be found under [_src/test/resources/config_](src/test/resources/config))

Depending on its usage the library will output one of the two:
* One AAS JSON file containing a serialized _AssetAdministrationShellEnvironment_ object
* One _AssetAdministrationShellEnvironment_ object (leveraging [admin-shell-io/java-model](https://github.com/admin-shell-io/java-model))

#### Note:
There is an _IdGeneration_ function being used to create values for Reference objects.
In order for the function to be called correctly, the XPath of the config file must be registered in the node graph, which happens in the _prepareGraph()_ of the IdGenerator.java class.

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
AML file validation includes the following steps (cf. [_AmlValidator.java_](https://github.wdf.sap.corp/AAS/aml-transformation-module/blob/develop/src/main/java/com/sap/dsc/aas/lib/aml/transform/validation/AmlValidator.java)):
- Check that the AML file is a valid XML file
- Check that the AML file is valid according to the [CAEX 3.0 class model](https://github.wdf.sap.corp/AAS/aml-transformation-module/blob/develop/src/main/resources/aml/CAEX_ClassModel_V.3.0.xsd)

### AMLX files
AMLX file validation includes the following steps (cf. [_AmlxValidator.java_](https://github.wdf.sap.corp/AAS/aml-transformation-module/blob/develop/src/main/java/com/sap/dsc/aas/lib/aml/amlx/AmlxValidator.java)):
- Check whether each document defined in */_rels/.rels* exists
- Check whether each file in the AMLX file (a ZIP archive) is defined in */_rels/.rels*
- Check that the root document exists
- Check that there is exactly one root document
- Check that the root document is a valid AML file

## Configuration
The configuration file describes how the AAS file should be generated.
See [this config file](src/test/resources/config/simpleConfig.json) for an example.

### Asset Administration Shell Environment
An AAS Asset Administration Shell Environment is generated using the following configuration:
```javascript
{
  "version": "1.0.0",
  "aasVersion": "3.0RC01",
  "configMappings": [
    {
      "from_xpath": "",
      "idGeneration": { ... },
      "assetInformation": { ... },
      "submodels": [ ... ],
      "assetShell": { ... }
    }
  ]
}
```

### Asset
An AAS Asset is generated using the following configuration:
```javascript
{
    "idGeneration":{
        "parameters":[
            {
                "from_string":"shellId"
            }
        ]
    },

    // XPath expression for finding the idShort
    // If this attribute is omitted, the xpath "@Name" is used by default
    "idShort_xpath":"@Name"
}
```

### Asset Information
AAS AssetInformation is generated using the following configuration:
```javascript
{
    // XPath expression for the type of the asset ("Type" or "Instance")
    // XPath allows hardcoded values by using single quotes
    // If this attribute is omitted, the xpath "'Type'" is used by default
    "kindType_xpath":"TYPE",

    "idGeneration":{
        "parameters":[
            {
                "from_xpath":"caex:Attribute[@Name='IdentificationData']/caex:Attribute[@Name='Manufacturer']/caex:Value",
                "from_string":"DefaultManufacturer"
            },
            {
                "from_string":"/"
            },
            {
                "from_xpath":"caex:Attribute[@Name='IdentificationData']/caex:Attribute[@Name='ManufacturerUri']/caex:Value"
            }
        ],
        "finalizeFunction":"concatenate_and_hash",
        "idGenerationName":"assetIdGeneration"
    },
    "globalAssetIdReference":{
        "valueId":"assetInformationGlobalAssetIdReference",
        "keyType":"CUSTOM",
        "keyElement":"ASSET"
    }
}
```

### Asset Shell
An AAS AssetAdministrationShell is generated using the following configuration:
```javascript
{
    "id": "assetId",
    "idShort_xpath": "@Name"
}
```

### Submodel
An AAS Submodel is generated using the following configuration:

```
{
    // XPath expression to be used to navigate to the root XML node(s) for the submodel.
    // One AAS submodel will be generated for each matching node.
    "from_xpath": "caex:Attribute[@Name='IdentificationData']"

    // This attribute is "syntax sugar", in other words it is
    // designed to make the config file less verbose.
    // This example corresponds to the XPath above.
    // NOTE: Pass either this attribute OR "from_xpath"
    "from_attributeName": "IdentificationData".

    // Pass a string representing the semantic ID (not an XPath)
    "semanticId": "http://sap.com",

    "id": "submodelId",
    "isShort_xpath": "@Name",

    // See "Submodel Element"
    "submodelElements": [{...}, {...}]
}

```

### Submodel Elements
AAS SubmodelElements are generated using type-specific configuration. This examples yields in an AAS Property:

```javascript
{
    // For the following two attributes see "Submodel".
    // One submodel element is generated for each matching XML node.
    "from_xpath": "caex:Attribute[@Name='Manufacturer']",
    "from_attributeName": "Manufacturer",

    "semanticId": "http://sap.com/submodelElement",

    // Define the value type of the submodel element
    // Examples: "string" or "float"
    "valueType": "string",

    // Define the model type of the submodel element
    // Example: "Property" or "Collection"
    "modelType": "Property"

    // This attribute is "syntax sugar", in other words it is
    // designed to make the config file less verbose.
    // This example corresponds to the combination of valueType and
    // modelType above.
    // Example: "string" or "collection"
    "elementType": "string"

    // XPath expression for finding the value of the submodel element
    "valueXPath": "caex:Value",

    // Hardcoded value to use if the value returned by XPath is null
    "valueDefault": "MyDefaultValue"

    // If this submodel element is a "collection":
    // Pass an array of submodel elements as children
    "submodelElements": [{...}, {...}]
}
```

Here are examples of additional mapping configurations for different SubmodelElements:

#### Blob
```javascript
{
    "from_xpath": "caex:InternalElement[@Name='ManualsBIN']/caex:InternalElement[@Name='BetriebsanleitungBIN']/caex:ExternalInterface[@Name='ExternalDataReference']",
    "idShort_xpath": "'BetriebsanleitungBIN'",
    "@type": "Blob",

    // type-specific submodel element attribute paths are listed explicitly
    "valueXPath": "caex:Attribute[@Name='refURI']/caex:Value",
    "mimeTypeXPath": "caex:Attribute[@Name='MIMEType']/caex:Value"
}
```

The matching AML:
```xml
<InternalElement Name="BetriebsanleitungBIN">
    <ExternalInterface Name="ExternalDataReference"
        RefBaseClassPath="AutomationMLBPRInterfaceClassLib/ExternalDataReference">
        <Attribute Name="MIMEType" AttributeDataType="xs:string">
            <DefaultValue></DefaultValue>
            <Value>application/pdf</Value>
        </Attribute>
        <Attribute Name="refURI" AttributeDataType="xs:anyURI"
            RefAttributeType="AutomationMLBaseAttributeTypeLib/refURI">
            <Value>foo</Value>
        </Attribute>
    </ExternalInterface>
    <SupportedRoleClass
        RefRoleClassPath="AutomationMLBPRRoleClassLib/ExternalData"/>
</InternalElement>
```

#### File
```javascript
{
    "from_xpath": "caex:InternalElement[@Name='Manuals']/caex:InternalElement[@Name='Betriebsanleitung']/caex:ExternalInterface[@Name='ExternalDataReference']",
    "idShort_xpath": "'Betriebsanleitung'",
    "@type": "File",

    // type-specific submodel element attribute paths are listed explicitly
    "valueXPath": "caex:Attribute[@Name='refURI']/caex:Value",
    "mimeTypeXPath": "caex:Attribute[@Name='MIMEType']/caex:Value"
}
```

The matching AML:
```xml
<InternalElement Name="Manuals">
    <InternalElement Name="Betriebsanleitung">
        <ExternalInterface Name="ExternalDataReference"
            RefBaseClassPath="AutomationMLBPRInterfaceClassLib/ExternalDataReference">
            <Attribute Name="MIMEType" AttributeDataType="xs:string">
                <DefaultValue></DefaultValue>
                <Value>application/pdf</Value>
            </Attribute>
            <Attribute Name="refURI" AttributeDataType="xs:anyURI"
                RefAttributeType="AutomationMLBaseAttributeTypeLib/refURI">
                <Value>manual/OI_wtt12l_en_de_fr_it_pt_es_zh_.pdf</Value>
            </Attribute>
        </ExternalInterface>
        <SupportedRoleClass
            RefRoleClassPath="AutomationMLBPRRoleClassLib/ExternalData"/>
    </InternalElement>
    <RoleRequirements
        RefBaseRoleClassPath="AutomationMLBaseRoleClassLib/AutomationMLBaseRole"/>
</InternalElement>
```

#### Entity
```javascript
{
    "from_attributeName": "PackagingAndTransportation",
    "@type": "Entity",
    "semanticId": "PackAndTransport",

    // type-specific submodel element attribute paths are listed explicitly
    "entityType": "SelfManagedEntity",
    "assetReference": {
        "valueId": "AssetIdExtern",
        "local": "false"
    },
    "localAssetReference": "false",
    "statements": [
        {
            "from_attributeName": "GTIN",
            "@type": "Property",
            "valueType": "string"
        }
    ]
}
```

The matching AML:
```xml
<Attribute Name="CommercialData">
    <Attribute Name="PackagingAndTransportation">
        <Attribute Name="GTIN">
            <Value>TestGlobalTradeItemNumber1234</Value>
        </Attribute>
        <Attribute Name="CustomsTariffNumber">
            <Value>1234</Value>
        </Attribute>
        <Attribute Name="CountryOfOrigin">
            <Value>TestCountryOfOriginDE</Value>
        </Attribute>
    </Attribute>
</Attribute>
```

#### MultiLanguageProperty
```javascript
{
    "from_attributeName": "Material",
    "@type": "MultiLanguageProperty"
}
```

The matching AML:
```xml
<Attribute Name="Material">
    <Value>Test Material</Value>
    <Attribute Name="aml-lang=en_US">
        <Description>This is the value name in english</Description>
        <Value>English Test Material</Value>
    </Attribute>
    <Attribute Name="aml-lang=de_DE">
        <Description>Dies ist der Name in Deutsch</Description>
        <Value>Test Material Deutsch</Value>
    </Attribute>
</Attribute>
```

#### Range
```javascript
{
    "from_attributeName": "AmbientTemperature",
    "@type": "Range",
    "semanticId": "AmbientTemperature",
    "valueType": "Integer",

    // type-specific submodel element attribute paths are listed explicitly
    "minValueXPath": "caex:Attribute[@Name='TemperatureMin']/caex:Value",
    "maxValueXPath": "caex:Attribute[@Name='TemperatureMax']/caex:Value"
}
```

The matching AML:
```xml
<Attribute Name="GeneralTechnicalData">
    <Attribute Name="AmbientTemperature">
        <Attribute Name="TemperatureMin">
            <Value>-273</Value>
        </Attribute>
        <Attribute Name="TemperatureMax">
            <Value>100</Value>
        </Attribute>
    </Attribute>
</Attribute>
```

#### Capability
```javascript
{
    "from_xpath": "caex:Attribute[@Name='ManufacturerURI']",
    "idShort_xpath": "'Browseable'",
    "@type": "Capability"
}
```

The matching AML:
```xml
<Attribute Name="IdentificationData">
    <Attribute Name="ManufacturerURI">
        <Value>http://www.example.com/manufacturerURI</Value>
    </Attribute>
</Attribute>
```

#### BasicEvent
```javascript
{
    "from_xpath": "caex:InternalElement[@Name='ManualsBasicEvent']/caex:InternalElement[@Name='SampleBasicEvent']/caex:ExternalInterface[@Name='ExternalDataReference']",
    "idShort_xpath": "'SampleBasicEvent'",
    "@type": "BasicEvent",

    // reference to another submodel element that is a Refereable
    "observed": {
        "valueIdGeneration": {
            "parameters": [
                {
                    "from_xpath": "caex:Attribute[@Name='refURI']/caex:Value"
                }
            ]
        },
        "keyElement": "Blob",
        "local": "false"
    }
}
```

The matching AML:
```xml
<InternalElement Name="ManualsBasicEvent">
    <InternalElement Name="SampleBasicEvent">
        <ExternalInterface Name="ExternalDataReference" RefBaseClassPath="AutomationMLBPRInterfaceClassLib/ExternalDataReference">
            <Attribute Name="refURI" AttributeDataType="xs:anyURI" RefAttributeType="AutomationMLBaseAttributeTypeLib/refURI">
            <Value>Blob</Value>
            </Attribute>
        </ExternalInterface>
        <SupportedRoleClass
            RefRoleClassPath="AutomationMLBPRRoleClassLib/ExternalData"/>
    </InternalElement>
    <RoleRequirements
        RefBaseRoleClassPath="AutomationMLBaseRoleClassLib/AutomationMLBaseRole"/>
</InternalElement>
```

#### Operation
```javascript
{
    "from_attributeName": "OperationA",
    "semanticId": "ops",
    "@type": "Operation",

    // type-specific submodel element attribute paths are listed explicitly
    "inputVariables": [
        {
            "from_xpath": "caex:Attribute[@Name='Inputs']/caex:Attribute",
            "@type": "Property",
            "valueType": "string"
        }
    ],
    "outputVariables": [
        {
            "from_xpath": "caex:Attribute[@Name='Outputs']/caex:Attribute",
            "@type": "Property",
            "valueType": "string"
        }
    ],
    "inOutputVariables": [
        {
            "from_xpath": "caex:Attribute[@Name='InOut']/caex:Attribute",
            "@type": "Property",
            "valueType": "string"
        }
    ]
}
```

The matching AML:
```xml
<Attribute Name="OperationA">
    <Attribute Name="Inputs">
        <Attribute Name="PinA">
            <Value>10</Value>
        </Attribute>
        <Attribute Name="PinB">
            <Value>15</Value>
        </Attribute>
        <Attribute Name="PinC">
            <Value>30</Value>
        </Attribute>
    </Attribute>
    <Attribute Name="Outputs">
        <Attribute Name="PinD"/>
        <Attribute Name="PinE">
            <Value>15</Value>
        </Attribute>
    </Attribute>
    <Attribute Name="InOut">
        <Attribute Name="PinF">
            <Value>10</Value>
        </Attribute>
    </Attribute>
</Attribute>
```

#### Property
```javascript
{
    "from_xpath": "caex:Attribute[@Name='InOut']/caex:Attribute",
    "@type": "Property",
    "valueType": "string"
}
```

The matching AML:
```xml
<Attribute Name="InOut">
    <Attribute Name="PinF">
        <Value>10</Value>
    </Attribute>
</Attribute>
```

#### ReferenceElement
```javascript
{
    "from_xpath": "caex:InternalElement[@Name='STEPGeometry']/caex:ExternalInterface[@Name='ExternalDataReference']",
    "idShort_xpath": "'STEPGeometry'",
    "@type": "ReferenceElement",

    // type-specific submodel element attribute paths are listed explicitly
    "value": {
        "valueIdGeneration": {
            "parameters": [
                {
                    "from_xpath": "caex:Attribute[@Name='refURI']/caex:Value"
                }
            ]
        },
        "keyElement": "ConceptDescription",
        "local": "false"
    }
}
```

The matching AML:
```xml
<InternalElement Name="STEPGeometry">
    <ExternalInterface Name="ExternalDataReference"
        RefBaseClassPath="AutomationMLBPRInterfaceClassLib/ExternalDataReference">
        <Attribute Name="MIMEType" AttributeDataType="xs:string">
            <Value>application/STEP</Value>
        </Attribute>
        <Attribute Name="refURI" AttributeDataType="xs:anyURI"
            RefAttributeType="AutomationMLBaseAttributeTypeLib/refURI">
            <Value>MGFTT2-DFC-C_20200519_134129_7UszRjlaUUSGJwr_3pyZ3g</Value>
        </Attribute>
    </ExternalInterface>
    <SupportedRoleClass RefRoleClassPath="AutomationMLComponentBaseRCL/GeometryModel"/>
</InternalElement>
```

#### RelationshipElement
```javascript
{
    "from_attributeName": "OperationA",
    "semanticId": "test",
    "@type": "RelationshipElement",
    "idShort_xpath": "'relElement'",

    // type-specific submodel element attribute paths are listed explicitly
    "first": {
        "valueId": "FOO",
        "keyElement": "ConceptDescription",
        "local": "false"
    },
    "second": {
        "valueId": "BAR",
        "keyElement": "ConceptDescription",
        "local": "true"
    }
}
```

The matching AML:
```xml

```

#### AnnotatedRelationshipElement
```javascript
{
    "from_attributeName": "OperationA",
    "semanticId": "test/test/test",
    "@type": "AnnotatedRelationshipElement",
    "idShort_xpath": "'annoRelElement'",

    // type-specific submodel element attribute paths are listed explicitly
    "first": {
        "valueId": "abc",
        "keyElement": "ConceptDescription",
        "local": "false"
    },
    "second": {
        "valueId": "def",
        "keyElement": "ConceptDescription",
        "local": "true"
    },
    "annotations": [
        {
            "from_xpath": "caex:Attribute[@Name='Inputs']/caex:Attribute",
            "@type": "Property",
            "valueType": "string"
        }
    ]
}
```

The matching AML:
```xml
<Attribute Name="OperationA">
    <Attribute Name="Inputs">
        <Attribute Name="PinA">
            <Value>10</Value>
        </Attribute>
        <Attribute Name="PinB">
            <Value>15</Value>
        </Attribute>
        <Attribute Name="PinC">
            <Value>30</Value>
        </Attribute>
    </Attribute>
    <Attribute Name="Outputs">
        <Attribute Name="PinD"/>
        <Attribute Name="PinE">
            <Value>15</Value>
        </Attribute>
    </Attribute>
    <Attribute Name="InOut">
        <Attribute Name="PinF">
            <Value>10</Value>
        </Attribute>
    </Attribute>
</Attribute>
```

#### SubmodelElementCollection
```javascript
{
    "from_attributeName": "ProductPriceDetails",
    "@type": "SubmodelElementCollection",
    "semanticId": "SubmodelElementCollectionSemanticId",

    // an array of 0..* contained submodel elements
    "submodelElements": [
        {
            "from_attributeName": "ValidStartDate",
            "@type": "Property",
            "valueType": "string"
        },
        {
            "from_xpath": "caex:Attribute[@Name='ProductPrice']/caex:Attribute[@Name='PriceAmount']",
            "@type": "Property",
            "valueType": "string"
        }
    ]
}
```

The matching AML:
```xml
<Attribute Name="ProductPriceDetails">
    <Attribute Name="ValidStartDate">
        <Value>2020-01-01</Value>
    </Attribute>
    <Attribute Name="ProductPrice">
        <Attribute Name="PriceAmount">
            <Value>TestPriceAmount</Value>
        </Attribute>
    </Attribute>
</Attribute>
```

## Known issues & limitations
Please see [Issues](https://github.com/SAP/aas-transformation-library/issues) list.

## Upcoming changes
Please refer to the Github issue board. For upcoming features check the "enhancement" label.

## Contributing
You are welcome to join forces with us in the quest to contribute to the Asset Administration Shell community! Simply check our [Contribution Guidelines](CONTRIBUTING.md).

## Code of Conduct
Everyone participating in this joint project is welcome as long as our [Code of Conduct](CODE_OF_CONDUCT.md) is being adhered to.

## To Do
Many improvements are coming! All tasks will be posted to our GitHub issue tracking system. As mentioned, some of the improvements will mean breaking changes. While we strive to avoid doing so, we cannot guarantee this will not happen before the first release.

## License
Copyright 2021 SAP SE or an SAP affiliate company and aas-transformation-library contributors. Please see our [LICENSE](LICENSE.md) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available via the [REUSE tool](https://api.reuse.software/info/github.com/SAP/aas-transformation-library).