package com.metao.book.order.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    NEW("NEW"),
    SUBMITTED("SUBMITTED"), 
    CONFIRMED("CONFIRMED"), 
    PAID("PAID"),           
    PAYMENT_FAILED("PAYMENT_FAILED"), 
    REJECTED("REJECTED"),   
    ROLLED_BACK("ROLLED_BACK");

    @JsonValue
    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public OrderStatus toStatus(String value) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.value.equalsIgnoreCase(value)) { 
                return orderStatus;
            }
        }
        throw new IllegalArgumentException("No matching OrderStatus for value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}