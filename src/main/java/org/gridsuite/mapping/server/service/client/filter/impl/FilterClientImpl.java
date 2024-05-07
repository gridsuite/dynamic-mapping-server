/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.client.filter.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.DynamicMappingException;
import org.gridsuite.mapping.server.service.client.AbstractRestClient;
import org.gridsuite.mapping.server.service.client.filter.FilterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.gridsuite.mapping.server.DynamicMappingException.Type.*;
import static org.gridsuite.mapping.server.service.client.utils.ExceptionUtils.handleHttpError;
import static org.gridsuite.mapping.server.service.client.utils.UrlUtils.buildEndPointUrl;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Service
public class FilterClientImpl extends AbstractRestClient implements FilterClient {

    @Autowired
    public FilterClientImpl(@Value("${gridsuite.services.filter-server.base-uri:http://filter-server/}") String baseUri, RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(baseUri, restTemplate, objectMapper);
    }

    @Override
    public List<ExpertFilter> getFilters(List<UUID> filterUuids) {
        if (CollectionUtils.isEmpty(filterUuids)) {
            return Collections.emptyList();
        }

        String endPointUrl = buildEndPointUrl(getBaseUri(), API_VERSION, FILTER_GET_END_POINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);
        uriComponentsBuilder.queryParam("ids", filterUuids);

        // call filter server Rest API
        try {
            return getRestTemplate().exchange(
                    uriComponentsBuilder.build().toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ExpertFilter>>() { }).getBody();

        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new DynamicMappingException(FILTER_NOT_FOUND, "Some filter not found");
            } else {
                throw handleHttpError(e, GET_FILTER_ERROR, getObjectMapper());
            }
        }
    }

    @Override
    public List<ExpertFilter> createFilters(Map<UUID, ExpertFilter> filtersToCreateMap) {
        if (filtersToCreateMap == null || filtersToCreateMap.isEmpty()) {
            return Collections.emptyList();
        }

        String endPointUrl = buildEndPointUrl(getBaseUri(), API_VERSION, FILTER_CREATE_IN_BATCH_END_POINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<UUID, ExpertFilter>> httpEntity = new HttpEntity<>(filtersToCreateMap, headers);

        // call filter server Rest API
        try {
            return getRestTemplate().exchange(
                    uriComponentsBuilder.build().toUriString(),
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<List<ExpertFilter>>() { }).getBody();
        } catch (HttpStatusCodeException e) {
            throw handleHttpError(e, CREATE_FILTER_ERROR, getObjectMapper());
        }
    }

    @Override
    public Map<UUID, UUID> duplicateFilters(List<UUID> filterUuids) {
        if (CollectionUtils.isEmpty(filterUuids)) {
            return Collections.emptyMap();
        }

        String endPointUrl = buildEndPointUrl(getBaseUri(), API_VERSION, FILTER_DUPLICATE_IN_BATCH_END_POINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<UUID>> httpEntity = new HttpEntity<>(filterUuids, headers);

        // call filter server Rest API
        try {
            return getRestTemplate().exchange(
                    uriComponentsBuilder.build().toUriString(),
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<Map<UUID, UUID>>() { }).getBody();
        } catch (HttpStatusCodeException e) {
            throw handleHttpError(e, CREATE_FILTER_ERROR, getObjectMapper());
        }
    }

    @Override
    public void deleteFilters(List<UUID> filterUuids) {
        if (CollectionUtils.isEmpty(filterUuids)) {
            return;
        }

        String endPointUrl = buildEndPointUrl(getBaseUri(), API_VERSION, FILTER_DELETE_IN_BATCH_END_POINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<UUID>> httpEntity = new HttpEntity<>(filterUuids, headers);

        // call filter server Rest API
        try {
            getRestTemplate().exchange(
                    uriComponentsBuilder.build().toUriString(),
                    HttpMethod.DELETE,
                    httpEntity,
                    Void.class);
        } catch (HttpStatusCodeException e) {
            throw handleHttpError(e, DELETE_FILTER_ERROR, getObjectMapper());
        }
    }
}
