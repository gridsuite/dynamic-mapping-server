/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.service.SupervisionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * @author Mathieu Deharbe <mathieu.deharbe at rte-france.com>
 *
 * endpoints only used for supervision from the admins, should never be used by any other users
 */
@RestController
@RequestMapping(value = "/supervision")
@Tag(name = "Mapping server - Supervision")
@AllArgsConstructor
public class SupervisionController {
    private final SupervisionService supervisionService;

    @GetMapping(value = "/filters")
    @Operation(summary = "Get all the uuids of the filters used by the mapping server")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "List of all the filters uuids")})
    public ResponseEntity<List<UUID>> getAllRootNetworks() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(supervisionService.getAllFiltersUuids());
    }
}
