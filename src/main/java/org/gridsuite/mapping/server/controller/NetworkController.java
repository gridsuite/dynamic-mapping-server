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
import org.gridsuite.mapping.server.dto.EquipmentValues;
import org.gridsuite.mapping.server.service.NetworkService;
import org.gridsuite.mapping.server.service.implementation.NetworkServiceImpl;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/networks")
@Api(value = "Mapping network server")
@AllArgsConstructor
@ComponentScan(basePackageClasses = {NetworkServiceImpl.class})

public class NetworkController {

    private final NetworkService networkService;

    @GetMapping(value = "/{caseUUID}/values")
    @ApiOperation(value = "Convert a mapping to a groovy script and return it")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Possible property values of the network") })

    public ResponseEntity<Flux<EquipmentValues>> getNetworkValuesFromExistingCase(@PathVariable("caseUUID") UUID caseUUID) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(networkService.getNetworkValuesFromExistingCase(Mono.just(caseUUID)));
    }

    @PostMapping(value = "/new")
    @ApiOperation(value = "Import an iidm")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Possible property values of the network") })
    public ResponseEntity<Flux<EquipmentValues>> getNetworkValues(@RequestPart FilePart networkFile) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(networkService.getNetworkValues(Mono.just(networkFile)));
    }

}
