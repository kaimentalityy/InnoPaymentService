package com.innowise.service.kafka;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.model.enums.EventType;
import com.innowise.service.impl.PaymentProcessingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentProcessingServiceImpl paymentProcessingService;

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {

        if (!EventType.ORDER_CREATE.getMessage().equals(event.getEventType())) {
            log.warn("Received unsupported event type: {}", event.getEventType());
            return;
        }
        paymentProcessingService.processPayment(event);
    }
}

