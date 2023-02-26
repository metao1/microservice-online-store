package com.metao.book.order.domain;

import com.metao.book.order.application.dto.OrderDTO;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderServiceInterface {

    Optional<String> createOrder(OrderDTO orderDTO);

    Optional<OrderEntity> getOrderByOrderId(OrderId orderId);

    Optional<List<OrderEntity>> getOrderByProductIdsAndOrderStatus(Set<String> productIds, Set<Status> orderStatus);

    Optional<List<OrderEntity>> getAllOrdersPageable(OrderId from, OrderId to, Set<Status> statusSet);

}
