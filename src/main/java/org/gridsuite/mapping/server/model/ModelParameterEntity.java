/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.dto.models.ModelParameter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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
@Table(name = "model_parameters", indexes = {@Index(name = "model_parameter_set_index", columnList = "set_name")})
public class ModelParameterEntity implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "name")
    private String name;

    @Column(name = "value_")
    private String value;

    @Id
    @EqualsAndHashCode.Include
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "set_name", referencedColumnName = "name", foreignKey = @ForeignKey(name = "parameter_set_parameter_fk"), updatable = false)
    private ModelParameterSetEntity parameterSet;

    public ModelParameterEntity(ModelParameterSetEntity parameterSet, ModelParameter parameter) {
        this(parameter.getName(), parameter.getValue(), parameterSet, null, null);
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
