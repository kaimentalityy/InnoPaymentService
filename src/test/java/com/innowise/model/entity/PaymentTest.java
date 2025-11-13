package com.innowise.model.entity;

import com.innowise.model.dto.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Payment payment = new Payment();
        LocalDateTime now = LocalDateTime.now();

        payment.setId("1");
        payment.setOrderId(2L);
        payment.setUserId(3L);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(now);
        payment.setPaymentAmount(BigDecimal.valueOf(250.75));

        assertEquals("1", payment.getId());
        assertEquals(2L, payment.getOrderId());
        assertEquals(3L, payment.getUserId());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(now, payment.getTimestamp());
        assertEquals(BigDecimal.valueOf(250.75), payment.getPaymentAmount());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment(
                "10",
                11L,
                12L,
                PaymentStatus.PENDING,
                now,
                BigDecimal.valueOf(500.00)
        );

        assertEquals("10", payment.getId());
        assertEquals(11L, payment.getOrderId());
        assertEquals(12L, payment.getUserId());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(now, payment.getTimestamp());
        assertEquals(BigDecimal.valueOf(500.00), payment.getPaymentAmount());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        Payment p1 = new Payment("1", 2L, 3L, PaymentStatus.SUCCESS, now, BigDecimal.TEN);
        Payment p2 = new Payment("1", 2L, 3L, PaymentStatus.SUCCESS, now, BigDecimal.TEN);
        Payment p3 = new Payment("4", 5L, 6L, PaymentStatus.FAILED, now, BigDecimal.ONE);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    void testToStringContainsFields() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
        Payment payment = new Payment("100", 101L, 102L, PaymentStatus.FAILED, now, BigDecimal.valueOf(999.99));

        String toString = payment.toString();

        assertTrue(toString.contains("id=100"));
        assertTrue(toString.contains("orderId=101"));
        assertTrue(toString.contains("userId=102"));
        assertTrue(toString.contains("status=FAILED"));
        assertTrue(toString.contains("paymentAmount=999.99"));
    }

    @Test
    void testEqualsWithNullAndDifferentClass() {
        Payment payment = new Payment();
        assertNotEquals(payment, null);
        assertNotEquals(payment, "SomeString");
    }
}
