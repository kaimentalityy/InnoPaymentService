package com.innowise.dao.repository;

import com.innowise.model.dto.PaymentStatus;
import com.innowise.model.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        payment1 = new Payment(null, 1L, 1L, PaymentStatus.PENDING, LocalDateTime.now(), BigDecimal.valueOf(50.0));
        payment2 = new Payment(null, 2L, 2L, PaymentStatus.SUCCESS, LocalDateTime.now(), BigDecimal.valueOf(100.0));

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
    }

    @Test
    void testSaveAndFindById() {
        Payment newPayment = new Payment(null, 3L, 3L, PaymentStatus.FAILED, LocalDateTime.now(), BigDecimal.valueOf(75.0));
        Payment saved = paymentRepository.save(newPayment);

        assertNotNull(saved.getId());

        Optional<Payment> found = paymentRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(PaymentStatus.FAILED, found.get().getStatus());
        assertEquals(BigDecimal.valueOf(75.0), found.get().getPaymentAmount());
    }

    @Test
    void testFindAll() {
        List<Payment> allPayments = paymentRepository.findAll();
        assertEquals(2, allPayments.size());
    }

    @Test
    void testDelete() {
        paymentRepository.delete(payment1);
        List<Payment> remaining = paymentRepository.findAll();
        assertEquals(1, remaining.size());
        assertEquals(payment2.getId(), remaining.get(0).getId());
    }

    @Test
    void testUpdatePayment() {
        payment1.setStatus(PaymentStatus.SUCCESS);
        payment1.setPaymentAmount(BigDecimal.valueOf(200.0));

        Payment updated = paymentRepository.save(payment1);
        assertEquals(PaymentStatus.SUCCESS, updated.getStatus());
        assertEquals(BigDecimal.valueOf(200.0), updated.getPaymentAmount());
    }
}
