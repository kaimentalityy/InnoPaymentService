package com.innowise.service;

import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.dto.PaymentStatus;

public interface PaymentService {

    PaymentResponseDto updatePaymentStatus(Long id, PaymentStatus newStatus);

    PaymentResponseDto createPayment(PaymentCreateRequestDto dto);
}
