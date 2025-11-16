package com.innowise.service.kafka;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.event.PaymentCreatedEvent;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.enums.EventType;
import com.innowise.model.enums.OrderStatus;
import com.innowise.model.enums.PaymentStatus;
import com.innowise.service.PaymentService;
import com.innowise.service.RandomNumberClient;
import com.innowise.service.impl.PaymentProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = com.innowise.PaymentApplication.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
        }
)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"order-events", "payment-events"})
@DirtiesContext
class OrderEventConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentEventProducer paymentEventProducer;

    @MockBean
    private RandomNumberClient randomNumberClient;

    private PaymentProcessingServiceImpl paymentProcessingService;

    private final BlockingQueue<PaymentCreatedEvent> receivedEvents = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        receivedEvents.clear();
        paymentProcessingService = new PaymentProcessingServiceImpl(paymentService, paymentEventProducer, randomNumberClient);

        doAnswer(invocation -> {
            PaymentCreatedEvent event = invocation.getArgument(0);
            receivedEvents.add(event);
            return null;
        }).when(paymentEventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    void testProcessPayment_Successful() throws InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(42);

        PaymentResponseDto pending = createPaymentResponse("p1", PaymentStatus.PENDING);
        PaymentResponseDto success = createPaymentResponse("p1", PaymentStatus.SUCCESS);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class))).thenReturn(pending);
        when(paymentService.updatePaymentStatus(pending.getId(), PaymentStatus.SUCCESS)).thenReturn(success);

        OrderCreatedEvent event = createOrderEvent(1L, 2L, new BigDecimal("50.00"));
        paymentProcessingService.processPayment(event);

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(2, TimeUnit.SECONDS);
        assertNotNull(paymentEvent);
        assertEquals(PaymentStatus.SUCCESS, paymentEvent.getStatus());

        verify(paymentService).createPayment(any());
        verify(paymentService).updatePaymentStatus(pending.getId(), PaymentStatus.SUCCESS);
        verify(paymentEventProducer).sendPaymentCreatedEvent(any());
        verify(randomNumberClient).generateRandomNumber();
    }

    @Test
    void testProcessPayment_Failed() throws InterruptedException {
        when(randomNumberClient.generateRandomNumber()).thenReturn(43);

        PaymentResponseDto pending = createPaymentResponse("p2", PaymentStatus.PENDING);
        PaymentResponseDto failed = createPaymentResponse("p2", PaymentStatus.FAILED);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class))).thenReturn(pending);
        when(paymentService.updatePaymentStatus(pending.getId(), PaymentStatus.FAILED)).thenReturn(failed);

        OrderCreatedEvent event = createOrderEvent(3L, 4L, new BigDecimal("100.00"));
        paymentProcessingService.processPayment(event);

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(2, TimeUnit.SECONDS);
        assertNotNull(paymentEvent);
        assertEquals(PaymentStatus.FAILED, paymentEvent.getStatus());

        verify(paymentService).createPayment(any());
        verify(paymentService).updatePaymentStatus(pending.getId(), PaymentStatus.FAILED);
        verify(paymentEventProducer).sendPaymentCreatedEvent(any());
        verify(randomNumberClient).generateRandomNumber();
    }

    @Test
    void testProcessPayment_WithPaymentServiceException() {
        when(randomNumberClient.generateRandomNumber()).thenReturn(50);
        when(paymentService.createPayment(any())).thenThrow(new RuntimeException("DB error"));

        OrderCreatedEvent event = createOrderEvent(5L, 6L, new BigDecimal("75.00"));
        assertThrows(RuntimeException.class, () -> paymentProcessingService.processPayment(event));

        verify(paymentService).createPayment(any());
        verify(paymentService, never()).updatePaymentStatus(any(), any());
        verify(paymentEventProducer, never()).sendPaymentCreatedEvent(any());
    }

    @Test
    void testProcessPayment_WithRandomNumberClientException() {
        when(randomNumberClient.generateRandomNumber()).thenThrow(new RuntimeException("Random service down"));

        PaymentResponseDto pending = createPaymentResponse("p3", PaymentStatus.PENDING);
        when(paymentService.createPayment(any())).thenReturn(pending);

        OrderCreatedEvent event = createOrderEvent(7L, 8L, new BigDecimal("50.00"));
        assertThrows(RuntimeException.class, () -> paymentProcessingService.processPayment(event));

        verify(paymentService).createPayment(any());
        verify(paymentService, never()).updatePaymentStatus(any(), any());
        verify(paymentEventProducer, never()).sendPaymentCreatedEvent(any());
        verify(randomNumberClient).generateRandomNumber();
    }

    private OrderCreatedEvent createOrderEvent(Long orderId, Long userId, BigDecimal amount) {
        return OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.getMessage())
                .orderId(orderId)
                .userId(userId)
                .totalAmount(amount)
                .status(OrderStatus.CONFIRMED)
                .build();
    }

    private PaymentResponseDto createPaymentResponse(String id, PaymentStatus status) {
        return PaymentResponseDto.builder()
                .id(id)
                .orderId(1L)
                .userId(2L)
                .paymentAmount(new BigDecimal("50.00"))
                .status(status)
                .build();
    }
}
