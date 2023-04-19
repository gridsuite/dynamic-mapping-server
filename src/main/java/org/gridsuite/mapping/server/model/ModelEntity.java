/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.utils.EquipmentType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Getter
@Setter
@Table(name = "models")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelEntity extends AbstractManuallyAssignedIdentifierEntity<String> implements Serializable {

    // Could be replaced with UUID, but we lose the ease of use of names
    @Id
    @Column(name = "model_name")
    private String modelName;

    @Column(name = "equipment_type")
    private EquipmentType equipmentType;

    @OneToMany(targetEntity = ModelParameterDefinitionEntity.class, mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelParameterDefinitionEntity> parameterDefinitions = new ArrayList<>(0);

    @OneToMany(targetEntity = ModelSetsGroupEntity.class, mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelSetsGroupEntity> setsGroups = new ArrayList<>(0);

    @ManyToMany(targetEntity = ModelVariableDefinitionEntity.class, mappedBy = "models")
    private List<ModelVariableDefinitionEntity> variableDefinitions = new ArrayList<>(0);

    @ManyToMany(targetEntity = ModelVariableSetEntity.class, mappedBy = "models")
    private List<ModelVariableSetEntity> variableSets = new ArrayList<>(0);

    @Override
    public String getId() {
        return modelName;
    }

    public ModelEntity(Model modelToConvert) {
        modelName = modelToConvert.getModelName();
        equipmentType = modelToConvert.getEquipmentType();
        parameterDefinitions = modelToConvert.getParameterDefinitions().stream().map(parameterDefinition -> new ModelParameterDefinitionEntity(parameterDefinition.getName(), modelToConvert.getModelName(), parameterDefinition.getType(), parameterDefinition.getOrigin(), parameterDefinition.getOriginName(), parameterDefinition.getFixedValue(), this)).collect(Collectors.toList());
        setsGroups = modelToConvert.getSetsGroups().stream().map(group -> new ModelSetsGroupEntity(this, group)).collect(Collectors.toList());
        variableDefinitions = modelToConvert.getVariableDefinitions().stream().map(variableDefinition -> new ModelVariableDefinitionEntity(List.of(this), null, variableDefinition)).collect(Collectors.toList());
        variableSets = modelToConvert.getVariablesSets().stream().map(variablesSet -> new ModelVariableSetEntity(List.of(this), variablesSet)).collect(Collectors.toList());
    }

}
