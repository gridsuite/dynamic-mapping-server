/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_sets_group", indexes = {@Index(name = "model_sets_group_name_index", columnList = "name")})
public class ModelSetsGroupEntity implements Serializable {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelParameterSetEntity> sets = new ArrayList<>(0);

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SetGroupType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_sets_group_model_id_fk"))
    private ModelEntity model;

    public ModelSetsGroupEntity(ModelEntity model, ParametersSetsGroup group) {
        this.id = group.getId() == null ? UUID.randomUUID() : group.getId();
        this.model = model;
        this.name = group.getName();
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
