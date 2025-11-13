package com.innowise.service;

import com.innowise.dao.repository.PaymentRepository;
import com.innowise.exception.PaymentNotFoundException;
import com.innowise.mapper.PaymentMapper;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.dto.PaymentStatus;
import com.innowise.model.entity.Payment;
import com.innowise.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private PaymentRepository paymentRepository;
    private PaymentMapper paymentMapper;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentMapper = mock(PaymentMapper.class);
        paymentService = new PaymentServiceImpl(paymentRepository, paymentMapper);
    }

    @Test
    void testCreatePayment() {
        PaymentCreateRequestDto dto = new PaymentCreateRequestDto(1L, 2L, BigDecimal.valueOf(100.50));
        Payment entity = new Payment();
        Payment savedEntity = new Payment();
        savedEntity.setId("10");

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId("10");

        when(paymentMapper.toEntity(dto)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.createPayment(dto);

        assertNotNull(result);
        assertEquals("10", result.getId());

        assertEquals(PaymentStatus.PENDING, entity.getStatus());
        assertNotNull(entity.getTimestamp());

        verify(paymentMapper).toEntity(dto);
        verify(paymentRepository).save(entity);
        verify(paymentMapper).toDto(savedEntity);
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        Payment existingPayment = new Payment();
        existingPayment.setId("5");
        existingPayment.setStatus(PaymentStatus.PENDING);

        Payment updatedPayment = new Payment();
        updatedPayment.setId("5");
        updatedPayment.setStatus(PaymentStatus.SUCCESS);

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId("5");
        responseDto.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById("5")).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(updatedPayment);
        when(paymentMapper.toDto(updatedPayment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.updatePaymentStatus("5", PaymentStatus.SUCCESS);

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, existingPayment.getStatus());
        assertEquals("5", result.getId());

        verify(paymentRepository).findById("5");
        verify(paymentRepository).save(existingPayment);
        verify(paymentMapper).toDto(updatedPayment);
    }

    @Test
    void testUpdatePaymentStatus_NotFound() {
        when(paymentRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.updatePaymentStatus("999", PaymentStatus.SUCCESS));

        verify(paymentRepository).findById("999");
        verify(paymentRepository, never()).save(any());
        verify(paymentMapper, never()).toDto(any());
    }
}
