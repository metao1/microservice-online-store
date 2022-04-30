package com.metao.book.order.domain;

import com.order.microservice.avro.OrderAvro;

import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

        void saveOrder(OrderAvro orderEntity);

        Optional<OrderAvro> getOrderByProductId(String productId);

        Optional<List<OrderAvro>> getAllOrdersPageble(int from, int to);
        
}
