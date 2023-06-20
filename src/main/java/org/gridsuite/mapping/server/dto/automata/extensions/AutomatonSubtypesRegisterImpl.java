/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.gridsuite.mapping.server.common.extensions.AbstractSubtypesRegister;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.dto.automata.BasicProperty;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.AutomatonPropertyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Component
public class AutomatonSubtypesRegisterImpl
        extends AbstractSubtypesRegister<AbstractAutomaton, AutomatonEntity>
        implements AutomatonSubtypesRegister {

    private final Map<String, Class<?>> types = new HashMap<>();

    @Autowired
    public AutomatonSubtypesRegisterImpl(ObjectMapper objectMapper) {

        super(objectMapper);

        // load pluggable types
        List<AutomatonSubtypesExtension> automatonSubtypesExtensions =
                Lists.newArrayList(ServiceLoader.load(AutomatonSubtypesExtension.class, AutomatonSubtypesExtension.class.getClassLoader()));
        automatonSubtypesExtensions.stream()
                .filter(cls -> cls.getClass().isAnnotationPresent(AutomatonSubtypes.class))
                .forEach(cls -> {
                    AutomatonSubtypes annotation = cls.getClass().getAnnotation(AutomatonSubtypes.class);
                    AutomatonSubtypes.Type[] subtypes = annotation.value();
                    Arrays.stream(subtypes).forEach(type -> types.put(type.name(), type.value()));
                });

    }

    public Map<String, Class<?>> getSubtypes() {
        return types;
    }

    @Override
    public AbstractAutomaton fromEntity(AutomatonEntity automatonEntity) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> dtoClass = getSubtypes().get(automatonEntity.getModel());
        Constructor<?> constructor = dtoClass.getConstructor();
        AbstractAutomaton abstractAutomaton = (AbstractAutomaton) constructor.newInstance();

        // build automaton common fields
        abstractAutomaton.setFamily(automatonEntity.getFamily());
        abstractAutomaton.setModel(automatonEntity.getModel());
        abstractAutomaton.setSetGroup(automatonEntity.getSetGroup());

        // build automaton particular fields
        abstractAutomaton.fromProperties(automatonEntity.getProperties().stream()
                .map(elem -> new BasicProperty(elem.getName(), elem.getValue(), elem.getType()))
                .collect(Collectors.toList()));

        return abstractAutomaton;
    }

    @Override
    public AutomatonEntity toEntity(AbstractAutomaton dto) {
        AutomatonEntity automatonEntity = new AutomatonEntity(dto);
        List<BasicProperty> properties = dto.toProperties();

        properties.forEach(elem -> automatonEntity.addProperty(new AutomatonPropertyEntity(automatonEntity.getAutomatonId(),
                elem.getName(), elem.getValue(), elem.getType(), automatonEntity)));

        return automatonEntity;
    }
}
