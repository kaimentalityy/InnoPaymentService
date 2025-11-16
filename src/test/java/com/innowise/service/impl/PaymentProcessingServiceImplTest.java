package com.innowise.service.impl;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.event.PaymentCreatedEvent;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.enums.PaymentStatus;
import com.innowise.service.PaymentService;
import com.innowise.service.RandomNumberClient;
import com.innowise.service.kafka.PaymentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceImplTest {

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
    private PaymentResponseDto pendingPayment;
    private PaymentResponseDto successPayment;
    private PaymentResponseDto failedPayment;

    @BeforeEach
    void setUp() {
        orderEvent = OrderCreatedEvent.builder()
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .build();

        pendingPayment = PaymentResponseDto.builder()
                .id("payment-123")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .build();

        successPayment = PaymentResponseDto.builder()
                .id("payment-123")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.SUCCESS)
                .build();

        failedPayment = PaymentResponseDto.builder()
                .id("payment-123")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.FAILED)
                .build();
    }

    @Test
    void processPayment_shouldProcessSuccessfulPayment_whenNumberIsEven() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(42);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).createPayment(paymentCreateCaptor.capture());
        PaymentCreateRequestDto capturedDto = paymentCreateCaptor.getValue();
        assertThat(capturedDto.getOrderId()).isEqualTo(100L);
        assertThat(capturedDto.getUserId()).isEqualTo(200L);
        assertThat(capturedDto.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("150.00"));

        verify(randomNumberClient).generateRandomNumber();
        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.SUCCESS);

        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        PaymentCreatedEvent capturedEvent = paymentEventCaptor.getValue();
        assertThat(capturedEvent.getPaymentId()).isEqualTo("payment-123");
        assertThat(capturedEvent.getOrderId()).isEqualTo(100L);
        assertThat(capturedEvent.getUserId()).isEqualTo(200L);
        assertThat(capturedEvent.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(capturedEvent.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_shouldCreatePaymentWithCorrectDto() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(50);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(anyString(), any(PaymentStatus.class)))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).createPayment(paymentCreateCaptor.capture());
        PaymentCreateRequestDto dto = paymentCreateCaptor.getValue();

        assertThat(dto).isNotNull();
        assertThat(dto.getOrderId()).isEqualTo(orderEvent.getOrderId());
        assertThat(dto.getUserId()).isEqualTo(orderEvent.getUserId());
        assertThat(dto.getPaymentAmount()).isEqualByComparingTo(orderEvent.getTotalAmount());
    }

    @Test
    void processPayment_shouldCallRandomNumberClient() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(100);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(anyString(), any(PaymentStatus.class)))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(randomNumberClient, times(1)).generateRandomNumber();
    }

    @Test
    void processPayment_shouldUpdatePaymentStatusToSuccess_whenEven() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(2);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_shouldPublishEventWithCorrectData_whenSuccess() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(10);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        PaymentCreatedEvent event = paymentEventCaptor.getValue();

        assertThat(event.getPaymentId()).isEqualTo(successPayment.getId());
        assertThat(event.getOrderId()).isEqualTo(orderEvent.getOrderId());
        assertThat(event.getUserId()).isEqualTo(orderEvent.getUserId());
        assertThat(event.getAmount()).isEqualByComparingTo(orderEvent.getTotalAmount());
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_shouldHandleZeroNumber() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(0);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.SUCCESS);
        verify(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    void processPayment_shouldHandleNegativeEvenNumber() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(-2);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_shouldHandleLargeEvenNumber() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(1000000);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_shouldHandleOrderWithZeroAmount() {
        OrderCreatedEvent zeroAmountEvent = OrderCreatedEvent.builder()
                .orderId(300L)
                .userId(400L)
                .totalAmount(BigDecimal.ZERO)
                .build();

        PaymentResponseDto zeroPending = PaymentResponseDto.builder()
                .id("payment-zero")
                .orderId(300L)
                .userId(400L)
                .paymentAmount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentResponseDto zeroSuccess = PaymentResponseDto.builder()
                .id("payment-zero")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(randomNumberClient.generateRandomNumber()).thenReturn(4);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(zeroPending);
        when(paymentService.updatePaymentStatus("payment-zero", PaymentStatus.SUCCESS))
                .thenReturn(zeroSuccess);

        paymentProcessingService.processPayment(zeroAmountEvent);

        verify(paymentService).createPayment(paymentCreateCaptor.capture());
        assertThat(paymentCreateCaptor.getValue().getPaymentAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void processPayment_shouldHandleOrderWithLargeAmount() {
        BigDecimal largeAmount = new BigDecimal("999999.99");
        OrderCreatedEvent largeAmountEvent = OrderCreatedEvent.builder()
                .orderId(500L)
                .userId(600L)
                .totalAmount(largeAmount)
                .build();

        PaymentResponseDto largePending = PaymentResponseDto.builder()
                .id("payment-large")
                .orderId(500L)
                .userId(600L)
                .paymentAmount(largeAmount)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentResponseDto largeSuccess = PaymentResponseDto.builder()
                .id("payment-large")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(randomNumberClient.generateRandomNumber()).thenReturn(6);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(largePending);
        when(paymentService.updatePaymentStatus("payment-large", PaymentStatus.SUCCESS))
                .thenReturn(largeSuccess);

        paymentProcessingService.processPayment(largeAmountEvent);

        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        assertThat(paymentEventCaptor.getValue().getAmount())
                .isEqualByComparingTo(largeAmount);
    }

    @Test
    void processPayment_shouldUsePaymentIdFromUpdatedPayment() {
        PaymentResponseDto updatedWithNewId = PaymentResponseDto.builder()
                .id("payment-updated-id")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.SUCCESS)
                .build();

        when(randomNumberClient.generateRandomNumber()).thenReturn(8);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(updatedWithNewId);

        paymentProcessingService.processPayment(orderEvent);

        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        assertThat(paymentEventCaptor.getValue().getPaymentId()).isEqualTo("payment-updated-id");
    }

    @Test
    void processPayment_shouldCallMethodsInCorrectOrder() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(12);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        var inOrder = inOrder(paymentService, randomNumberClient, eventProducer);
        inOrder.verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
        inOrder.verify(randomNumberClient).generateRandomNumber();
        inOrder.verify(paymentService).updatePaymentStatus(anyString(), any(PaymentStatus.class));
        inOrder.verify(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    void processPayment_shouldHandleDifferentPaymentIds() {
        PaymentResponseDto pendingWithDifferentId = PaymentResponseDto.builder()
                .id("payment-different")
                .status(PaymentStatus.PENDING)
                .build();

        PaymentResponseDto successWithDifferentId = PaymentResponseDto.builder()
                .id("payment-different")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(randomNumberClient.generateRandomNumber()).thenReturn(14);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingWithDifferentId);
        when(paymentService.updatePaymentStatus("payment-different", PaymentStatus.SUCCESS))
                .thenReturn(successWithDifferentId);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus(eq("payment-different"), eq(PaymentStatus.SUCCESS));
        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        assertThat(paymentEventCaptor.getValue().getPaymentId()).isEqualTo("payment-different");
    }

    @Test
    void processPayment_shouldNotCallEventProducer_beforePaymentStatusUpdate() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(16);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        var inOrder = inOrder(paymentService, eventProducer);
        inOrder.verify(paymentService).updatePaymentStatus(anyString(), any(PaymentStatus.class));
        inOrder.verify(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    void processPayment_shouldUseStatusNameInEvent() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(18);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        assertThat(paymentEventCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
