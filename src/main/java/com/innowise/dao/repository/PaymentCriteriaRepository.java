package com.innowise.dao.repository;

import com.innowise.model.dto.PaymentStatus;
import com.innowise.model.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentCriteriaRepository {

    BigDecimal findTotalAmountByPeriod(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> search(String userId, String orderId, PaymentStatus status, int page, int size);
}
