/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.ModelVariableSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public interface ModelVariablesSetRepository extends JpaRepository<ModelVariableSetEntity, UUID> {
    List<ModelVariableSetEntity> findAllByNameIn(Collection<String> names);

    Optional<ModelVariableSetEntity> findByName(String name);

    void deleteAllByNameIn(Collection<String> names);
}
