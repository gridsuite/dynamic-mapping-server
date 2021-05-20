package org.gridsuite.mapping.server.controller;

import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.service.MappingService;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.annotations.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/mappings")
@Api(value = "Mapping server")
@AllArgsConstructor
public class MappingController {

    private final MappingService mappingService;

    @GetMapping(value = "/")
    @ApiOperation(value = "Get all mappings")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The list of mappings")})
    public ResponseEntity<List<InputMapping>> getMappingList() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappingService.getMappingList());
    }

    @PostMapping(value = "/{mappingName}")
    @ApiOperation(value = "Create a mapping")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The id of the mapping"),
            @ApiResponse(code = 409, message = "The mapping already exist"),
            @ApiResponse(code = 500, message = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<InputMapping> createMapping(@PathVariable("mappingName") String mappingName, @RequestBody InputMapping mapping) {
        InputMapping createMapping = mappingService.createMapping(mappingName, mapping);
        return ResponseEntity.ok().body(createMapping);
    }

    @DeleteMapping(path = "/{mappingName}")
    @ApiOperation(value = "delete the mapping")
    @ApiResponse(code = 200, message = "Mapping deleted")
    public ResponseEntity<String> deleteMapping(@PathVariable("mappingName") String mappingName) {
        mappingService.deleteMapping(mappingName);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(mappingName);
    }

    @PostMapping(value = "/rename/{oldName}/to/{newName}")
    @ApiOperation(value = "Rename a mapping")
    @ApiResponses(value = {@ApiResponse(code = 200, message = " Both names of the mapping"),
            @ApiResponse(code = 404, message = "Mapping not found"),
            @ApiResponse(code = 500, message = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<RenameObject> renameMapping(@PathVariable("oldName") String oldName, @PathVariable("newName") String newName) {
        RenameObject renamedMapping = mappingService.renameMapping(oldName, newName);
        return ResponseEntity.ok().body(renamedMapping);
    }

    @PostMapping(value = "/copy/{originalName}/to/{copyName}")
    @ApiOperation(value = "Copy a mapping")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Mapping Copy"),
            @ApiResponse(code = 404, message = "Mapping not found"),
            @ApiResponse(code = 500, message = "The storage is down or a mapping with the same name already exists")})
    public ResponseEntity<InputMapping> copyMapping(@PathVariable("originalName") String originalName, @PathVariable("copyName") String copyName) {
        InputMapping copiedMapping = mappingService.copyMapping(originalName, copyName);
        return ResponseEntity.ok().body(copiedMapping);
    }

}
