package com.innowise.service.impl;

import com.innowise.dao.repository.PaymentRepository;
import com.innowise.exception.PaymentNotFoundException;
import com.innowise.mapper.PaymentMapper;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.entity.Payment;
import com.innowise.model.enums.PaymentStatus;
import com.innowise.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * Implementation of {@link PaymentService} that manages payment persistence
 * operations.
 * <p>
 * This service handles CRUD operations for payment entities, including:
 * <ul>
 * <li>Creating new payment records with PENDING status</li>
 * <li>Updating payment status (e.g., SUCCESS, FAILED)</li>
 * </ul>
 * </p>
 * <p>
 * All operations are transactional and include validation of input DTOs.
 * </p>
 *
 * @see PaymentService
 * @see PaymentRepository
 * @see PaymentMapper
 */
@Service
@Validated
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    /**
     * Creates a new payment record with PENDING status.
     * <p>
     * This method validates the input DTO, converts it to an entity,
     * sets the initial status to PENDING, adds a timestamp, and persists
     * it to the database.
     * </p>
     *
     * @param dto the payment creation request containing order and user details
     * @return the created payment as a response DTO
     * @throws jakarta.validation.ConstraintViolationException if validation fails
     */
    @Override
    @Transactional
    public PaymentResponseDto createPayment(@Valid PaymentCreateRequestDto dto) {
        Payment payment = paymentMapper.toEntity(dto);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTimestamp(LocalDateTime.now());

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    /**
     * Updates the status of an existing payment.
     * <p>
     * This method retrieves the payment by ID, updates its status,
     * and persists the changes to the database.
     * </p>
     *
     * @param id        the payment ID
     * @param newStatus the new payment status to set
     * @return the updated payment as a response DTO
     * @throws PaymentNotFoundException if no payment exists with the given ID
     */
    @Override
    @Transactional
    public PaymentResponseDto updatePaymentStatus(String id, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException());
        payment.setStatus(newStatus);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }
}
