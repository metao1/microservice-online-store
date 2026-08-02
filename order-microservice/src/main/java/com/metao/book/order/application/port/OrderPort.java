package com.metao.book.order.application.port;

import com.metao.book.order.domain.model.aggregate.OrderAggregate;
import com.metao.book.order.domain.model.valueobject.OrderId;
import java.util.Optional;

public interface OrderPort {

    Optional<OrderAggregate> findByIdForUpdate(OrderId orderId);
}
