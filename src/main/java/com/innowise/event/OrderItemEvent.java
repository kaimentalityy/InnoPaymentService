package com.innowise.event;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemEvent {
    private Long itemId;
    private String itemName;
    private BigDecimal price;
    private Integer quantity;
}
