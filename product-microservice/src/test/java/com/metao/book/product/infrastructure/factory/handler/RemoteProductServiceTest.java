package com.metao.book.product.infrastructure.factory.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.config.KafkaConfig;
import com.metao.book.product.application.config.SerdsConfig;
import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.infrastructure.factory.handler.kafka.KafkaProductConsumerConfiguration;
import com.metao.book.product.infrastructure.factory.handler.kafka.KafkaProductConsumerTestConfig;
import com.metao.book.product.util.ProductTestUtils;
import com.metao.book.shared.Currency;
import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ProductsResponseEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(classes = {KafkaProductConsumerConfiguration.class, KafkaProductConsumerTestConfig.class,
    SerdsConfig.class}, exclude = {KafkaConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class RemoteProductServiceTest extends SpringBootEmbeddedKafka {

    private static final String PRODUCT_ID = "PRODUCT_ID";

    @Autowired
    RemoteProductService remoteProductService;
    @Autowired
    KafkaProductConsumerTestConfig consumer;
    @MockBean
    ProductService productService;

    @Value("${kafka.topic.product}")
    String productTopic;

    @Test
    @SneakyThrows
    void handleGetProductEvent() {
        var productEvent = ProductEvent.newBuilder()
            .setCurrency(Currency.eur)
            .setPrice(120)
            .setTitle("TITLE")
            .setProductId(PRODUCT_ID)
            .setDescription("DESCRIPTION")
            .setImageUrl("IMAGE_URL")
            .build();
        var pe = ProductTestUtils.createProductEntity();
        when(productService.getProductById(new ProductId(PRODUCT_ID)))
            .thenReturn(Optional.of(pe));
        var getProductEvent = ProductsResponseEvent.newBuilder()
            .setOccurredOn(Instant.now().getEpochSecond())
            .setProducts(List.of(productEvent))
            .build();
        remoteProductService.handle(getProductEvent);

        consumer.getLatch().await(5, TimeUnit.SECONDS);
        assertEquals(0, consumer.getLatch().getCount());
    }
}