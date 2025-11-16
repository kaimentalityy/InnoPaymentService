package com.innowise.service.kafka;

import com.innowise.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCreatedEvent(PaymentCreatedEvent event) {
        log.info("Sending PAYMENT_CREATED event for payment ID: {}, order ID: {}",
                event.getPaymentId(), event.getOrderId());

        CompletableFuture<SendResult<String, Object>> future = sendEvent(event);
        future.whenComplete((result, ex) -> handleSendResult(event, result, ex));
    }

    private CompletableFuture<SendResult<String, Object>> sendEvent(PaymentCreatedEvent event) {
        String key = event.getOrderId().toString();
        return kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, key, event);
    }

    private void handleSendResult(PaymentCreatedEvent event,
                                  SendResult<String, Object> result,
                                  Throwable exception) {
        if (exception == null) {
            logSendSuccess(event, result);
        } else {
            logSendFailure(event, exception);
        }
    }

    private void logSendSuccess(PaymentCreatedEvent event, SendResult<String, Object> result) {
        log.info("PAYMENT_CREATED event sent successfully for payment ID: {}, offset: {}",
                event.getPaymentId(), result.getRecordMetadata().offset());
    }

    private void logSendFailure(PaymentCreatedEvent event, Throwable exception) {
        log.error("Failed to send PAYMENT_CREATED event for payment ID: {}",
                event.getPaymentId(), exception);
    }
}