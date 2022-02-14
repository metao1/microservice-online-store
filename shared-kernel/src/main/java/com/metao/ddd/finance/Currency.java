package com.metao.ddd.finance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Currency {
    EUR("euro"), DLR("dollar");

    private final String value;

    Currency(String curr) {
        this.value = curr;
    }

    @JsonCreator
    public static Currency fromValue(String curr) {
        for (Currency currency : Currency.values()) {
            if (currency.value.equals(curr)) {
                return currency;
            }
        }
        throw new RuntimeException("invalid value " + curr + " for currency");
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }
}
