/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.VariableType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_variable_definitions", indexes = {@Index(name = "model_variable_definitions_model_name_index", columnList = "model_name")})
@IdClass(ModelVariableDefinitionId.class)
public class ModelVariableDefinitionEntity implements Serializable {
    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "model_name")
    private String modelName;

    @Column(name = "type")
    @Enumerated
    private VariableType type;

    @Column(name = "unit")
    private String unit;

    @Column(name = "factor")
    private Double factor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_name", foreignKey = @ForeignKey(name = "model_variable_definition_fk"))
    @MapsId("modelName")
    private ModelEntity model;
}
