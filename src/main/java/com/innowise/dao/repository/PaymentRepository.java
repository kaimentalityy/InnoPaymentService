package com.innowise.dao.repository;

import com.innowise.model.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, Long>, PaymentCriteriaRepository {
}
