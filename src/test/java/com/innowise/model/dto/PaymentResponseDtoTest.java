package com.innowise.model.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentResponseDtoTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        PaymentResponseDto dto = new PaymentResponseDto();
        LocalDateTime now = LocalDateTime.now();

        dto.setId("1");
        dto.setOrderId(2L);
        dto.setUserId(3L);
        dto.setStatus(PaymentStatus.SUCCESS);
        dto.setTimestamp(now);
        dto.setPaymentAmount(BigDecimal.valueOf(100.50));

        assertEquals("1", dto.getId());
        assertEquals(2L, dto.getOrderId());
        assertEquals(3L, dto.getUserId());
        assertEquals(PaymentStatus.SUCCESS, dto.getStatus());
        assertEquals(now, dto.getTimestamp());
        assertEquals(BigDecimal.valueOf(100.50), dto.getPaymentAmount());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        PaymentResponseDto dto = new PaymentResponseDto(
                "10",
                11L,
                12L,
                PaymentStatus.FAILED,
                now,
                BigDecimal.valueOf(50.00)
        );

        assertEquals("10", dto.getId());
        assertEquals(11L, dto.getOrderId());
        assertEquals(12L, dto.getUserId());
        assertEquals(PaymentStatus.FAILED, dto.getStatus());
        assertEquals(now, dto.getTimestamp());
        assertEquals(BigDecimal.valueOf(50.00), dto.getPaymentAmount());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        PaymentResponseDto dto = PaymentResponseDto.builder()
                .id("20")
                .orderId(21L)
                .userId(22L)
                .status(PaymentStatus.PENDING)
                .timestamp(now)
                .paymentAmount(BigDecimal.valueOf(200.00))
                .build();

        assertEquals("20", dto.getId());
        assertEquals(21L, dto.getOrderId());
        assertEquals(22L, dto.getUserId());
        assertEquals(PaymentStatus.PENDING, dto.getStatus());
        assertEquals(now, dto.getTimestamp());
        assertEquals(BigDecimal.valueOf(200.00), dto.getPaymentAmount());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        PaymentResponseDto dto1 = new PaymentResponseDto("1", 2L, 3L, PaymentStatus.SUCCESS, now, BigDecimal.TEN);
        PaymentResponseDto dto2 = new PaymentResponseDto("1", 2L, 3L, PaymentStatus.SUCCESS, now, BigDecimal.TEN);
        PaymentResponseDto dto3 = new PaymentResponseDto("4", 5L, 6L, PaymentStatus.FAILED, now, BigDecimal.ONE);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void testToStringContainsFields() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 0);
        PaymentResponseDto dto = new PaymentResponseDto("100", 101L, 102L, PaymentStatus.SUCCESS, now, BigDecimal.valueOf(999.99));

        String toString = dto.toString();

        assertTrue(toString.contains("id=100"));
        assertTrue(toString.contains("orderId=101"));
        assertTrue(toString.contains("userId=102"));
        assertTrue(toString.contains("status=SUCCESS"));
        assertTrue(toString.contains("paymentAmount=999.99"));
    }
}
