package com.innowise.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for custom business metrics using Micrometer.
 * These metrics will be exposed via Prometheus and visualized in Grafana.
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    /**
     * Customizes the MeterRegistry to add common tags to all metrics.
     * These tags help identify metrics in Prometheus/Grafana.
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "inno-payment-service",
                        "service", "payment-service");
    }

    /**
     * Enables @Timed annotation support for method execution timing.
     * This allows precise timing of specific methods.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Counter for tracking total payments created
     */
    @Bean
    public Counter paymentsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("payments.created.total")
                .description("Total number of payments created")
                .tag("service", "payment-service")
                .register(registry);
    }

    /**
     * Counter for tracking successful payments
     */
    @Bean
    public Counter paymentsSuccessCounter(MeterRegistry registry) {
        return Counter.builder("payments.status.success")
                .description("Number of successful payments")
                .tag("service", "payment-service")
                .tag("status", "success")
                .register(registry);
    }

    /**
     * Counter for tracking failed payments
     */
    @Bean
    public Counter paymentsFailedCounter(MeterRegistry registry) {
        return Counter.builder("payments.status.failed")
                .description("Number of failed payments")
                .tag("service", "payment-service")
                .tag("status", "failed")
                .register(registry);
    }

    /**
     * Counter for tracking refunded payments
     */
    @Bean
    public Counter paymentsRefundedCounter(MeterRegistry registry) {
        return Counter.builder("payments.status.refunded")
                .description("Number of refunded payments")
                .tag("service", "payment-service")
                .tag("status", "refunded")
                .register(registry);
    }

    /**
     * Timer for tracking payment processing duration
     */
    @Bean
    public Timer paymentProcessingTimer(MeterRegistry registry) {
        return Timer.builder("payment.processing.duration")
                .description("Time taken to process a payment")
                .tag("service", "payment-service")
                .register(registry);
    }

    /**
     * Counter for tracking Kafka order messages consumed
     */
    @Bean
    public Counter kafkaOrderMessagesConsumedCounter(MeterRegistry registry) {
        return Counter.builder("kafka.order.messages.consumed")
                .description("Total number of order messages consumed from Kafka")
                .tag("service", "payment-service")
                .register(registry);
    }

    /**
     * Counter for tracking Order Service HTTP calls
     */
    @Bean
    public Counter orderServiceCallsCounter(MeterRegistry registry) {
        return Counter.builder("order.service.calls.total")
                .description("Total number of HTTP calls to Order Service")
                .tag("service", "payment-service")
                .register(registry);
    }

    /**
     * Timer for tracking MongoDB operations
     */
    @Bean
    public Timer mongoOperationTimer(MeterRegistry registry) {
        return Timer.builder("mongodb.operation.duration")
                .description("Time taken for MongoDB operations")
                .tag("service", "payment-service")
                .register(registry);
    }
}
