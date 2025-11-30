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

import java.util.concurrent.ExecutionException;

/**
 * Implementation of {@link PaymentProcessingService} that orchestrates the
 * payment processing workflow.
 * <p>
 * This service coordinates the entire payment lifecycle:
 * <ol>
 * <li>Creates a pending payment record</li>
 * <li>Simulates payment processing using a random number generator</li>
 * <li>Updates the payment status based on the simulation result</li>
 * <li>Publishes a payment event to Kafka</li>
 * <li>Handles failures by marking the payment as failed</li>
 * </ol>
 * </p>
 * <p>
 * The payment success/failure is determined by whether the generated random
 * number
 * is even (success) or odd (failure).
 * </p>
 *
 * @see PaymentProcessingService
 * @see PaymentService
 * @see RandomNumberClient
 * @see PaymentEventProducer
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    private final PaymentService paymentService;

    private final PaymentEventProducer paymentEventProducer;

    private final RandomNumberClient randomNumberClient;

    /**
     * Processes a payment for an order creation event.
     * <p>
     * This method performs the following steps:
     * <ol>
     * <li>Creates a pending payment record in the database</li>
     * <li>Generates a random number to simulate payment processing</li>
     * <li>Determines payment status (SUCCESS if even, FAILED if odd)</li>
     * <li>Updates the payment status in the database</li>
     * <li>Publishes a PaymentCreatedEvent to Kafka</li>
     * <li>If event publishing fails, marks the payment as FAILED</li>
     * </ol>
     * </p>
     *
     * @param event the order created event containing order details
     * @throws RuntimeException if payment creation or status update fails
     */
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
                .amount(updated.getPaymentAmount())
                .status(updated.getStatus())
                .build();

        try {
            paymentEventProducer.sendPaymentCreatedEvent(paymentEvent);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending payment event for paymentId={}", updated.getId(), e);
            paymentService.updatePaymentStatus(updated.getId(), PaymentStatus.FAILED);
            return;
        } catch (ExecutionException e) {
            paymentService.updatePaymentStatus(updated.getId(), PaymentStatus.FAILED);
            log.error("Failed to send payment event for paymentId={}", updated.getId(), e);
            throw new RuntimeException(e);
        }
        log.info("Payment processed for order {} with status {}", event.getOrderId(), status);
    }
}
