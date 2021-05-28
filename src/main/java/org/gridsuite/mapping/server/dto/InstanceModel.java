/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import lombok.Data;
import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.gridsuite.mapping.server.utils.EquipmentType;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
public class InstanceModel {
    private String id;

    private String modelName;

    private EquipmentType equipmentType;

    private ModelParams params;

    InstanceModel(InstanceModelEntity instanceModelEntity) {
        id = instanceModelEntity.getId();
        modelName = instanceModelEntity.getModelName();
        equipmentType = instanceModelEntity.getEquipmentType();
        params = new SetParams(instanceModelEntity.getParams());
    }
}
