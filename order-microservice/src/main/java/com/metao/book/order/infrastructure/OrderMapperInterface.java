package com.metao.book.order.infrastructure;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderEvent;
import com.metao.book.shared.Status;
import java.math.BigDecimal;
import java.time.Instant;

public interface OrderMapperInterface {

    default OrderEvent toAvro(OrderDTO dto) {
        return OrderEvent.newBuilder()
            .setStatus(Status.NEW)
            .setCurrency(Currency.dlr)
            .setPrice(dto.getPrice().doubleValue())
            .setSource("STOCK")
            .setCreatedOn(Instant.now().toEpochMilli())
            .setStatus(Status.valueOf(dto.getStatus().toString()))
            .setQuantity(dto.getQuantity().doubleValue())
            .setProductId(dto.getProductId())
            .setCustomerId(dto.getCustomerId())
            .setOrderId(dto.getOrderId())
            .build();
    }

    default OrderDTO toDto(OrderEvent order) {
        return OrderDTO.builder()
            .orderId(order.getOrderId())
            .productId(order.getProductId())
            .customerId(order.getCustomerId())
            .status(convertToStatus(order.getStatus()))
            .currency(convertToCurrency(order.getCurrency()))
            .quantity(BigDecimal.valueOf(order.getQuantity()))
            .price(BigDecimal.valueOf(order.getPrice()))
            .build();
    }

    default com.metao.book.order.domain.Status convertToStatus(Status status) {
        return com.metao.book.order.domain.Status.valueOf(status.toString());
    }

    default com.metao.book.order.domain.Currency convertToCurrency(Currency currency) {
        return com.metao.book.order.domain.Currency.valueOf(currency.toString());
    }
}
