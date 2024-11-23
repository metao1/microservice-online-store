package com.metao.book.order.domain;

import com.metao.book.order.application.card.OrderRepository;
import com.metao.book.order.application.card.OrderSpecifications;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Optional<OrderEntity> getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    public Page<OrderEntity> getOrderByProductIdsAndOrderStatus(
        Set<String> productIds, Set<OrderStatus> statuses, int offset, int pageSize, String sortByFieldName
    ) {
        var pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.ASC, sortByFieldName));
        var spec = OrderSpecifications.findByOrdersByCriteria(productIds, statuses);
        return orderRepository.findAll(spec, pageable);
    }

    public String updateOrder(@Valid OrderEntity updatedOrder) {
        //TODO validate the updatedOrder fields are compatible with business criteria
        orderRepository.save(updatedOrder);
        return updatedOrder.getOrderId();
    }

    public void save(OrderEntity foundOrder) {
        // TODO validate the foundOrder fields are compatible with business criteria
        orderRepository.save(foundOrder);
    }
}
