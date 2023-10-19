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
import org.gridsuite.mapping.server.dto.models.ModelParameter;
import org.gridsuite.mapping.server.utils.SetGroupType;

import jakarta.persistence.*;
import java.io.Serializable;

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
@IdClass(ModelParameterId.class)
public class ModelParameterEntity implements Serializable {

    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "model_name")
    private String modelName;

    @Id
    @Column(name = "group_name")
    private String groupName;

    @Id
    @Column(name = "group_type")
    private SetGroupType groupType;

    @Id
    @Column(name = "set_name")
    private String setName;

    @Column(name = "value_")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(foreignKey = @ForeignKey(name = "parameter_set_fk"), value = {
        @JoinColumn(name = "set_name", referencedColumnName = "name", insertable = false, updatable = false),
        @JoinColumn(name = "group_name", referencedColumnName = "group_name", insertable = false, updatable = false),
        @JoinColumn(name = "model_name", referencedColumnName = "model_name", insertable = false, updatable = false),
        @JoinColumn(name = "group_type", referencedColumnName = "group_type", insertable = false, updatable = false)
    })
    private ModelParameterSetEntity set;

    public ModelParameterEntity(ModelParameterSetEntity set, ModelParameter parameter) {
        this.set = set;
        name = parameter.getName();
        groupName = set.getGroup().getName();
        groupType = set.getGroup().getType();
        modelName = set.getGroup().getModelName();
        setName = set.getName();
        value = parameter.getValue();
    }
}
