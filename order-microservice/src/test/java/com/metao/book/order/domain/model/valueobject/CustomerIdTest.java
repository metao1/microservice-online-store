package com.metao.book.order.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CustomerId Value Object Tests")
class CustomerIdTest {

    @Test
    @DisplayName("should create customer ID with value")
    void createCustomerId_withValue_shouldSucceed() {
        // GIVEN
        String value = "customer-123";

        // WHEN
        CustomerId customerId = new CustomerId(value);

        // THEN
        assertThat(customerId.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("should create customer ID with default constructor for Hibernate")
    void createCustomerId_withDefaultConstructor_shouldSucceed() {
        // WHEN
        CustomerId customerId = new CustomerId();

        // THEN
        assertThat(customerId.getValue()).isNull();
    }

    @Test
    @DisplayName("should be equal when values are same")
    void equals_withSameValue_shouldReturnTrue() {
        // GIVEN
        String value = "customer-123";
        CustomerId id1 = new CustomerId(value);
        CustomerId id2 = new CustomerId(value);

        // WHEN & THEN
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when values are different")
    void equals_withDifferentValue_shouldReturnFalse() {
        // GIVEN
        CustomerId id1 = new CustomerId("customer-123");
        CustomerId id2 = new CustomerId("customer-456");

        // WHEN & THEN
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("toString should return value")
    void toString_shouldReturnValue() {
        // GIVEN
        String value = "customer-123";
        CustomerId customerId = new CustomerId(value);

        // WHEN
        String result = customerId.toString();

        // THEN
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("should handle null value gracefully")
    void createCustomerId_withNullValue_shouldAllowForOrm() {
        // WHEN
        CustomerId customerId = new CustomerId(null);

        // THEN
        assertThat(customerId.getValue()).isNull();
    }

    @Test
    @DisplayName("should handle empty string value")
    void createCustomerId_withEmptyString_shouldSucceed() {
        // WHEN
        CustomerId customerId = new CustomerId("");

        // THEN
        assertThat(customerId.getValue()).isEmpty();
    }
}
