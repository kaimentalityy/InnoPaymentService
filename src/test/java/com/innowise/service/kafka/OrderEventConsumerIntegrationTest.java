package com.innowise.service.kafka;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.event.PaymentCreatedEvent;
import com.innowise.model.dto.PaymentCreateRequestDto;
import com.innowise.model.dto.PaymentResponseDto;
import com.innowise.model.dto.PaymentStatus;
import com.innowise.service.PaymentService;
import com.innowise.service.RandomNumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.annotation.EnableKafka;
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
@EnableKafka
@DirtiesContext
class OrderEventConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private PaymentEventProducer paymentEventProducer;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private RandomNumberService randomNumberService;

    private final BlockingQueue<PaymentCreatedEvent> receivedEvents = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        receivedEvents.clear();
        doAnswer(invocation -> {
            PaymentCreatedEvent event = invocation.getArgument(0);
            receivedEvents.add(event);
            return null;
        }).when(paymentEventProducer).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    void testOrderCreatedEventProcessing_WithSuccessfulPayment() throws InterruptedException {
        when(randomNumberService.isEven()).thenReturn(true);

        PaymentResponseDto pendingPayment = createPaymentResponse("100", 1L, 2L,
                BigDecimal.valueOf(50.0), PaymentStatus.PENDING);
        PaymentResponseDto updatedPayment = createPaymentResponse("100", 1L, 2L,
                BigDecimal.valueOf(50.0), PaymentStatus.SUCCESS);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS))
                .thenReturn(updatedPayment);

        kafkaTemplate.send("order-events", createOrderEvent(1L, 2L, BigDecimal.valueOf(50.0)));

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertNotNull(paymentEvent, "Payment event should be received");
        assertEquals(updatedPayment.getId(), paymentEvent.getPaymentId());
        assertEquals(updatedPayment.getOrderId(), paymentEvent.getOrderId());
        assertEquals(updatedPayment.getUserId(), paymentEvent.getUserId());
        assertEquals("SUCCESS", paymentEvent.getStatus());

        verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
        verify(paymentService).updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS);
        verify(randomNumberService).isEven();
    }

    @Test
    void testOrderCreatedEventProcessing_WithFailedPayment() throws InterruptedException {
        when(randomNumberService.isEven()).thenReturn(false);

        PaymentResponseDto pendingPayment = createPaymentResponse("200", 2L, 3L,
                BigDecimal.valueOf(100.0), PaymentStatus.PENDING);
        PaymentResponseDto failedPayment = createPaymentResponse("200", 2L, 3L,
                BigDecimal.valueOf(100.0), PaymentStatus.FAILED);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(pendingPayment.getId(), PaymentStatus.FAILED))
                .thenReturn(failedPayment);

        kafkaTemplate.send("order-events", createOrderEvent(2L, 3L, BigDecimal.valueOf(100.0)));

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertNotNull(paymentEvent, "Payment event should be received");
        assertEquals(failedPayment.getId(), paymentEvent.getPaymentId());
        assertEquals("FAILED", paymentEvent.getStatus());

        verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
        verify(paymentService).updatePaymentStatus(pendingPayment.getId(), PaymentStatus.FAILED);
        verify(randomNumberService).isEven();
    }

    @Test
    void testOrderCreatedEventProcessing_WithLargeAmount() throws InterruptedException {
        BigDecimal largeAmount = new BigDecimal("999999.99");
        when(randomNumberService.isEven()).thenReturn(true);

        PaymentResponseDto pendingPayment = createPaymentResponse("300", 3L, 4L,
                largeAmount, PaymentStatus.PENDING);
        PaymentResponseDto updatedPayment = createPaymentResponse("300", 3L, 4L,
                largeAmount, PaymentStatus.SUCCESS);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS))
                .thenReturn(updatedPayment);

        kafkaTemplate.send("order-events", createOrderEvent(3L, 4L, largeAmount));

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertNotNull(paymentEvent, "Payment event should be received");
        assertEquals(largeAmount, paymentEvent.getAmount());
        assertEquals("SUCCESS", paymentEvent.getStatus());

        verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
        verify(paymentService).updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS);
    }

    @Test
    void testOrderCreatedEventProcessing_WithZeroAmount() throws InterruptedException {
        BigDecimal zeroAmount = BigDecimal.ZERO;
        when(randomNumberService.isEven()).thenReturn(true);

        PaymentResponseDto pendingPayment = createPaymentResponse("400", 4L, 5L,
                zeroAmount, PaymentStatus.PENDING);
        PaymentResponseDto updatedPayment = createPaymentResponse("400", 4L, 5L,
                zeroAmount, PaymentStatus.SUCCESS);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS))
                .thenReturn(updatedPayment);

        kafkaTemplate.send("order-events", createOrderEvent(4L, 5L, zeroAmount));

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertNotNull(paymentEvent, "Payment event should be received");
        assertEquals(zeroAmount, paymentEvent.getAmount());

        verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
    }

    @Test
    void testOrderCreatedEventProcessing_WithPaymentServiceException() throws InterruptedException {
        when(randomNumberService.isEven()).thenReturn(true);
        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        kafkaTemplate.send("order-events", createOrderEvent(5L, 6L, BigDecimal.valueOf(75.0)));

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(2, TimeUnit.SECONDS);
        assertNull(paymentEvent, "No payment event should be sent when service throws exception");

        verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
        verify(paymentService, never()).updatePaymentStatus(anyString(), any(PaymentStatus.class));
        verify(paymentEventProducer, never()).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    void testOrderCreatedEventProcessing_WithUpdatePaymentStatusException() throws InterruptedException {
        when(randomNumberService.isEven()).thenReturn(true);

        PaymentResponseDto pendingPayment = createPaymentResponse("500", 6L, 7L,
                BigDecimal.valueOf(50.0), PaymentStatus.PENDING);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS))
                .thenThrow(new RuntimeException("Update failed"));

        kafkaTemplate.send("order-events", createOrderEvent(6L, 7L, BigDecimal.valueOf(50.0)));

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(2, TimeUnit.SECONDS);
        assertNull(paymentEvent, "No payment event should be sent when update fails");

        verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
        verify(paymentService).updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS);
        verify(paymentEventProducer, never()).sendPaymentCreatedEvent(any(PaymentCreatedEvent.class));
    }

    @Test
    void testOrderCreatedEventProcessing_WithRandomNumberServiceException() throws InterruptedException {
        when(randomNumberService.isEven()).thenThrow(new RuntimeException("Random service unavailable"));

        PaymentResponseDto pendingPayment = createPaymentResponse("600", 7L, 8L,
                BigDecimal.valueOf(50.0), PaymentStatus.PENDING);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);

        kafkaTemplate.send("order-events", createOrderEvent(7L, 8L, BigDecimal.valueOf(50.0)));

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(2, TimeUnit.SECONDS);
        assertNull(paymentEvent, "No payment event should be sent when random service fails");

        verify(paymentService).createPayment(any(PaymentCreateRequestDto.class));
        verify(randomNumberService).isEven();
        verify(paymentService, never()).updatePaymentStatus(anyString(), any(PaymentStatus.class));
    }

    @Test
    void testOrderCreatedEventProcessing_IgnoresNonCreateOrderEvents() throws InterruptedException {
        OrderCreatedEvent nonCreateEvent = OrderCreatedEvent.builder()
                .eventType("UPDATE_ORDER")
                .orderId(8L)
                .userId(9L)
                .totalAmount(BigDecimal.valueOf(50.0))
                .status("UPDATED")
                .build();

        kafkaTemplate.send("order-events", nonCreateEvent);

        PaymentCreatedEvent paymentEvent = receivedEvents.poll(2, TimeUnit.SECONDS);
        assertNull(paymentEvent, "No payment event should be sent for non-CREATE_ORDER events");

        verify(paymentService, never()).createPayment(any(PaymentCreateRequestDto.class));
        verify(randomNumberService, never()).isEven();
    }

    @Test
    void testOrderCreatedEventProcessing_MultipleEvents() throws InterruptedException {
        when(randomNumberService.isEven()).thenReturn(true, false);

        PaymentResponseDto pendingPayment1 = createPaymentResponse("700", 9L, 10L,
                BigDecimal.valueOf(30.0), PaymentStatus.PENDING);
        PaymentResponseDto successPayment1 = createPaymentResponse("700", 9L, 10L,
                BigDecimal.valueOf(30.0), PaymentStatus.SUCCESS);

        PaymentResponseDto pendingPayment2 = createPaymentResponse("800", 10L, 11L,
                BigDecimal.valueOf(40.0), PaymentStatus.PENDING);
        PaymentResponseDto failedPayment2 = createPaymentResponse("800", 10L, 11L,
                BigDecimal.valueOf(40.0), PaymentStatus.FAILED);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment1, pendingPayment2);
        when(paymentService.updatePaymentStatus(pendingPayment1.getId(), PaymentStatus.SUCCESS))
                .thenReturn(successPayment1);
        when(paymentService.updatePaymentStatus(pendingPayment2.getId(), PaymentStatus.FAILED))
                .thenReturn(failedPayment2);

        kafkaTemplate.send("order-events", createOrderEvent(9L, 10L, BigDecimal.valueOf(30.0)));
        kafkaTemplate.send("order-events", createOrderEvent(10L, 11L, BigDecimal.valueOf(40.0)));

        PaymentCreatedEvent event1 = receivedEvents.poll(5, TimeUnit.SECONDS);
        PaymentCreatedEvent event2 = receivedEvents.poll(5, TimeUnit.SECONDS);

        assertNotNull(event1, "First payment event should be received");
        assertNotNull(event2, "Second payment event should be received");
        assertEquals("SUCCESS", event1.getStatus());
        assertEquals("FAILED", event2.getStatus());

        verify(paymentService, times(2)).createPayment(any(PaymentCreateRequestDto.class));
        verify(randomNumberService, times(2)).isEven();
    }

    @Test
    void testOrderCreatedEventProcessing_VerifiesPaymentCreateRequestFields() throws InterruptedException {
        Long orderId = 11L;
        Long userId = 12L;
        BigDecimal amount = BigDecimal.valueOf(123.45);

        when(randomNumberService.isEven()).thenReturn(true);

        PaymentResponseDto pendingPayment = createPaymentResponse("900", orderId, userId,
                amount, PaymentStatus.PENDING);
        PaymentResponseDto updatedPayment = createPaymentResponse("900", orderId, userId,
                amount, PaymentStatus.SUCCESS);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(pendingPayment.getId(), PaymentStatus.SUCCESS))
                .thenReturn(updatedPayment);

        kafkaTemplate.send("order-events", createOrderEvent(orderId, userId, amount));

        receivedEvents.poll(5, TimeUnit.SECONDS);

        verify(paymentService).createPayment(argThat(dto ->
                dto.getOrderId().equals(orderId) &&
                        dto.getUserId().equals(userId) &&
                        dto.getPaymentAmount().equals(amount)
        ));
    }

    @Test
    void testOrderCreatedEventProcessing_VerifiesPaymentEventFields() throws InterruptedException {
        String paymentId = "1000";
        Long orderId = 12L;
        Long userId = 13L;
        BigDecimal amount = BigDecimal.valueOf(250.00);

        when(randomNumberService.isEven()).thenReturn(false);

        PaymentResponseDto pendingPayment = createPaymentResponse(paymentId, orderId, userId,
                amount, PaymentStatus.PENDING);
        PaymentResponseDto failedPayment = createPaymentResponse(paymentId, orderId, userId,
                amount, PaymentStatus.FAILED);

        when(paymentService.createPayment(any(PaymentCreateRequestDto.class)))
                .thenReturn(pendingPayment);
        when(paymentService.updatePaymentStatus(paymentId, PaymentStatus.FAILED))
                .thenReturn(failedPayment);

        kafkaTemplate.send("order-events", createOrderEvent(orderId, userId, amount));

        PaymentCreatedEvent event = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertNotNull(event);
        assertEquals(paymentId, event.getPaymentId());
        assertEquals(orderId, event.getOrderId());
        assertEquals(userId, event.getUserId());
        assertEquals(amount, event.getAmount());
        assertEquals("FAILED", event.getStatus());
    }

    private OrderCreatedEvent createOrderEvent(Long orderId, Long userId, BigDecimal amount) {
        return OrderCreatedEvent.builder()
                .eventType("CREATE_ORDER")
                .orderId(orderId)
                .userId(userId)
                .totalAmount(amount)
                .status("NEW")
                .build();
    }

    private PaymentResponseDto createPaymentResponse(String id, Long orderId, Long userId,
                                                     BigDecimal amount, PaymentStatus status) {
        return PaymentResponseDto.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .status(status)
                .build();
    }
}