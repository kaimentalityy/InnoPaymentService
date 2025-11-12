package com.innowise.event;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderCreatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemEvent> items;
}