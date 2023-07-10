package org.gridsuite.mapping.server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler {
    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getStatusText());
    }
}
