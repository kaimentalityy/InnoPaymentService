package com.innowise.service;

import com.innowise.event.OrderCreatedEvent;

public interface PaymentProcessingService {
    void processPayment(OrderCreatedEvent event);
}
