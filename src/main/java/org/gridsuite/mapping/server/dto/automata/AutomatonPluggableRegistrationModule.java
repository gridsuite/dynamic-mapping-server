package org.gridsuite.mapping.server.dto.automata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.mapping.server.common.plugins.PluggableTypesProvider;
import org.gridsuite.mapping.server.common.plugins.PluggableTypeRegistrationModule;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.plugins.automaton.AutomatonPluggableTypesProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AutomatonPluggableRegistrationModule extends PluggableTypeRegistrationModule<AbstractAutomaton, AutomatonEntity> {

    @Autowired
    public AutomatonPluggableRegistrationModule(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public PluggableTypesProvider<AbstractAutomaton, AutomatonEntity> getPluggableTypesProvider() {
        return new AutomatonPluggableTypesProvider();
    }
}
