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
import org.gridsuite.mapping.server.service.client.AbstractRestClient;
import org.gridsuite.mapping.server.service.client.filter.FilterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.gridsuite.mapping.server.service.client.utils.UrlUtils.buildEndPointUrl;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Service
public class FilterClientImpl extends AbstractRestClient implements FilterClient {

    @Autowired
    public FilterClientImpl(@Value("${gridsuite.services.filter-server.base-uri:http://filter-server/}") String baseUri,
                            RestClient restClient, ObjectMapper objectMapper) {
        super(baseUri, restClient, objectMapper);
    }

    private String getEndPointUrl(String endpoint) {
        return buildEndPointUrl(getBaseUri(), API_VERSION, endpoint);
    }

    @Override
    public List<ExpertFilter> getFilters(List<UUID> filterUuids) {
        if (CollectionUtils.isEmpty(filterUuids)) {
            return Collections.emptyList();
        }

        String endPointUrl = getEndPointUrl(FILTERS_GET_ENDPOINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);
        uriComponentsBuilder.queryParam("ids", filterUuids);

        // call filter server Rest API
        return getRestClient().get()
                .uri(uriComponentsBuilder.build().toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public List<ExpertFilter> createFilters(Map<UUID, ExpertFilter> filtersToCreateMap) {
        if (filtersToCreateMap == null || filtersToCreateMap.isEmpty()) {
            return Collections.emptyList();
        }

        String endPointUrl = getEndPointUrl(FILTERS_CREATE_IN_BATCH_ENDPOINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // call filter server Rest API
        return getRestClient().post()
                .uri(uriComponentsBuilder.build().toUriString())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(filtersToCreateMap)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExpertFilter>>() {
                });

    }

    @Override
    public List<ExpertFilter> updateFilters(Map<UUID, ExpertFilter> filtersToUpdateMap) {
        if (filtersToUpdateMap == null || filtersToUpdateMap.isEmpty()) {
            return Collections.emptyList();
        }

        String endPointUrl = getEndPointUrl(FilterClient.FILTERS_UPDATE_IN_BATCH_ENDPOINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        // call filter server Rest API
        return getRestClient().put()
                .uri(uriComponentsBuilder.build().toUriString())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(filtersToUpdateMap)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExpertFilter>>() {
                });

    }

    @Override
    public Map<UUID, UUID> duplicateFilters(List<UUID> filterUuids) {
        if (CollectionUtils.isEmpty(filterUuids)) {
            return Collections.emptyMap();
        }

        String endPointUrl = getEndPointUrl(FILTERS_DUPLICATE_IN_BATCH_ENDPOINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // call filter server Rest API
        return getRestClient().post()
                .uri(uriComponentsBuilder.build().toUriString())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(filterUuids)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<UUID, UUID>>() {
                });

    }

    @Override
    public void deleteFilters(List<UUID> filterUuids) {
        if (CollectionUtils.isEmpty(filterUuids)) {
            return;
        }

        String endPointUrl = getEndPointUrl(FILTERS_DELETE_IN_BATCH_ENDPOINT);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // call filter server Rest API
        getRestClient().method(HttpMethod.DELETE)
                .uri(uriComponentsBuilder.build().toUriString())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(filterUuids)
                .retrieve()
                .toBodilessEntity();

    }
}
