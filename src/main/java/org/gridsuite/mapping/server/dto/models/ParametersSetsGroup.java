/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.ModelSetsGroupEntity;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParametersSetsGroup {

    private String name;

    private SetGroupType type;

    private List<ParametersSet> sets;

    private String modelName;

    public ParametersSetsGroup(ModelSetsGroupEntity setsGroupEntity) {
        name = setsGroupEntity.getName();
        type = setsGroupEntity.getType();
        sets = setsGroupEntity.getSets().stream().map(ParametersSet::new).collect(Collectors.toList());
        modelName = setsGroupEntity.getModelName();
    }
}
