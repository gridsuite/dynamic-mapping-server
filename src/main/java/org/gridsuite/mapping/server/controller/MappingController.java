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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/mappings")
@Tag(name = "Mapping server")
@AllArgsConstructor
public class MappingController {

    private static final String CONFLICT_MAPPING_ERROR_MESSAGE = "A mapping already exists with name: ";

    private final MappingService mappingService;

    @GetMapping(value = "/")
    @Operation(summary = "Get all mappings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of mappings")})
    public ResponseEntity<List<InputMapping>> getMappingList() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMappingList());
    }

    @GetMapping(value = "/{mappingName}")
    @Operation(summary = "Get a mapping by name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The requested mapping")})
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
    public ResponseEntity<InputMapping> saveMapping(@PathVariable(name = "mappingName", required = false) String mappingName, @RequestBody InputMapping mapping) {
        InputMapping savedMapping = mappingService.saveMapping(mappingName, mapping);
        return ResponseEntity.ok().body(savedMapping);
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
        return ResponseEntity.ok().body(renameMappingWithUniquenessCheck(oldName, newName));
    }

    private RenameObject renameMappingWithUniquenessCheck(String oldName, String newName) {
        try {
            return mappingService.renameMapping(oldName, newName);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, CONFLICT_MAPPING_ERROR_MESSAGE + newName, ex);
        }
    }

    @PostMapping(value = "/copy/{originalName}/to/{copyName}")
    @Operation(summary = "Copy a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mapping Copy"),
        @ApiResponse(responseCode = "404", description = "Mapping not found"),
        @ApiResponse(responseCode = "500", description = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<InputMapping> copyMapping(@PathVariable("originalName") String originalName, @PathVariable("copyName") String copyName) {
        return ResponseEntity.ok().body(copyMappingWithUniquenessCheck(originalName, copyName));
    }

    private InputMapping copyMappingWithUniquenessCheck(String originalName, String copyName) {
        try {
            return mappingService.copyMapping(originalName, copyName);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, CONFLICT_MAPPING_ERROR_MESSAGE + copyName, ex);
        }
    }

}
