package com.innowise.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "consumerGroupId", "payment-service");
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "earliest");
        ReflectionTestUtils.setField(kafkaConfig, "orderTopic", "order-events");
        ReflectionTestUtils.setField(kafkaConfig, "paymentTopic", "payment-events");
        ReflectionTestUtils.setField(kafkaConfig, "trustedPackages", "com.innowise.*");
    }

    @Test
    void kafkaAdmin_shouldCreateKafkaAdmin() {
        KafkaAdmin kafkaAdmin = kafkaConfig.kafkaAdmin();

        assertThat(kafkaAdmin).isNotNull();
        assertThat(kafkaAdmin.getConfigurationProperties())
                .containsEntry("bootstrap.servers", "localhost:9092");
    }

    @Test
    void orderEventsTopic_shouldCreateTopicWithCorrectName() {
        NewTopic topic = kafkaConfig.orderEventsTopic();

        assertThat(topic).isNotNull();
        assertThat(topic.name()).isEqualTo("order-events");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
    }

    @Test
    void paymentEventsTopic_shouldCreateTopicWithCorrectName() {
        NewTopic topic = kafkaConfig.paymentEventsTopic();

        assertThat(topic).isNotNull();
        assertThat(topic.name()).isEqualTo("payment-events");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
    }

    @Test
    void consumerFactory_shouldCreateConsumerFactory() {
        ConsumerFactory<String, Object> consumerFactory = kafkaConfig.consumerFactory();

        assertThat(consumerFactory).isNotNull();
        assertThat(consumerFactory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                .containsEntry(ConsumerConfig.GROUP_ID_CONFIG, "payment-service")
                .containsEntry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }

    @Test
    void producerFactory_shouldCreateProducerFactory() {
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        assertThat(producerFactory).isNotNull();
        assertThat(producerFactory.getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                .containsEntry(ProducerConfig.RETRIES_CONFIG, 3)
                .containsEntry(ProducerConfig.ACKS_CONFIG, "all");
    }

    @Test
    void kafkaTemplate_shouldCreateKafkaTemplate() {
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.kafkaTemplate(producerFactory);

        assertThat(kafkaTemplate).isNotNull();
        assertThat(kafkaTemplate.getProducerFactory()).isEqualTo(producerFactory);
    }

    @Test
    void kafkaListenerContainerFactory_shouldCreateFactory() {
        ConsumerFactory<String, Object> consumerFactory = kafkaConfig.consumerFactory();
        DefaultErrorHandler errorHandler = mock(DefaultErrorHandler.class);

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = kafkaConfig
                .kafkaListenerContainerFactory(consumerFactory, errorHandler);

        assertThat(factory).isNotNull();
        assertThat(factory.getConsumerFactory()).isEqualTo(consumerFactory);
    }

    @Test
    void errorHandler_shouldCreateErrorHandler() {
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.kafkaTemplate(producerFactory);

        DefaultErrorHandler errorHandler = kafkaConfig.errorHandler(kafkaTemplate);

        assertThat(errorHandler).isNotNull();
    }

    @Test
    void kafkaAdmin_shouldUseCorrectBootstrapServers() {
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "kafka1:9092,kafka2:9092");

        KafkaAdmin kafkaAdmin = kafkaConfig.kafkaAdmin();

        assertThat(kafkaAdmin.getConfigurationProperties())
                .containsEntry("bootstrap.servers", "kafka1:9092,kafka2:9092");
    }

    @Test
    void consumerFactory_shouldUseCorrectGroupId() {
        ReflectionTestUtils.setField(kafkaConfig, "consumerGroupId", "test-group");

        ConsumerFactory<String, Object> consumerFactory = kafkaConfig.consumerFactory();

        assertThat(consumerFactory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    }

    @Test
    void consumerFactory_shouldUseCorrectAutoOffsetReset() {
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "latest");

        ConsumerFactory<String, Object> consumerFactory = kafkaConfig.consumerFactory();

        assertThat(consumerFactory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    }

    @Test
    void orderEventsTopic_shouldUseDifferentTopicName() {
        ReflectionTestUtils.setField(kafkaConfig, "orderTopic", "custom-order-topic");

        NewTopic topic = kafkaConfig.orderEventsTopic();

        assertThat(topic.name()).isEqualTo("custom-order-topic");
    }

    @Test
    void paymentEventsTopic_shouldUseDifferentTopicName() {
        ReflectionTestUtils.setField(kafkaConfig, "paymentTopic", "custom-payment-topic");

        NewTopic topic = kafkaConfig.paymentEventsTopic();

        assertThat(topic.name()).isEqualTo("custom-payment-topic");
    }

    @Test
    void producerFactory_shouldHaveCorrectRetryConfiguration() {
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        assertThat(producerFactory.getConfigurationProperties())
                .containsEntry(ProducerConfig.RETRIES_CONFIG, 3)
                .containsEntry(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000)
                .containsEntry(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 45000);
    }

    @Test
    void producerFactory_shouldHaveCorrectBatchConfiguration() {
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        assertThat(producerFactory.getConfigurationProperties())
                .containsEntry(ProducerConfig.BATCH_SIZE_CONFIG, 16384)
                .containsEntry(ProducerConfig.LINGER_MS_CONFIG, 5);
    }

    @Test
    void producerFactory_shouldHaveCorrectCompressionType() {
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        assertThat(producerFactory.getConfigurationProperties())
                .containsEntry(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
    }
}
