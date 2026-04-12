package com.metao.book.order.domain.repository;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface OrderRepository {

    void save(OrderAggregate order);
    Optional<OrderAggregate> findById(OrderId orderId);
    Optional<OrderAggregate> findByIdForUpdate(OrderId orderId);
    List<OrderAggregate> findByUserId(UserId userId);
    Page<OrderAggregate> findByUserId(UserId userId, int offset, int limit);
    void delete(OrderId orderId);
}
