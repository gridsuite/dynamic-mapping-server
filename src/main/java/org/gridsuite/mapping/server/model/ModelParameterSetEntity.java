/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.ParametersSet;
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
@Table(name = "model_parameter_sets")
public class ModelParameterSetEntity implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "parameterSet", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<ModelParameterEntity> parameters;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns(foreignKey = @ForeignKey(name = "model_parameter_sets_fk"), value = {
            @JoinColumn(name = "group_name", referencedColumnName = "name", insertable = false, updatable = false),
            @JoinColumn(name = "group_type", referencedColumnName = "type", insertable = false, updatable = false)
    })
    private ModelSetsGroupEntity group;

    public ModelParameterSetEntity(ModelSetsGroupEntity group, ParametersSet set) {
        this(set.getName(), null, group, null, null);
        this.parameters = set.getParameters().stream().map(parameter -> new ModelParameterEntity(this, parameter))
                .collect(Collectors.toSet());
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
    public void addParameters(Collection<ModelParameterEntity> parameters) {
        parameters.forEach(parameter -> parameter.setParameterSet(this));
        this.parameters.addAll(parameters);
    }

    public void removeParameters(Collection<ModelParameterEntity> parameters) {
        parameters.forEach(parameter -> parameter.setParameterSet(null));
        this.parameters.removeAll(parameters);
    }
}
