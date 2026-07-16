/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model.workspace;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.mapping.server.dto.workspace.MappingWorkspaceItem;

import java.util.UUID;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "mapping_workspace_item", indexes = {
    @Index(name = "idx_mapping_workspace_item_mapping_id", columnList = "mapping_id")
})
public class MappingWorkspaceItemEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "mapping_id", nullable = false)
    private UUID mappingId;

    @Column(name = "pinned", nullable = false)
    private boolean pinned;

    public MappingWorkspaceItemEntity(MappingWorkspaceItem dto) {
        assignAttributes(dto);
    }

    private void assignAttributes(MappingWorkspaceItem dto) {
        if (id == null) {
            id = UUID.randomUUID();
        }
        mappingId = dto.mappingId();
        pinned = dto.pinned();
    }

    public void update(MappingWorkspaceItem dto) {
        assignAttributes(dto);
    }

    public MappingWorkspaceItem toDto(boolean toDuplicate) {
        return new MappingWorkspaceItem(
                toDuplicate ? null : id,
                mappingId,
                pinned
        );
    }
}
