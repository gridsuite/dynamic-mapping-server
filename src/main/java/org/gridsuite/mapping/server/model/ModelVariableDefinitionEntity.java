/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.models.ModelVariableDefinition;
import org.gridsuite.mapping.server.utils.VariableType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_variable_definitions", indexes = {@Index(name = "model_variable_definitions_variable_set_name_index", columnList = "variable_set_name")})
@IdClass(ModelVariableDefinitionId.class)
public class ModelVariableDefinitionEntity implements Serializable {
    @Id
    @Column(name = "variable_definition_name")
    private String name;

    @Column(name = "variable_set_name")
    private String variableSetName;

    @Column(name = "type")
    @Enumerated
    private VariableType type;

    @Column(name = "unit")
    private String unit;

    @Column(name = "factor")
    private Double factor;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "models_model_variable_definitions",
            joinColumns = {@JoinColumn(name = "variable_definition_name")},
            inverseJoinColumns = {@JoinColumn(name = "model_name")}
    )
    private List<ModelEntity> models;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variable_set_name", foreignKey = @ForeignKey(name = "variable_set_variable_definition_fk"), insertable = false, updatable = false)
    private ModelVariableSetEntity variablesSet;

    public ModelVariableDefinitionEntity(List<ModelEntity> models, ModelVariableSetEntity variablesSet, ModelVariableDefinition variableDefinition) {
        this(variableDefinition.getName(), variablesSet != null ? variablesSet.getName() : null, variableDefinition.getType(), variableDefinition.getUnit(), variableDefinition.getFactor(), models, variablesSet);
    }
}
