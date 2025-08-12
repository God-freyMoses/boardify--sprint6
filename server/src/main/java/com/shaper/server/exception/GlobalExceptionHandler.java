package com.shaper.server.exception;

import com.shaper.server.system.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Result> handleDataNotFoundException(DataNotFoundException e) {
        log.warn("Data not found: {}", e.getMessage());
        Result result = new Result(HttpStatus.NOT_FOUND.value(), false, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnAutororizedException.class)
    public ResponseEntity<Result> handleUnAutororizedException(UnAutororizedException e) {
        log.warn("Unauthorized access: {}", e.getMessage());
        Result result = new Result(HttpStatus.UNAUTHORIZED.value(), false, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result> handleIllegalStateException(IllegalStateException e) {
        log.warn("Illegal state: {}", e.getMessage());
        Result result = new Result(HttpStatus.CONFLICT.value(), false, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        Result result = new Result(HttpStatus.BAD_REQUEST.value(), false, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Result result = new Result(HttpStatus.BAD_REQUEST.value(), false, "Validation failed", errors);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception caught: {}", e.getMessage(), e);
        Result result = new Result(HttpStatus.INTERNAL_SERVER_ERROR.value(), false, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleGenericException(Exception e) {
        log.error("Generic exception caught: {}", e.getMessage(), e);
        Result result = new Result(HttpStatus.INTERNAL_SERVER_ERROR.value(), false, "An unexpected error occurred: " + e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
