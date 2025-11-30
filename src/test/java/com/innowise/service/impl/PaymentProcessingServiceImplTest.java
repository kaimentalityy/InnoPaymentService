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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessingServiceImpl Tests")
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
    @DisplayName("Should process successful payment when random number is even")
    void processPayment_shouldProcessSuccessfulPayment_whenNumberIsEven()
            throws ExecutionException, InterruptedException {
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
    @DisplayName("Should create payment with correct DTO")
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
    @DisplayName("Should call random number client exactly once")
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
    @DisplayName("Should update payment status to SUCCESS when number is even")
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
    @DisplayName("Should publish event with correct data when payment is successful")
    void processPayment_shouldPublishEventWithCorrectData_whenSuccess()
            throws ExecutionException, InterruptedException {
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
    @DisplayName("Should handle zero as even number")
    void processPayment_shouldHandleZeroNumber() throws ExecutionException, InterruptedException {
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
    @DisplayName("Should handle negative even number")
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
    @DisplayName("Should handle large even number")
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
    @DisplayName("Should process failed payment when random number is odd")
    void processPayment_shouldProcessFailedPayment_whenNumberIsOdd()
            throws ExecutionException, InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(1);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.FAILED))
                .thenReturn(failedPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        PaymentCreatedEvent capturedEvent = paymentEventCaptor.getValue();
        assertThat(capturedEvent.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should update payment status to FAILED when number is odd")
    void processPayment_shouldUpdatePaymentStatusToFailed_whenOdd() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(3);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.FAILED))
                .thenReturn(failedPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should publish event with FAILED status when number is odd")
    void processPayment_shouldPublishEventWithFailedStatus_whenOdd()
            throws ExecutionException, InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(5);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.FAILED))
                .thenReturn(failedPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        PaymentCreatedEvent event = paymentEventCaptor.getValue();
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(event.getPaymentId()).isEqualTo("payment-123");
    }

    @Test
    @DisplayName("Should handle negative odd number")
    void processPayment_shouldHandleNegativeOddNumber() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(-3);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.FAILED))
                .thenReturn(failedPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should handle large odd number")
    void processPayment_shouldHandleLargeOddNumber() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(999999);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.FAILED))
                .thenReturn(failedPayment);

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
    }

    

    @Test
    @DisplayName("Should mark payment as FAILED and throw RuntimeException when ExecutionException occurs")
    void processPayment_shouldMarkPaymentAsFailed_whenEventPublishingThrowsExecutionException()
            throws ExecutionException, InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(4);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);
        doThrow(new ExecutionException("Kafka error", new RuntimeException()))
                .when(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));

        assertThrows(RuntimeException.class, () -> {
            paymentProcessingService.processPayment(orderEvent);
        });

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
        verify(paymentService, times(2)).updatePaymentStatus(anyString(), any(PaymentStatus.class));
    }

    @Test
    @DisplayName("Should mark payment as FAILED and return when InterruptedException occurs")
    void processPayment_shouldMarkPaymentAsFailed_whenEventPublishingThrowsInterruptedException()
            throws ExecutionException, InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(6);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);
        doThrow(new InterruptedException("Thread interrupted"))
                .when(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));

        paymentProcessingService.processPayment(orderEvent);

        verify(paymentService).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
        verify(paymentService, times(2)).updatePaymentStatus(anyString(), any(PaymentStatus.class));

        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); 
    }

    @Test
    @DisplayName("Should throw RuntimeException when generic exception occurs during event publishing")
    void processPayment_shouldThrowException_whenEventPublishingFails()
            throws ExecutionException, InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(2);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(successPayment);
        doThrow(new RuntimeException("Kafka is down"))
                .when(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));

        assertThrows(RuntimeException.class, () -> {
            paymentProcessingService.processPayment(orderEvent);
        });

        verify(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
        verify(paymentService, times(1)).updatePaymentStatus("payment-123", PaymentStatus.SUCCESS);
        verify(paymentService, never()).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should throw RuntimeException when exception occurs during failed payment event publishing")
    void processPayment_shouldThrowException_whenExceptionDuringFailedPaymentEventPublishing()
            throws ExecutionException, InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(7);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.FAILED))
                .thenReturn(failedPayment);
        doThrow(new RuntimeException("Kafka connection lost"))
                .when(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));

        assertThrows(RuntimeException.class, () -> {
            paymentProcessingService.processPayment(orderEvent);
        });

        verify(eventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
        verify(paymentService, times(1)).updatePaymentStatus("payment-123", PaymentStatus.FAILED);
    }

    

    @Test
    @DisplayName("Should handle order with zero amount")
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
                .orderId(300L)
                .userId(400L)
                .paymentAmount(BigDecimal.ZERO)
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
    @DisplayName("Should handle order with large amount")
    void processPayment_shouldHandleOrderWithLargeAmount() throws ExecutionException, InterruptedException {
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
                .orderId(500L)
                .userId(600L)
                .paymentAmount(largeAmount)
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
    @DisplayName("Should use updated payment amount in event")
    void processPayment_shouldUseUpdatedPaymentAmount_inEvent() throws ExecutionException, InterruptedException {
        PaymentResponseDto updatedWithDifferentAmount = PaymentResponseDto.builder()
                .id("payment-123")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("200.00"))
                .status(PaymentStatus.SUCCESS)
                .build();

        when(randomNumberClient.generateRandomNumber()).thenReturn(8);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus("payment-123", PaymentStatus.SUCCESS))
                .thenReturn(updatedWithDifferentAmount);

        paymentProcessingService.processPayment(orderEvent);

        verify(eventProducer).sendPaymentCreatedEvent(paymentEventCaptor.capture());
        assertThat(paymentEventCaptor.getValue().getAmount())
                .isEqualByComparingTo(new BigDecimal("200.00"));
    }

    

    @Test
    @DisplayName("Should use payment ID from updated payment in event")
    void processPayment_shouldUsePaymentIdFromUpdatedPayment() throws ExecutionException, InterruptedException {
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
    @DisplayName("Should handle different payment IDs correctly")
    void processPayment_shouldHandleDifferentPaymentIds() throws ExecutionException, InterruptedException {
        PaymentResponseDto pendingWithDifferentId = PaymentResponseDto.builder()
                .id("payment-different")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .build();

        PaymentResponseDto successWithDifferentId = PaymentResponseDto.builder()
                .id("payment-different")
                .orderId(100L)
                .userId(200L)
                .paymentAmount(new BigDecimal("150.00"))
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
    @DisplayName("Should call methods in correct order")
    void processPayment_shouldCallMethodsInCorrectOrder() throws ExecutionException, InterruptedException {
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
    @DisplayName("Should not call event producer before payment status update")
    void processPayment_shouldNotCallEventProducer_beforePaymentStatusUpdate()
            throws ExecutionException, InterruptedException {
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
    @DisplayName("Should use correct payment status in event")
    void processPayment_shouldUseStatusNameInEvent() throws ExecutionException, InterruptedException {
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