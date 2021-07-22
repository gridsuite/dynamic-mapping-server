/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.EquipmentType;

import javax.persistence.*;
import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Getter
@Setter
@Table(name = "models")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelEntity extends AbstractManuallyAssignedIdentifierEntity<String> {

    // Could be replaced with UUID but we lose the ease of use of names
    @Id
    @Column(name = "model_name")
    private String modelName;

    @Column(name = "equipment_type")
    private EquipmentType equipmentType;

    @OneToMany(targetEntity = ModelParameterDefinitionEntity.class, mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelParameterDefinitionEntity> parameterDefinitions;

    @OneToMany(targetEntity = ModelParameterSetEntity.class, mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelParameterSetEntity> sets;

    @Override
    public String getId() {
        return modelName;
    }

}
