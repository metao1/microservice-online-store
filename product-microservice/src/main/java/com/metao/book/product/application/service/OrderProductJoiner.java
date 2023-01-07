package com.metao.book.product.application.service;

import com.metao.book.shared.ProductEvent;
import com.metao.book.shared.StockReservationEvent;
import java.time.Instant;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.springframework.stereotype.Component;

@Component
public class OrderProductJoiner implements ValueJoiner<Double, ProductEvent, StockReservationEvent> {

    private static final String CUSTOMER_ID = "CUSTOMER_ID";

    @Override
    public StockReservationEvent apply(Double totalOrderQuantity, ProductEvent product) {
        return StockReservationEvent.newBuilder()
            .setCreatedOn(Instant.now().toEpochMilli())
            .setProductId(product.getProductId())
            .setAvailable(calculateAvailableItems(totalOrderQuantity, product.getVolume()))
            .setReserved(totalOrderQuantity)
            .setCustomerId(CUSTOMER_ID)
            .build();
    }

    private Double calculateAvailableItems(Double totalOrderQuantity, Double totalProductCount) {
        return totalOrderQuantity == null ? 100 : (totalProductCount - totalOrderQuantity);
    }

}