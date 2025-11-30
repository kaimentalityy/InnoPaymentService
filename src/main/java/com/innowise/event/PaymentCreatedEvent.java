package com.innowise.event;

import com.innowise.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentCreatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;
    private String paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentStatus status;

    public PaymentCreatedEvent() {
        this.eventTimestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = "CREATE_PAYMENT";
    }

    public PaymentCreatedEvent(String eventId, String eventType, LocalDateTime eventTimestamp,
                               String paymentId, Long orderId, Long userId,
                               BigDecimal amount, PaymentStatus status) {
        this.eventId = eventId != null ? eventId : java.util.UUID.randomUUID().toString();
        this.eventType = eventType != null ? eventType : "CREATE_PAYMENT";
        this.eventTimestamp = eventTimestamp != null ? eventTimestamp : LocalDateTime.now();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }

    public static PaymentCreatedEventBuilder builder() {
        return new PaymentCreatedEventBuilder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("CREATE_PAYMENT")
                .eventTimestamp(LocalDateTime.now());
    }
}
