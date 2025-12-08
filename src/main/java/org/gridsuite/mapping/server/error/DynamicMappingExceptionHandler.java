/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.error;

import com.powsybl.ws.commons.error.AbstractBusinessExceptionHandler;
import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import com.powsybl.ws.commons.error.ServerNameProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@ControllerAdvice
public class DynamicMappingExceptionHandler extends AbstractBusinessExceptionHandler<DynamicMappingException, DynamicMappingErrorBusinessCode> {
    protected DynamicMappingExceptionHandler(ServerNameProvider serverNameProvider) {
        super(serverNameProvider);
    }

    @Override
    protected @NonNull DynamicMappingErrorBusinessCode getBusinessCode(DynamicMappingException e) {
        return e.getBusinessErrorCode();
    }

    protected HttpStatus mapStatus(DynamicMappingErrorBusinessCode businessErrorCode) {
        return switch (businessErrorCode) {
            case MAPPING_NAME_NOT_PROVIDED -> HttpStatus.BAD_REQUEST;
        };
    }

    @ExceptionHandler(DynamicMappingException.class)
    protected ResponseEntity<PowsyblWsProblemDetail> handleDynamicMappingException(
            DynamicMappingException exception, HttpServletRequest request) {
        return super.handleDomainException(exception, request);
    }
}
