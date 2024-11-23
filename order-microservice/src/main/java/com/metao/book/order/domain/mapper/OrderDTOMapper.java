package com.metao.book.order.domain.mapper;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderStatus;
import com.metao.book.order.domain.dto.OrderDTO;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.Currency;
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

    public static OrderEntity toEntity(OrderDTO orderDTO) {
        String customerId = orderDTO.customerId();
        String productId = orderDTO.productId();
        BigDecimal productCount = orderDTO.quantity();
        Money money = new Money(Currency.getInstance(orderDTO.currency()), orderDTO.price());
        OrderStatus orderStatus = OrderStatus.valueOf(orderDTO.status());
        return new OrderEntity(customerId, productId, productCount, money, orderStatus);
    }

}
