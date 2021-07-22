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
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;

import javax.persistence.*;
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
@Table(name = "model_parameter_definitions", indexes = {@Index(name = "model_parameter_definitions_model_name_index", columnList = "model_name")})
@IdClass(ModelParameterDefinitionId.class)
public class ModelParameterDefinitionEntity implements Serializable {

    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "model_name")
    private String modelName;

    @Column(name = "type")
    @Enumerated
    private ParameterType type;

    @Column(name = "origin")
    @Enumerated
    private ParameterOrigin origin;

    @Column(name = "origin_name")
    private String originName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_name", foreignKey = @ForeignKey(name = "model_parameter_definition_fk"))
    @MapsId("modelName")
    private ModelEntity model;
}
