package com.innowise.service;

import com.innowise.event.OrderCreatedEvent;

/**
 * Service interface for processing payment workflows.
 * <p>
 * This service orchestrates the complete payment processing lifecycle,
 * from receiving order events to publishing payment events.
 * </p>
 *
 * @see OrderCreatedEvent
 */
public interface PaymentProcessingService {

    /**
     * Processes a payment for an order creation event.
     * <p>
     * This method handles the entire payment workflow including
     * payment creation, status determination, and event publishing.
     * </p>
     *
     * @param event the order created event to process
     */
    void processPayment(OrderCreatedEvent event);
}
