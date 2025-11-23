package com.innowise.service.kafka;

import com.innowise.event.OrderCreatedEvent;
import com.innowise.model.enums.EventType;
import com.innowise.model.enums.OrderStatus;
import com.innowise.service.PaymentProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private PaymentProcessingService paymentProcessingService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    private OrderCreatedEvent validEvent;

    @BeforeEach
    void setUp() {
        validEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessValidEvent() {
        orderEventConsumer.handleOrderCreatedEvent(validEvent);

        verify(paymentProcessingService).processPayment(validEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldNotProcessEvent_whenEventTypeIsNotOrderCreate() {
        OrderCreatedEvent invalidTypeEvent = OrderCreatedEvent.builder()
                .eventType("ORDER_UPDATE")
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(invalidTypeEvent);

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldThrowException_whenOrderIdIsNull() {
        OrderCreatedEvent nullOrderIdEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(null)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        assertThatThrownBy(() -> orderEventConsumer.handleOrderCreatedEvent(nullOrderIdEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order ID cannot be null in OrderCreatedEvent");

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldNotProcessEvent_whenEventTypeIsOrderDelete() {
        OrderCreatedEvent deleteEvent = OrderCreatedEvent.builder()
                .eventType("ORDER_DELETE")
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(deleteEvent);

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldNotProcessEvent_whenEventTypeIsOrderCancel() {
        OrderCreatedEvent cancelEvent = OrderCreatedEvent.builder()
                .eventType("ORDER_CANCEL")
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(cancelEvent);

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldNotProcessEvent_whenEventTypeIsEmpty() {
        OrderCreatedEvent emptyTypeEvent = OrderCreatedEvent.builder()
                .eventType("")
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(emptyTypeEvent);

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withZeroAmount() {
        OrderCreatedEvent zeroAmountEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(300L)
                .userId(400L)
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(zeroAmountEvent);

        verify(paymentProcessingService).processPayment(zeroAmountEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withLargeAmount() {
        OrderCreatedEvent largeAmountEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(500L)
                .userId(600L)
                .totalAmount(new BigDecimal("999999.99"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(largeAmountEvent);

        verify(paymentProcessingService).processPayment(largeAmountEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldCallProcessPaymentOnce_forValidEvent() {
        orderEventConsumer.handleOrderCreatedEvent(validEvent);

        verify(paymentProcessingService, times(1)).processPayment(validEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withDifferentOrderStatuses() {
        OrderCreatedEvent pendingEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(700L)
                .userId(800L)
                .totalAmount(new BigDecimal("250.00"))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(pendingEvent);

        verify(paymentProcessingService).processPayment(pendingEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldThrowException_whenOrderIdIsNull_evenWithValidEventType() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(null)
                .userId(100L)
                .totalAmount(new BigDecimal("50.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        assertThatThrownBy(() -> orderEventConsumer.handleOrderCreatedEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order ID cannot be null");

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldNotProcessEvent_whenEventTypeIsNull() {
        OrderCreatedEvent nullEventType = OrderCreatedEvent.builder()
                .eventType(null)
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(nullEventType);

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldNotProcessEvent_whenEventTypeIsLowerCase() {
        OrderCreatedEvent lowerCaseEvent = OrderCreatedEvent.builder()
                .eventType("order_create")
                .orderId(100L)
                .userId(200L)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(lowerCaseEvent);

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withNegativeAmount() {
        OrderCreatedEvent negativeAmountEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(900L)
                .userId(1000L)
                .totalAmount(new BigDecimal("-50.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(negativeAmountEvent);

        verify(paymentProcessingService).processPayment(negativeAmountEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withNullAmount() {
        OrderCreatedEvent nullAmountEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(1100L)
                .userId(1200L)
                .totalAmount(null)
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(nullAmountEvent);

        verify(paymentProcessingService).processPayment(nullAmountEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withNullUserId() {
        OrderCreatedEvent nullUserIdEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(1300L)
                .userId(null)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(nullUserIdEvent);

        verify(paymentProcessingService).processPayment(nullUserIdEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withNullStatus() {
        OrderCreatedEvent nullStatusEvent = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(1400L)
                .userId(1500L)
                .totalAmount(new BigDecimal("200.00"))
                .status(null)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(nullStatusEvent);

        verify(paymentProcessingService).processPayment(nullStatusEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldPropagateException_whenPaymentProcessingFails() {
        RuntimeException expectedException = new RuntimeException("Payment processing failed");
        doThrow(expectedException).when(paymentProcessingService).processPayment(validEvent);

        assertThatThrownBy(() -> orderEventConsumer.handleOrderCreatedEvent(validEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Payment processing failed");

        verify(paymentProcessingService).processPayment(validEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldNotProcessEvent_whenEventTypeHasWhitespace() {
        OrderCreatedEvent whitespaceEvent = OrderCreatedEvent.builder()
                .eventType(" ORDER_CREATE ")
                .orderId(1600L)
                .userId(1700L)
                .totalAmount(new BigDecimal("300.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        orderEventConsumer.handleOrderCreatedEvent(whitespaceEvent);

        verify(paymentProcessingService, never()).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessMultipleValidEvents() {
        OrderCreatedEvent event1 = createValidEvent(1L, 2L);
        OrderCreatedEvent event2 = createValidEvent(3L, 4L);
        OrderCreatedEvent event3 = createValidEvent(5L, 6L);

        orderEventConsumer.handleOrderCreatedEvent(event1);
        orderEventConsumer.handleOrderCreatedEvent(event2);
        orderEventConsumer.handleOrderCreatedEvent(event3);

        verify(paymentProcessingService).processPayment(event1);
        verify(paymentProcessingService).processPayment(event2);
        verify(paymentProcessingService).processPayment(event3);
        verify(paymentProcessingService, times(3)).processPayment(any());
    }

    @Test
    void handleOrderCreatedEvent_shouldProcessEvent_withAllOrderStatuses() {
        OrderCreatedEvent confirmedEvent = createEventWithStatus(OrderStatus.CONFIRMED);
        OrderCreatedEvent pendingEvent = createEventWithStatus(OrderStatus.PAYMENT_PENDING);
        OrderCreatedEvent cancelledEvent = createEventWithStatus(OrderStatus.CANCELLED);

        orderEventConsumer.handleOrderCreatedEvent(confirmedEvent);
        orderEventConsumer.handleOrderCreatedEvent(pendingEvent);
        orderEventConsumer.handleOrderCreatedEvent(cancelledEvent);

        verify(paymentProcessingService).processPayment(confirmedEvent);
        verify(paymentProcessingService).processPayment(pendingEvent);
        verify(paymentProcessingService).processPayment(cancelledEvent);
    }

    @Test
    void handleOrderCreatedEvent_shouldThrowException_whenOrderIdIsNull_beforeProcessing() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(null)
                .userId(200L)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.CONFIRMED)
                .build();

        assertThatThrownBy(() -> orderEventConsumer.handleOrderCreatedEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order ID cannot be null");

        verifyNoInteractions(paymentProcessingService);
    }

    private OrderCreatedEvent createValidEvent(Long orderId, Long userId) {
        return OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(orderId)
                .userId(userId)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.CONFIRMED)
                .build();
    }

    private OrderCreatedEvent createEventWithStatus(OrderStatus status) {
        return OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATE.name())
                .orderId(System.currentTimeMillis())
                .userId(System.currentTimeMillis() + 1)
                .totalAmount(new BigDecimal("150.00"))
                .status(status)
                .build();
    }
}
