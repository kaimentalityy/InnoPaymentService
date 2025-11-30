package com.innowise.service.impl;

import com.innowise.dao.repository.PaymentRepository;
import com.innowise.event.OrderCreatedEvent;
import com.innowise.event.PaymentCreatedEvent;
import com.innowise.exception.PaymentNotFoundException;
import com.innowise.mapper.PaymentMapper;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.entity.Payment;
import com.innowise.model.enums.PaymentStatus;
import com.innowise.service.PaymentService;
import com.innowise.service.RandomNumberClient;
import com.innowise.service.kafka.PaymentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    private PaymentCreateRequestDto createRequestDto;
    private Payment payment;
    private PaymentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        createRequestDto = PaymentCreateRequestDto.builder()
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .build();

        payment = Payment.builder()
                .id("payment-123")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .timestamp(LocalDateTime.now())
                .build();

        responseDto = PaymentResponseDto.builder()
                .id("payment-123")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void createPayment_shouldCreatePaymentSuccessfully() {
        Payment mappedPayment = Payment.builder()
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .build();

        when(paymentMapper.toEntity(createRequestDto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.createPayment(createRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("payment-123");
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(200L);
        assertThat(result.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentMapper).toEntity(createRequestDto);
        verify(paymentRepository).save(paymentCaptor.capture());
        verify(paymentMapper).toDto(payment);

        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(capturedPayment.getTimestamp()).isNotNull();
    }

    @Test
    void createPayment_shouldSetStatusToPending() {
        Payment mappedPayment = Payment.builder()
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .build();

        when(paymentMapper.toEntity(createRequestDto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        paymentService.createPayment(createRequestDto);

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void createPayment_shouldSetTimestamp() {
        Payment mappedPayment = Payment.builder()
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .build();

        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        when(paymentMapper.toEntity(createRequestDto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        paymentService.createPayment(createRequestDto);

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertThat(savedPayment.getTimestamp()).isNotNull();
        assertThat(savedPayment.getTimestamp()).isAfterOrEqualTo(beforeCreation);
        assertThat(savedPayment.getTimestamp()).isBeforeOrEqualTo(afterCreation);
    }

    @Test
    void createPayment_shouldCallMapperToEntity() {
        Payment mappedPayment = Payment.builder().build();

        when(paymentMapper.toEntity(createRequestDto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        paymentService.createPayment(createRequestDto);

        verify(paymentMapper).toEntity(createRequestDto);
    }

    @Test
    void createPayment_shouldCallRepositorySave() {
        Payment mappedPayment = Payment.builder().build();

        when(paymentMapper.toEntity(createRequestDto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        paymentService.createPayment(createRequestDto);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldCallMapperToDto() {
        Payment mappedPayment = Payment.builder().build();

        when(paymentMapper.toEntity(createRequestDto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        paymentService.createPayment(createRequestDto);

        verify(paymentMapper).toDto(payment);
    }

    @Test
    void createPayment_shouldHandleZeroAmount() {
        PaymentCreateRequestDto zeroAmountDto = PaymentCreateRequestDto.builder()
                .orderId(100L)
                .userId(200L)
                .paymentAmount(BigDecimal.ZERO)
                .build();

        Payment mappedPayment = Payment.builder()
                .paymentAmount(BigDecimal.ZERO)
                .build();

        Payment savedPayment = Payment.builder()
                .id("payment-zero")
                .paymentAmount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentResponseDto zeroResponseDto = PaymentResponseDto.builder()
                .id("payment-zero")
                .paymentAmount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentMapper.toEntity(zeroAmountDto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(zeroResponseDto);

        PaymentResponseDto result = paymentService.createPayment(zeroAmountDto);

        assertThat(result.getPaymentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_shouldUpdateStatusToSuccess() {
        String paymentId = "payment-123";
        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .timestamp(LocalDateTime.now())
                .build();

        Payment updatedPayment = Payment.builder()
                .id(paymentId)
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();

        PaymentResponseDto updatedResponseDto = PaymentResponseDto.builder()
                .id(paymentId)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(updatedPayment);
        when(paymentMapper.toDto(updatedPayment)).thenReturn(updatedResponseDto);

        PaymentResponseDto result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.SUCCESS);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(paymentCaptor.capture());
        verify(paymentMapper).toDto(updatedPayment);

        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void updatePaymentStatus_shouldUpdateStatusToFailed() {
        String paymentId = "payment-456";
        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .build();

        Payment updatedPayment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.FAILED)
                .build();

        PaymentResponseDto failedResponseDto = PaymentResponseDto.builder()
                .id(paymentId)
                .status(PaymentStatus.FAILED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(updatedPayment);
        when(paymentMapper.toDto(updatedPayment)).thenReturn(failedResponseDto);

        PaymentResponseDto result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.FAILED);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void updatePaymentStatus_shouldThrowExceptionWhenPaymentNotFound() {
        String nonExistentId = "non-existent-id";
        when(paymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updatePaymentStatus(nonExistentId, PaymentStatus.SUCCESS))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(paymentRepository).findById(nonExistentId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentMapper, never()).toDto(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_shouldCallFindById() {
        String paymentId = "payment-789";
        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(existingPayment);
        when(paymentMapper.toDto(existingPayment)).thenReturn(responseDto);

        paymentService.updatePaymentStatus(paymentId, PaymentStatus.SUCCESS);

        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void updatePaymentStatus_shouldSaveUpdatedPayment() {
        String paymentId = "payment-save";
        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(existingPayment);
        when(paymentMapper.toDto(existingPayment)).thenReturn(responseDto);

        paymentService.updatePaymentStatus(paymentId, PaymentStatus.SUCCESS);

        verify(paymentRepository).save(existingPayment);
    }

    @Test
    void updatePaymentStatus_shouldReturnMappedDto() {
        String paymentId = "payment-mapped";
        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .build();

        Payment updatedPayment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.SUCCESS)
                .build();

        PaymentResponseDto mappedDto = PaymentResponseDto.builder()
                .id(paymentId)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(updatedPayment);
        when(paymentMapper.toDto(updatedPayment)).thenReturn(mappedDto);

        PaymentResponseDto result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.SUCCESS);

        assertThat(result).isSameAs(mappedDto);
        verify(paymentMapper).toDto(updatedPayment);
    }

    @Test
    void updatePaymentStatus_shouldHandleMultipleStatusChanges() {
        String paymentId = "payment-multi";
        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(responseDto);

        paymentService.updatePaymentStatus(paymentId, PaymentStatus.SUCCESS);

        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        reset(paymentRepository, paymentMapper);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(responseDto);

        paymentService.updatePaymentStatus(paymentId, PaymentStatus.FAILED);

        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void updatePaymentStatus_shouldPreserveOtherPaymentFields() {
        String paymentId = "payment-preserve";
        LocalDateTime originalTimestamp = LocalDateTime.now().minusDays(1);
        BigDecimal originalAmount = new BigDecimal("500.00");

        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .orderId(999L)
                .userId(888L)
                .paymentAmount(originalAmount)
                .status(PaymentStatus.PENDING)
                .timestamp(originalTimestamp)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(existingPayment);
        when(paymentMapper.toDto(existingPayment)).thenReturn(responseDto);

        paymentService.updatePaymentStatus(paymentId, PaymentStatus.SUCCESS);

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(savedPayment.getOrderId()).isEqualTo(999L);
        assertThat(savedPayment.getUserId()).isEqualTo(888L);
        assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(originalAmount);
        assertThat(savedPayment.getTimestamp()).isEqualTo(originalTimestamp);
    }

    @Test
    void updatePaymentStatus_shouldThrowPaymentNotFoundException_WithCorrectExceptionType() {
        String nonExistentId = "does-not-exist";
        when(paymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.updatePaymentStatus(nonExistentId, PaymentStatus.SUCCESS)
        );

        assertThat(exception).isInstanceOf(PaymentNotFoundException.class);
        verify(paymentRepository).findById(nonExistentId);
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class PaymentProcessingServiceTest {

        @Mock
        private PaymentService paymentService;

        @Mock
        private PaymentEventProducer eventProducer;

        @Mock
        private RandomNumberClient randomNumberClient;

        @InjectMocks
        private PaymentProcessingServiceImpl paymentProcessingService;

        @Captor
        private ArgumentCaptor<PaymentCreateRequestDto> paymentCreateCaptor;

        @Captor
        private ArgumentCaptor<PaymentCreatedEvent> paymentEventCaptor;

        private OrderCreatedEvent orderEvent;

        @BeforeEach
        void setUp() {
            orderEvent = OrderCreatedEvent.builder()
                    .orderId(100L)
                    .userId(200L)
                    .totalAmount(new BigDecimal("150.00"))
                    .build();
        }

        @Test
        void processPayment_shouldCreateSuccessfulPayment_whenRandomNumberIsEven() throws ExecutionException, InterruptedException {
            when(randomNumberClient.generateRandomNumber()).thenReturn(42);

            PaymentResponseDto pending = createPaymentResponse("payment-1", PaymentStatus.PENDING);
            PaymentResponseDto success = createPaymentResponse("payment-1", PaymentStatus.SUCCESS);

            when(paymentService.createPayment(any(PaymentCreateRequestDto.class))).thenReturn(pending);
            when(paymentService.updatePaymentStatus("payment-1", PaymentStatus.SUCCESS)).thenReturn(success);

            paymentProcessingService.processPayment(orderEvent);

            verify(paymentService).createPayment(paymentCreateCaptor.capture());
            PaymentCreateRequestDto dto = paymentCreateCaptor.getValue();
            assertThat(dto.getOrderId()).isEqualTo(100L);
            assertThat(dto.getUserId()).isEqualTo(200L);
            assertThat(dto.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("150.00"));

            verify(paymentService).updatePaymentStatus("payment-1", PaymentStatus.SUCCESS);

            verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
            PaymentCreatedEvent event = paymentEventCaptor.getValue();
            assertThat(event.getPaymentId()).isEqualTo("payment-1");
            assertThat(event.getOrderId()).isEqualTo(100L);
            assertThat(event.getUserId()).isEqualTo(200L);
            assertThat(event.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(event.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        void processPayment_shouldCreateFailedPayment_whenRandomNumberIsOdd() throws ExecutionException, InterruptedException {
            when(randomNumberClient.generateRandomNumber()).thenReturn(13);

            PaymentResponseDto pending = createPaymentResponse("payment-2", PaymentStatus.PENDING);
            PaymentResponseDto failed = createPaymentResponse("payment-2", PaymentStatus.FAILED);

            when(paymentService.createPayment(any())).thenReturn(pending);
            when(paymentService.updatePaymentStatus("payment-2", PaymentStatus.FAILED)).thenReturn(failed);

            paymentProcessingService.processPayment(orderEvent);

            verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
            verify(paymentService).updatePaymentStatus("payment-2", PaymentStatus.FAILED);

            verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
            PaymentCreatedEvent event;
            event = paymentEventCaptor.getValue();
            assertThat(event.getPaymentId()).isEqualTo("payment-2");
            assertThat(event.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        private PaymentResponseDto createPaymentResponse(String id, PaymentStatus status) {
            return PaymentResponseDto.builder()
                    .id(id)
                    .orderId(100L)
                    .userId(200L)
                    .paymentAmount(new BigDecimal("150.00"))
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}