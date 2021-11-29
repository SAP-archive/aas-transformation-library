package com.sap.dsc.aas.lib.ua.transform;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.mapping.MappingSpecificationParser;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import io.adminshell.aas.v3.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UaIntegrationTest {

    public static final String INTEGRATION_CONFIG = "src/test/resources/ua/uaIntegrationTest.json";
    public static final String UA_BIG_MACHINE = "src/test/resources/ua/big.machine.nodeset.xml";
    public static final String NAMEPLATE_CONFIG = "src/test/resources/ua/diToNameplate.json";


    private static AssetAdministrationShellEnvironment shellEnv;

    @BeforeEach
    protected void setUp() throws Exception {
        TestUtils.resetBindings();
    }

    @Test
    void testIntegration() throws Exception {
        InputStream uaInputStream = Files.newInputStream(Paths.get(UA_BIG_MACHINE));
        UANodeSetTransformer uaTransformer = new UANodeSetTransformer();
        MappingSpecification mapping = new MappingSpecificationParser().loadMappingSpecification(INTEGRATION_CONFIG);
        shellEnv = uaTransformer.transform(uaInputStream, mapping);
        boolean idInEnv = shellEnv.getSubmodels().stream().map(s -> s.getIdentification().getIdentifier())
                .collect(Collectors.toList()).contains("http://exp.organization.com/UA/BigMachine/ns=4;i=1281");
        assertTrue(idInEnv);
    }

    @Test
    void testUaNameplate() throws Exception {
        InputStream uaInputStream = Files.newInputStream(Paths.get(UA_BIG_MACHINE));
        UANodeSetTransformer uaTransformer = new UANodeSetTransformer();
        MappingSpecification mapping = new MappingSpecificationParser().loadMappingSpecification(NAMEPLATE_CONFIG);
        shellEnv = uaTransformer.transform(uaInputStream, mapping);
        Submodel np = getSubmodel("Nameplate");
        assertEquals(5, np.getSubmodelElements().size());
        SubmodelElementCollection address = (SubmodelElementCollection) getElement("Address", np);
        address.getValues().stream().map(Referable::getIdShort)
                .collect(Collectors.toList()).forEach(Assertions::assertNotNull);
        assertTrue(shellEnv.getAssetAdministrationShells().size() > 0);
        shellEnv.getAssetAdministrationShells().get(0).getSubmodels().forEach(sm ->
                assertTrue(sm.getKeys().size() > 0));
    }


    SubmodelElement getElement(String idShort, Submodel submodel) {
        assertThat(submodel.getSubmodelElements()).isNotNull();

        return submodel.getSubmodelElements().stream()
                .filter(submodelElement -> submodelElement.getIdShort().equals(idShort))
                .findFirst()
                .orElseThrow(() -> new AssertionFailedError("SubmodelElement with IdShort '" + idShort + "' not found"));
    }

    Submodel getSubmodel(String idShort) {
        assertNotNull(shellEnv.getSubmodels());

        return shellEnv.getSubmodels().stream()
                .filter(submodel -> submodel.getIdShort().equals(idShort))
                .findFirst()
                .orElseThrow(() -> new AssertionFailedError("Submodel with IdShort '" + idShort + "' not found"));
    }
}