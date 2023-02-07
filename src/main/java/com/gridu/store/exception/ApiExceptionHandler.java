package com.gridu.store.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiExceptionObject> handleException(ApiException ex) {
        HttpStatus httpStatus = ex.getExceptions().getHttpStatus();
        String message = ex.getExceptions().getMessage();
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                httpStatus.toString(),
                message,
                LocalDateTime.now().format(formatter)
        );
        return new ResponseEntity<>(apiExceptionObject, httpStatus);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleBadRequestException(MethodArgumentNotValidException ex) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String errorMessages = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                httpStatus.toString(),
                errorMessages,
                LocalDateTime.now().format(formatter)
        );
        return new ResponseEntity<>(apiExceptionObject, httpStatus);
    }
}
