package com.metao.book.retails.infrustructure.factory.handler;

import com.metao.book.retails.application.config.KafkaProductConsumer;
import com.metao.book.retails.application.config.SerdsConfig;
import com.metao.book.retails.application.service.ProductService;
import com.metao.book.retails.domain.event.GetProductEvent;
import com.metao.book.retails.util.ProductTestUtils;
import com.metao.book.shared.domain.RemoteEvent;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@DirtiesContext
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@ImportAutoConfiguration(classes = {KafkaProductConsumerConfiguration.class, SerdsConfig.class})
@SpringBootTest
class RemoteProductServiceTest extends SpringBootEmbeddedKafka {

    @Autowired
    private RemoteProductService remoteProductService;

    @Autowired
    private KafkaProductConsumer consumer;

    @MockBean
    private ProductService productService;
    private KafkaMessageListenerContainer container;
    private LinkedBlockingQueue<ConsumerRecord<String, RemoteEvent>> records;

    @Autowired
    public ConsumerFactory<String, RemoteEvent> consumerFactory;

    @BeforeEach
    void setUp() {

        ContainerProperties containerProperties = new ContainerProperties("products");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, RemoteEvent>) record -> records.add(record));
        container.start();
        ContainerTestUtils.waitForAssignment(container, kafkaEmbeddedBroker.getPartitionsPerTopic());
    }

    @AfterEach
    void tearDown() {
        container.stop();
    }

    @Test
    @SneakyThrows
    void handleGetProductEvent() {
        var getProductEvent = new GetProductEvent("id", Instant.now(), Instant.now());
        var pe = ProductTestUtils.createProductEntity();
        when(productService.getProductById("id")).thenReturn(Optional.of(pe));
        remoteProductService.handle(getProductEvent);

        consumer.getLatch().await(30, TimeUnit.SECONDS);
        ConsumerRecord singleRecord = records.poll(100, TimeUnit.MILLISECONDS);
        assertThat(singleRecord).isNotNull();
        assertEquals(0, consumer.getLatch().getCount());
    }
}