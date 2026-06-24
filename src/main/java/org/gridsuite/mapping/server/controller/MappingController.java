/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.mapping.server.RestConfig;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.service.MappingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/mappings")
@Tag(name = "Mapping server")
public class MappingController {

    private final MappingService mappingService;
    private final ObjectMapper exportMappingObjectMapper;

    public MappingController(MappingService mappingService,
                             @Qualifier(RestConfig.EXPORT_MAPPING_OBJECT_MAPPER_BEAN) ObjectMapper exportMappingObjectMapper) {
        this.mappingService = mappingService;
        this.exportMappingObjectMapper = exportMappingObjectMapper;
    }

    @GetMapping(value = "")
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

    @GetMapping(value = "/{mappingId}/export")
    @Operation(summary = "Export a mapping to a JSON file")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The mapping exported as a JSON file"),
        @ApiResponse(responseCode = "404", description = "Mapping not found")})
    public ResponseEntity<byte[]> exportMapping(@PathVariable("mappingId") UUID mappingId, @RequestParam(value = "fileName", required = false) String fileName) {
        InputMapping mapping = mappingService.getMapping(mappingId);
        try {
            byte[] jsonBytes = exportMappingObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(mapping);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", Optional.ofNullable(fileName).orElse("dynamic_mapping") + ".json");

            return ResponseEntity.ok().headers(headers).body(jsonBytes);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serializing mapping to JSON", e);
        }
    }

    @GetMapping(value = "/{mappingId}/models")
    @Operation(summary = "Get models used in the given mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of mapped models")})
    public ResponseEntity<List<Model>> getMappedModelsList(@PathVariable("mappingId") UUID mappingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMappedModelsList(mappingId));
    }

    @PostMapping(value = "")
    @Operation(summary = "Save a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The id of the mapping"),
        @ApiResponse(responseCode = "500", description = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<UUID> postMapping(@RequestBody InputMapping mapping) {
        InputMapping savedMapping = mappingService.saveMapping(null, mapping);
        return ResponseEntity.ok().body(savedMapping.getId());
    }

    @PutMapping(value = "/{mappingId}")
    @Operation(summary = "Replace a mapping by a new one, if mapping id not exist create a new one with given mapping id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The id of the mapping")})
    public ResponseEntity<UUID> putMapping(@PathVariable(name = "mappingId") UUID mappingId, @RequestBody InputMapping mapping) {
        InputMapping savedMapping = mappingService.saveMapping(mappingId, mapping);
        return ResponseEntity.ok().body(savedMapping.getId());
    }

    @DeleteMapping(path = "/{mappingId}")
    @Operation(summary = "delete the mapping")
    @ApiResponse(responseCode = "200", description = "Mapping deleted")
    public ResponseEntity<UUID> deleteMapping(@PathVariable("mappingId") UUID mappingId) {
        mappingService.deleteMapping(mappingId);
        return ResponseEntity.ok().body(mappingId);
    }

    @PostMapping(value = "{originalId}/duplicate")
    @Operation(summary = "Copy a mapping")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mapping Copy"),
        @ApiResponse(responseCode = "404", description = "Mapping not found")})
    public ResponseEntity<UUID> copyMapping(@PathVariable("originalId") UUID originalId) {
        return ResponseEntity.ok().body(mappingService.copyMapping(originalId));
    }

}
