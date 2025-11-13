package com.innowise.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentCreateRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto();
        dto.setOrderId(1L);
        dto.setUserId(2L);
        dto.setPaymentAmount(BigDecimal.valueOf(10.50));

        assertEquals(1L, dto.getOrderId());
        assertEquals(2L, dto.getUserId());
        assertEquals(BigDecimal.valueOf(10.50), dto.getPaymentAmount());
    }

    @Test
    void testAllArgsConstructor() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(3L, 4L, BigDecimal.valueOf(25.75));

        assertEquals(3L, dto.getOrderId());
        assertEquals(4L, dto.getUserId());
        assertEquals(BigDecimal.valueOf(25.75), dto.getPaymentAmount());
    }

    @Test
    void testBuilder() {
        PaymentCreateRequestDto dto = PaymentCreateRequestDto.builder()
                .orderId(5L)
                .userId(6L)
                .paymentAmount(BigDecimal.valueOf(99.99))
                .build();

        assertEquals(5L, dto.getOrderId());
        assertEquals(6L, dto.getUserId());
        assertEquals(BigDecimal.valueOf(99.99), dto.getPaymentAmount());
    }

    @Test
    void testEqualsAndHashCode() {
        PaymentCreateRequestDto dto1 = new PaymentCreateRequestDto(7L, 8L, BigDecimal.TEN);
        PaymentCreateRequestDto dto2 = new PaymentCreateRequestDto(7L, 8L, BigDecimal.TEN);
        PaymentCreateRequestDto dto3 = new PaymentCreateRequestDto(9L, 10L, BigDecimal.ONE);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void testToStringContainsFields() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(11L, 12L, BigDecimal.valueOf(5.5));
        String toString = dto.toString();

        assertTrue(toString.contains("orderId=11"));
        assertTrue(toString.contains("userId=12"));
        assertTrue(toString.contains("paymentAmount=5.5"));
    }

    // ✅ Validation tests
    @Test
    void whenValid_thenNoViolations() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(1L, 2L, BigDecimal.valueOf(1.00));
        Set<ConstraintViolation<PaymentCreateRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenOrderIdIsNull_thenViolation() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(null, 2L, BigDecimal.TEN);
        Set<ConstraintViolation<PaymentCreateRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderId")));
    }

    @Test
    void whenUserIdIsNull_thenViolation() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(1L, null, BigDecimal.TEN);
        Set<ConstraintViolation<PaymentCreateRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }

    @Test
    void whenPaymentAmountIsNull_thenViolation() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(1L, 2L, null);
        Set<ConstraintViolation<PaymentCreateRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentAmount")));
    }

    @Test
    void whenPaymentAmountTooSmall_thenViolation() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(1L, 2L, BigDecimal.valueOf(0.001));
        Set<ConstraintViolation<PaymentCreateRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must be greater than or equal to 0.01")));
    }
}
