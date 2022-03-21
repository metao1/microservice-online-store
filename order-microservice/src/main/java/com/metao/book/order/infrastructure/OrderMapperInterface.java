package com.metao.book.order.infrastructure;

import com.metao.book.order.application.dto.OrderDTO;
import com.metao.book.order.domain.OrderEntity;
import com.metao.book.shared.domain.financial.Money;

public interface OrderMapperInterface {

        default OrderEntity toEntity(OrderDTO dto) {
                return new OrderEntity(dto.getStatus(), dto.getProductId(), dto.getCustomerId(), dto.getProductCount(),
                                new Money(dto.getCurrency(), dto.getAmount()));
        }

        default OrderDTO toDto(OrderEntity entity) {
                return new OrderDTO(entity.id(), entity.productId(),
                                entity.customerId(), entity.status(),
                                entity.productCount(), entity.currency(),
                                entity.amount());
        }
}
