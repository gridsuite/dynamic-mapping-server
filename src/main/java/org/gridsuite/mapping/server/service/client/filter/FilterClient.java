/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.client.filter;

import org.gridsuite.filter.expertfilter.ExpertFilter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.gridsuite.mapping.server.service.client.RestClient.DELIMITER;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public interface FilterClient {
    String API_VERSION = "v1";
    String FILTER_BASE_END_POINT = "filters";
    String FILTER_GET_END_POINT = FILTER_BASE_END_POINT + DELIMITER + "metadata";
    String FILTER_CREATE_END_POINT = FILTER_BASE_END_POINT;
    String FILTER_DUPLICATE_END_POINT = FILTER_BASE_END_POINT;
    String FILTER_DELETE_END_POINT = FILTER_BASE_END_POINT;

    List<ExpertFilter> createFilters(Map<UUID, ExpertFilter> filtersToCreateMap);

    List<ExpertFilter> duplicateFilters(Map<UUID, UUID> filterUuidsToDuplicateMap);

    void deleteFilters(List<UUID> filterUuids);

    List<ExpertFilter> getFilters(List<UUID> filterUuids);

}
