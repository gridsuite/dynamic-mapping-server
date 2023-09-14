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
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "models")
public class ModelEntity implements Serializable {

    // Could be replaced with UUID, but we lose the ease of use of names
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "name")
    private String modelName;

    @Column(name = "equipment_type")
    private EquipmentType equipmentType;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelModelParameterDefinitionEntity> parameterDefinitions = new ArrayList<>();

    @OneToMany(targetEntity = ModelSetsGroupEntity.class, mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelSetsGroupEntity> setsGroups = new ArrayList<>(0);

    // must exclude CascadeType.REMOVE to avoid unexpected cascade on delete a ModelVariableDefinitionEntity
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "models_model_variable_definitions",
            joinColumns = {@JoinColumn(name = "model_name")},
            inverseJoinColumns = {@JoinColumn(name = "variable_definition_name")}
    )
    private Set<ModelVariableDefinitionEntity> variableDefinitions = new LinkedHashSet<>(0);

    @ManyToMany(targetEntity = ModelVariableSetEntity.class, mappedBy = "models", cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<ModelVariableSetEntity> variableSets = new LinkedHashSet<>(0);

    public ModelEntity(Model modelToConvert) {
        modelName = modelToConvert.getModelName();
        equipmentType = modelToConvert.getEquipmentType();
        if (modelToConvert.getParameterDefinitions() != null) {
            modelToConvert.getParameterDefinitions().forEach(parameterDefinition -> this.addParameterDefinition(new ModelParameterDefinitionEntity(parameterDefinition), parameterDefinition.getOrigin()));
        }
        setsGroups = modelToConvert.getSetsGroups() != null ? modelToConvert.getSetsGroups().stream().map(group -> new ModelSetsGroupEntity(this, group)).collect(Collectors.toList()) : null;
        variableDefinitions = modelToConvert.getVariableDefinitions() != null ? modelToConvert.getVariableDefinitions().stream().map(variableDefinition -> new ModelVariableDefinitionEntity(this, null, variableDefinition)).collect(Collectors.toCollection(LinkedHashSet::new)) : null;
        variableSets = modelToConvert.getVariablesSets() != null ? modelToConvert.getVariablesSets().stream().map(variablesSet -> new ModelVariableSetEntity(this, variablesSet)).collect(Collectors.toCollection(LinkedHashSet::new)) : null;
    }

    @CreatedDate
    @Temporal(TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @LastModifiedDate
    @Temporal(TIMESTAMP)
    @Column(name = "updated_date")
    private Date updatedDate;

    // --- utils methods --- //

    public void addParameterDefinition(ModelParameterDefinitionEntity parameterDefinition, ParameterOrigin origin) {
        ModelModelParameterDefinitionEntity modelModelParameterDefinitionEntity = new ModelModelParameterDefinitionEntity(this, parameterDefinition, origin);
        this.parameterDefinitions.add(modelModelParameterDefinitionEntity);
    }

    public void addParameterDefinitions(Collection<ModelParameterDefinitionEntity> parameterDefinitions, ParameterOrigin origin) {
        parameterDefinitions.forEach(parameterDefinition -> addParameterDefinition(parameterDefinition, origin));
    }

    public void removeParameterDefinition(ModelParameterDefinitionEntity parameterDefinition) {
        for (Iterator<ModelModelParameterDefinitionEntity> iter = this.parameterDefinitions.iterator(); iter.hasNext();) {
            ModelModelParameterDefinitionEntity modelModelParameterDefinitionEntity = iter.next();
            if (modelModelParameterDefinitionEntity.getModel().equals(this) && modelModelParameterDefinitionEntity.getParameterDefinition().equals(parameterDefinition)) {
                iter.remove();
                modelModelParameterDefinitionEntity.setModel(null);
                modelModelParameterDefinitionEntity.setParameterDefinition(null);
            }
        }
    }

    public void removeParameterDefinitions(Collection<ModelParameterDefinitionEntity> parameterDefinitions) {
        parameterDefinitions.forEach(this::removeParameterDefinition);
    }

    public void addVariableDefinitions(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getModels().add(this));
        this.variableDefinitions.addAll(variableDefinitions);
    }

    public void removeVariableDefinitions(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getModels().remove(this));
        this.variableDefinitions.removeAll(variableDefinitions);
    }

    public void addVariablesSets(Collection<ModelVariableSetEntity> variablesSets) {
        variablesSets.forEach(variablesSet -> variablesSet.getModels().add(this));
        this.variableSets.addAll(variablesSets);
    }

    public void removeVariablesSets(Collection<ModelVariableSetEntity> variablesSets) {
        variablesSets.forEach(variablesSet -> variablesSet.getModels().remove(this));
        this.variableSets.removeAll(variablesSets);
    }

}
