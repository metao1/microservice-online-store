package com.metao.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.metao.shared.test.KafkaContainer;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@Import(KafkaConsumerConfigTest.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {
    KafkaEventHandler.class, KafkaEventConfiguration.class
})
class KafkaIntegrationIT extends KafkaContainer {

    @Autowired
    KafkaEventHandler eventHandler;

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    KafkaTemplate<String, CreatedEventTest> kafkaTemplate;

    @Test
    @Order(1)
    @SneakyThrows
    void testSendEventToAKafkaTopic_consumesFromSameKafkaTopicSuccessfully() {
        String orderId = "integrationTestOrderId";
        CreatedEventTest createdEventTest = CreatedEventTest.newBuilder()
            .setId(orderId)
            .setCustomerId("custIntegrationTest")
            .setProductSku("prodIntegrationTest")
            .setPrice(19.99)
            .setQuantity(1.0)
            .setCurrency("EUR")
            .setStatus(CreatedEventTest.Status.NEW)
            .build();

        String kafkaTopic = eventHandler.getKafkaTopic(createdEventTest.getClass());

        kafkaTemplate.send(kafkaTopic, orderId, createdEventTest);
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<PartitionInfo> partitions = kafkaTemplate.partitionsFor(kafkaTopic);
                int partition = Math.abs(createdEventTest.getId().hashCode()) % partitions.size();
                ConsumerFactory<String, CreatedEventTest> bean = beanFactory.getBean(ConsumerFactory.class);
                kafkaTemplate.setConsumerFactory(bean);
                var event = kafkaTemplate.receive(kafkaTopic, partition, 0);
                assertThat(event)
                    .isNotNull()
                    .extracting(ConsumerRecord::value)
                    .satisfies(createdEvent -> {
                        assertThat(createdEvent.getId()).isEqualTo("integrationTestOrderId");
                        assertThat(createdEvent.getCustomerId()).isEqualTo("custIntegrationTest");
                        assertThat(createdEvent.getProductSku()).isEqualTo("prodIntegrationTest");
                        assertThat(createdEvent.getQuantity()).isEqualTo(1.0);
                        assertThat(createdEvent.getPrice()).isEqualTo(19.99);
                        assertThat(createdEvent.getCurrency()).isEqualTo("EUR");
                        assertThat(createdEvent.hasUpdateTime()).isFalse();
                    });
            });
    }

    @Test
    @Order(2)
    @SneakyThrows
    void testConcurrentProduceAndConsumeOnSameTopic() {
        String orderIdPrefix = "concurrentOrder-" + System.currentTimeMillis();
        String kafkaTopic = eventHandler.getKafkaTopic(CreatedEventTest.class);
        int messageCount = 100;

        // Configure KafkaTemplate for receive operations using the same pattern as the existing test
        @SuppressWarnings("unchecked")
        ConsumerFactory<String, CreatedEventTest> consumerFactory = beanFactory.getBean(ConsumerFactory.class);
        kafkaTemplate.setConsumerFactory(consumerFactory);

        List<PartitionInfo> partitions = kafkaTemplate.partitionsFor(kafkaTopic);
        int partition = Math.abs(orderIdPrefix.hashCode()) % partitions.size();

        Set<String> expectedIds = IntStream.range(0, messageCount)
            .mapToObj(i -> orderIdPrefix + i)
            .collect(Collectors.toSet());
        Set<String> receivedIds = Collections.synchronizedSet(new HashSet<>());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable consumerTask = () -> {
            long endTime = System.currentTimeMillis() + Duration.ofSeconds(20).toMillis();
            long offset = 0L;
            while (System.currentTimeMillis() < endTime && !receivedIds.containsAll(expectedIds)) {
                var record = kafkaTemplate.receive(kafkaTopic, partition, offset);
                if (record != null && record.value() != null) {
                    String id = record.value().getId();
                    if (id.startsWith(orderIdPrefix)) {
                        receivedIds.add(id);
                    }
                }
                offset++;
            }
        };

        Runnable producerTask = () -> {
            for (int i = 0; i < messageCount; i++) {
                CreatedEventTest event = CreatedEventTest.newBuilder()
                    .setId(orderIdPrefix + i)
                    .setCustomerId("cust-" + i)
                    .setProductSku("prod-" + i)
                    .setPrice(10.0 + i)
                    .setQuantity(1.0)
                    .setCurrency("USD")
                    .setStatus(CreatedEventTest.Status.NEW)
                    .build();
                kafkaTemplate.send(kafkaTopic, orderIdPrefix, event);
            }
            kafkaTemplate.flush();
        };

        try {
            executor.submit(consumerTask);
            executor.submit(producerTask);

            await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> assertThat(receivedIds).containsAll(expectedIds));
        } finally {
            executor.shutdownNow();
        }

        assertThat(receivedIds).hasSize(messageCount);
    }

    @Test
    @Order(3)
    @SneakyThrows
    void testMultipleConcurrentProducersSingleConsumer() {
        String orderIdPrefix = "multiProducerOrder-" + System.currentTimeMillis();
        String kafkaTopic = eventHandler.getKafkaTopic(CreatedEventTest.class);

        int producerThreads = 3;
        int messagesPerProducer = 50;
        int totalMessages = producerThreads * messagesPerProducer;

        // Configure KafkaTemplate for receive operations using the same pattern as the existing test
        @SuppressWarnings("unchecked")
        ConsumerFactory<String, CreatedEventTest> consumerFactory = beanFactory.getBean(ConsumerFactory.class);

        kafkaTemplate.setConsumerFactory(consumerFactory);

        List<PartitionInfo> partitions = kafkaTemplate.partitionsFor(kafkaTopic);
        int partition = Math.abs(orderIdPrefix.hashCode()) % partitions.size();

        Set<String> expectedIds = new HashSet<>();
        for (int p = 0; p < producerThreads; p++) {
            for (int i = 0; i < messagesPerProducer; i++) {
                expectedIds.add(orderIdPrefix + "-p" + p + "-" + i);
            }
        }

        Set<String> receivedIds = Collections.synchronizedSet(new HashSet<>());

        ExecutorService executor = Executors.newFixedThreadPool(producerThreads + 1);

        Runnable consumerTask = () -> {
            long endTime = System.currentTimeMillis() + Duration.ofSeconds(30).toMillis();
            long offset = 0L;
            while (System.currentTimeMillis() < endTime && !receivedIds.containsAll(expectedIds)) {
                var record = kafkaTemplate.receive(kafkaTopic, partition, offset);
                if (record != null && record.value() != null) {
                    String id = record.value().getId();
                    if (id.startsWith(orderIdPrefix)) {
                        receivedIds.add(id);
                    }
                }
                offset++;
            }
        };

        executor.submit(consumerTask);

        for (int p = 0; p < producerThreads; p++) {
            final int producerIndex = p;
            executor.submit(() -> {
                for (int i = 0; i < messagesPerProducer; i++) {
                    String id = orderIdPrefix + "-p" + producerIndex + "-" + i;
                    CreatedEventTest event = CreatedEventTest.newBuilder()
                        .setId(id)
                        .setCustomerId("cust-" + producerIndex + "-" + i)
                        .setProductSku("prod-" + producerIndex + "-" + i)
                        .setPrice(20.0 + i)
                        .setQuantity(1.0)
                        .setCurrency("USD")
                        .setStatus(CreatedEventTest.Status.NEW)
                        .build();
                    kafkaTemplate.send(kafkaTopic, orderIdPrefix, event);
                }
                kafkaTemplate.flush();
            });
        }

        try {
            await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(receivedIds).containsAll(expectedIds));
        } finally {
            executor.shutdownNow();
        }

        assertThat(receivedIds).hasSize(totalMessages);
    }

}
