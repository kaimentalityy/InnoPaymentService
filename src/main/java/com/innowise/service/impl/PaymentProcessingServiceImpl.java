package com.innowise.service.impl;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.event.PaymentCreatedEvent;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.enums.PaymentStatus;
import com.innowise.service.PaymentProcessingService;
import com.innowise.service.PaymentService;
import com.innowise.service.RandomNumberClient;
import com.innowise.service.kafka.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    private final PaymentService paymentService;
    private final PaymentEventProducer eventProducer;
    private final RandomNumberClient randomNumberClient;

    @Override
    public void processPayment(OrderCreatedEvent event) {
        PaymentCreateRequestDto dto = PaymentCreateRequestDto.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .paymentAmount(event.getTotalAmount())
                .build();

        PaymentResponseDto pending = paymentService.createPayment(dto);

        int number = randomNumberClient.generateRandomNumber();
        PaymentStatus status = number % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        PaymentResponseDto updated = paymentService.updatePaymentStatus(pending.getId(), status);

        PaymentCreatedEvent paymentEvent = PaymentCreatedEvent.builder()
                .paymentId(updated.getId())
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getTotalAmount())
                .status(status)
                .build();

        eventProducer.sendPaymentCreatedEvent(paymentEvent);

        log.info("Payment processed for order {} with status {}", event.getOrderId(), status);
    }
}
