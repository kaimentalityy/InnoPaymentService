package com.innowise.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Centralized error message constants for the Order Service.
 * These messages are used by custom exceptions and global exception handlers.
 */
@Getter
@AllArgsConstructor
public enum ErrorMessage {

    PAYMENT_NOT_FOUND("Payment not found");

    private final String message;
}
