package com.innowise.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentCreatedEvent {
    private String eventId;
    private String eventType = "CREATE_PAYMENT";
    private LocalDateTime eventTimestamp;
    private String paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;
    
    public PaymentCreatedEvent() {
        this.eventTimestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    public PaymentCreatedEvent(String paymentId, Long orderId, Long userId,
                             BigDecimal amount, String status) {
        this();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }
}
