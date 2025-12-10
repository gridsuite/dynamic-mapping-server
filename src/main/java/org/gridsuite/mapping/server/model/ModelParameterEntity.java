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
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_parameter", indexes = {@Index(name = "model_parameter_name_index", columnList = "name")})
public class ModelParameterEntity implements Serializable {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value_")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_parameter_set_id_fk"))
    private ModelParameterSetEntity set;

    public ModelParameterEntity(ModelParameterSetEntity set, ModelParameter parameter) {
        this.id = parameter.getId() == null ? UUID.randomUUID() : parameter.getId();
        this.set = set;
        this.name = parameter.getName();
        this.value = parameter.getValue();
    }
}
