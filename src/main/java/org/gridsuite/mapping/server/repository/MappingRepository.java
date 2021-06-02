/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.gridsuite.mapping.server.model.MappingEntity;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Repository
public interface MappingRepository extends JpaRepository<MappingEntity, String> {
}
