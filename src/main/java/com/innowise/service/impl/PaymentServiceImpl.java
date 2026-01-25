package com.innowise.service.impl;

import com.innowise.dao.repository.PaymentRepository;
import com.innowise.exception.PaymentNotFoundException;
import com.innowise.mapper.PaymentMapper;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.entity.Payment;
import com.innowise.model.enums.PaymentStatus;
import com.innowise.service.PaymentService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
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

    private final Counter paymentsCreatedCounter;
    private final Counter paymentsSuccessCounter;
    private final Counter paymentsFailedCounter;
    private final Timer paymentProcessingTimer;

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
    public PaymentResponseDto createPayment(PaymentCreateRequestDto dto) {
        return paymentProcessingTimer.record(() -> {
            Payment payment = paymentMapper.toEntity(dto);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTimestamp(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);
            paymentsCreatedCounter.increment();
            return paymentMapper.toDto(saved);
        });
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
        Payment saved = paymentRepository.save(payment);

        if (newStatus == PaymentStatus.SUCCESS) {
            paymentsSuccessCounter.increment();
        } else if (newStatus == PaymentStatus.FAILED) {
            paymentsFailedCounter.increment();
        }

        return paymentMapper.toDto(saved);
    }
}
