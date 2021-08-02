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
@Tag(name = "Mapping server")
@AllArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    @GetMapping(value = "/from/{mappingName}")
    @Operation(summary = "Convert a mapping to a groovy script and return it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The converted mapping"),
            @ApiResponse(responseCode = "404", description = "Mapping not found"),
            @ApiResponse(responseCode = "400", description = "Something happened") })
    public ResponseEntity<Script> createFromMapping(@PathVariable("mappingName") String mappingName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(scriptService.createFromMapping(mappingName));
    }

    @GetMapping(value = "/")
    @Operation(summary = "Get all scripts")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of scripts")})
    public ResponseEntity<List<Script>> getScriptList() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(scriptService.getAllScripts());
    }

    @DeleteMapping(value = "/{scriptName}")
    @Operation(summary = "delete the script")
    @ApiResponse(responseCode = "200", description = "Script deleted")
    public ResponseEntity<String> deleteScript(@PathVariable("scriptName") String scriptName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                scriptService.deleteScript(scriptName));
    }

    @PostMapping(value = "/{scriptName}")
    @Operation(summary = "Save a script")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Script save"),
            @ApiResponse(responseCode = "404", description = "Script not found")})
    public ResponseEntity<Void> saveScript(@PathVariable("scriptName") String scriptName, @RequestBody Script script) {
        scriptService.saveScript(scriptName, script);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(null);
    }

    @PostMapping(value = "/rename/{oldName}/to/{newName}")
    @Operation(summary = "Rename a script")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = " Both names of the script"),
            @ApiResponse(responseCode = "404", description = "Script not found"),
            @ApiResponse(responseCode = "500", description = "The storage is down or a script with the same name already exists")})
    public ResponseEntity<RenameObject> renameMapping(@PathVariable("oldName") String oldName, @PathVariable("newName") String newName) {
        RenameObject renamedMapping = scriptService.renameScript(oldName, newName);
        return ResponseEntity.ok().body(renamedMapping);
    }

    @PostMapping(value = "/copy/{originalName}/to/{copyName}")
    @Operation(summary = "Copy a script")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Script Copy"),
            @ApiResponse(responseCode = "404", description = "Script not found"),
            @ApiResponse(responseCode = "500", description = "The storage is down or a script with the same name already exists")})
    public ResponseEntity<Script> copyMapping(@PathVariable("originalName") String originalName, @PathVariable("copyName") String copyName) {
        Script copiedScript = scriptService.copyScript(originalName, copyName);
        return ResponseEntity.ok().body(copiedScript);
    }

}
