/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getStatusText());
    }

    @ExceptionHandler(DynamicMappingException.class)
    protected ResponseEntity<Object> handleDynamicSimulationException(DynamicMappingException exception) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(exception.getMessage(), exception);
        }

        DynamicMappingException.Type type = exception.getType();
        return switch (type) {
            case MAPPING_NAME_NOT_PROVIDED
                -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
            case FILTER_NOT_FOUND
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
            case URI_SYNTAX,
                GET_FILTER_ERROR,
                CREATE_FILTER_ERROR,
                DUPLICATE_FILTER_ERROR,
                DELETE_FILTER_ERROR
                -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        };
    }
}
