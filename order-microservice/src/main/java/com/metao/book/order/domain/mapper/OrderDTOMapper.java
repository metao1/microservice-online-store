package com.metao.book.order.domain.mapper;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.dto.OrderDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderDTOMapper {

    public static OrderDTO toOrderDTO(OrderEntity orderEntity) {
        return OrderDTO.builder()
            .orderId(orderEntity.getOrderId())
            .customerId(orderEntity.getCustomerId())
            .productId(orderEntity.getProductId())
            .currency(orderEntity.getCurrency().toString())
            .status(orderEntity.getStatus().toString())
            .quantity(orderEntity.getQuantity())
            .price(orderEntity.getPrice())
            .build();
    }
}
