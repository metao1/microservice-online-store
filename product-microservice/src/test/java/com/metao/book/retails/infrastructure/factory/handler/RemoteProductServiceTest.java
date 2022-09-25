package com.metao.book.retails.infrastructure.factory.handler;

import com.metao.book.retails.application.config.KafkaConfig;
import com.metao.book.retails.application.config.SerdsConfig;
import com.metao.book.retails.application.service.ProductService;
import com.metao.book.retails.infrastructure.factory.handler.kafka.KafkaProductConsumer;
import com.metao.book.retails.infrastructure.factory.handler.kafka.KafkaProductConsumerConfiguration;
import com.metao.book.retails.util.ProductTestUtils;
import com.metao.book.shared.GetProductEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@Slf4j
@DirtiesContext
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)

@SpringBootTest
@ImportAutoConfiguration(classes = {KafkaProductConsumerConfiguration.class, KafkaProductConsumer.class, SerdsConfig.class}, exclude = {KafkaConfig.class})
class RemoteProductServiceTest extends SpringBootEmbeddedKafka {

    @Autowired
    public ConsumerFactory<String, GetProductEvent> consumerFactory;
    @Autowired
    RemoteProductService remoteProductService;
    @Autowired
    KafkaProductConsumer consumer;
    @MockBean
    ProductService productService;
    private KafkaMessageListenerContainer<String, GetProductEvent> container;

    @BeforeEach
    void setUp() {
        ContainerProperties containerProperties = new ContainerProperties("product-request");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
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
        var getProductEvent = new GetProductEvent("id", Instant.now().toEpochMilli());
        var pe = ProductTestUtils.createProductEntity();
        when(productService.getProductById("id")).thenReturn(Optional.of(pe));
        remoteProductService.handle(getProductEvent);

        consumer.getLatch().await(5, TimeUnit.SECONDS);
        assertEquals(0, consumer.getLatch().getCount());
    }
}