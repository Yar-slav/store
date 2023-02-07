package com.gridu.store.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A class that represents an API response.
 */
@Getter
@RequiredArgsConstructor
public class ApiExceptionObject {

    private final String httpStatus;
    private final String message;
    private final String timestamp;
}
