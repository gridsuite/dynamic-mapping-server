/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.error;

import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.mapping.server.error.DynamicMappingErrorBusinessCode.MAPPING_NAME_NOT_PROVIDED;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
class DynamicMappingExceptionHandlerTest {
    private DynamicMappingExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DynamicMappingExceptionHandler(() -> "dynamic-mapping");
    }

    @Test
    void mapsInteralErrorBusinessErrorToStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/results-endpoint/uuid");
        DynamicMappingException exception = new DynamicMappingException(MAPPING_NAME_NOT_PROVIDED, "filter deleted");
        ResponseEntity<PowsyblWsProblemDetail> response = handler.handleDynamicMappingException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertEquals("dynamicMapping.mappingNameNotProvided", response.getBody().getBusinessErrorCode());
    }
}
