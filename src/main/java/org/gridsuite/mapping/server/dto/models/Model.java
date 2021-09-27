/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.model.ModelEntity;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParameterOrigin;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@AllArgsConstructor
public class Model {

    private String modelName;

    private EquipmentType equipmentType;

    private List<ModelParameterDefinition> parameterDefinitions;

    private List<ParametersSetsGroup> setsGroups;

    public Model(ModelEntity modelEntity) {
        modelName = modelEntity.getModelName();
        equipmentType = modelEntity.getEquipmentType();
        parameterDefinitions = modelEntity.getParameterDefinitions().stream().map(ModelParameterDefinition::new).collect(Collectors.toList());
        setsGroups = modelEntity.getSetsGroups().stream().map(ParametersSetsGroup::new).collect(Collectors.toList());
    }

    public boolean isParameterSetGroupValid(String groupName, boolean strict) {
        ParametersSetsGroup groupToTest = setsGroups.stream().filter(group -> group.getName().equals(groupName)).findAny().orElse(null);
        if (groupToTest == null) {
            return false;
        } else {
            AtomicBoolean isValid = new AtomicBoolean(true);
            List<ParametersSet> sets = groupToTest.getSets();
            for (ParametersSet set : sets) {
                if (isValid.get()) {
                    isValid.set(isParameterSetValid(set));
                }
            }
            return !(strict && sets.isEmpty()) && isValid.get();
        }
    }

    public boolean isParameterSetValid(ParametersSet setToTest) {
        AtomicBoolean isValid = new AtomicBoolean(true);
        List<ModelParameter> parameters = setToTest.getParameters();
        parameterDefinitions.stream().filter(definition -> ParameterOrigin.USER.equals(definition.getOrigin())).forEach(definition -> {
            if (isValid.get()) {
                isValid.set(parameters.stream().filter(param -> param.getName().equals(definition.getName())).findAny().orElse(null) != null);
            }
        });
        return isValid.get();
    }
}


