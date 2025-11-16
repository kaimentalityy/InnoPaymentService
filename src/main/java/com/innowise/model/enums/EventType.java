package com.innowise.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    PAYMENT_CREATED("PAYMENT_CREATED"),
    ORDER_CREATE("ORDER_CREATE");

    private final String message;
}
