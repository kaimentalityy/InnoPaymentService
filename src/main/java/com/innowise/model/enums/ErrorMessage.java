package com.innowise.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Centralized error message constants for the Payment Service.
 */
@Getter
@AllArgsConstructor
public enum ErrorMessage {

    PAYMENT_NOT_FOUND("Payment not found"),
    INTERNAL_SERVER_ERROR("Internal server error"),
    PAYMENT_FAILED("Payment failed");

    private final String message;
}
