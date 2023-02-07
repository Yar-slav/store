package com.gridu.store.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Exceptions{

    ORDER_NOT_FOUND("Order not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_BELONG_USER("This number of order does not belong to you", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND("Product not found", HttpStatus.NOT_FOUND),
    PRODUCTS_NOT_ENOUGH("Amount of products not enough", HttpStatus.FORBIDDEN),
    USER_EXIST("User with this email already exist", HttpStatus.CONFLICT),
    USER_INCORRECT_PASSWORD("Incorrect password", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("User is not found", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;
}
