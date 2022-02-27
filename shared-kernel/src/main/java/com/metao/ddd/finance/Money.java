package com.metao.ddd.finance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.ddd.base.ValueObject;
import org.springframework.lang.NonNull;

import java.util.Objects;

public class Money implements ValueObject {

    @JsonProperty("currency")
    private final Currency currency;

    @JsonProperty("amount")
    private final int amount;

    @JsonCreator
    public Money(@NonNull Currency currency, @NonNull int amount) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        if (amount < 0) {
            throw new RuntimeException("nothing is free, even so can't be negative");
        }
        this.amount = amount;
    }

    public Money(@NonNull Currency currency, double amount) {
        this(currency, (int) (amount * 100));
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }
}
