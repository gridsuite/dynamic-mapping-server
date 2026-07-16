/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.dto.workspace.Workspace;
import org.gridsuite.mapping.server.service.WorkspaceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@RestController
@RequestMapping(value = "/workspaces")
@AllArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @GetMapping(value = "/{userId}")
    @Operation(summary = "Get or create an empty workspace for a given user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Workspace for the user")})
    public ResponseEntity<Workspace> getWorkspace(@PathVariable("userId") String userId) {
        Workspace workspace = workspaceService.getOrCreateWorkspace(userId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(workspace);
    }

    @PutMapping(value = "/{workspaceId}")
    @Operation(summary = "Update a workspace of a workspaces config")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Workspace has been updated")})
    public ResponseEntity<Void> updateWorkspace(@PathVariable("workspaceId") UUID workspaceId,
                                                @RequestBody Workspace workspace) {
        workspaceService.updateWorkspace(workspaceId, workspace);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{workspaceId}")
    @Operation(summary = "Delete a workspace")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Workspace has been deleted")})
    public ResponseEntity<Void> deleteWorkspace(@PathVariable("workspaceId") UUID workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.ok().build();
    }

}
