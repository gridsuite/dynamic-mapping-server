/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.VariablesSet;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_variable_sets")
public class ModelVariableSetEntity implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "variable_set_name")
    private String name;

    @ManyToMany(targetEntity = ModelVariableDefinitionEntity.class, mappedBy = "variablesSets", cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<ModelVariableDefinitionEntity> variableDefinitions = new LinkedHashSet<>(0);

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
        name = "models_model_variable_sets",
        joinColumns = {@JoinColumn(name = "variable_set_name")},
        inverseJoinColumns = {@JoinColumn(name = "model_name")}
    )
    private Set<ModelEntity> models;

    @CreatedDate
    @Temporal(TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @LastModifiedDate
    @Temporal(TIMESTAMP)
    @Column(name = "updated_date")
    private Date updatedDate;

    public ModelVariableSetEntity(ModelEntity model, VariablesSet variablesSet) {
        this.models = model != null ? new LinkedHashSet<>(Arrays.asList(model)) : new LinkedHashSet<>();
        this.name = variablesSet.getName();
        this.variableDefinitions = variablesSet.getVariableDefinitions().stream().map(variableDefinition -> new ModelVariableDefinitionEntity(model, this, variableDefinition)).collect(Collectors.toSet());
    }

    // --- utils methods --- //
    public void addVariableDefinitions(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getVariablesSets().add(this));
        this.variableDefinitions.addAll(variableDefinitions);
    }

    public void removeVariableDefinitions(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getVariablesSets().remove(this));
        this.variableDefinitions.removeAll(variableDefinitions);
    }
}
