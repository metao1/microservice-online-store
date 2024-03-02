package com.metao.book.order.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    NEW("NEW"),
    SUBMITTED("SUBMITTED"),
    REJECTED("REJECTED"),
    CONFIRMED("CONFIRMED"),
    ROLLED_BACK("ROLLED_BACK");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    @JsonCreator
    public Status toStatus(String value) {
        for (Status status : Status.values()) {
            if (value.equals(this.value.toUpperCase())) {
                return status;
            }
        }
        throw new RuntimeException("No value matched for Status");
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

}
