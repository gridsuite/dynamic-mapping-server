package org.gridsuite.mapping.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.gridsuite.mapping.server.dto.InstanceModel;
import org.gridsuite.mapping.server.service.InstanceModelService;
import org.gridsuite.mapping.server.service.implementation.InstanceModelServiceImpl;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/instances")
@Api(value = "Mapping instance model server")
@AllArgsConstructor
@ComponentScan(basePackageClasses = {InstanceModelServiceImpl.class})
public class InstanceModelController {

    private final InstanceModelService instanceModelService;

    @GetMapping(value = "/")
    @ApiOperation(value = "get model instances")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "all models instances")})
    public ResponseEntity<List<InstanceModel>> getModels() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(instanceModelService.getInstances());
    }
}
