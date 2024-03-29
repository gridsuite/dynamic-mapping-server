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
import org.gridsuite.mapping.server.dto.MatchedRule;
import org.gridsuite.mapping.server.dto.NetworkValues;
import org.gridsuite.mapping.server.dto.OutputNetwork;
import org.gridsuite.mapping.server.dto.RuleToMatch;
import org.gridsuite.mapping.server.service.NetworkService;
import org.gridsuite.mapping.server.service.implementation.NetworkServiceImpl;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RestController
@RequestMapping(value = "/network")
@Tag(name = "Mapping network server")
@AllArgsConstructor
@ComponentScan(basePackageClasses = {NetworkServiceImpl.class})

public class NetworkController {

    private final NetworkService networkService;

    @GetMapping(value = "/{networkUuid}/values")
    @Operation(summary = "Fetch property values of a known network from the server")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of property values of the network")})

    public ResponseEntity<NetworkValues> getNetworkValuesFromExistingCase(@PathVariable("networkUuid") UUID networkUuid) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(networkService.getNetworkValuesFromExistingNetwork(networkUuid));
    }

    @GetMapping(value = "/")
    @Operation(summary = "Get all known networks names")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of the network names")})

    public ResponseEntity<List<OutputNetwork>> getKnownNetworks() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(networkService.getNetworks());
    }

    @PostMapping(value = "/new")
    @Operation(summary = "Post a network and retrieve its values")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of property values of the network")})
    public ResponseEntity<NetworkValues> getNetworkValues(@RequestPart("file") MultipartFile networkFile) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(networkService.getNetworkValues(networkFile));
    }

    @PostMapping(value = "/{networkUuid}/matches/rule")
    @Operation(summary = "Get the equipment ids that matches the given rule.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Possible property values of the network")})
    public ResponseEntity<MatchedRule> getNetworkMatches(@PathVariable("networkUuid") UUID networkUuid, @RequestBody RuleToMatch ruleToMatch) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(networkService.getNetworkMatches(networkUuid, ruleToMatch));
    }

}
