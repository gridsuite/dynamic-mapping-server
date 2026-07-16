/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.workspace.Workspace;
import org.gridsuite.mapping.server.error.DynamicMappingException;
import org.gridsuite.mapping.server.model.workspace.WorkspaceEntity;
import org.gridsuite.mapping.server.repository.WorkspaceRepository;
import org.gridsuite.mapping.server.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.gridsuite.mapping.server.error.DynamicMappingErrorBusinessCode.WORKSPACE_NOT_ALLOW_CHANGE_USER;
import static org.gridsuite.mapping.server.error.DynamicMappingErrorBusinessCode.WORKSPACE_NOT_FOUND;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Autowired
    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Transactional
    @Override
    public Workspace getOrCreateWorkspace(String userId) {
        return workspaceRepository.findByUserId(userId)
                .map(entity -> entity.toDto(false))
                .orElseGet(() -> {
                    Workspace workspace = new Workspace(null, userId, null);
                    return workspaceRepository.save(new WorkspaceEntity(workspace)).toDto(false);
                });
    }

    @Transactional
    @Override
    public void updateWorkspace(UUID workspaceId, Workspace workspace) {
        WorkspaceEntity workspaceEntity = workspaceRepository.findById(workspaceId).orElseThrow(
            () -> new DynamicMappingException(WORKSPACE_NOT_FOUND, "Workspace not found"));
        if (workspace.userId() == null || !workspace.userId().equals(workspaceEntity.getUserId())) {
            throw new DynamicMappingException(WORKSPACE_NOT_ALLOW_CHANGE_USER, "Changing user of an existing workspace is not allowed");
        }
        workspaceEntity.update(workspace);
    }

    @Transactional
    @Override
    public void deleteWorkspace(UUID workspaceId) {
        workspaceRepository.deleteById(workspaceId);
    }
}
