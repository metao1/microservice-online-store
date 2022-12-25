package com.metao.book.order.application.service;

import com.metao.book.shared.ReservationEvent;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.springframework.stereotype.Component;

@Component
public class ReservationJoiner implements ValueJoiner<String, ReservationEvent, String> {

    /**
     * Return a joined value consisting of {@code value1} and {@code value2}.
     *
     * @param value1 the first value for joining
     * @param value2 the second value for joining
     * @return the joined value
     */
    @Override
    public String apply(String value1, ReservationEvent value2) {
        return null;
    }
}
