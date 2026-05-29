/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.service.MappingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/mappings")
@Tag(name = "Mapping server")
@AllArgsConstructor
public class MappingController {

    private static final String CONFLICT_MAPPING_ERROR_MESSAGE = "A mapping already exists with uuid: ";

    private final MappingService mappingService;

    @GetMapping(value = "/")
    @Operation(summary = "Get all mappings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of mappings")})
    public ResponseEntity<List<InputMapping>> getMappingList() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMappingList());
    }

    @GetMapping(value = "/{mappingId}")
    @Operation(summary = "Get a mapping by id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The requested mapping")})
    public ResponseEntity<InputMapping> getMapping(@PathVariable("mappingId") UUID mappingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMapping(mappingId));
    }

    @GetMapping(value = "/{mappingId}/models")
    @Operation(summary = "Get models used in the given mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of mapped models")})
    public ResponseEntity<List<Model>> getMappedModelsList(@PathVariable("mappingId") UUID mappingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMappedModelsList(mappingId));
    }

    @PostMapping(value = "/{mappingId}")
    @Operation(summary = "Save a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The id of the mapping"),
        @ApiResponse(responseCode = "409", description = "The mapping already exist"),
        @ApiResponse(responseCode = "500", description = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<InputMapping> saveMapping(@PathVariable(name = "mappingId") UUID mappingId, @RequestBody InputMapping mapping) {
        InputMapping savedMapping = mappingService.saveMapping(mappingId, mapping);
        return ResponseEntity.ok().body(savedMapping);
    }

    @DeleteMapping(path = "/{mappingId}")
    @Operation(summary = "delete the mapping")
    @ApiResponse(responseCode = "200", description = "Mapping deleted")
    public ResponseEntity<UUID> deleteMapping(@PathVariable("mappingId") UUID mappingId) {
        mappingService.deleteMapping(mappingId);
        return ResponseEntity.ok().body(mappingId);
    }

    @PostMapping(value = "{originalId}/copy")
    @Operation(summary = "Copy a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mapping Copy"),
        @ApiResponse(responseCode = "404", description = "Mapping not found"),
        @ApiResponse(responseCode = "500", description = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<UUID> copyMapping(@PathVariable("originalId") UUID originalId) {
        return ResponseEntity.ok().body(mappingService.copyMapping(originalId));
    }

}
