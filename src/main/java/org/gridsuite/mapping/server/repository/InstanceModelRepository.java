/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Repository
public interface InstanceModelRepository extends JpaRepository<InstanceModelEntity, String> {

    Optional<List<InstanceModelEntity>> findByModelName(String name);
}
