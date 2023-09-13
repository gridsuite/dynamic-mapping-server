/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.model.ModelParameterDefinitionEntity;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@AllArgsConstructor
public class ModelParameterDefinition {

    private String name;

    private ParameterType type;

    private ParameterOrigin origin;

    private String originName;

    private String fixedValue;

    public ModelParameterDefinition(ModelParameterDefinitionEntity modelParameterDefinitionEntity, ParameterOrigin origin) {
        name = modelParameterDefinitionEntity.getName();
        type = modelParameterDefinitionEntity.getType();
        this.origin = origin;
        originName = modelParameterDefinitionEntity.getOriginName();
        fixedValue = modelParameterDefinitionEntity.getFixedValue();
    }
}
