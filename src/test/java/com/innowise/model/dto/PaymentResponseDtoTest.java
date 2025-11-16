package com.innowise.model.dto;

import com.innowise.model.enums.PaymentStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentResponseDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyDto() {
        PaymentResponseDto dto = new PaymentResponseDto();

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isNull();
        assertThat(dto.getOrderId()).isNull();
        assertThat(dto.getUserId()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getTimestamp()).isNull();
        assertThat(dto.getPaymentAmount()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateDtoWithAllFields() {
        String id = "payment-123";
        Long orderId = 100L;
        Long userId = 200L;
        PaymentStatus status = PaymentStatus.SUCCESS;
        LocalDateTime timestamp = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("150.00");

        PaymentResponseDto dto = new PaymentResponseDto(
                id, orderId, userId, status, timestamp, amount
        );

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getOrderId()).isEqualTo(orderId);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getStatus()).isEqualTo(status);
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getPaymentAmount()).isEqualByComparingTo(amount);
    }

    @Test
    void builder_shouldCreateValidDto() {
        String id = "payment-456";
        Long orderId = 300L;
        Long userId = 400L;
        PaymentStatus status = PaymentStatus.PENDING;
        LocalDateTime timestamp = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("250.50");

        PaymentResponseDto dto = PaymentResponseDto.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .timestamp(timestamp)
                .paymentAmount(amount)
                .build();

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getOrderId()).isEqualTo(orderId);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getStatus()).isEqualTo(status);
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getPaymentAmount()).isEqualByComparingTo(amount);
    }

    @Test
    void validation_shouldPassForValidDto() {
        PaymentResponseDto dto = createValidDto();

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldFailWhenIdIsNull() {
        PaymentResponseDto dto = createValidDto();
        dto.setId(null);

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment ID cannot be blank");
    }

    @Test
    void validation_shouldFailWhenIdIsEmpty() {
        PaymentResponseDto dto = createValidDto();
        dto.setId("");

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment ID cannot be blank");
    }

    @Test
    void validation_shouldFailWhenIdIsBlank() {
        PaymentResponseDto dto = createValidDto();
        dto.setId("   ");

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment ID cannot be blank");
    }

    @Test
    void validation_shouldFailWhenOrderIdIsNull() {
        PaymentResponseDto dto = createValidDto();
        dto.setOrderId(null);

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Order ID cannot be null");
    }

    @Test
    void validation_shouldFailWhenUserIdIsNull() {
        PaymentResponseDto dto = createValidDto();
        dto.setUserId(null);

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("User ID cannot be null");
    }

    @Test
    void validation_shouldFailWhenStatusIsNull() {
        PaymentResponseDto dto = createValidDto();
        dto.setStatus(null);

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment status cannot be null");
    }

    @Test
    void validation_shouldPassForAllPaymentStatuses() {
        for (PaymentStatus status : PaymentStatus.values()) {
            PaymentResponseDto dto = createValidDto();
            dto.setStatus(status);

            Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    void validation_shouldFailWhenTimestampIsNull() {
        PaymentResponseDto dto = createValidDto();
        dto.setTimestamp(null);

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Timestamp cannot be null");
    }

    @Test
    void validation_shouldFailWhenTimestampIsInFuture() {
        PaymentResponseDto dto = createValidDto();
        dto.setTimestamp(LocalDateTime.now().plusDays(1));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Timestamp cannot be in the future");
    }

    @Test
    void validation_shouldPassWhenTimestampIsNow() {
        PaymentResponseDto dto = createValidDto();
        dto.setTimestamp(LocalDateTime.now());

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldPassWhenTimestampIsInPast() {
        PaymentResponseDto dto = createValidDto();
        dto.setTimestamp(LocalDateTime.now().minusDays(1));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldFailWhenPaymentAmountIsNull() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(null);

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment amount cannot be null");
    }

    @Test
    void validation_shouldFailWhenPaymentAmountIsNegative() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(new BigDecimal("-0.01"));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment amount cannot be negative");
    }

    @Test
    void validation_shouldPassWhenPaymentAmountIsZero() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(BigDecimal.ZERO);

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldPassWhenPaymentAmountIsPositive() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(new BigDecimal("999999.99"));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldFailWhenPaymentAmountHasTooManyIntegerDigits() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(new BigDecimal("12345678901.00"));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment amount must have at most 10 integer digits and 2 decimal places");
    }

    @Test
    void validation_shouldFailWhenPaymentAmountHasTooManyFractionDigits() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(new BigDecimal("100.123"));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Payment amount must have at most 10 integer digits and 2 decimal places");
    }

    @Test
    void validation_shouldPassWhenPaymentAmountHasMaxIntegerDigits() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(new BigDecimal("9999999999.99"));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldPassWhenPaymentAmountHasMaxFractionDigits() {
        PaymentResponseDto dto = createValidDto();
        dto.setPaymentAmount(new BigDecimal("100.99"));

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void setters_shouldUpdateAllFields() {
        PaymentResponseDto dto = new PaymentResponseDto();
        String id = "payment-789";
        Long orderId = 500L;
        Long userId = 600L;
        PaymentStatus status = PaymentStatus.FAILED;
        LocalDateTime timestamp = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("350.75");

        dto.setId(id);
        dto.setOrderId(orderId);
        dto.setUserId(userId);
        dto.setStatus(status);
        dto.setTimestamp(timestamp);
        dto.setPaymentAmount(amount);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getOrderId()).isEqualTo(orderId);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getStatus()).isEqualTo(status);
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getPaymentAmount()).isEqualByComparingTo(amount);
    }

    @Test
    void getters_shouldReturnCorrectValues() {
        String id = "payment-get";
        Long orderId = 700L;
        Long userId = 800L;
        PaymentStatus status = PaymentStatus.SUCCESS;
        LocalDateTime timestamp = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("450.00");

        PaymentResponseDto dto = new PaymentResponseDto(
                id, orderId, userId, status, timestamp, amount
        );

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getOrderId()).isEqualTo(orderId);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getStatus()).isEqualTo(status);
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getPaymentAmount()).isEqualByComparingTo(amount);
    }

    @Test
    void equals_shouldReturnTrueForSameValues() {
        String id = "payment-eq";
        Long orderId = 100L;
        Long userId = 200L;

        PaymentResponseDto dto1 = PaymentResponseDto.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.of(2024, 1, 1, 12, 0))
                .paymentAmount(new BigDecimal("100.00"))
                .build();

        PaymentResponseDto dto2 = PaymentResponseDto.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.of(2024, 1, 1, 12, 0))
                .paymentAmount(new BigDecimal("100.00"))
                .build();

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentValues() {
        PaymentResponseDto dto1 = PaymentResponseDto.builder()
                .id("payment-1")
                .orderId(100L)
                .userId(200L)
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .paymentAmount(new BigDecimal("100.00"))
                .build();

        PaymentResponseDto dto2 = PaymentResponseDto.builder()
                .id("payment-2")
                .orderId(200L)
                .userId(300L)
                .status(PaymentStatus.FAILED)
                .timestamp(LocalDateTime.now())
                .paymentAmount(new BigDecimal("200.00"))
                .build();

        assertThat(dto1).isNotEqualTo(dto2);
        assertThat(dto1.hashCode()).isNotEqualTo(dto2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        PaymentResponseDto dto = PaymentResponseDto.builder()
                .id("payment-str")
                .orderId(100L)
                .userId(200L)
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.of(2024, 1, 1, 12, 0))
                .paymentAmount(new BigDecimal("150.00"))
                .build();

        String result = dto.toString();

        assertThat(result).contains("payment-str");
        assertThat(result).contains("100");
        assertThat(result).contains("200");
        assertThat(result).contains("SUCCESS");
        assertThat(result).contains("150.00");
    }

    @Test
    void validation_shouldFailWithMultipleViolations() {
        PaymentResponseDto dto = PaymentResponseDto.builder()
                .id("")
                .orderId(null)
                .userId(null)
                .status(null)
                .timestamp(LocalDateTime.now().plusDays(1))
                .paymentAmount(new BigDecimal("-10.00"))
                .build();

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(6);
    }

    @Test
    void validation_shouldPassForMinimalValidDto() {
        PaymentResponseDto dto = PaymentResponseDto.builder()
                .id("1")
                .orderId(1L)
                .userId(1L)
                .status(PaymentStatus.PENDING)
                .timestamp(LocalDateTime.now())
                .paymentAmount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<PaymentResponseDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    private PaymentResponseDto createValidDto() {
        return PaymentResponseDto.builder()
                .id("payment-valid")
                .orderId(100L)
                .userId(200L)
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .paymentAmount(new BigDecimal("150.00"))
                .build();
    }
}