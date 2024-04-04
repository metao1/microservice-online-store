package com.metao.book.product.infrastructure.factory.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.event.ProductCreatedEvent;
import com.metao.book.product.infrastructure.BaseKafkaIT;
import com.metao.book.product.infrastructure.factory.handler.kafka.KafkaProductConsumerTestConfig;
import com.metao.book.product.infrastructure.factory.producer.KafkaProductProducer;
import com.metao.book.product.util.ProductTestUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"container"})
@Import({
    KafkaProductConsumerTestConfig.class
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaProductHandlerTest extends BaseKafkaIT {

    private static final Currency EUR = Currency.getInstance(Locale.GERMANY);

    private static final String ASIN = "ASIN";

    @Autowired
    KafkaProductProducer kafkaProductProducer;

    @MockBean
    ProductService productService;

    @Autowired
    private KafkaProductConsumerTestConfig consumer;

    @Test
    @SneakyThrows
    void handleGetProductEvent() {
        var productEvent = ProductEvent.builder()
            .volume(BigDecimal.valueOf(100d))
            .currency(EUR)
            .price(BigDecimal.valueOf(120d))
            .title("TITLE")
            .description("DESCRIPTION")
            .imageUrl("IMAGE_URL")
            .build();

        var productCreatedEvent = ProductCreatedEvent.builder()
            .productEvent(productEvent)
            .occurredOn(Instant.now())
            .id("RANDOM_ID")
            .build();

        var pe = ProductTestUtils.createProductEntity();

        when(productService.getProductById(new ProductId(ASIN)))
            .thenReturn(Optional.of(pe));

        kafkaProductProducer.sendToKafka(productCreatedEvent);

        consumer.getLatch().await(5, TimeUnit.SECONDS);
        assertEquals(0, consumer.getLatch().getCount());
    }

    @Test
    @SneakyThrows
    void handleGetProductEvent_ExceptionWhenRequiredFieldIsNull() {
        var productEvent = ProductEvent.builder()
            .asin(ASIN)
            .currency(EUR)
            .price(BigDecimal.valueOf(120.0))
            .title("TITLE")
            .description("DESCRIPTION")
            .build();

        var productCreatedEvent = ProductCreatedEvent.builder()
            .productEvent(productEvent)
            .occurredOn(Instant.now())
            //id is null intentionally
            .build();

        var pe = ProductTestUtils.createProductEntity();
        when(productService.getProductById(new ProductId(ASIN)))
            .thenReturn(Optional.of(pe));

        Assertions.assertThrows(RuntimeException.class, () ->
            kafkaProductProducer.sendToKafka(productCreatedEvent)
        );
    }
}