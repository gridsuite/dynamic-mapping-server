/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.utils.SetGroupType;
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
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_sets_group", indexes = {@Index(name = "model_sets_group_model_name_index", columnList = "model_name")})
public class ModelSetsGroupEntity implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "name")
    private String name;

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "type")
    private SetGroupType type;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ModelParameterSetEntity> sets = new LinkedHashSet<>(0);

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "model_name", foreignKey = @ForeignKey(name = "model_sets_groups_fk"), updatable = false)
    private ModelEntity model;

    public ModelSetsGroupEntity(ModelEntity model, ParametersSetsGroup group) {
        this(group.getName(), group.getType(), null, model, null, null);
        this.sets = group.getSets().stream().map(set -> new ModelParameterSetEntity(this, set)).collect(Collectors.toSet());
    }

    @CreatedDate
    @Temporal(TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @LastModifiedDate
    @Temporal(TIMESTAMP)
    @Column(name = "updated_date")
    private Date updatedDate;

    public void addSets(Collection<ModelParameterSetEntity> parameterSets) {
        parameterSets.forEach(parameterSet -> parameterSet.setGroup(this));
        this.sets.addAll(parameterSets);
    }

    public void removeSets(Collection<ModelParameterSetEntity> parameterSets) {
        parameterSets.forEach(parameterSet -> parameterSet.setGroup(null));
        this.sets.removeAll(parameterSets);
    }
}
