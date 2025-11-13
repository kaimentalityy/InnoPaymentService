package com.innowise.service.impl;

import com.innowise.dao.repository.PaymentRepository;
import com.innowise.exception.PaymentNotFoundException;
import com.innowise.mapper.PaymentMapper;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.dto.PaymentStatus;
import com.innowise.model.entity.Payment;
import com.innowise.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentCreateRequestDto dto) {
        Payment payment = paymentMapper.toEntity(dto);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTimestamp(LocalDateTime.now());
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponseDto updatePaymentStatus(String id, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException());
        payment.setStatus(newStatus);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }
}
