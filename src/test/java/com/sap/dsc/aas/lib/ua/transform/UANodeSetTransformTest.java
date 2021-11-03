package com.sap.dsc.aas.lib.ua.transform;

import com.sap.dsc.aas.lib.config.ConfigLoader;
import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.transform.DocumentTransformer;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.model.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.io.File;
import java.util.List;

public class UANodeSetTransformTest {

    private static final String NODESET = "ua/big.machine.nodeset.xml";
    private static final String CONFIG_JSON = "src/test/resources/ua/machineConfig.json";

    private static final String GLOBAL_ASSET_ID = "assetInformationDevice";
    private static final String AAS_ID_LONG = "deviceAASID";
    private static final String AAS_ID_SHORT = "devId";
    private static final String SM_ID_LONG = "DeviceTopology";
    private static final String SM_ID_SHORT = "DeviceTopology";

    private static final String PROP_DEV_REV_ID_SHORT = "ns=1;i=15041";

    private DocumentTransformer transformer;
    private ConfigLoader configLoader;
    private AssetAdministrationShellEnvironment aasEnvironment;

    @BeforeEach
    void setUp() throws IOException, TransformationException {
        InputStream is = new FileInputStream(
                new File(Thread.currentThread().getContextClassLoader().getResource(NODESET).getPath()));
        transformer = new UANodeSetTransformer();
        configLoader = new ConfigLoader();
        aasEnvironment = transformer.transform(is, configLoader.loadConfig(CONFIG_JSON));
    }

    @Test
    void testAssetsAAS() throws IOException, TransformationException, SerializationException {
        List<AssetAdministrationShell> assetAdminstrationShells = aasEnvironment.getAssetAdministrationShells();
        Assert.assertEquals(1, assetAdminstrationShells.size());

        AssetAdministrationShell assetAdministrationShell = filterAssetAdministrationShell(assetAdminstrationShells, AAS_ID_LONG);
        Assert.assertEquals(AAS_ID_LONG, assetAdministrationShell.getIdentification().getIdentifier());
        Assert.assertEquals(IdentifierType.CUSTOM, assetAdministrationShell.getIdentification().getIdType());
        Assert.assertEquals(AAS_ID_SHORT, assetAdministrationShell.getIdShort());

        List<Key> submodelReferencesKeys = assetAdministrationShell.getSubmodels().get(0).getKeys();
        Assert.assertEquals(SM_ID_LONG, submodelReferencesKeys.get(0).getValue());
        Assert.assertEquals(KeyType.CUSTOM, submodelReferencesKeys.get(0).getIdType());
        Assert.assertEquals(KeyElements.SUBMODEL, submodelReferencesKeys.get(0).getType());

        AssetInformation assetInformation = assetAdministrationShell.getAssetInformation();
        List<Key> globalAssetId = assetInformation.getGlobalAssetId().getKeys();
        Assert.assertEquals(AssetKind.TYPE, assetInformation.getAssetKind());
        Assert.assertEquals(GLOBAL_ASSET_ID, globalAssetId.get(0).getValue());
        Assert.assertEquals(KeyType.CUSTOM, globalAssetId.get(0).getIdType());
        Assert.assertEquals(KeyElements.ASSET, globalAssetId.get(0).getType());
    }

    @Test
    void testSubmodels() {
        List<Submodel> submodels = aasEnvironment.getSubmodels();
        Assert.assertEquals(1, submodels.size());

        Submodel submodel = filterSubmodel(submodels, SM_ID_LONG);
        Assert.assertEquals(SM_ID_LONG, submodel.getIdentification().getIdentifier());
        Assert.assertEquals(IdentifierType.CUSTOM, submodel.getIdentification().getIdType());
        Assert.assertEquals(SM_ID_SHORT, submodel.getIdShort());

        List<Key> submodelSemanticIds = submodel.getSemanticId().getKeys();
        Assert.assertEquals("http://device.semantic.com", submodelSemanticIds.get(0).getValue());
        Assert.assertEquals(KeyType.IRDI, submodelSemanticIds.get(0).getIdType());
        Assert.assertEquals(KeyElements.CONCEPT_DESCRIPTION, submodelSemanticIds.get(0).getType());

        Assert.assertEquals(17, submodel.getSubmodelElements().size());
    }

    @Test
    void testSubmodelElementsProperty() {
        List<SubmodelElement> submodelElements = aasEnvironment.getSubmodels().get(0).getSubmodelElements();
        Assert.assertEquals(17, submodelElements.size());

        SubmodelElement propertyDeviceRevision = filterSubmodelElement(submodelElements, PROP_DEV_REV_ID_SHORT);

        Assert.assertEquals(PROP_DEV_REV_ID_SHORT, propertyDeviceRevision.getIdShort());
        Assert.assertEquals(ModelingKind.INSTANCE, propertyDeviceRevision.getKind());
        Assert.assertTrue(propertyDeviceRevision instanceof Property);
        Assert.assertEquals("DeviceRevision", ((Property) propertyDeviceRevision).getValue());
        Assert.assertEquals("string", ((Property) propertyDeviceRevision).getValueType());
        Assert.assertEquals("PARAMETER", propertyDeviceRevision.getCategory());

        List<Key> propertyDeviceRevisionSemantics = propertyDeviceRevision.getSemanticId().getKeys();
        Assert.assertEquals("0112/2///61987#ABA574#006", propertyDeviceRevisionSemantics.get(0).getValue());
        Assert.assertEquals(KeyType.IRDI, propertyDeviceRevisionSemantics.get(0).getIdType());
        Assert.assertEquals(KeyElements.CONCEPT_DESCRIPTION, propertyDeviceRevisionSemantics.get(0).getType());

        List<Key> propertyDeviceRevisionValueIds = ((Property) propertyDeviceRevision).getValueId().getKeys();
        Assert.assertEquals("DeviceRevision", propertyDeviceRevisionValueIds.get(0).getValue());
        Assert.assertEquals(KeyType.CUSTOM, propertyDeviceRevisionValueIds.get(0).getIdType());
        Assert.assertEquals(KeyElements.PROPERTY, propertyDeviceRevisionValueIds.get(0).getType());
    }

    private AssetAdministrationShell filterAssetAdministrationShell(List<AssetAdministrationShell> assetAdministrationShells, String idLong) {

        for(AssetAdministrationShell assetAdministrationShell : assetAdministrationShells) {
            if(assetAdministrationShell.getIdentification().getIdentifier().equals(idLong))
                return assetAdministrationShell;
        }
        return null;
    }

    private Submodel filterSubmodel(List<Submodel> submodels, String idLong) {
        for(Submodel submodel : submodels) {
            if(submodel.getIdentification().getIdentifier().equals(idLong))
                return submodel;
        }
        return null;
    }

    private SubmodelElement filterSubmodelElement(List<SubmodelElement> submodelElements, String idShort) {
        for(SubmodelElement submodelElement : submodelElements) {
            if(submodelElement.getIdShort().equals(idShort))
                return submodelElement;
        }
        return null;
    }
}
