package org.gridsuite.mapping.server.error;

import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.mapping.server.error.DynamicMappingErrorBusinessCode.DELETE_FILTER_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicMappingExceptionHandlerTest {
    private DynamicMappingExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DynamicMappingExceptionHandler(() -> "dynamic-mapping");
    }

    @Test
    void mapsInteralErrorBusinessErrorToStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/results-endpoint/uuid");
        DynamicMappingException exception = new DynamicMappingException(DELETE_FILTER_ERROR, "filter deleted");
        ResponseEntity<PowsyblWsProblemDetail> response = handler.handleShortcircuitException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertEquals("", response.getBody().getBusinessErrorCode());
    }
}
