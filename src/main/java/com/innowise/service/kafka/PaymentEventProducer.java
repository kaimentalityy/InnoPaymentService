package com.innowise.service.kafka;

import com.innowise.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCreatedEvent(PaymentCreatedEvent event) {
        try {
            log.info("Sending CREATE_PAYMENT event for payment ID: {}, order ID: {}",
                    event.getPaymentId(), event.getOrderId());

            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.getOrderId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("CREATE_PAYMENT event sent successfully for payment ID: {}, offset: {}",
                                    event.getPaymentId(), result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send CREATE_PAYMENT event for payment ID: {}",
                                    event.getPaymentId(), ex);
                        }
                    });

        } catch (Exception e) {
            log.error("Error sending CREATE_PAYMENT event for payment ID: {}", event.getPaymentId(), e);
        }
    }
}
