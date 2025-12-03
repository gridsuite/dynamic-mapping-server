/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package org.gridsuite.mapping.server.error;

import com.powsybl.ws.commons.error.BusinessErrorCode;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
public enum DynamicMappingErrorBusinessCode implements BusinessErrorCode {
    MAPPING_NAME_NOT_PROVIDED("dynamicMapping.mappingNameNotProvided"),
    GET_FILTER_ERROR("dynamicMapping.getFilterError"),
    CREATE_FILTER_ERROR("dynamicMapping.createFilterError"),
    UPDATE_FILTER_ERROR("dynamicMapping.updateFilterError"),
    DUPLICATE_FILTER_ERROR("dynamicMapping.duplicateFilterError"),
    DELETE_FILTER_ERROR("dynamicMapping.deleteFilterError"),
    FILTER_NOT_FOUND("dynamicMapping.filterNotFound");

    private final String code;

    DynamicMappingErrorBusinessCode(String code) {
        this.code = code;
    }

    public String value() {
        return code;
    }
}
