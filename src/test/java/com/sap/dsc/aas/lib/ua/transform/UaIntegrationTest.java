package com.sap.dsc.aas.lib.ua.transform;

import com.sap.dsc.aas.lib.TestUtils;
import com.sap.dsc.aas.lib.mapping.MappingSpecificationParser;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UaIntegrationTest {

    public static final String CONFIG_JSON = "src/test/resources/ua/uaIntegrationTest.json";
    public static final String UA_INPUT = "src/test/resources/ua/big.machine.nodeset.xml";

    private static AssetAdministrationShellEnvironment shellEnv;

    @BeforeEach
    protected void setUp() throws Exception {
        TestUtils.resetBindings();
        InputStream uaInputStream = Files.newInputStream(Paths.get(UA_INPUT));
        UANodeSetTransformer uaTransformer = new UANodeSetTransformer();
        MappingSpecification mapping = new MappingSpecificationParser().loadMappingSpecification(CONFIG_JSON);
        shellEnv = uaTransformer.transform(uaInputStream, mapping);
    }

    @Test
    void testFull() throws Exception {
        System.out.println(shellEnv.getSubmodels().size());
        System.out.println(getSubmodel("E-mice"));
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