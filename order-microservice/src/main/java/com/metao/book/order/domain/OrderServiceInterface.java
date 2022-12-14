package com.metao.book.order.domain;


import com.metao.book.shared.OrderEvent;

import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

    void saveOrder(OrderEvent orderEntity);

    Optional<OrderEvent> getOrderByProductId(String productId);

    Optional<List<OrderEvent>> getAllOrdersPageable(int from, int to);

}
