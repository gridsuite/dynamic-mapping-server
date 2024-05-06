/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, UUID> {
    @Query("SELECT rule.filterUuid FROM RuleEntity rule WHERE rule.mapping.name = :mappingName ")
    List<UUID> findFilterUuidsByMappingName(@Param("mappingName") String mappingName);
}
