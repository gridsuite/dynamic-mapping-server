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
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.dto.models.ParametersSet;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.dto.models.SimpleModel;
import org.gridsuite.mapping.server.service.ModelService;
import org.gridsuite.mapping.server.service.implementation.ModelServiceImpl;
import org.gridsuite.mapping.server.utils.SetGroupType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/models")
@Tag(name = "Mapping model server")
@AllArgsConstructor
@ComponentScan(basePackageClasses = {ModelServiceImpl.class})

public class ModelController {

    private final ModelService modelService;

    @GetMapping(value = "/{modelName}/parameters/sets/{groupName}/{groupType}")
    @Operation(summary = "get all parameters sets for a given group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "parameter sets of the group")})

    public ResponseEntity<List<ParametersSet>> getSetsGroupsFromModelName(@PathVariable("modelName") String modelName, @PathVariable("groupName") String groupName, @PathVariable("groupType") SetGroupType groupType) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getSetsFromGroup(modelName, groupName, groupType));
    }

    @PostMapping(value = "/{modelName}/parameters/sets/strict")
    @Operation(summary = "Save a new parameter sets group with checking sets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameter Set Group Saved")})
    public ResponseEntity<ParametersSetsGroup> saveParametersSet(@PathVariable("modelName") String modelName, @RequestBody ParametersSetsGroup setsGroup) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveParametersSetsGroup(modelName, setsGroup, true));
    }

    @PostMapping(value = "/{modelName}/parameters/sets/")
    @Operation(summary = "Save a new parameter sets group without checking sets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parameter Set Group Saved")})
    public ResponseEntity<ParametersSetsGroup> saveSimpleParametersSet(@PathVariable("modelName") String modelName, @RequestBody ParametersSetsGroup setsGroup) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveParametersSetsGroup(modelName, setsGroup, false));
    }

    @GetMapping(value = "/{modelName}/parameters/definitions/")
    @Operation(summary = "get parameters definitions for a given model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "parameters definitions of the model")})
    public ResponseEntity<List<ModelParameterDefinition>> getParametersDefinitionsFromModelName(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getParametersDefinitionsFromModelName(modelName));
    }

    @GetMapping(value = "/")
    @Operation(summary = "get models")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "simplified versions of all models")})
    public ResponseEntity<List<SimpleModel>> getModels() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getModels());
    }
}
