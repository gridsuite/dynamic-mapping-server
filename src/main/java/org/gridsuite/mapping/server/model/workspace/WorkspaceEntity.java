/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model.workspace;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.mapping.server.dto.workspace.MappingWorkspaceItem;
import org.gridsuite.mapping.server.dto.workspace.Workspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "workspace")
public class WorkspaceEntity implements Serializable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", foreignKey = @ForeignKey(name = "workspace_id_fk"))
    @OrderColumn(name = "item_order")
    private List<MappingWorkspaceItemEntity> mappingWorkspaceItems = new ArrayList<>();

    public WorkspaceEntity(Workspace dto) {
        assignAttributes(dto);
    }

    private void assignAttributes(Workspace dto) {
        if (id == null) {
            id = UUID.randomUUID();
        }
        userId = dto.userId();
        assignMappingWorkspaceItems(dto.mappingWorkspaceItems());
    }

    private void assignMappingWorkspaceItems(List<MappingWorkspaceItem> mappingWorkspaceItemsList) {
        if (CollectionUtils.isEmpty(mappingWorkspaceItemsList)) {
            mappingWorkspaceItems.clear();
            return;
        }

        // build existing mappings Map
        Map<UUID, MappingWorkspaceItemEntity> mappingWorkspaceItemByIdMap = mappingWorkspaceItems.stream()
                .collect(Collectors.toMap(MappingWorkspaceItemEntity::getId, entity -> entity));

        // merge existing and add new mappings
        List<MappingWorkspaceItemEntity> mergedMappingWorkspaceItems = new ArrayList<>();
        for (MappingWorkspaceItem mappingWorkspaceItem : mappingWorkspaceItemsList) {
            if (mappingWorkspaceItem.id() != null) {
                MappingWorkspaceItemEntity existingEntity = mappingWorkspaceItemByIdMap.get(mappingWorkspaceItem.id());
                if (existingEntity != null) {
                    existingEntity.update(mappingWorkspaceItem);
                    mergedMappingWorkspaceItems.add(existingEntity);
                }
            } else {
                mergedMappingWorkspaceItems.add(new MappingWorkspaceItemEntity(mappingWorkspaceItem));
            }
        }

        // by clear/addAll, existing elements that are not present in the new list will be removed systematically
        mappingWorkspaceItems.clear();
        mappingWorkspaceItems.addAll(mergedMappingWorkspaceItems);
    }

    public void update(Workspace dto) {
        assignAttributes(dto);
    }

    public Workspace toDto(boolean toDuplicate) {
        return new Workspace(
            toDuplicate ? null : id,
            userId,
            mappingWorkspaceItems.stream().map(entity -> entity.toDto(toDuplicate)).toList()
        );
    }
}
