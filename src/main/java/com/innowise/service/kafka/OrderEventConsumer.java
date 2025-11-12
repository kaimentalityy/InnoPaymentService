package com.innowise.service.kafka;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.event.PaymentCreatedEvent;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.dto.PaymentStatus;
import com.innowise.service.PaymentService;
import com.innowise.service.RandomNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final RandomNumberService randomNumberService;
    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        if ("CREATE_ORDER".equals(event.getEventType())) {
            log.info("Received CREATE_ORDER event for order ID: {}", event.getOrderId());
            processPayment(event);
        }
    }

    private void processPayment(OrderCreatedEvent event) {
        try {
            log.info("Processing payment for order ID: {}, user ID: {}, amount: {}",
                    event.getOrderId(), event.getUserId(), event.getTotalAmount());

            PaymentCreateRequestDto paymentCreateRequestDto = PaymentCreateRequestDto.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .paymentAmount(event.getTotalAmount())
                    .build();

            PaymentResponseDto pendingPayment = paymentService.createPayment(paymentCreateRequestDto);
            log.info("Payment created with PENDING status, ID: {}", pendingPayment.getId());

            PaymentStatus finalStatus = randomNumberService.isEven()
                    ? PaymentStatus.SUCCESS
                    : PaymentStatus.FAILED;

            PaymentResponseDto updatedPayment = paymentService.updatePaymentStatus(pendingPayment.getId(), finalStatus);

            PaymentCreatedEvent paymentEvent = new PaymentCreatedEvent(
                    updatedPayment.getId(),
                    event.getOrderId(),
                    event.getUserId(),
                    event.getTotalAmount(),
                    finalStatus.name()
            );

            paymentEventProducer.sendPaymentCreatedEvent(paymentEvent);
            log.info("Payment processed for order ID: {}, final status: {}", event.getOrderId(), finalStatus);

        } catch (Exception e) {
            log.error("Failed to process payment for order ID: {}", event.getOrderId(), e);
        }
    }
}
