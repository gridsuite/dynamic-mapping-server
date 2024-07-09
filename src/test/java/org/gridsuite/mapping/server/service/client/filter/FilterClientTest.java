/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.service.client.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.StringExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.mapping.server.DynamicMappingException;
import org.gridsuite.mapping.server.service.client.AbstractWireMockRestClientTest;
import org.gridsuite.mapping.server.service.client.filter.impl.FilterClientImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.gridsuite.mapping.server.DynamicMappingException.Type.*;
import static org.gridsuite.mapping.server.service.client.filter.FilterClient.*;
import static org.gridsuite.mapping.server.service.client.utils.UrlUtils.buildEndPointUrl;
import static org.gridsuite.mapping.server.utils.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public class FilterClientTest extends AbstractWireMockRestClientTest {

    private FilterClient filterClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private List<ExpertFilter> createFilterList() {
        // Create for load
        List<AbstractExpertRule> loadRules = new ArrayList<>();
        StringExpertRule loadRule = StringExpertRule.builder().value("LOAD")
                .field(FieldType.ID).operator(OperatorType.IS).build();
        loadRules.add(loadRule);

        CombinatorExpertRule loadRootCombinator = CombinatorExpertRule.builder()
            .combinator(CombinatorType.AND).rules(loadRules).build();

        ExpertFilter loadExpertFilter = new ExpertFilter(UUID.randomUUID(), null, EquipmentType.LOAD, loadRootCombinator);

        // Create for generator
        List<AbstractExpertRule> generatorRules = new ArrayList<>();
        StringExpertRule generatorRule = StringExpertRule.builder().value("GENERATOR")
                .field(FieldType.ID).operator(OperatorType.IS).build();
        generatorRules.add(generatorRule);

        CombinatorExpertRule generatorRootCombinator = CombinatorExpertRule.builder()
            .combinator(CombinatorType.AND).rules(generatorRules).build();

        ExpertFilter generatorExpertFilter = new ExpertFilter(UUID.randomUUID(), null, EquipmentType.GENERATOR, generatorRootCombinator);

        return List.of(loadExpertFilter, generatorExpertFilter);
    }

    @Override
    public void setup() {
        super.setup();
        filterClient = new FilterClientImpl(
                // use new WireMockServer(FILTER_PORT) to test with local server if needed
                initMockWebServer(new WireMockServer(wireMockConfig().dynamicPort())),
                restTemplate,
                objectMapper);
    }

    private String getEndpointUrl(String endpoint) {
        return buildEndPointUrl("", API_VERSION,
                endpoint);
    }

    @Test
    public void testCreateFilters() throws JsonProcessingException {
        // prepare filters to create
        List<ExpertFilter> filterList = createFilterList();
        Map<UUID, ExpertFilter> filtersToCreate = filterList.stream()
                .collect(Collectors.toMap(ExpertFilter::getId, filter -> filter));

        // mock response for test case POST with url - /filters/batch
        String endpointUrl = getEndpointUrl(FILTERS_CREATE_IN_BATCH_ENDPOINT);
        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(filtersToCreate)))
                .willReturn(WireMock.ok()
                        .withBody(objectMapper.writeValueAsString(filterList))
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                ));

        List<ExpertFilter> createdFilters = filterClient.createFilters(filtersToCreate);

        // check result
        for (ExpertFilter filter : createdFilters) {
            assertThat(filter).recursivelyEquals(filtersToCreate.get(filter.getId()));
        }
    }

    @Test
    public void testCreateFiltersGivenException() throws JsonProcessingException {
        // prepare filters to create
        List<ExpertFilter> filterList = createFilterList();
        Map<UUID, ExpertFilter> filtersToCreate = filterList.stream()
                .collect(Collectors.toMap(ExpertFilter::getId, filter -> filter));

        // mock response for test case POST with url - /filters/batch
        String endpointUrl = getEndpointUrl(FILTERS_CREATE_IN_BATCH_ENDPOINT);
        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(filtersToCreate)))
                .willReturn(WireMock.serverError()
                        .withBody(ERROR_MESSAGE_JSON)
                ));

        DynamicMappingException exception = catchThrowableOfType(
                () -> filterClient.createFilters(filtersToCreate),
                DynamicMappingException.class);

        // check result
        assertThat(exception.getType())
                .isEqualTo(CREATE_FILTER_ERROR);
        assertThat(exception.getMessage())
                .isEqualTo(ERROR_MESSAGE);
    }

    public void testUpdateFilters() throws JsonProcessingException {
        // prepare filters to update
        List<ExpertFilter> filterList = createFilterList();
        Map<UUID, ExpertFilter> filtersToUpdate = filterList.stream()
                .collect(Collectors.toMap(ExpertFilter::getId, filter -> filter));

        // mock response for test case PUT with url - /filters/batch
        String endpointUrl = getEndpointUrl(FILTERS_UPDATE_IN_BATCH_ENDPOINT);
        wireMockServer.stubFor(WireMock.put(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(filtersToUpdate)))
                .willReturn(WireMock.ok()
                        .withBody(objectMapper.writeValueAsString(filterList))
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                ));

        List<ExpertFilter> updatedFilters = filterClient.updateFilters(filtersToUpdate);

        // check result
        for (ExpertFilter filter : updatedFilters) {
            assertThat(filter).recursivelyEquals(filtersToUpdate.get(filter.getId()));
        }
    }

    @Test
    public void testUpdateFiltersGivenException() throws JsonProcessingException {
        // prepare filters to update
        List<ExpertFilter> filterList = createFilterList();
        Map<UUID, ExpertFilter> filtersToUpdate = filterList.stream()
                .collect(Collectors.toMap(ExpertFilter::getId, filter -> filter));

        // mock response for test case PUT with url - /filters/batch
        String endpointUrl = getEndpointUrl(FILTERS_UPDATE_IN_BATCH_ENDPOINT);
        wireMockServer.stubFor(WireMock.put(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(filtersToUpdate)))
                .willReturn(WireMock.serverError()
                        .withBody(ERROR_MESSAGE_JSON)
                ));

        DynamicMappingException exception = catchThrowableOfType(
                () -> filterClient.updateFilters(filtersToUpdate),
                DynamicMappingException.class);

        // check result
        assertThat(exception.getType())
                .isEqualTo(UPDATE_FILTER_ERROR);
        assertThat(exception.getMessage())
                .isEqualTo(ERROR_MESSAGE);
    }

    @Test
    public void testDuplicateFilters() throws JsonProcessingException {
        // prepare filters to duplicate
        List<ExpertFilter> filterList = createFilterList();
        List<UUID> sourceUuids = filterList.stream().map(ExpertFilter::getId).toList();
        Map<UUID, UUID> sourceUuidAndNewUuidMap = sourceUuids.stream()
                .collect(Collectors.toMap(uuid -> uuid, uuid -> UUID.randomUUID()));

        // mock response for test case POST with url - /filters/batch/duplicate
        String endpointUrl = getEndpointUrl(FILTERS_DUPLICATE_IN_BATCH_ENDPOINT);
        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(sourceUuids)))
                .willReturn(WireMock.ok()
                        .withBody(objectMapper.writeValueAsString(sourceUuidAndNewUuidMap))
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                ));

        Map<UUID, UUID> sourceUuidAndNewUuidMapResult = filterClient.duplicateFilters(sourceUuids);

        // check result
        assertThat(sourceUuidAndNewUuidMap).containsExactlyEntriesOf(sourceUuidAndNewUuidMapResult);
    }

    @Test
    public void testDuplicateFiltersGivenException() throws JsonProcessingException {
        // prepare filters to duplicate
        List<ExpertFilter> filterList = createFilterList();
        List<UUID> sourceUuids = filterList.stream().map(ExpertFilter::getId).toList();

        // mock response for test case POST with url - /filters/batch/duplicate
        String endpointUrl = getEndpointUrl(FILTERS_DUPLICATE_IN_BATCH_ENDPOINT);
        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(sourceUuids)))
                .willReturn(WireMock.serverError()
                        .withBody(ERROR_MESSAGE_JSON)
                ));

        DynamicMappingException exception = catchThrowableOfType(
                () -> filterClient.duplicateFilters(sourceUuids),
                DynamicMappingException.class);

        // check result
        assertThat(exception.getType())
                .isEqualTo(DUPLICATE_FILTER_ERROR);
        assertThat(exception.getMessage())
                .isEqualTo(ERROR_MESSAGE);
    }

    @Test
    public void testDeleteFilters() throws JsonProcessingException {
        // prepare filters to delete
        List<ExpertFilter> filterList = createFilterList();
        List<UUID> sourceUuids = filterList.stream().map(ExpertFilter::getId).toList();

        // mock response for test case DELETE with url - /filters
        String endpointUrl = getEndpointUrl(FILTERS_BASE_ENDPOINT);
        wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(sourceUuids)))
                .willReturn(WireMock.ok()));

        assertDoesNotThrow(() -> filterClient.deleteFilters(sourceUuids));
    }

    @Test
    public void testDeleteFiltersGivenException() throws JsonProcessingException {
        // prepare filters to delete
        List<ExpertFilter> filterList = createFilterList();
        List<UUID> sourceUuids = filterList.stream().map(ExpertFilter::getId).toList();

        // mock response for test case DELETE with url - /filters
        String endpointUrl = getEndpointUrl(FILTERS_BASE_ENDPOINT);
        wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching(endpointUrl))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(sourceUuids)))
                .willReturn(WireMock.serverError()
                        .withBody(ERROR_MESSAGE_JSON)));

        DynamicMappingException exception = catchThrowableOfType(
                () -> filterClient.deleteFilters(sourceUuids),
                DynamicMappingException.class);

        // check result
        assertThat(exception.getType())
                .isEqualTo(DELETE_FILTER_ERROR);
        assertThat(exception.getMessage())
                .isEqualTo(ERROR_MESSAGE);
    }

    @Test
    public void testGetFilters() throws JsonProcessingException {
        // prepare filters to get
        List<ExpertFilter> filterList = createFilterList();
        List<UUID> uuids = filterList.stream().map(ExpertFilter::getId).toList();

        // mock response for test case GET with url - /filters
        String endpointUrl = getEndpointUrl(FILTERS_GET_ENDPOINT);
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching(endpointUrl + ".*"))
                .withQueryParam("ids", WireMock.matching(".*"))
                .willReturn(WireMock.ok()
                        .withBody(objectMapper.writeValueAsString(filterList))
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                ));

        List<ExpertFilter> filters = filterClient.getFilters(uuids);

        // check result
        Map<UUID, ExpertFilter> uuidFitlerMap = filterList.stream().collect(Collectors.toMap(ExpertFilter::getId, filter -> filter));
        for (ExpertFilter filter : filters) {
            assertThat(filter).recursivelyEquals(uuidFitlerMap.get(filter.getId()));
        }
    }

    @Test
    public void testGetFiltersGivenException() {
        // prepare filters to get
        List<ExpertFilter> filterList = createFilterList();
        List<UUID> uuids = filterList.stream().map(ExpertFilter::getId).toList();

        // mock response for test case GET with url - /filters
        String endpointUrl = getEndpointUrl(FILTERS_GET_ENDPOINT);
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching(endpointUrl + ".*"))
                .withQueryParam("ids", WireMock.matching(".*"))
                .willReturn(WireMock.serverError()
                        .withBody(ERROR_MESSAGE_JSON)
                ));

        DynamicMappingException exception = catchThrowableOfType(
                () -> filterClient.getFilters(uuids),
                DynamicMappingException.class);

        // check result
        assertThat(exception.getType())
                .isEqualTo(GET_FILTER_ERROR);
        assertThat(exception.getMessage())
                .isEqualTo(ERROR_MESSAGE);
    }
}
