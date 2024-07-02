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
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.service.MappingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/mappings")
@Tag(name = "Mapping server")
@AllArgsConstructor
public class MappingController {

    private final MappingService mappingService;

    @GetMapping(value = "/")
    @Operation(summary = "Get all mappings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of mappings")})
    public ResponseEntity<List<InputMapping>> getMappingList() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMappingList());
    }

    @GetMapping(value = "/{mappingName}")
    @Operation(summary = "Get a mapping by name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Get a mapping by name")})
    public ResponseEntity<InputMapping> getMapping(@PathVariable("mappingName") String mappingName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMapping(mappingName));
    }

    @GetMapping(value = "/{mappingName}/models")
    @Operation(summary = "Get models used in the given mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of mapped models")})
    public ResponseEntity<List<Model>> getMappedModelsList(@PathVariable("mappingName") String mappingName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMappedModelsList(mappingName));
    }

    @PostMapping(value = "/{mappingName}")
    @Operation(summary = "Save a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The id of the mapping"),
        @ApiResponse(responseCode = "409", description = "The mapping already exist"),
        @ApiResponse(responseCode = "500", description = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<InputMapping> createMapping(@PathVariable(name = "mappingName", required = false) String mappingName, @RequestBody InputMapping mapping) {
        InputMapping createMapping = mappingService.createMapping(mappingName, mapping);
        return ResponseEntity.ok().body(createMapping);
    }

    @DeleteMapping(path = "/{mappingName}")
    @Operation(summary = "delete the mapping")
    @ApiResponse(responseCode = "200", description = "Mapping deleted")
    public ResponseEntity<String> deleteMapping(@PathVariable("mappingName") String mappingName) {
        mappingService.deleteMapping(mappingName);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(mappingName);
    }

    @PostMapping(value = "/rename/{oldName}/to/{newName}")
    @Operation(summary = "Rename a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = " Both names of the mapping"),
        @ApiResponse(responseCode = "404", description = "Mapping not found"),
        @ApiResponse(responseCode = "500", description = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<RenameObject> renameMapping(@PathVariable("oldName") String oldName, @PathVariable("newName") String newName) {
        RenameObject renamedMapping = mappingService.renameMapping(oldName, newName);
        return ResponseEntity.ok().body(renamedMapping);
    }

    @PostMapping(value = "/copy/{originalName}/to/{copyName}")
    @Operation(summary = "Copy a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mapping Copy"),
        @ApiResponse(responseCode = "404", description = "Mapping not found"),
        @ApiResponse(responseCode = "500", description = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<InputMapping> copyMapping(@PathVariable("originalName") String originalName, @PathVariable("copyName") String copyName) {
        InputMapping copiedMapping = mappingService.copyMapping(originalName, copyName);
        return ResponseEntity.ok().body(copiedMapping);
    }

}
