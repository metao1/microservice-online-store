package com.metao.book.product.infrastructure.factory.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.infrastructure.BaseKafkaIT;
import com.metao.book.product.infrastructure.factory.handler.kafka.KafkaProductConsumerTestConfig;
import com.metao.book.product.infrastructure.factory.producer.KafkaProductProducer;
import com.metao.book.product.util.ProductTestUtils;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({
    KafkaProductConsumerTestConfig.class
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class KafkaProductHandlerIT extends BaseKafkaIT {

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
        var productCreatedEvent = ProductTestUtils.productCreatedEvent();
        var pe = ProductTestUtils.createProductEntity();

        when(productService.getProductById(new ProductId(ASIN)))
            .thenReturn(Optional.of(pe));

        kafkaProductProducer.publish(productCreatedEvent);

        consumer.getLatch().await(5, TimeUnit.SECONDS);
        assertEquals(0, consumer.getLatch().getCount());
    }
}