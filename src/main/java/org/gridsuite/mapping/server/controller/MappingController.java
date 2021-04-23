package org.gridsuite.mapping.server.controller;

import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.service.MappingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping(value = "/{mappingName} ")
    @ApiOperation(value = "Create a mapping")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The id of the mapping"),
            @ApiResponse(code = 409, message = "The mapping already exist"),
            @ApiResponse(code = 500, message = "The storage is down or a file with the same name already exists")})
    public ResponseEntity<InputMapping> createMapping(@PathVariable("mappingName") String mappingName, @RequestBody InputMapping mapping) {
    InputMapping createMapping = mappingService.createMapping(mappingName, mapping)
        return ResponseEntity.ok().body(createMapping);
    }


    @DeleteMapping(value = "/{mappingName}")
    @ApiOperation(value = "delete the mapping")
    @ApiResponse(code = 200, message = "Mapping deleted")
    public ResponseEntity<Void> deleteMapping(@PathVariable("mappingName") String mappingName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                mappingService.deleteMapping(mappingName));
    }

}
