package org.gridsuite.mapping.server.plugins.automaton;

import org.gridsuite.mapping.server.common.plugins.PluggableTypesProvider;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.model.AutomatonEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class AutomatonPluggableTypesProvider implements PluggableTypesProvider<AbstractAutomaton, AutomatonEntity> {

    private final Map<String, Class<?>> pluggableTypes = Map.of(
            "VOLTAGE", TapChangerBlockingAutomaton.class
    );

    @Override
    public Map<String, Class<?>> getPluggableTypes() {
        return pluggableTypes;
    }

    @Override
    public AbstractAutomaton fromEntity(AutomatonEntity automatonEntity) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> dtoClass = pluggableTypes.get(automatonEntity.getFamily().name());
        Constructor<?> constructor = dtoClass.getConstructor(AutomatonEntity.class);

        return (AbstractAutomaton) constructor.newInstance(automatonEntity);
    }
}
