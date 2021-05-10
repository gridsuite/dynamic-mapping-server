package org.gridsuite.mapping.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.dto.Script;
import org.gridsuite.mapping.server.service.ScriptService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/scripts")
@Api(value = "Mapping server")
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
    public ResponseEntity<Void> deleteScript(@PathVariable("scriptName") String scriptName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                scriptService.deleteScript(scriptName));
    }

    @PutMapping(value = "/{scriptName}")
    @ApiOperation(value = "update the script")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Script updated"),
            @ApiResponse(code = 404, message = "Script not found")})
    public ResponseEntity<Void> updateScript(@PathVariable("scriptName") String scriptName, @RequestBody Script script) {
        scriptService.saveScript(scriptName, script);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(null);
    }

}
