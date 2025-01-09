package de.kulose.musicquizhost.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.InvocationTargetException;

@Slf4j
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle failures commonly thrown from code
     */
    @ExceptionHandler({InvocationTargetException.class, IllegalArgumentException.class, ClassCastException.class,
            ConversionFailedException.class})
    @ResponseBody
    public ResponseEntity handleMiscFailures(Throwable t) {
        return errorResponse(t, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    @ResponseBody
    public ResponseEntity handleBadRequestFailures(Throwable t) {
        return errorResponse(t, HttpStatus.BAD_REQUEST);
    }


    protected ResponseEntity<String> errorResponse(Throwable throwable,
                                                             HttpStatus status) {
        log.error(throwable.getMessage(), throwable);
        return ResponseEntity.status(status).body(throwable.getMessage());
    }

    protected <T> ResponseEntity<T> response(T body, HttpStatus status) {
        log.debug("Responding with a status of {}", status);
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }
}
