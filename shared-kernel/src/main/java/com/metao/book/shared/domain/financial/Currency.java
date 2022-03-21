package com.metao.book.shared.domain.financial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enumeration of currencies.
 */
public enum Currency {
    EUR("eur"), SEK("sek"), NOK("nok"), DKK("dkk"), DLR("dlr");
    
    private final String value;

    private Currency(String value){
        this.value = value;
    }

    @JsonCreator
    public Currency fromValues(@JsonProperty("currency") String value) {
        for(Currency val: Currency.values()) {
            if(val.value.equals(this.value)){
                return val;
            }
        }
        throw new RuntimeException("can't deserialize the value "+ value + " to currency");
    }

    @Override
    @JsonValue    
    public String toString() {
            return value;
    }
}
