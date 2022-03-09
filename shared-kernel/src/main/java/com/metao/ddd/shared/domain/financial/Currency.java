package com.metao.ddd.shared.domain.financial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of currencies.
 */
public enum Currency {
    EUR("eur"), SEK("sek"), NOK("nok"), DKK("dkk"), DLR("dlr");
    
    private String value;

    private Currency(String value){
        this.value = value;
    }

    @JsonCreator
    public Currency toCurrency(String value) {
        for(Currency val: Currency.values()) {
            if(val.value.equals(this.value)){
                return val;
            }
        }
        throw new RuntimeException("can't deserialize the value "+ value + " to currency");
    }

    @JsonValue
    public String value() {
            return value;
    }
}
