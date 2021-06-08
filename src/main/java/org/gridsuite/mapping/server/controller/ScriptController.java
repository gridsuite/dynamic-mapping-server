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
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.dto.Script;
import org.gridsuite.mapping.server.service.ScriptService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/scripts")
@Api(value = "Mapping script server")
@AllArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    @GetMapping(value = "/from/{mappingName}")
    @ApiOperation(value = "Convert a mapping to a groovy script and return it")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The converted mapping"),
            @ApiResponse(code = 404, message = "Mapping not found"),
            @ApiResponse(code = 400, message = "Something happened") })
    public ResponseEntity<Script> createFromMapping(@PathVariable("mappingName") String mappingName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(scriptService.createFromMapping(mappingName));
    }

    @GetMapping(value = "/")
    @ApiOperation(value = "Get all scripts")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The list of scripts")})
    public ResponseEntity<List<Script>> getScriptList() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(scriptService.getAllScripts());
    }

    @DeleteMapping(value = "/{scriptName}")
    @ApiOperation(value = "delete the script")
    @ApiResponse(code = 200, message = "Script deleted")
    public ResponseEntity<String> deleteScript(@PathVariable("scriptName") String scriptName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                scriptService.deleteScript(scriptName));
    }

    @PostMapping(value = "/{scriptName}")
    @ApiOperation(value = "Save a script")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Script save"),
            @ApiResponse(code = 404, message = "Script not found")})
    public ResponseEntity<Void> saveScript(@PathVariable("scriptName") String scriptName, @RequestBody Script script) {
        scriptService.saveScript(scriptName, script);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(null);
    }

    @PostMapping(value = "/rename/{oldName}/to/{newName}")
    @ApiOperation(value = "Rename a script")
    @ApiResponses(value = {@ApiResponse(code = 200, message = " Both names of the script"),
            @ApiResponse(code = 404, message = "Script not found"),
            @ApiResponse(code = 500, message = "The storage is down or a script with the same name already exists")})
    public ResponseEntity<RenameObject> renameMapping(@PathVariable("oldName") String oldName, @PathVariable("newName") String newName) {
        RenameObject renamedMapping = scriptService.renameScript(oldName, newName);
        return ResponseEntity.ok().body(renamedMapping);
    }

    @PostMapping(value = "/copy/{originalName}/to/{copyName}")
    @ApiOperation(value = "Copy a script")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Script Copy"),
            @ApiResponse(code = 404, message = "Script not found"),
            @ApiResponse(code = 500, message = "The storage is down or a script with the same name already exists")})
    public ResponseEntity<Script> copyMapping(@PathVariable("originalName") String originalName, @PathVariable("copyName") String copyName) {
        Script copiedScript = scriptService.copyScript(originalName, copyName);
        return ResponseEntity.ok().body(copiedScript);
    }

}
