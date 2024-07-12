/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.dto.models.Model;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public interface MappingService {

    List<InputMapping> getMappingList();

    InputMapping getMapping(String mappingName);

    InputMapping saveMapping(String mappingName, InputMapping mapping);

    String deleteMapping(String mappingName);

    RenameObject renameMapping(String oldName, String newName);

    InputMapping copyMapping(String originalName, String copyName);

    List<Model> getMappedModelsList(String mappingName);
}
