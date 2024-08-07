/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import com.powsybl.iidm.network.Network;
import org.gridsuite.mapping.server.dto.MatchedRule;
import org.gridsuite.mapping.server.dto.NetworkValues;
import org.gridsuite.mapping.server.dto.OutputNetwork;
import org.gridsuite.mapping.server.dto.RuleToMatch;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public interface NetworkService {

    NetworkValues getNetworkValuesFromExistingNetwork(UUID networkUuid);

    NetworkValues getNetworkValues(MultipartFile file);

    List<OutputNetwork> getNetworks();

    Network getNetwork(UUID networkUuid);

    MatchedRule getNetworkMatches(UUID networkUuid, RuleToMatch ruleToMatch);
}
