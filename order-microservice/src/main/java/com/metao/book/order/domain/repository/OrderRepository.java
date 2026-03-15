package com.metao.book.order.domain.repository;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    OrderAggregate save(OrderAggregate order);
    Optional<OrderAggregate> findById(OrderId orderId);
    Optional<OrderAggregate> findByIdForUpdate(OrderId orderId);
    List<OrderAggregate> findByuserId(UserId userId);
    void delete(OrderId orderId);
}
