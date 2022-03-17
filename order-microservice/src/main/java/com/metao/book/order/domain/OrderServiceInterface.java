package com.metao.book.order.domain;

import java.util.List;
import java.util.Optional;

import com.metao.book.order.infrastructure.KafkaListenableCallback;

public interface OrderServiceInterface {

        void saveOrder(OrderEntity orderEntity, KafkaListenableCallback<OrderId, OrderEntity> kafkaListenerHandler);

        Optional<OrderEntity> getOrderByProductId(ProductId productId);

        Optional<List<OrderEntity>> getAllOrdersPageble(int limit, int offset);
        
}
