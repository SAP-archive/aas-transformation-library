package com.sap.dsc.aas.lib.transform.postprocessor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;

class AutoWireSubmodelsTest {

	@Test
	void testAccept() {
		DefaultAssetAdministrationShellEnvironment env = new DefaultAssetAdministrationShellEnvironment();
		DefaultSubmodel defaultSubmodel = new DefaultSubmodel();
		defaultSubmodel.setIdentification(new DefaultIdentifier.Builder().identifier("mySubmodel").build());
		DefaultAssetAdministrationShell defaultAssetAdministrationShell = new DefaultAssetAdministrationShell();
		env.getSubmodels().add(defaultSubmodel);
		env.getAssetAdministrationShells().add(defaultAssetAdministrationShell);

		new AutoWireSubmodels().accept(env);

		Assertions.assertEquals("mySubmodel",
				env.getAssetAdministrationShells().get(0).getSubmodels().get(0).getKeys().get(0).getValue());
	}

}
