package com.metao.book.product.infrastructure.factory.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.protobuf.Timestamp;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.product.infrastructure.persistence.repository.JpaProductRepository;
import com.metao.book.shared.ProductUpdatedEvent;
import com.metao.kafka.KafkaEventHandler;
import com.metao.shared.test.KafkaContainer;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProductUpdatedEventConsumptionIT extends KafkaContainer {

    private static final String INVENTORY_REDUCTION_MARKER = "INVENTORY_REDUCTION";

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private KafkaTemplate<String, ProductUpdatedEvent> kafkaTemplate;

    @Autowired
    private KafkaEventHandler kafkaEventHandler;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Test
    @DisplayName("should reduce product volume when inventory-reduction event is consumed")
    void shouldReduceProductVolumeWhenInventoryReductionEventIsConsumed() {
        await().atMost(Duration.ofSeconds(10)).until(() ->
            kafkaListenerEndpointRegistry.getListenerContainers().stream().allMatch(MessageListenerContainer::isRunning)
        );

        String sku = uniqueSku();
        createProduct(sku, 8);
        assertThat(getCurrentVolume(sku)).isEqualByComparingTo(BigDecimal.valueOf(8));

        ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
            .setSku(sku)
            .setDescription(INVENTORY_REDUCTION_MARKER)
            .setVolume(3.0)
            .setUpdatedTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .build();

        kafkaTemplate.executeInTransaction(template -> {
            try {
                template.send(kafkaEventHandler.getKafkaTopic(ProductUpdatedEvent.class), "order-1:" + sku, event)
                    .get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to publish product-updated event", e);
            }
            return null;
        });

        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(300)).untilAsserted(() ->
            assertThat(getCurrentVolume(sku)).isEqualByComparingTo(BigDecimal.valueOf(5))
        );
    }

    private void createProduct(String sku, int volume) {
        ProductSku productSku = ProductSku.of(sku);
        jpaProductRepository.deleteById(productSku);

        ProductEntity entity = new ProductEntity(
            productSku,
            ProductTitle.of("Inventory Event Product"),
            ProductDescription.of("Created for product-updated integration test"),
            Quantity.of(BigDecimal.valueOf(volume)),
            Money.of(Currency.getInstance("EUR"), BigDecimal.valueOf(19.99)),
            ImageUrl.of("https://example.com/product.jpg"),
            Instant.now(),
            Instant.now()
        );
        jpaProductRepository.saveAndFlush(entity);
    }

    private BigDecimal getCurrentVolume(String sku) {
        return jpaProductRepository.findById(ProductSku.of(sku))
            .orElseThrow()
            .getVolume()
            .value();
    }

    private String uniqueSku() {
        String suffix = Long.toString(System.nanoTime(), 36).toUpperCase();
        String base = ("E2E" + suffix).replaceAll("[^A-Z0-9]", "");
        if (base.length() < 10) {
            base = (base + "0000000000").substring(0, 10);
        }
        return base.substring(0, 10);
    }
}
