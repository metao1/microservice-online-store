package com.metao.book.order.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderId Value Object Tests")
class OrderIdTest {

    @Test
    @DisplayName("should generate unique order IDs")
    void generate_shouldCreateUniqueIds() {
        // WHEN
        OrderId id1 = OrderId.generate();
        OrderId id2 = OrderId.generate();

        // THEN
        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotEqualTo(id2.value());
    }

    @Test
    @DisplayName("should create order ID from string value")
    void of_shouldCreateOrderIdFromValue() {
        // GIVEN
        String value = "order-123";

        // WHEN
        OrderId orderId = OrderId.of(value);

        // THEN
        assertThat(orderId.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("should be equal when values are same")
    void equals_withSameValue_shouldReturnTrue() {
        // GIVEN
        String value = "order-123";
        OrderId id1 = OrderId.of(value);
        OrderId id2 = OrderId.of(value);

        // WHEN & THEN
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when values are different")
    void equals_withDifferentValue_shouldReturnFalse() {
        // GIVEN
        OrderId id1 = OrderId.of("order-123");
        OrderId id2 = OrderId.of("order-456");

        // WHEN & THEN
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("toString should return value")
    void toString_shouldReturnValue() {
        // GIVEN
        String value = "order-123";
        OrderId orderId = OrderId.of(value);

        // WHEN
        String result = orderId.toString();

        // THEN
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("generated IDs should be valid UUIDs")
    void generate_shouldCreateValidUuid() {
        // WHEN
        OrderId orderId = OrderId.generate();

        // THEN
        assertThat(orderId.value())
            .isNotNull()
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}
