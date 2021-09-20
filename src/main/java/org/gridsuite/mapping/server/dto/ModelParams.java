/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import lombok.Data;
import org.gridsuite.mapping.server.model.ModelParamsEmbeddable;
import org.gridsuite.mapping.server.utils.ParamsType;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
public class ModelParams {
    private String name;
    private ParamsType type;

    ModelParams(ModelParamsEmbeddable modelParamsEmbeddable) {
        name = modelParamsEmbeddable.getName();
        type = modelParamsEmbeddable.getType();
    }
}
