/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.dto.models.ParametersSet;

import java.util.List;

public interface ModelService {
    List<ParametersSet> getParametersSetsFromModelName(String modelName);

    ParametersSet saveParametersSet(String modelName, ParametersSet set);

    List<ModelParameterDefinition> getParametersDefinitionsFromModelName(String modelName);
}
