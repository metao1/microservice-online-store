package com.metao.book.order.domain;

import com.metao.book.order.application.dto.OrderDTO;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderServiceInterface {

    Optional<String> createOrder(OrderDTO orderDTO);

    Optional<OrderDTO> getOrderByOrderId(OrderId orderId);

    Optional<List<OrderDTO>> getAllOrdersPageable(OrderId from, OrderId to, Set<Status> statusSet);

}
