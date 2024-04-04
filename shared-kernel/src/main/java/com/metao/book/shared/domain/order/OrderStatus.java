package com.metao.book.shared.domain.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
        NEW("NEW"),
        SUBMITTED("SUBMITTED"),
        REJECTED("REJECTED"),
        CONFIRMED("CONFIRMED"),
        ROLLED_BACK("ROLLED_BACK");

        @JsonValue
        private final String value;

        OrderStatus(String value) {
            this.value = value;
        }

        @JsonCreator
        public OrderStatus toStatus(String value) {
            for (OrderStatus orderStatus : OrderStatus.values()) {
                if (value.equals(this.value.toUpperCase())) {
                    return orderStatus;
                }
            }
            throw new RuntimeException("No value matched for Status");
        }

    @Override
    public String toString() {
        return value;
    }
}