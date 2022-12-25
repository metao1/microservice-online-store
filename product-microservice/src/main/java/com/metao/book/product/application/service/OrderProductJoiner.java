package com.metao.book.product.application.service;

import java.time.Instant;

import org.apache.kafka.streams.kstream.ValueJoiner;

import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.ReservationEvent;

public class OrderProductJoiner implements ValueJoiner<Double, ProductEvent, ReservationEvent> {

        private static final String CUSTOMER_ID = "CUSTOMER_ID";

        @Override
        public ReservationEvent apply(Double reserved, ProductEvent product) {
                return ReservationEvent.newBuilder()
                                .setCreatedOn(Instant.now().toEpochMilli())
                                .setProductId(product.getProductId())
                                .setAvailable((product.getVolume() == null) ? 100 : (product.getVolume() - reserved))
                                .setReserved(reserved)
                                .setCustomerId(CUSTOMER_ID)
                                .build();
        }

}