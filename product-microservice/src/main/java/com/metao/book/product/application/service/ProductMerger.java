package com.metao.book.product.application.service;

import com.metao.book.shared.ReservationEvent;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductMerger implements KeyValueMapper<String, ReservationEvent, String> {

    /**
     * Map a record with the given key and value to a new value.
     *
     * @param key              the key of the record
     * @param reservationEvent the value of the record
     * @return the new value
     */
    @Override
    public String apply(String key, ReservationEvent reservationEvent) {
        return reservationEvent.getProductId();
    }
}
