package org.gridsuite.mapping.server.common.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.*;

public abstract class PluggableTypeRegistrationModule<D, E> extends SimpleModule {

    protected PluggableTypeRegistrationModule(ObjectMapper objectMapper) {
        super();
        registerTypes(objectMapper);
    }

    public abstract PluggableTypesProvider<D, E> getPluggableTypesProvider();

    private void registerTypes(ObjectMapper objectMapper) {

        // Create a list to hold all the subtypes
        List<NamedType> pluggableTypes = new ArrayList<>();

        // Add the additional subtype
        getPluggableTypesProvider()
                .getPluggableTypes()
                .forEach((k, v) -> pluggableTypes.add(new NamedType(v, k)));

        // Register the merged subtypes with the ObjectMapper
        objectMapper
                .getSubtypeResolver()
                .registerSubtypes(pluggableTypes.toArray(new NamedType[0]));
    }
}
