package com.metao.book.order.infrastructure;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.shared.Currency;
import com.metao.book.shared.OrderAvro;
import com.metao.book.shared.Status;

public interface OrderMapperInterface {

    default OrderAvro toAvro(OrderDTO dto) {
        return OrderAvro.newBuilder()
                .setStatus(Status.NEW)
                .setCurrency(Currency.dlr)
                .setPrice(dto.getPrice())
                .setStatus(Status.valueOf(dto.getStatus().toString()))
                .setQuantity(dto.getQuantity())
                .setProductId(dto.getProductId())
                .setCustomerId(dto.getCustomerId())
                .setOrderId(dto.getOrderId())
                .build();
    }

    default OrderDTO toDto(OrderAvro order) {
        return new OrderDTO(order.getOrderId(),
                order.getProductId(),
                order.getCustomerId(),
                convertToStatus(order.getStatus()),
                order.getQuantity(),
                com.metao.book.order.domain.Currency.DLR,
                order.getPrice());
    }

    default com.metao.book.order.domain.Status convertToStatus(Status status) {
        return com.metao.book.order.domain.Status.valueOf(status.toString());
    }

    default Currency convertToCurrency(Currency currency) {
        return Currency.valueOf(currency.toString());
    }
}
