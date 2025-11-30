package com.innowise.service;

import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.enums.PaymentStatus;

/**
 * Service interface for managing payment operations.
 * <p>
 * This service defines the contract for payment-related business logic,
 * including creating new payments and updating their status.
 * </p>
 *
 * @see PaymentCreateRequestDto
 * @see PaymentResponseDto
 * @see PaymentStatus
 */
public interface PaymentService {

    /**
     * Updates the status of an existing payment.
     *
     * @param id        the payment ID
     * @param newStatus the new payment status to set
     * @return the updated payment as a response DTO
     * @throws com.innowise.exception.PaymentNotFoundException if payment not found
     */
    PaymentResponseDto updatePaymentStatus(String id, PaymentStatus newStatus);

    /**
     * Creates a new payment record.
     *
     * @param dto the payment creation request containing order and user details
     * @return the created payment as a response DTO
     * @throws jakarta.validation.ConstraintViolationException if validation fails
     */
    PaymentResponseDto createPayment(PaymentCreateRequestDto dto);
}
