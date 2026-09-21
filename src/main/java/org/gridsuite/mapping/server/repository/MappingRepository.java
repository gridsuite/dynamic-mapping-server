/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.MappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Repository
public interface MappingRepository extends JpaRepository<MappingEntity, UUID> {

    @Modifying
    @Query("UPDATE MappingEntity m SET m.studyUuid = :studyUuid WHERE m.mappingId = :mappingId")
    int updateStudyUuid(@Param("mappingId") UUID mappingId, @Param("studyUuid") UUID studyUuid);
}
