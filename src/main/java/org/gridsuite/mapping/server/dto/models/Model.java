/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.models;

import lombok.Data;
import org.gridsuite.mapping.server.model.ModelEntity;
import org.gridsuite.mapping.server.utils.EquipmentType;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
public class Model {

    private String modelName;

    private EquipmentType equipmentType;

    private List<ModelParameterDefinition> parameterDefinitions;

    private List<ParametersSet> sets;

    public Model(ModelEntity modelEntity) {
        modelName = modelEntity.getModelName();
        equipmentType = modelEntity.getEquipmentType();
        parameterDefinitions = modelEntity.getParameterDefinitions().stream().map(parameterDefinitionEntity -> new ModelParameterDefinition(parameterDefinitionEntity)).collect(Collectors.toList());
        sets = modelEntity.getSets().stream().map(parametersSetEntity -> new ParametersSet(parametersSetEntity)).collect(Collectors.toList());
    }

    public ModelEntity convertToEntity() {
        return new ModelEntity(this);
    }

    public boolean isParameterSetValid(String setName) {
        ParametersSet setToTest = sets.stream().filter(set -> set.getName() == setName).findAny().orElse(null);
        if (setToTest == null) {
            return false;
        } else {
            AtomicBoolean isValid = new AtomicBoolean(true);
            List<ModelParameter> parameters = setToTest.getParameters();
            parameterDefinitions.forEach(definition -> {
                if (isValid.get()) {
                    isValid.set(parameters.stream().filter(param -> param.getName() == definition.getName()).findAny().orElse(null) != null);

                }
            });
            return isValid.get();
        }
    }
}
