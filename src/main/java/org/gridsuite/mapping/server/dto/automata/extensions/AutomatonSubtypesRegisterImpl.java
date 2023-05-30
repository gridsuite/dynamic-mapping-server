/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.gridsuite.mapping.server.common.extensions.AbstractSubtypesRegister;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.AutomatonPropertyEntity;
import org.gridsuite.mapping.server.utils.AttributeConverter;
import org.gridsuite.mapping.server.utils.PropertyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    private final ApplicationContext context;

    private final Map<String, Class<?>> types = new HashMap<>();

    @Autowired
    public AutomatonSubtypesRegisterImpl(ObjectMapper objectMapper, ApplicationContext context) {

        super(objectMapper);

        this.context = context;

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
        Class<?> dtoClass = getSubtypes().get(automatonEntity.getFamily().name());
        Constructor<?> constructor = dtoClass.getConstructor();
        AbstractAutomaton abstractAutomaton = (AbstractAutomaton) constructor.newInstance();

        // enrich non-meta attributes
        List<Field> entityPropertyFields = FieldUtils.getFieldsListWithAnnotation(dtoClass, EntityProperty.class);
        List<Field> nonMetaFields = entityPropertyFields.stream().filter(field -> !field.getAnnotation(EntityProperty.class).meta()).collect(Collectors.toList());

        for (Field dtoField : nonMetaFields) {
            EntityProperty entityPropertyAnnotation = dtoField.getAnnotation(EntityProperty.class);
            String propertyEntityName = entityPropertyAnnotation.value();
            if (StringUtils.isBlank(propertyEntityName)) {
                propertyEntityName = dtoField.getName();
            }

            Field entityField = FieldUtils.getDeclaredField(AutomatonEntity.class, propertyEntityName, true);
            entityField.setAccessible(true);
            Object value = entityField.get(automatonEntity);

            dtoField.setAccessible(true);
            dtoField.set(abstractAutomaton, value);
        }

        // enrich meta attributes
        List<Field> metaFields = entityPropertyFields.stream().filter(field -> field.getAnnotation(EntityProperty.class).meta()).collect(Collectors.toList());
        for (Field field : metaFields) {
            EntityProperty entityPropertyAnnotation = field.getAnnotation(EntityProperty.class);
            String propertyEntityName = entityPropertyAnnotation.value();
            String propertyEntityValue = automatonEntity.getProperty(propertyEntityName);
            Object convertedValue = propertyEntityValue;

            // try to convert value if converter provided
            try {
                var converter = entityPropertyAnnotation.converter();
                AttributeConverter converterBean = (AttributeConverter) context.getBean(converter);
                convertedValue = converterBean.toDtoAttribute(propertyEntityValue);
            } catch (Exception e) {
                // do nothing
            }

            field.setAccessible(true);
            field.set(abstractAutomaton, convertedValue);
        }

        return abstractAutomaton;
    }

    @Override
    public AutomatonEntity toEntity(AbstractAutomaton dto) throws IllegalAccessException {
        UUID createdId = UUID.randomUUID();
        AutomatonEntity automatonEntity = new AutomatonEntity();

        // enrich non-meta attributes
        automatonEntity.setAutomatonId(createdId);
        automatonEntity.setFamily(dto.getFamily());
        automatonEntity.setModel(dto.getModel());
        automatonEntity.setSetGroup(dto.getSetGroup());

        Class<?> dtoClass = dto.getClass();
        List<Field> entityPropertyFields = FieldUtils.getFieldsListWithAnnotation(dtoClass, EntityProperty.class);
        List<Field> nonMetaFields = entityPropertyFields.stream().filter(field -> !field.getAnnotation(EntityProperty.class).meta()).collect(Collectors.toList());

        for (Field dtoField : nonMetaFields) {
            EntityProperty entityPropertyAnnotation = dtoField.getAnnotation(EntityProperty.class);
            String propertyEntityName = entityPropertyAnnotation.value();
            if (StringUtils.isBlank(propertyEntityName)) {
                propertyEntityName = dtoField.getName();
            }

            Field entityField = FieldUtils.getDeclaredField(AutomatonEntity.class, propertyEntityName, true);
            dtoField.setAccessible(true);
            Object value = dtoField.get(dto);

            entityField.setAccessible(true);
            entityField.set(automatonEntity, value);
        }

        // enrich meta attributes
        List<Field> metaFields = entityPropertyFields.stream().filter(field -> field.getAnnotation(EntityProperty.class).meta()).collect(Collectors.toList());
        for (Field field : metaFields) {
            EntityProperty entityPropertyAnnotation = field.getAnnotation(EntityProperty.class);
            String propertyEntityName = entityPropertyAnnotation.value();
            field.setAccessible(true);
            Object value = field.get(dto);
            var convertedValue = value;

            try {
                var converter = entityPropertyAnnotation.converter();
                AttributeConverter converterBean = (AttributeConverter) context.getBean(converter);
                convertedValue = converterBean.toEntityAttribute(value);
            } catch (Exception e) {
                // do nothing
            }

            automatonEntity.addProperty(new AutomatonPropertyEntity(automatonEntity.getAutomatonId(),
                    propertyEntityName, (String) convertedValue, PropertyType.STRING, automatonEntity));
        }

        return automatonEntity;
    }
}
