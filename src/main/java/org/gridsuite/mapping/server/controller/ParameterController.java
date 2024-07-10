/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import org.gridsuite.mapping.server.dto.ParameterFile;
import org.gridsuite.mapping.server.service.ParameterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@RestController
@RequestMapping(value = "/parameters")
@Tag(name = "Mapping server")
@AllArgsConstructor
public class ParameterController {

    private final ParameterService parameterService;

    @GetMapping(value = "/export")
    @Operation(summary = "Export parameter sets in used models of a given mapping into *.par format")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Used parameter sets serialized in *.par format")})
    public ResponseEntity<ParameterFile> exportParameters(@RequestParam("mappingName") String mappingName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(parameterService.exportParameters(mappingName));
    }

}
