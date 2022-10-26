package com.metao.book.retails.infrastructure.factory.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.metao.book.retails.application.config.KafkaConfig;
import com.metao.book.retails.application.config.SerdsConfig;
import com.metao.book.retails.application.service.ProductService;
import com.metao.book.retails.infrastructure.factory.handler.kafka.KafkaProductConsumer;
import com.metao.book.retails.infrastructure.factory.handler.kafka.KafkaProductConsumerConfiguration;
import com.metao.book.retails.util.ProductTestUtils;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ImportAutoConfiguration(classes = {KafkaProductConsumerConfiguration.class, KafkaProductConsumer.class, SerdsConfig.class}, exclude = {KafkaConfig.class})
class RemoteProductServiceTest extends SpringBootEmbeddedKafka {

    private static final String PRODUCT_ID = "PRODUCT_ID";
    @Autowired
    public ConsumerFactory<String, ProductsResponseEvent> consumerFactory;
    @Autowired
    RemoteProductService remoteProductService;
    @Autowired
    KafkaProductConsumer consumer;
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
        when(productService.getProductById(PRODUCT_ID)).thenReturn(Optional.of(pe));
        var getProductEvent = ProductsResponseEvent.newBuilder()
            .setOccurredOn(Instant.now().getEpochSecond())
            .setProducts(List.of(productEvent))
            .build();
        remoteProductService.handle(getProductEvent);

        consumer.getLatch().await(5, TimeUnit.SECONDS);
        assertEquals(0, consumer.getLatch().getCount());
    }
}