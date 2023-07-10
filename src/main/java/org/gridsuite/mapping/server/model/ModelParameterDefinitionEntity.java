/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
@Table(name = "model_parameter_definitions")
public class ModelParameterDefinitionEntity implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated
    private ParameterType type;

    @Column(name = "origin")
    @Enumerated
    private ParameterOrigin origin;

    @Column(name = "origin_name")
    private String originName;

    @Column(name = "fixed_value")
    private String fixedValue;

    @ManyToMany(
        mappedBy = "parameterDefinitions",
        cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}
    )
    private Set<ModelEntity> models;

    public ModelParameterDefinitionEntity(ModelEntity model, ModelParameterDefinition parameterDefinition) {
        this(parameterDefinition.getName(), parameterDefinition.getType(), parameterDefinition.getOrigin(), parameterDefinition.getOriginName(), parameterDefinition.getFixedValue(),
                model != null ? new LinkedHashSet<>(List.of(model)) : new LinkedHashSet<>(), null, null);
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
