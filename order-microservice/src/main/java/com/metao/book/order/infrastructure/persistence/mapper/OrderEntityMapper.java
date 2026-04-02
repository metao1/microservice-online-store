package com.metao.book.order.infrastructure.persistence.mapper;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.infrastructure.persistence.entity.OrderItemEntity;
import com.metao.book.order.infrastructure.persistence.entity.OrderJpaEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderEntityMapper {

    public static OrderJpaEntity toEntity(OrderAggregate order) {
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        List<OrderItemEntity> itemEntities = order.getItems().stream()
            .map(item -> {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setProductSku(item.getProductSku());
                itemEntity.setQuantity(item.getQuantity());
                itemEntity.setProductTitle(item.getTitle());
                itemEntity.setUnitPrice(item.getUnitPrice());
                itemEntity.setOrder(entity);
                return itemEntity;
            })
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        entity.setItems(itemEntities);
        return entity;
    }

    public static OrderAggregate toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
            .map(itemEntity -> new OrderItem(
                itemEntity.getProductSku(),
                itemEntity.getProductTitle(),
                itemEntity.getQuantity(),
                itemEntity.getUnitPrice()
            ))
            .toList();

        return OrderAggregate.reconstitute(
            OrderId.of(entity.getId()),
            entity.getUserId(),
            items,
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
