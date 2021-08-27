/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.dto.models.ParametersSet;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.dto.models.SimpleModel;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public interface ModelService {

    List<ModelParameterDefinition> getParametersDefinitionsFromModelName(String modelName);

    List<SimpleModel> getModels();

    List<ParametersSet> getSetsFromGroup(String modelName, String groupName);

    ParametersSetsGroup saveParametersSetsGroup(String modelName, ParametersSetsGroup setsGroup, Boolean strict);
}
