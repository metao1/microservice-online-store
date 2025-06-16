package com.metao.book.order.domain.repository;

import com.metao.book.order.domain.model.aggregate.Order;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    List<Order> findByCustomerId(CustomerId customerId);

    void delete(OrderId orderId);
}