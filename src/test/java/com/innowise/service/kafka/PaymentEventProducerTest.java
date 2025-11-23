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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(paymentEventProducer, "PAYMENT_EVENTS_TOPIC", "payment-events");

                testEvent = PaymentCreatedEvent.builder()
                                .eventId("event-123")
                                .paymentId("payment-123")
                                .orderId(100L)
                                .userId(200L)
                                .amount(new BigDecimal("150.00"))
                                .status(PaymentStatus.SUCCESS)
                                .eventTimestamp(LocalDateTime.now())
                                .build();
        }

        @Test
        void sendPaymentCreatedEvent_shouldSendEventSuccessfully() throws ExecutionException, InterruptedException {
                RecordMetadata metadata = new RecordMetadata(
                                new TopicPartition("payment-events", 0),
                                0L,
                                0,
                                System.currentTimeMillis(),
                                0,
                                0);
                ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                "payment-events",
                                "100",
                                testEvent);
                SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
                CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                paymentEventProducer.sendPaymentCreatedEvent(testEvent);

                verify(kafkaTemplate, atLeastOnce()).send(
                                topicCaptor.capture(),
                                keyCaptor.capture(),
                                eventCaptor.capture());

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
        void sendPaymentCreatedEvent_shouldThrowException_whenFutureGetFails() {
                CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
                future.completeExceptionally(new RuntimeException("Kafka send failed"));

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                assertThatThrownBy(() -> paymentEventProducer.sendPaymentCreatedEvent(testEvent))
                                .isInstanceOf(ExecutionException.class);

                verify(kafkaTemplate, atLeastOnce()).send(eq("payment-events"), eq("100"), eq(testEvent));
        }

        @Test
        void sendPaymentCreatedEvent_shouldUseOrderIdAsKey() throws ExecutionException, InterruptedException {
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
                                0);
                ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                "payment-events",
                                "999",
                                eventWithDifferentOrderId);
                SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
                CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                paymentEventProducer.sendPaymentCreatedEvent(eventWithDifferentOrderId);

                verify(kafkaTemplate, atLeastOnce()).send(
                                eq("payment-events"),
                                keyCaptor.capture(),
                                eq(eventWithDifferentOrderId));

                assertThat(keyCaptor.getValue()).isEqualTo("999");
        }

        @Test
        void sendPaymentCreatedEvent_shouldSendToCorrectTopic() throws ExecutionException, InterruptedException {
                RecordMetadata metadata = new RecordMetadata(
                                new TopicPartition("payment-events", 0),
                                0L,
                                0,
                                System.currentTimeMillis(),
                                0,
                                0);
                ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                "payment-events",
                                "100",
                                testEvent);
                SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
                CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                paymentEventProducer.sendPaymentCreatedEvent(testEvent);

                verify(kafkaTemplate, atLeastOnce()).send(
                                topicCaptor.capture(),
                                anyString(),
                                any());

                assertThat(topicCaptor.getValue()).isEqualTo("payment-events");
        }

        @Test
        void sendPaymentCreatedEvent_shouldHandleEventWithZeroAmount() throws ExecutionException, InterruptedException {
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
                                0);
                ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                "payment-events",
                                "200",
                                zeroAmountEvent);
                SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
                CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                paymentEventProducer.sendPaymentCreatedEvent(zeroAmountEvent);

                verify(kafkaTemplate, atLeastOnce()).send(
                                eq("payment-events"),
                                eq("200"),
                                eventCaptor.capture());

                assertThat(eventCaptor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void sendPaymentCreatedEvent_shouldHandleSuccessPaymentStatus()
                        throws ExecutionException, InterruptedException {
                PaymentCreatedEvent successEvent = createEvent("payment-success", 400L, PaymentStatus.SUCCESS);

                RecordMetadata metadata = new RecordMetadata(
                                new TopicPartition("payment-events", 0),
                                0L,
                                0,
                                System.currentTimeMillis(),
                                0,
                                0);
                ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                "payment-events",
                                "400",
                                successEvent);
                SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
                CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                paymentEventProducer.sendPaymentCreatedEvent(successEvent);

                verify(kafkaTemplate, atLeastOnce()).send(
                                eq("payment-events"),
                                eq("400"),
                                eventCaptor.capture());

                assertThat(eventCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        void sendPaymentCreatedEvent_shouldHandleFailedPaymentStatus() throws ExecutionException, InterruptedException {
                PaymentCreatedEvent failedEvent = createEvent("payment-failed", 500L, PaymentStatus.FAILED);

                RecordMetadata metadata = new RecordMetadata(
                                new TopicPartition("payment-events", 0),
                                0L,
                                0,
                                System.currentTimeMillis(),
                                0,
                                0);
                ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                "payment-events",
                                "500",
                                failedEvent);
                SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
                CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                paymentEventProducer.sendPaymentCreatedEvent(failedEvent);

                verify(kafkaTemplate, atLeastOnce()).send(
                                eq("payment-events"),
                                eq("500"),
                                eventCaptor.capture());

                assertThat(eventCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        void sendPaymentCreatedEvent_shouldHandlePendingPaymentStatus()
                        throws ExecutionException, InterruptedException {
                PaymentCreatedEvent pendingEvent = createEvent("payment-pending", 600L, PaymentStatus.PENDING);

                RecordMetadata metadata = new RecordMetadata(
                                new TopicPartition("payment-events", 0),
                                0L,
                                0,
                                System.currentTimeMillis(),
                                0,
                                0);
                ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                "payment-events",
                                "600",
                                pendingEvent);
                SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);
                CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

                when(kafkaTemplate.send(anyString(), anyString(), any()))
                                .thenReturn(future);

                paymentEventProducer.sendPaymentCreatedEvent(pendingEvent);

                verify(kafkaTemplate, atLeastOnce()).send(
                                eq("payment-events"),
                                eq("600"),
                                eventCaptor.capture());

                assertThat(eventCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);
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
