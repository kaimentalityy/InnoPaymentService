package com.innowise.mapper;

import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.enums.PaymentStatus;
import com.innowise.model.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        // Get MapStruct implementation
        paymentMapper = Mappers.getMapper(PaymentMapper.class);
    }

    @Test
    void testToEntity_ShouldMapFieldsCorrectly() {
        PaymentCreateRequestDto dto = PaymentCreateRequestDto.builder()
                .orderId(1L)
                .userId(2L)
                .paymentAmount(BigDecimal.valueOf(100.50))
                .build();

        Payment entity = paymentMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getOrderId(), entity.getOrderId());
        assertEquals(dto.getUserId(), entity.getUserId());
        assertEquals(dto.getPaymentAmount(), entity.getPaymentAmount());

        // Fields ignored in mapping should remain null
        assertNull(entity.getId());
        assertNull(entity.getStatus());
        assertNull(entity.getTimestamp());
    }

    @Test
    void testToDto_ShouldMapFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Payment entity = new Payment(
                "10",
                1L,
                2L,
                PaymentStatus.PENDING,
                now,
                BigDecimal.valueOf(200.00)
        );

        PaymentResponseDto dto = paymentMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getOrderId(), dto.getOrderId());
        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getStatus(), dto.getStatus());
        assertEquals(entity.getTimestamp(), dto.getTimestamp());
        assertEquals(entity.getPaymentAmount(), dto.getPaymentAmount());
    }

    @Test
    void testToEntity_NullInput_ShouldReturnNull() {
        Payment entity = paymentMapper.toEntity(null);
        assertNull(entity);
    }

    @Test
    void testToDto_NullInput_ShouldReturnNull() {
        PaymentResponseDto dto = paymentMapper.toDto(null);
        assertNull(dto);
    }
}
