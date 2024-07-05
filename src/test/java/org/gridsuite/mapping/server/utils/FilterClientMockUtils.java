/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.service.client.filter.FilterClient;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public final class FilterClientMockUtils {

    private FilterClientMockUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void mockAll(Map<UUID, ExpertFilter> filtersMockDB, FilterClient filterClient, ObjectMapper objectMapper) {
        mockCreateFilters(filtersMockDB, filterClient);
        mockGetFilters(filtersMockDB, filterClient);
        mockDuplicateFilters(filtersMockDB, filterClient, objectMapper);
        mockDeleteFilters(filtersMockDB, filterClient);
    }

    public static void mockDeleteFilters(Map<UUID, ExpertFilter> filtersMockDB, FilterClient filterClient) {
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            List<UUID> data = (List<UUID>) args[0];

            if (CollectionUtils.isEmpty(data)) {
                return null;
            }

            data.forEach(filtersMockDB::remove);
            return null;
        }).when(filterClient).deleteFilters(any());
    }

    public static void mockGetFilters(Map<UUID, ExpertFilter> filtersMockDB, FilterClient filterClient) {
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            List<UUID> data = (List<UUID>) args[0];

            if (CollectionUtils.isEmpty(data)) {
                return Collections.emptyList();
            }

            return data.stream().map(filtersMockDB::get).toList();
        }).when(filterClient).getFilters(any());
    }

    public static void mockDuplicateFilters(Map<UUID, ExpertFilter> filtersMockDB, FilterClient filterClient, ObjectMapper objectMapper) {
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            List<UUID> data = (List<UUID>) args[0];

            if (CollectionUtils.isEmpty(data)) {
                return Collections.emptyList();
            }

            Map<UUID, ExpertFilter> newData = new HashMap<>();
            Map<UUID, UUID> uuidsMap = new HashMap<>();
            data.forEach(sourceUuid -> {
                ExpertFilter sourceFilter = filtersMockDB.get(sourceUuid);
                try {
                    /* deep copy */
                    ExpertFilter newFilter = objectMapper.readValue(objectMapper.writeValueAsString(sourceFilter), ExpertFilter.class);
                    UUID newUuid = UUID.randomUUID();
                    uuidsMap.put(sourceUuid, newUuid);
                    newFilter.setId(newUuid);
                    newData.put(newUuid, newFilter);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
            filtersMockDB.putAll(newData);

            return uuidsMap;
        }).when(filterClient).duplicateFilters(any());
    }

    public static void mockCreateFilters(Map<UUID, ExpertFilter> filtersMockDB, FilterClient filterClient) {
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            Map<UUID, ExpertFilter> data = (Map<UUID, ExpertFilter>) args[0];

            if (data == null || data.isEmpty()) {
                return Collections.emptyList();
            }
            filtersMockDB.putAll(data);

            return List.copyOf(data.values());
        }).when(filterClient).createFilters(any());
    }
}
