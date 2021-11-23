package com.sap.dsc.aas.lib.transform.postprocessor;

import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PostProcessor which adds each {@link Submodel} as a {@link Reference} to each {@link AssetAdministrationShell} of the same {@link
 * AssetAdministrationShellEnvironment}
 *
 * @author br-iosb
 */
public class AutoWireSubmodels implements Consumer<AssetAdministrationShellEnvironment> {

    @Override
    public void accept(AssetAdministrationShellEnvironment t) {
        ArrayList<Reference> smRefs = t.getSubmodels().stream()
            .flatMap(submodel -> submodel.getIdentification() == null ? Stream.empty() :
                Stream.of(createReference(submodel.getIdentification().getIdentifier(), KeyElements.SUBMODEL, KeyType.CUSTOM)))
            .collect(Collectors.toCollection(ArrayList::new));
        t.getAssetAdministrationShells().forEach(aas -> aas.getSubmodels().addAll(smRefs));
    }

    protected Reference createReference(String value, KeyElements keyElement, KeyType keyType) {
        Key key = new DefaultKey.Builder().idType(keyType).type(keyElement).value(value).build();

        return new DefaultReference.Builder().keys(Arrays.asList(key)).build();
    }
}
