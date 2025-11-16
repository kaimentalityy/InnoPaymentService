package com.innowise.service.kafka;

import com.innowise.event.PaymentCreatedEvent;
import com.innowise.model.enums.PaymentStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentEventProducer paymentEventProducer;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<PaymentCreatedEvent> eventCaptor;

    private PaymentCreatedEvent testEvent;
    private CompletableFuture<SendResult<String, Object>> future;

    @BeforeEach
    void setUp() {
        testEvent = PaymentCreatedEvent.builder()
                .eventId("event-123")
                .paymentId("payment-123")
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("150.00"))
                .status(PaymentStatus.SUCCESS)
                .eventTimestamp(LocalDateTime.now())
                .build();

        future = new CompletableFuture<>();
    }

    @Test
    void sendPaymentCreatedEvent_shouldSendEventSuccessfully() {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "100",
                testEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(testEvent);

        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo("payment-events");
        assertThat(keyCaptor.getValue()).isEqualTo("100");

        PaymentCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getPaymentId()).isEqualTo("payment-123");
        assertThat(capturedEvent.getOrderId()).isEqualTo(100L);
        assertThat(capturedEvent.getUserId()).isEqualTo(200L);
        assertThat(capturedEvent.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(capturedEvent.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleFailedSend() {
        RuntimeException exception = new RuntimeException("Kafka send failed");
        future.completeExceptionally(exception);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(testEvent);

        verify(kafkaTemplate).send(eq("payment-events"), eq("100"), eq(testEvent));
    }

    @Test
    void sendPaymentCreatedEvent_shouldUseOrderIdAsKey() {
        PaymentCreatedEvent eventWithDifferentOrderId = PaymentCreatedEvent.builder()
                .paymentId("payment-456")
                .orderId(999L)
                .userId(888L)
                .amount(new BigDecimal("500.00"))
                .status(PaymentStatus.FAILED)
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "999",
                eventWithDifferentOrderId
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(eventWithDifferentOrderId);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                keyCaptor.capture(),
                eq(eventWithDifferentOrderId)
        );

        assertThat(keyCaptor.getValue()).isEqualTo("999");
    }

    @Test
    void sendPaymentCreatedEvent_shouldSendToCorrectTopic() {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "100",
                testEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(testEvent);

        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                anyString(),
                any()
        );

        assertThat(topicCaptor.getValue()).isEqualTo("payment-events");
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleSuccessfulSendWithMetadata() {
        long expectedOffset = 42L;
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                expectedOffset,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "100",
                testEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(testEvent);

        verify(kafkaTemplate).send(eq("payment-events"), eq("100"), eq(testEvent));

        assertThat(metadata.offset()).isEqualTo(expectedOffset);
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleMultipleEvents() {
        PaymentCreatedEvent event1 = createEvent("payment-1", 101L, PaymentStatus.SUCCESS);
        PaymentCreatedEvent event2 = createEvent("payment-2", 102L, PaymentStatus.FAILED);
        PaymentCreatedEvent event3 = createEvent("payment-3", 103L, PaymentStatus.PENDING);

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "100",
                testEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);

        CompletableFuture<SendResult<String, Object>> future1 = new CompletableFuture<>();
        CompletableFuture<SendResult<String, Object>> future2 = new CompletableFuture<>();
        CompletableFuture<SendResult<String, Object>> future3 = new CompletableFuture<>();

        future1.complete(sendResult);
        future2.complete(sendResult);
        future3.complete(sendResult);

        when(kafkaTemplate.send(anyString(), eq("101"), eq(event1))).thenReturn(future1);
        when(kafkaTemplate.send(anyString(), eq("102"), eq(event2))).thenReturn(future2);
        when(kafkaTemplate.send(anyString(), eq("103"), eq(event3))).thenReturn(future3);

        paymentEventProducer.sendPaymentCreatedEvent(event1);
        paymentEventProducer.sendPaymentCreatedEvent(event2);
        paymentEventProducer.sendPaymentCreatedEvent(event3);

        verify(kafkaTemplate).send(eq("payment-events"), eq("101"), eq(event1));
        verify(kafkaTemplate).send(eq("payment-events"), eq("102"), eq(event2));
        verify(kafkaTemplate).send(eq("payment-events"), eq("103"), eq(event3));
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleEventWithZeroAmount() {
        PaymentCreatedEvent zeroAmountEvent = PaymentCreatedEvent.builder()
                .paymentId("payment-zero")
                .orderId(200L)
                .userId(300L)
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.SUCCESS)
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "200",
                zeroAmountEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(zeroAmountEvent);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("200"),
                eventCaptor.capture()
        );

        assertThat(eventCaptor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleEventWithLargeAmount() {
        PaymentCreatedEvent largeAmountEvent = PaymentCreatedEvent.builder()
                .paymentId("payment-large")
                .orderId(300L)
                .userId(400L)
                .amount(new BigDecimal("999999.99"))
                .status(PaymentStatus.SUCCESS)
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "300",
                largeAmountEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(largeAmountEvent);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("300"),
                eventCaptor.capture()
        );

        assertThat(eventCaptor.getValue().getAmount())
                .isEqualByComparingTo(new BigDecimal("999999.99"));
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleNullPointerExceptionInWhenComplete() {
        CompletableFuture<SendResult<String, Object>> nullFuture = new CompletableFuture<>();

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(nullFuture);

        paymentEventProducer.sendPaymentCreatedEvent(testEvent);

        nullFuture.completeExceptionally(new NullPointerException("Null result"));

        verify(kafkaTemplate).send(eq("payment-events"), eq("100"), eq(testEvent));
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleSuccessPaymentStatus() {
        PaymentCreatedEvent successEvent = createEvent("payment-success", 400L, PaymentStatus.SUCCESS);

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "400",
                successEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(successEvent);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("400"),
                eventCaptor.capture()
        );

        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleFailedPaymentStatus() {
        PaymentCreatedEvent failedEvent = createEvent("payment-failed", 500L, PaymentStatus.FAILED);

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "500",
                failedEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(failedEvent);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("500"),
                eventCaptor.capture()
        );

        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandlePendingPaymentStatus() {
        PaymentCreatedEvent pendingEvent = createEvent("payment-pending", 600L, PaymentStatus.PENDING);

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "600",
                pendingEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(pendingEvent);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("600"),
                eventCaptor.capture()
        );

        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void sendPaymentCreatedEvent_shouldHandleEventWithAllFields() {
        PaymentCreatedEvent completeEvent = PaymentCreatedEvent.builder()
                .eventId("event-complete")
                .eventType("CREATE_PAYMENT")
                .eventTimestamp(LocalDateTime.now())
                .paymentId("payment-complete")
                .orderId(700L)
                .userId(800L)
                .amount(new BigDecimal("250.50"))
                .status(PaymentStatus.SUCCESS)
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "700",
                completeEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(completeEvent);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("700"),
                eventCaptor.capture()
        );

        PaymentCreatedEvent captured = eventCaptor.getValue();
        assertThat(captured.getEventId()).isEqualTo("event-complete");
        assertThat(captured.getEventType()).isEqualTo("CREATE_PAYMENT");
        assertThat(captured.getPaymentId()).isEqualTo("payment-complete");
        assertThat(captured.getOrderId()).isEqualTo(700L);
        assertThat(captured.getUserId()).isEqualTo(800L);
        assertThat(captured.getAmount()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(captured.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void sendPaymentCreatedEvent_shouldCallKafkaTemplateOnce() {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("payment-events", 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                "payment-events",
                "100",
                testEvent
        );
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        paymentEventProducer.sendPaymentCreatedEvent(testEvent);

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    private PaymentCreatedEvent createEvent(String paymentId, Long orderId, PaymentStatus status) {
        return PaymentCreatedEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(orderId + 100)
                .amount(new BigDecimal("100.00"))
                .status(status)
                .build();
    }
}