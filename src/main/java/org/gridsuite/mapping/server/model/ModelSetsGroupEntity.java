/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.utils.SetGroupType;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_sets_group", indexes = {@Index(name = "model_sets_group_model_name_index", columnList = "model_name")})
@IdClass(ModelSetsGroupId.class)
public class ModelSetsGroupEntity implements Serializable {

    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "model_name")
    private String modelName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelParameterSetEntity> sets = new ArrayList<>(0);

    @Id
    @Column(name = "type")
    private SetGroupType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_name", foreignKey = @ForeignKey(name = "model_sets_groups_fk"))
    @MapsId("modelName")
    private ModelEntity model;

    public ModelSetsGroupEntity(ModelEntity model, ParametersSetsGroup group) {
        this.model = model;
        this.name = group.getName();
        this.modelName = model.getModelName();
        this.type = group.getType();
        this.sets = group.getSets().stream().map(set -> new ModelParameterSetEntity(this, set)).collect(Collectors.toList());
    }

    // --- utils methods --- //

    /**
     * Add a parameter set into the set group
     * @param parameterSet given parameter set
     */
    public void addParameterSet(ModelParameterSetEntity parameterSet) {
        this.sets.add(parameterSet);
    }

    /**
     * Remove a parameter set from a set group
     * @param parameterSet given parameter set
     */
    public void removeParameterSet(ModelParameterSetEntity parameterSet) {
        this.sets.remove(parameterSet);
    }
}
