package com.metao.book.order.infrastructure;

import com.metao.book.order.application.dto.CreateOrderDTO;
import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.Status;
import java.util.UUID;

public interface OrderMapperInterface {

    default OrderDTO toEvent(CreateOrderDTO dto) {
        return OrderDTO.builder()
            .orderId(UUID.randomUUID().toString())
            .customerId(dto.accountId())
            .productId(dto.productId())
            .currency(dto.currency())
            .quantity(dto.quantity())
            .status(Status.NEW)
            .price(dto.price())
            .build();
    }

    static OrderDTO toOrderDTO(OrderEntity orderEntity) {
        return OrderDTO.builder()
            .orderId(orderEntity.id().toUUID())
            .price(orderEntity.getPrice())
            .currency(orderEntity.getCurrency())
            .productId(orderEntity.getProductId())
            .status(orderEntity.getStatus())
            .customerId(orderEntity.getCustomerId())
            .quantity(orderEntity.getProductCount())
            .build();
    }
}
