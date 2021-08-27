/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.dto.models.ParametersSet;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.dto.models.SimpleModel;
import org.gridsuite.mapping.server.service.ModelService;
import org.gridsuite.mapping.server.service.implementation.ModelServiceImpl;
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
@Api(value = "Mapping model server")
@AllArgsConstructor
@ComponentScan(basePackageClasses = {ModelServiceImpl.class})

public class ModelController {

    private final ModelService modelService;

    @GetMapping(value = "/{modelName}/parameters/sets/{groupName}")
    @ApiOperation(value = "get all parameters sets for a given group")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "parameter sets of the group")})

    public ResponseEntity<List<ParametersSet>> getSetsGroupsFromModelName(@PathVariable("modelName") String modelName, @PathVariable("groupName") String groupName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getSetsFromGroup(modelName, groupName));
    }

    @PostMapping(value = "/{modelName}/parameters/sets/strict")
    @ApiOperation(value = "Save a new parameter sets group without checking sets")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Parameter Set Group Saved")})
    public ResponseEntity<ParametersSetsGroup> saveParametersSet(@PathVariable("modelName") String modelName, @RequestBody ParametersSetsGroup setsGroup) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveParametersSetsGroup(modelName, setsGroup, true));
    }

    @PostMapping(value = "/{modelName}/parameters/sets/")
    @ApiOperation(value = "Save a new parameter sets group without checking sets")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Parameter Set Group Saved")})
    public ResponseEntity<ParametersSetsGroup> saveSimpleParametersSet(@PathVariable("modelName") String modelName, @RequestBody ParametersSetsGroup setsGroup) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveParametersSetsGroup(modelName, setsGroup, false));
    }

    @GetMapping(value = "/{modelName}/parameters/definitions/")
    @ApiOperation(value = "get parameters definitions for a given model")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "parameters definitions of the model")})
    public ResponseEntity<List<ModelParameterDefinition>> getParametersDefinitionsFromModelName(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getParametersDefinitionsFromModelName(modelName));
    }

    @GetMapping(value = "/")
    @ApiOperation(value = "get models names")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "names of all models")})
    public ResponseEntity<List<SimpleModel>> getModels() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getModels());
    }
}
