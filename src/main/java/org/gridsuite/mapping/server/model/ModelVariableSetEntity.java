/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.model;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.mapping.server.dto.models.VariablesSet;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.persistence.TemporalType.TIMESTAMP;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_variable_set")
public class ModelVariableSetEntity implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "model_variable_set_model_variable_definition",
            joinColumns = {@JoinColumn(name = "variable_set_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_variable_set_model_variable_definition_variable_set_id_fk"))},
            inverseJoinColumns = {@JoinColumn(name = "variable_definition_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_variable_set_model_variable_definition_variable_definition_id_fk"))}
    )
    private Set<ModelVariableDefinitionEntity> variableDefinitions = new LinkedHashSet<>(0);

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
        name = "model_model_variable_set",
        joinColumns = {@JoinColumn(name = "variable_set_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_model_variable_set_variable_set_id_fk"))},
        inverseJoinColumns = {@JoinColumn(name = "model_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_model_variable_set_model_id_fk"))}
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
        this.id = variablesSet.getId() == null ? UUID.randomUUID() : variablesSet.getId();
        this.models = model != null ? new LinkedHashSet<>(List.of(model)) : new LinkedHashSet<>();
        this.name = variablesSet.getName();
        this.variableDefinitions = variablesSet.getVariableDefinitions().stream().map(variableDefinition -> new ModelVariableDefinitionEntity(model, this, variableDefinition)).collect(Collectors.toSet());
    }

    // --- utils methods --- //
    /**
     * Add all the variable definitions in the given collection to relation with the variable set
     * @param variableDefinitions collection containing variable definitions to be added
     */
    public void addAllVariableDefinition(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getVariablesSets().add(this));
        this.variableDefinitions.addAll(variableDefinitions);
    }

    /**
     * Remove all the variable definitions in the given collection from relation with the variable set
     * @param variableDefinitions collection containing variable definitions to be removed
     */
    public void removeAllVariableDefinition(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getVariablesSets().remove(this));
        this.variableDefinitions.removeAll(variableDefinitions);
    }
}
