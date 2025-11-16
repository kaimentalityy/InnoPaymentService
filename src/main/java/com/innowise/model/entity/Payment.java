package com.innowise.model.entity;

import com.innowise.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
@Builder
public class Payment {

    @Id
    private String id;

    private Long orderId;

    private Long userId;

    @Field("status")
    private PaymentStatus status;

    private LocalDateTime timestamp;

    private BigDecimal paymentAmount;
}
