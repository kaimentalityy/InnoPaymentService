package com.innowise.event;

import com.innowise.model.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCreatedEventTest {

    @Test
    void noArgsConstructor_shouldInitializeDefaultValues() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        PaymentCreatedEvent event = new PaymentCreatedEvent();

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("CREATE_PAYMENT");
        assertThat(event.getEventTimestamp()).isNotNull();
        assertThat(event.getEventTimestamp()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getEventTimestamp()).isBeforeOrEqualTo(afterCreation);
        assertThat(event.getPaymentId()).isNull();
        assertThat(event.getOrderId()).isNull();
        assertThat(event.getUserId()).isNull();
        assertThat(event.getAmount()).isNull();
        assertThat(event.getStatus()).isNull();
    }

    @Test
    void noArgsConstructor_shouldGenerateUniqueEventIds() {
        PaymentCreatedEvent event1 = new PaymentCreatedEvent();
        PaymentCreatedEvent event2 = new PaymentCreatedEvent();

        assertThat(event1.getEventId()).isNotNull();
        assertThat(event2.getEventId()).isNotNull();
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void allArgsConstructor_shouldUseProvidedValues() {
        String eventId = "event-123";
        String eventType = "PAYMENT_PROCESSED";
        LocalDateTime timestamp = LocalDateTime.now();
        String paymentId = "payment-456";
        Long orderId = 100L;
        Long userId = 200L;
        BigDecimal amount = new BigDecimal("150.00");
        PaymentStatus status = PaymentStatus.SUCCESS;

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                eventId, eventType, timestamp, paymentId, orderId, userId, amount, status
        );

        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getEventType()).isEqualTo(eventType);
        assertThat(event.getEventTimestamp()).isEqualTo(timestamp);
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getAmount()).isEqualByComparingTo(amount);
        assertThat(event.getStatus()).isEqualTo(status);
    }

    @Test
    void allArgsConstructor_shouldGenerateDefaultsWhenNull() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                null, null, null, "payment-789", 300L, 400L, new BigDecimal("250.00"), PaymentStatus.FAILED
        );

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("CREATE_PAYMENT");
        assertThat(event.getEventTimestamp()).isNotNull();
        assertThat(event.getEventTimestamp()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getEventTimestamp()).isBeforeOrEqualTo(afterCreation);
        assertThat(event.getPaymentId()).isEqualTo("payment-789");
        assertThat(event.getOrderId()).isEqualTo(300L);
        assertThat(event.getUserId()).isEqualTo(400L);
        assertThat(event.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void builder_shouldCreateEventWithDefaultValues() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-001")
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCESS)
                .build();

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("CREATE_PAYMENT");
        assertThat(event.getEventTimestamp()).isNotNull();
        assertThat(event.getEventTimestamp()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getEventTimestamp()).isBeforeOrEqualTo(afterCreation);
        assertThat(event.getPaymentId()).isEqualTo("payment-001");
        assertThat(event.getOrderId()).isEqualTo(100L);
        assertThat(event.getUserId()).isEqualTo(200L);
        assertThat(event.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void builder_shouldGenerateUniqueEventIds() {
        PaymentCreatedEvent event1 = PaymentCreatedEvent.builder()
                .paymentId("payment-1")
                .build();

        PaymentCreatedEvent event2 = PaymentCreatedEvent.builder()
                .paymentId("payment-2")
                .build();

        assertThat(event1.getEventId()).isNotNull();
        assertThat(event2.getEventId()).isNotNull();
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void builder_shouldAllowOverridingDefaults() {
        String customEventId = "custom-event-id";
        String customEventType = "CUSTOM_PAYMENT";
        LocalDateTime customTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0);

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .eventId(customEventId)
                .eventType(customEventType)
                .eventTimestamp(customTimestamp)
                .paymentId("payment-custom")
                .orderId(999L)
                .userId(888L)
                .amount(new BigDecimal("777.77"))
                .status(PaymentStatus.PENDING)
                .build();

        assertThat(event.getEventId()).isEqualTo(customEventId);
        assertThat(event.getEventType()).isEqualTo(customEventType);
        assertThat(event.getEventTimestamp()).isEqualTo(customTimestamp);
        assertThat(event.getPaymentId()).isEqualTo("payment-custom");
        assertThat(event.getOrderId()).isEqualTo(999L);
        assertThat(event.getUserId()).isEqualTo(888L);
        assertThat(event.getAmount()).isEqualByComparingTo(new BigDecimal("777.77"));
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void setters_shouldUpdateAllFields() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        String newEventId = "new-event-id";
        String newEventType = "UPDATED_PAYMENT";
        LocalDateTime newTimestamp = LocalDateTime.of(2025, 12, 31, 23, 59);
        String newPaymentId = "payment-new";
        Long newOrderId = 555L;
        Long newUserId = 666L;
        BigDecimal newAmount = new BigDecimal("333.33");
        PaymentStatus newStatus = PaymentStatus.SUCCESS;

        event.setEventId(newEventId);
        event.setEventType(newEventType);
        event.setEventTimestamp(newTimestamp);
        event.setPaymentId(newPaymentId);
        event.setOrderId(newOrderId);
        event.setUserId(newUserId);
        event.setAmount(newAmount);
        event.setStatus(newStatus);

        assertThat(event.getEventId()).isEqualTo(newEventId);
        assertThat(event.getEventType()).isEqualTo(newEventType);
        assertThat(event.getEventTimestamp()).isEqualTo(newTimestamp);
        assertThat(event.getPaymentId()).isEqualTo(newPaymentId);
        assertThat(event.getOrderId()).isEqualTo(newOrderId);
        assertThat(event.getUserId()).isEqualTo(newUserId);
        assertThat(event.getAmount()).isEqualByComparingTo(newAmount);
        assertThat(event.getStatus()).isEqualTo(newStatus);
    }

    @Test
    void getters_shouldReturnCorrectValues() {
        String eventId = "get-event";
        String eventType = "GET_PAYMENT";
        LocalDateTime timestamp = LocalDateTime.now();
        String paymentId = "payment-get";
        Long orderId = 111L;
        Long userId = 222L;
        BigDecimal amount = new BigDecimal("444.44");
        PaymentStatus status = PaymentStatus.SUCCESS;

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                eventId, eventType, timestamp, paymentId, orderId, userId, amount, status
        );

        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getEventType()).isEqualTo(eventType);
        assertThat(event.getEventTimestamp()).isEqualTo(timestamp);
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getAmount()).isEqualByComparingTo(amount);
        assertThat(event.getStatus()).isEqualTo(status);
    }

    @Test
    void builder_shouldHandleZeroAmount() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-zero")
                .amount(BigDecimal.ZERO)
                .build();

        assertThat(event.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void builder_shouldHandleLargeAmount() {
        BigDecimal largeAmount = new BigDecimal("999999999.99");

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-large")
                .amount(largeAmount)
                .build();

        assertThat(event.getAmount()).isEqualByComparingTo(largeAmount);
    }

    @Test
    void builder_shouldHandleNullPaymentId() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(null)
                .orderId(100L)
                .build();

        assertThat(event.getPaymentId()).isNull();
        assertThat(event.getOrderId()).isEqualTo(100L);
    }

    @Test
    void setEventId_shouldUpdateEventId() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        String newEventId = "updated-event-id";

        event.setEventId(newEventId);

        assertThat(event.getEventId()).isEqualTo(newEventId);
    }

    @Test
    void setEventType_shouldUpdateEventType() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        String newEventType = "PAYMENT_UPDATED";

        event.setEventType(newEventType);

        assertThat(event.getEventType()).isEqualTo(newEventType);
    }

    @Test
    void setEventTimestamp_shouldUpdateTimestamp() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        LocalDateTime newTimestamp = LocalDateTime.of(2024, 6, 15, 10, 30);

        event.setEventTimestamp(newTimestamp);

        assertThat(event.getEventTimestamp()).isEqualTo(newTimestamp);
    }

    @Test
    void setPaymentId_shouldUpdatePaymentId() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        String newPaymentId = "payment-updated";

        event.setPaymentId(newPaymentId);

        assertThat(event.getPaymentId()).isEqualTo(newPaymentId);
    }

    @Test
    void setOrderId_shouldUpdateOrderId() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        Long newOrderId = 999L;

        event.setOrderId(newOrderId);

        assertThat(event.getOrderId()).isEqualTo(newOrderId);
    }

    @Test
    void setUserId_shouldUpdateUserId() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        Long newUserId = 888L;

        event.setUserId(newUserId);

        assertThat(event.getUserId()).isEqualTo(newUserId);
    }

    @Test
    void setAmount_shouldUpdateAmount() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        BigDecimal newAmount = new BigDecimal("555.55");

        event.setAmount(newAmount);

        assertThat(event.getAmount()).isEqualByComparingTo(newAmount);
    }

    @Test
    void setStatus_shouldUpdateStatus() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        PaymentStatus newStatus = PaymentStatus.FAILED;

        event.setStatus(newStatus);

        assertThat(event.getStatus()).isEqualTo(newStatus);
    }

    @Test
    void allArgsConstructor_shouldHandleAllNulls() {
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                null, null, null, null, null, null, null, null
        );

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("CREATE_PAYMENT");
        assertThat(event.getEventTimestamp()).isNotNull();
        assertThat(event.getPaymentId()).isNull();
        assertThat(event.getOrderId()).isNull();
        assertThat(event.getUserId()).isNull();
        assertThat(event.getAmount()).isNull();
        assertThat(event.getStatus()).isNull();
    }

    @Test
    void builder_shouldCreateMinimalEvent() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder().build();

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("CREATE_PAYMENT");
        assertThat(event.getEventTimestamp()).isNotNull();
        assertThat(event.getPaymentId()).isNull();
        assertThat(event.getOrderId()).isNull();
        assertThat(event.getUserId()).isNull();
        assertThat(event.getAmount()).isNull();
        assertThat(event.getStatus()).isNull();
    }

    @Test
    void builder_shouldHandleEmptyStrings() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("")
                .status(null)
                .build();

        assertThat(event.getPaymentId()).isEmpty();
        assertThat(event.getStatus()).isNull();
    }

    @Test
    void setters_shouldAllowNullValues() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-123")
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.SUCCESS)
                .build();

        event.setEventId(null);
        event.setEventType(null);
        event.setEventTimestamp(null);
        event.setPaymentId(null);
        event.setOrderId(null);
        event.setUserId(null);
        event.setAmount(null);
        event.setStatus(null);

        assertThat(event.getEventId()).isNull();
        assertThat(event.getEventType()).isNull();
        assertThat(event.getEventTimestamp()).isNull();
        assertThat(event.getPaymentId()).isNull();
        assertThat(event.getOrderId()).isNull();
        assertThat(event.getUserId()).isNull();
        assertThat(event.getAmount()).isNull();
        assertThat(event.getStatus()).isNull();
    }

    @Test
    void noArgsConstructor_shouldGenerateValidUUID() {
        PaymentCreatedEvent event = new PaymentCreatedEvent();

        assertThat(event.getEventId()).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        );
    }

    @Test
    void allArgsConstructor_shouldGenerateValidUUIDWhenNull() {
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                null, "CUSTOM", LocalDateTime.now(), "payment-1", 100L, 200L, 
                new BigDecimal("50.00"), PaymentStatus.SUCCESS
        );

        assertThat(event.getEventId()).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        );
    }

    @Test
    void builder_shouldGenerateValidUUID() {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId("payment-123")
                .build();

        assertThat(event.getEventId()).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        );
    }

    @Test
    void multipleConstructorCalls_shouldGenerateDifferentTimestamps() throws InterruptedException {
        PaymentCreatedEvent event1 = new PaymentCreatedEvent();
        Thread.sleep(10);
        PaymentCreatedEvent event2 = new PaymentCreatedEvent();

        assertThat(event1.getEventTimestamp()).isNotNull();
        assertThat(event2.getEventTimestamp()).isNotNull();
        assertThat(event1.getEventTimestamp()).isBeforeOrEqualTo(event2.getEventTimestamp());
    }
}