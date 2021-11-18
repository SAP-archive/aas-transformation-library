package com.sap.dsc.aas.lib.transform.postprocessor;

import java.util.function.Consumer;

import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;

/**
 * PostProcessor which adds each {@link Submodel} as a {@link Reference} to each
 * {@link AssetAdministrationShell} of the same
 * {@link AssetAdministrationShellEnvironment}
 * 
 * @author br-iosb
 *
 */
public class AutoWireSubmodels implements Consumer<AssetAdministrationShellEnvironment> {

	@Override
	public void accept(AssetAdministrationShellEnvironment t) {
		// TODO Auto-generated method stub
	}

}
