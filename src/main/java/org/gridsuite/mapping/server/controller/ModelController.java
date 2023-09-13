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
import org.gridsuite.mapping.server.dto.models.*;
import org.gridsuite.mapping.server.service.ModelService;
import org.gridsuite.mapping.server.service.implementation.ModelServiceImpl;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
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

    @GetMapping(value = "/automaton-definitions")
    @Operation(summary = "get all automaton definitions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "automaton definitions in json format")
    })
    public ResponseEntity<String> getAutomatonDefinitions() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getAutomatonDefinitions());
    }

    @GetMapping(value = "/{modelName}/parameters/sets/{groupName}/{groupType}")
    @Operation(summary = "get all parameters sets for a given group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "parameter sets of the group")})
    public ResponseEntity<List<ParametersSet>> getSetsGroupsFromModelName(@PathVariable("modelName") String modelName, @PathVariable("groupName") String groupName, @PathVariable("groupType") SetGroupType groupType) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getSetsFromGroup(modelName, groupName, groupType));
    }

    @PostMapping(value = "/{modelName}/parameters/sets/strict")
    @Operation(summary = "Save a new parameter sets group without checking sets")
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

    @PostMapping(value = "/")
    @Operation(summary = "Post a model")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> saveModel(@RequestBody Model model) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveModel(model));
    }

    @DeleteMapping(value = "/{modelName}/parameters/sets/{groupName}/{groupType}/{setName}")
    @Operation(summary = "Delete a parameter set")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "updated parameter group")})
    public ResponseEntity<ParametersSetsGroup> deleteSet(@PathVariable("modelName") String modelName, @PathVariable("groupName") String groupName, @PathVariable("groupType") SetGroupType groupType, @PathVariable("setName") String setName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.deleteSet(modelName, groupName, groupType, setName));
    }

    @GetMapping(value = "/")
    @Operation(summary = "get models names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "names of all models")})
    public ResponseEntity<List<SimpleModel>> getModels() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getModels());
    }

    // --- BEGIN parameter definition-related endpoints --- //
    @GetMapping(value = "/{modelName}/parameters/definitions")
    @Operation(summary = "get parameters definitions for a given model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "parameter definitions of the model")})
    public ResponseEntity<List<ModelParameterDefinition>> getParameterDefinitionsFromModel(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getParameterDefinitionsFromModel(modelName));
    }

    @PostMapping(value = "/{modelName}/parameters/definitions")
    @Operation(summary = "Add new parameter definitions to model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> addNewParameterDefinitionsToModel(@PathVariable("modelName") String modelName, @RequestBody List<ModelParameterDefinition> parameterDefinitions) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.addNewParameterDefinitionsToModel(modelName, parameterDefinitions));
    }

    @PatchMapping(value = "/{modelName}/parameters/definitions/add")
    @Operation(summary = "Add existing parameter definitions to model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> addExistingParameterDefinitionsToModel(@PathVariable("modelName") String modelName, @RequestParam("parameterOrigin") ParameterOrigin origin, @RequestBody List<String> parameterDefinitionNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.addExistingParameterDefinitionsToModel(modelName, parameterDefinitionNames, origin));
    }

    @PatchMapping(value = "/{modelName}/parameters/definitions/remove")
    @Operation(summary = "Remove existing parameter definitions from model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> removeExistingParameterDefinitionsFromModel(@PathVariable("modelName") String modelName, @RequestBody List<String> parameterDefinitionNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeExistingParameterDefinitionsFromModel(modelName, parameterDefinitionNames));
    }

    @PatchMapping(value = "/{modelName}/parameters/definitions/remove-all")
    @Operation(summary = "Reset empty parameter definitions on model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> removeAllParameterDefinitionsFromModel(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeAllParameterDefinitionsOnModel(modelName));
    }

    @PostMapping(value = "/parameters/definitions")
    @Operation(summary = "Save new parameter definitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved parameter definitions")})
    public ResponseEntity<List<ModelParameterDefinition>> saveNewParameterDefinitions(@RequestBody List<ModelParameterDefinition> parameterDefinitions) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveNewParameterDefinitions(parameterDefinitions));
    }

    @DeleteMapping(value = "/parameters/definitions")
    @Operation(summary = "Delete parameter definitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted parameter definitions")})
    public ResponseEntity<List<String>> deleteParameterDefinitions(@RequestBody List<String> parameterDefinitionNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.deleteParameterDefinitions(parameterDefinitionNames));
    }
    // --- END parameter definition-related endpoints --- //

    // --- BEGIN variable-related endpoints --- //
    @GetMapping(value = "/{modelName}/variables")
    @Operation(summary = "get variable definitions for a given model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "variable definitions of the model")})
    public ResponseEntity<List<ModelVariableDefinition>> getVariablesFromModel(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getVariableDefinitionsFromModel(modelName));
    }

    @PostMapping(value = "/{modelName}/variables")
    @Operation(summary = "Add new variable definitions to model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> addNewVariablesToModel(@PathVariable("modelName") String modelName, @RequestBody List<ModelVariableDefinition> variableDefinitions) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.addNewVariableDefinitionsToModel(modelName, variableDefinitions));
    }

    @PatchMapping(value = "/{modelName}/variables/add")
    @Operation(summary = "Add existing variable definitions to model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> addExistingVariablesToModel(@PathVariable("modelName") String modelName, @RequestBody List<String> variableDefinitionNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.addExistingVariableDefinitionsToModel(modelName, variableDefinitionNames));
    }

    @PatchMapping(value = "/{modelName}/variables/remove")
    @Operation(summary = "Remove existing variable definitions from model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> removeExistingVariablesFromModel(@PathVariable("modelName") String modelName, @RequestBody List<String> variableDefinitionNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeExistingVariableDefinitionsFromModel(modelName, variableDefinitionNames));
    }

    @PatchMapping(value = "/{modelName}/variables/remove-all")
    @Operation(summary = "Reset empty variable definitions on model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> removeAllVariablesOnModel(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeAllVariableDefinitionsOnModel(modelName));
    }

    @GetMapping(value = "/variables-sets/{variableSetName}/variables")
    @Operation(summary = "Add variable definitions from variables set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "variable definitions of variables set")})
    public ResponseEntity<List<ModelVariableDefinition>> getVariablesFromVariablesSet(@PathVariable("variableSetName") String variableSetName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getVariableDefinitionsFromVariablesSet(variableSetName));
    }

    @PostMapping(value = "/variables-sets/{variableSetName}/variables")
    @Operation(summary = "Add new variable definitions to variables set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved variables set")})
    public ResponseEntity<VariablesSet> addNewVariableDefinitionToVariablesSet(@PathVariable("variableSetName") String variableSetName, @RequestBody List<ModelVariableDefinition> variableDefinitions) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.addNewVariableDefinitionToVariablesSet(variableSetName, variableDefinitions));
    }

    @PatchMapping(value = "/variables-sets/{variableSetName}/variables/remove")
    @Operation(summary = "Remove existing variable definitions from variables set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved variables set")})
    public ResponseEntity<VariablesSet> removeExistingVariableDefinitionFromVariablesSet(@PathVariable("variableSetName") String variableSetName, @RequestBody List<String> variableDefinitionNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeExistingVariableDefinitionFromVariablesSet(variableSetName, variableDefinitionNames));
    }

    @PatchMapping(value = "/variables-sets/{variableSetName}/variables/remove-all")
    @Operation(summary = "Reset empty variable definitions on variables set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved variables set")})
    public ResponseEntity<VariablesSet> removeAllVariableDefinitionOnVariablesSet(@PathVariable("variableSetName") String variableSetName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeAllVariableDefinitionOnVariablesSet(variableSetName));
    }

    @GetMapping(value = "/{modelName}/variables-sets")
    @Operation(summary = "get variable sets for a given model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "variable sets of the model")})
    public ResponseEntity<List<VariablesSet>> getVariablesSetsFromModel(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.getVariablesSetsFromModel(modelName));
    }

    @PostMapping(value = "/{modelName}/variables-sets")
    @Operation(summary = "Add new variables set to model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> addNewVariablesSetsToModel(@PathVariable("modelName") String modelName, @RequestBody List<VariablesSet> variableSets) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.addNewVariablesSetsToModel(modelName, variableSets));
    }

    @PatchMapping(value = "/{modelName}/variables-sets/add")
    @Operation(summary = "Add existing variables set to model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> addExistingVariablesSetsToModel(@PathVariable("modelName") String modelName, @RequestBody List<String> variablesSetNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.addExistingVariablesSetsToModel(modelName, variablesSetNames));
    }

    @PatchMapping(value = "/{modelName}/variables-sets/remove")
    @Operation(summary = "Remove existing variables set from model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> removeExistingVariablesSetsFromModel(@PathVariable("modelName") String modelName, @RequestBody List<String> variablesSetNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeExistingVariablesSetsFromModel(modelName, variablesSetNames));
    }

    @PatchMapping(value = "/{modelName}/variables-sets/remove-all")
    @Operation(summary = "Reset empty variables sets on model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved model")})
    public ResponseEntity<Model> removeAllExistingVariablesSetsFromModel(@PathVariable("modelName") String modelName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.removeAllExistingVariablesSetsFromModel(modelName));
    }

    @PostMapping(value = "/variables")
    @Operation(summary = "Save new variable definitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved variable definitions")})
    public ResponseEntity<List<ModelVariableDefinition>> saveNewVariables(@RequestBody List<ModelVariableDefinition> variableDefinitions) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveNewVariableDefinitions(variableDefinitions));
    }

    @DeleteMapping(value = "/variables")
    @Operation(summary = "Delete variables")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted variables")})
    public ResponseEntity<List<String>> deleteVariables(@RequestBody List<String> variableDefinitionNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.deleteVariableDefinitions(variableDefinitionNames));
    }

    @PostMapping(value = "/variables-sets")
    @Operation(summary = "Save new variables set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved variables set")})
    public ResponseEntity<VariablesSet> saveNewVariablesSet(@RequestBody VariablesSet variablesSet) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.saveNewVariablesSet(variablesSet));
    }

    @DeleteMapping(value = "/variables-sets")
    @Operation(summary = "Delete variables set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted variables set")})
    public ResponseEntity<List<String>> deleteVariablesSets(@RequestBody List<String> variablesSetNames) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(modelService.deleteVariablesSets(variablesSetNames));
    }
    // --- END variable-related endpoints --- //

}
