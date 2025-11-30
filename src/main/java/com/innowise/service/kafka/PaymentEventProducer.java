package com.innowise.service.kafka;

import com.innowise.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Kafka producer service for publishing payment-related events.
 * <p>
 * This service is responsible for sending {@link PaymentCreatedEvent} messages
 * to the configured Kafka topic. It uses synchronous sending with blocking
 * to ensure that events are successfully published before proceeding.
 * </p>
 * <p>
 * The producer uses the order ID as the message key to ensure that all events
 * for the same order are sent to the same partition, maintaining ordering.
 * </p>
 *
 * @see PaymentCreatedEvent
 * @see KafkaTemplate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    @Value("${spring.kafka.topic.payment-events}")
    private String PAYMENT_EVENTS_TOPIC;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends a payment created event to the Kafka topic synchronously.
     * <p>
     * This method blocks until the event is successfully sent or an error occurs.
     * It uses the order ID as the message key to ensure partition affinity.
     * </p>
     * <p>
     * If the event cannot be sent successfully, a {@link RuntimeException} is
     * thrown
     * to allow the caller to handle the failure (e.g., by rolling back the
     * transaction).
     * </p>
     *
     * @param event the payment created event to send
     * @throws ExecutionException   if the send operation fails
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws RuntimeException     if the event cannot be completed successfully
     */
    public void sendPaymentCreatedEvent(PaymentCreatedEvent event) throws ExecutionException, InterruptedException {
        CompletableFuture<SendResult<String, Object>> future = sendEvent(event);
        try {
            SendResult<String, Object> result = future.get();
            log.info("Successfully sent PAYMENT_CREATED event for payment ID: {} to partition: {}",
                    event.getPaymentId(), result.getRecordMetadata().partition());
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to send PAYMENT_CREATED event for payment ID: {}", event.getPaymentId(), e);
            throw e;
        }
    }

    /**
     * Internal method to send an event to Kafka asynchronously.
     * <p>
     * Uses the order ID as the partition key to ensure all events for the same
     * order are sent to the same partition, maintaining event ordering.
     * </p>
     *
     * @param event the payment event to send
     * @return a CompletableFuture containing the send result
     */
    private CompletableFuture<SendResult<String, Object>> sendEvent(PaymentCreatedEvent event) {
        String key = event.getOrderId().toString();
        return kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, key, event);
    }
}
