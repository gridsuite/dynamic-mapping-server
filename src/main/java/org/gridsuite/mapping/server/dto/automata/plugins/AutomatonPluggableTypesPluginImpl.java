/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.mapping.server.common.plugins.AbstractPluggableTypesPlugin;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.plugins.automaton.AutomatonPluggableTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Component
public class AutomatonPluggableTypesPluginImpl
        extends AbstractPluggableTypesPlugin<AbstractAutomaton, AutomatonEntity>
        implements AutomatonPluggableTypesPlugin {

    @Autowired
    public AutomatonPluggableTypesPluginImpl(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public Map<String, Class<?>> getPluggableTypes() {
        return AutomatonPluggableTypes.TYPES;
    }

    @Override
    public AbstractAutomaton fromEntity(AutomatonEntity automatonEntity) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> dtoClass = AutomatonPluggableTypes.TYPES.get(automatonEntity.getFamily().name());
        Constructor<?> constructor = dtoClass.getConstructor(AutomatonEntity.class);

        return (AbstractAutomaton) constructor.newInstance(automatonEntity);
    }
}
