/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Mathieu Deharbe <mathieu.deharbe at rte-france.com>
 */
@Service
@AllArgsConstructor
public class SupervisionService {
    private final RuleRepository ruleRepository;

    @Transactional(readOnly = true)
    public List<UUID> getAllFiltersUuids() {
        return ruleRepository.findAll().stream()
                .map(RuleEntity::getFilterUuid)
                .filter(Objects::nonNull)
                .toList();
    }
}
