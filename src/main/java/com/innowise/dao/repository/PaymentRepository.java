package com.innowise.dao.repository;

import com.innowise.model.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByUserId(String userId);
    List<Payment> findByStatus(String status);
    List<Payment> findByOrderId(String orderId);
}