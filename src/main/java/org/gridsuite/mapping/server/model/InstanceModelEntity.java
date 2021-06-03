/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.EquipmentType;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Getter
@Setter
@Table(name = "instance_models")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceModelEntity extends AbstractManuallyAssignedIdentifierEntity<String> {

    // Could be replaced with UUID but we lose the ease of use of names
    @Id
    private String id;

    private String modelName;

    private EquipmentType equipmentType;

    @Embedded
    private ModelParamsEmbeddable params;
}
