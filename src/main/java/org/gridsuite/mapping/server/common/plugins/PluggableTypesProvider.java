package org.gridsuite.mapping.server.common.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface PluggableTypesProvider<D, E> {
    Map<String, Class<?>> getPluggableTypes();

    D fromEntity(E entity) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
