package com.innowise.model.dto;

import com.innowise.model.enums.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    @NotBlank(message = "Payment ID cannot be blank")
    private String id;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Payment status cannot be null")
    private PaymentStatus status;

    @NotNull(message = "Timestamp cannot be null")
    @PastOrPresent(message = "Timestamp cannot be in the future")
    private LocalDateTime timestamp;

    @NotNull(message = "Payment amount cannot be null")
    @DecimalMin(value = "0.0", message = "Payment amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Payment amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal paymentAmount;
}
