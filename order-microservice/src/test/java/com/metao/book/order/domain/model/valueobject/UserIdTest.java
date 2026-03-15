package com.metao.book.order.domain.model.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("userId Value Object Tests")
class UserIdTest {

    @Test
    @DisplayName("should create customer ID with value")
    void createuserId_withValue_shouldSucceed() {
        // GIVEN
        String value = "customer-123";

        // WHEN
        UserId userId = UserId.of(value);

        // THEN
        assertThat(userId.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("should create customer ID with default constructor for Hibernate")
    void createuserId_withDefaultConstructor_shouldSucceed() {
        // WHEN
        UserId userId = UserId.of("");

        // THEN
        assertThat(userId.value()).isEmpty();
    }

    @Test
    @DisplayName("should be equal when values are same")
    void equals_withSameValue_shouldReturnTrue() {
        // GIVEN
        String value = "customer-123";
        UserId id1 = UserId.of(value);
        UserId id2 = UserId.of(value);

        // WHEN & THEN
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when values are different")
    void equals_withDifferentValue_shouldReturnFalse() {
        // GIVEN
        UserId id1 = UserId.of("customer-123");
        UserId id2 = UserId.of("customer-456");

        // WHEN & THEN
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("toString should return value")
    void toString_shouldReturnValue() {
        // GIVEN
        String value = "customer-123";
        UserId userId = UserId.of(value);

        // WHEN
        String result = userId.toString();

        // THEN
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("should handle null value gracefully")
    void createuserId_withNullValue_shouldNotAllowForOrm() {
        // WHEN & THEN
        assertThatThrownBy(() -> UserId.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should handle empty string value")
    void createuserId_withEmptyString_shouldSucceed() {
        // WHEN
        UserId userId = UserId.of("");

        // THEN
        assertThat(userId.value()).isEmpty();
    }
}
