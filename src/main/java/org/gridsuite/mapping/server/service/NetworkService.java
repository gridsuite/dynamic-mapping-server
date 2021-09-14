/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.EquipmentValues;
import org.gridsuite.mapping.server.dto.OutputNetwork;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public interface NetworkService {

    List<EquipmentValues> getNetworkValuesFromExistingNetwork(UUID networkUuid);

    List<EquipmentValues> getNetworkValues(MultipartFile file);

    List<OutputNetwork> getNetworks();
}
