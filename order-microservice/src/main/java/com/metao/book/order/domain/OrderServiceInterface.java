package com.metao.book.order.domain;

import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

        void saveOrder(OrderEntity orderEntity);

        Optional<OrderEntity> getOrderByProductId(OrderId productId);

        Optional<List<OrderEntity>> getAllOrdersPageble(int from, int to);
        
}
