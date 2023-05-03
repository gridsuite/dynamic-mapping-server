/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.ModelVariableDefinition;
import org.gridsuite.mapping.server.utils.VariableType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_variable_definitions", indexes = {@Index(name = "model_variable_definitions_variable_set_name_index", columnList = "variable_set_name")})
public class ModelVariableDefinitionEntity implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "variable_definition_name")
    private String name;

    @Column(name = "type")
    @Enumerated
    private VariableType type;

    @Column(name = "unit")
    private String unit;

    @Column(name = "factor")
    private Double factor;

    // must exclude CascadeType.REMOVE to avoid unexpected cascade on delete a ModelVariableDefinitionEntity
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "models_model_variable_definitions",
            joinColumns = {@JoinColumn(name = "variable_definition_name")},
            inverseJoinColumns = {@JoinColumn(name = "model_name")}
    )
    private Set<ModelEntity> models;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "variable_set_name", foreignKey = @ForeignKey(name = "variable_set_variable_definition_fk"), updatable = false)
    private ModelVariableSetEntity variablesSet;

    public ModelVariableDefinitionEntity(ModelEntity model, ModelVariableSetEntity variablesSet, ModelVariableDefinition variableDefinition) {
        this(variableDefinition.getName(), variableDefinition.getType(), variableDefinition.getUnit(), variableDefinition.getFactor(), model != null ? new LinkedHashSet<>(Arrays.asList(model)) : new LinkedHashSet<>(), variablesSet, null, null);
    }

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private Date updatedDate;

}
