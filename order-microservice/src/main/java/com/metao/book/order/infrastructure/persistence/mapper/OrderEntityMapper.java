package com.metao.book.order.infrastructure.persistence.mapper;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.infrastructure.persistence.entity.OrderEntity;
import com.metao.book.order.infrastructure.persistence.entity.OrderItemEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

/**
 * Mapper between Order domain object and OrderEntity
 */
@UtilityClass
public class OrderEntityMapper {

    /**
     * Convert domain Order to OrderEntity
     */
    public static OrderEntity toEntity(OrderAggregate order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().value());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemEntity> itemEntities = order.getItems().stream()
            .map(item -> {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setProductSku(item.getProductSku());
                itemEntity.setQuantity(item.getQuantity());
                itemEntity.setUnitPrice(item.getUnitPrice());
                itemEntity.setOrder(entity);
                return itemEntity;
            })
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        entity.setItems(itemEntities);
        return entity;
    }

    /**
     * Convert OrderEntity to domain Order
     */
    public static OrderAggregate toDomain(OrderEntity entity) {
        OrderAggregate order = new OrderAggregate(OrderId.of(entity.getId()), entity.getCustomerId());
        // Only update status if it's different from the default CREATED status
        if (entity.getStatus() != OrderStatus.CREATED) {
            order.updateStatus(entity.getStatus());
        }

        entity.getItems().forEach(itemEntity -> {
            order.addItem(
                itemEntity.getProductSku(),
                itemEntity.getQuantity(),
                itemEntity.getUnitPrice());
        });

        return order;
    }
}
