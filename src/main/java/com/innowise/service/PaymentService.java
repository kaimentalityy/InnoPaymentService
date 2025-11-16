package com.innowise.service;

import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.enums.PaymentStatus;

public interface PaymentService {

    PaymentResponseDto updatePaymentStatus(String id, PaymentStatus newStatus);

    PaymentResponseDto createPayment(PaymentCreateRequestDto dto);
}
