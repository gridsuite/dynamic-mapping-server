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
import org.gridsuite.mapping.server.dto.models.ModelParameter;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_parameters", indexes = {@Index(name = "model_parameter_set_index", columnList = "set_name")})
public class ModelParameterEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "set_id")
    private UUID setId;

    @Column(name = "value_")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(foreignKey = @ForeignKey(name = "parameter_set_fk"), value = {
        @JoinColumn(name = "set_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private ModelParameterSetEntity set;

    public ModelParameterEntity(ModelParameterSetEntity set, ModelParameter parameter) {
        this.id = parameter.getId() == null ? UUID.randomUUID() : parameter.getId();
        this.set = set;
        this.name = parameter.getName();
        this.setId = set.getId();
        this.value = parameter.getValue();
    }
}
