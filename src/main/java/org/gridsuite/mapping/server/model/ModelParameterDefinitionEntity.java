/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;

import static jakarta.persistence.TemporalType.TIMESTAMP;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_parameter_definitions")
public class ModelParameterDefinitionEntity implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated
    private ParameterType type;

    @Column(name = "origin_name")
    private String originName;

    @Column(name = "fixed_value")
    private String fixedValue;

    @OneToMany(mappedBy = "parameterDefinition")
    List<ModelModelParameterDefinitionEntity> models = new ArrayList<>();

    public ModelParameterDefinitionEntity(ModelParameterDefinition parameterDefinition) {
        this(parameterDefinition.getName(), parameterDefinition.getType(), parameterDefinition.getOriginName(), parameterDefinition.getFixedValue(), null, null, null);
    }

    @CreatedDate
    @Temporal(TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @LastModifiedDate
    @Temporal(TIMESTAMP)
    @Column(name = "updated_date")
    private Date updatedDate;
}
