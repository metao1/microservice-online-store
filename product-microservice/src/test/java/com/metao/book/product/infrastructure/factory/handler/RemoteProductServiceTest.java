package com.metao.book.product.infrastructure.factory.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.metao.book.product.application.service.ProductService;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.infrastructure.factory.handler.kafka.KafkaProductConsumerTestConfig;
import com.metao.book.product.util.ProductTestUtils;
import com.metao.book.shared.Currency;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.metao.book.shared.ProductEvent;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({ "test", "container" })
@Import({
    KafkaProductConsumerTestConfig.class
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RemoteProductServiceTest extends SpringBootEmbeddedKafka {

        private static final String PRODUCT_ID = "PRODUCT_ID";

        @Autowired
        RemoteProductService remoteProductService;

        @Autowired
        KafkaProductConsumerTestConfig consumer;

        @MockBean
        ProductService productService;

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

                remoteProductService.handle(productEvent);

                consumer.getLatch().await(5, TimeUnit.SECONDS);
                assertEquals(0, consumer.getLatch().getCount());
        }
}