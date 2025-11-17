/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.persistence.TemporalType.TIMESTAMP;

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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private UUID id;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "equipment_type")
    private EquipmentType equipmentType;

    @Column(name = "default_model", columnDefinition = "boolean default false")
    private boolean defaultModel;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelModelParameterDefinitionEntity> parameterDefinitions = new ArrayList<>();

    @OneToMany(targetEntity = ModelSetsGroupEntity.class, mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelSetsGroupEntity> setsGroups = new ArrayList<>(0);

    // must exclude CascadeType.REMOVE to avoid unexpected cascade on delete a ModelVariableDefinitionEntity
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "models_model_variable_definitions",
            joinColumns = {@JoinColumn(name = "model_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "variable_definition_id", referencedColumnName = "id")}
    )
    private Set<ModelVariableDefinitionEntity> variableDefinitions = LinkedHashSet.newLinkedHashSet(0);

    @ManyToMany(targetEntity = ModelVariableSetEntity.class, mappedBy = "models", cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<ModelVariableSetEntity> variableSets = LinkedHashSet.newLinkedHashSet(0);

    public ModelEntity(Model modelToConvert) {
        id = modelToConvert.getId() == null ? UUID.randomUUID() : modelToConvert.getId();
        modelName = modelToConvert.getModelName();
        equipmentType = modelToConvert.getEquipmentType();
        defaultModel = modelToConvert.isDefaultModel();
        if (modelToConvert.getParameterDefinitions() != null) {
            modelToConvert.getParameterDefinitions().forEach(parameterDefinition ->
                    this.addParameterDefinition(new ModelParameterDefinitionEntity(parameterDefinition), parameterDefinition.getOrigin(), parameterDefinition.getOriginName()));
        }
        setsGroups = modelToConvert.getSetsGroups() != null ? modelToConvert.getSetsGroups().stream()
                .map(group -> new ModelSetsGroupEntity(this, group)).collect(Collectors.toList()) : null;
        variableDefinitions = modelToConvert.getVariableDefinitions() != null ? modelToConvert.getVariableDefinitions().stream()
                .map(variableDefinition -> new ModelVariableDefinitionEntity(this, null, variableDefinition))
                .collect(Collectors.toCollection(LinkedHashSet::new)) : null;
        variableSets = modelToConvert.getVariablesSets() != null ? modelToConvert.getVariablesSets().stream()
                .map(variablesSet -> new ModelVariableSetEntity(this, variablesSet)).collect(Collectors.toCollection(LinkedHashSet::new)) : null;
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

    /**
     * Add a parameter definition to the relation with the model
     * @param parameterDefinition given parameter definition to be added
     * @param origin given origin which is the extra information of the relation
     * @param originName given originName which is the extra information of the relation
     */
    public void addParameterDefinition(ModelParameterDefinitionEntity parameterDefinition, ParameterOrigin origin, String originName) {
        ModelModelParameterDefinitionEntity modelModelParameterDefinitionEntity = new ModelModelParameterDefinitionEntity(this, parameterDefinition, origin, originName);
        parameterDefinition.getModels().add(modelModelParameterDefinitionEntity);
        this.parameterDefinitions.add(modelModelParameterDefinitionEntity);
    }

    /**
     * Add all the parameter definitions in the given collection to relation with the model
     * @param parameterDefinitions collection containing parameter definitions to be added
     * @param origin given origin which is the extra information of relations
     * @param originName given origin which is the extra information of relations
     */
    public void addAllParameterDefinition(Collection<ModelParameterDefinitionEntity> parameterDefinitions, ParameterOrigin origin, String originName) {
        parameterDefinitions.forEach(parameterDefinition -> addParameterDefinition(parameterDefinition, origin, originName));
    }

    /**
     * Remove a parameter definition from the relation with the model
     * @param parameterDefinition given parameter definition to be removed from the relation, if present
     */
    public void removeParameterDefinition(ModelParameterDefinitionEntity parameterDefinition) {
        for (Iterator<ModelModelParameterDefinitionEntity> iter = this.parameterDefinitions.iterator(); iter.hasNext();) {
            ModelModelParameterDefinitionEntity modelModelParameterDefinitionEntity = iter.next();
            if (modelModelParameterDefinitionEntity.getModel().equals(this) && modelModelParameterDefinitionEntity.getParameterDefinition().equals(parameterDefinition)) {
                iter.remove();
                modelModelParameterDefinitionEntity.setModel(null);
                modelModelParameterDefinitionEntity.setParameterDefinition(null);
                parameterDefinition.getModels().remove(modelModelParameterDefinitionEntity);
                break;
            }
        }
    }

    /**
     * Remove all the parameter definitions in the given collection from relations with the model
     * @param parameterDefinitions collection containing parameter definitions to be removed
     */
    public void removeAllParameterDefinition(Collection<ModelParameterDefinitionEntity> parameterDefinitions) {
        parameterDefinitions.forEach(this::removeParameterDefinition);
    }

    /**
     * Add all the variable definitions in the given collection to relation with the model
     * @param variableDefinitions collection containing variable definitions to be added
     */
    public void addAllVariableDefinition(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getModels().add(this));
        this.variableDefinitions.addAll(variableDefinitions);
    }

    /**
     * Remove all the variable definitions in the given collection from relation with the model
     * @param variableDefinitions collection containing variable definitions to be removed
     */
    public void removeAllVariableDefinition(Collection<ModelVariableDefinitionEntity> variableDefinitions) {
        variableDefinitions.forEach(variableDefinition -> variableDefinition.getModels().remove(this));
        this.variableDefinitions.removeAll(variableDefinitions);
    }

    /**
     * Add all the variable set in the given collection to relation with the model
     * @param variablesSets collection containing variable sets to be added
     */
    public void addAllVariablesSet(Collection<ModelVariableSetEntity> variablesSets) {
        variablesSets.forEach(variablesSet -> variablesSet.getModels().add(this));
        this.variableSets.addAll(variablesSets);
    }

    /**
     * Remove all the variable set in the given collection from relation with the model
     * @param variablesSets collection containing variable sets to be removed
     */
    public void removeAllVariablesSet(Collection<ModelVariableSetEntity> variablesSets) {
        variablesSets.forEach(variablesSet -> variablesSet.getModels().remove(this));
        this.variableSets.removeAll(variablesSets);
    }

}
