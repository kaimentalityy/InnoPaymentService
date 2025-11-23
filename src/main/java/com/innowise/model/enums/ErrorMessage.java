package com.innowise.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Centralized error message constants for the Payment Service.
 * Each constant now includes a unique numeric code and a default human-readable message.
 */
@Getter
@AllArgsConstructor
public enum ErrorMessage {

    PAYMENT_NOT_FOUND(4001, "The requested payment resource was not found."),
    VALIDATION_ERROR(4000, "One or more request fields failed validation."),
    PAYMENT_FAILED(4002, "Payment processing failed due to external or business reasons."),
    INTERNAL_SERVER_ERROR(5000, "An unexpected internal server error occurred.");

    private final int code;
    private final String defaultMessage;
}