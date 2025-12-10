/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.models.ParametersSet;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_parameter_set", indexes = {@Index(name = "model_parameter_set_name_index", columnList = "name")})
public class ModelParameterSetEntity implements Serializable {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelParameterEntity> parameters;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_parameter_set_group_id_fk"))
    private ModelSetsGroupEntity group;

    public ModelParameterSetEntity(ModelSetsGroupEntity group, ParametersSet set) {
        this.id = set.getId() == null ? UUID.randomUUID() : set.getId();
        this.group = group;
        this.name = set.getName();
        this.parameters = set.getParameters().stream().map(parameter -> new ModelParameterEntity(this, parameter)).collect(Collectors.toList());
        this.lastModifiedDate = set.getLastModifiedDate();
    }
}
