package com.innowise.service.kafka;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.model.enums.EventType;
import com.innowise.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer service for processing order-related events.
 * <p>
 * This service listens to the order events topic and processes
 * {@link OrderCreatedEvent} messages by triggering the payment
 * processing workflow.
 * </p>
 * <p>
 * The consumer performs validation on incoming events to ensure
 * data integrity before processing.
 * </p>
 *
 * @see OrderCreatedEvent
 * @see PaymentProcessingService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentProcessingService paymentProcessingService;

    /**
     * Handles incoming order created events from Kafka.
     * <p>
     * This method performs the following validations:
     * <ul>
     * <li>Checks that the event type is ORDER_CREATE</li>
     * <li>Validates that the order ID is not null</li>
     * </ul>
     * If validation passes, the event is forwarded to the payment processing
     * service.
     * </p>
     *
     * @param event the order created event to process
     * @throws IllegalArgumentException if the order ID is null
     */
    @KafkaListener(topics = "${spring.kafka.topic.order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {

        if (!EventType.ORDER_CREATE.toString().equals(event.getEventType())) {
            log.warn("Received unsupported event type: {}", event.getEventType());
            return;
        }
        if (event.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null in OrderCreatedEvent");
        }
        paymentProcessingService.processPayment(event);

        log.info("Order processed successfully by Payment Service: {}", event.getOrderId());
    }
}
