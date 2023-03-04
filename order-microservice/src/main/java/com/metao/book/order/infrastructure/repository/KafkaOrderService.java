package com.metao.book.order.infrastructure.repository;

import static com.metao.book.shared.kafka.Constants.TRANSACTION_MANAGER;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.Status;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KafkaOrderService {

    private final OrderRepository orderRepository;

    public Optional<OrderEntity> getOrder(@NotNull OrderId orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER, propagation = Propagation.REQUIRED)
    public Optional<List<OrderEntity>> searchOrdersWithProductId(@NotNull Set<String> productId, Set<Status> statuses) {
        return Optional.of(orderRepository.findByProductIdIsInAndStatusIsIn(productId, statuses));
    }

    public Optional<List<OrderEntity>> getOrders(@NotNull OrderId from, @NotNull OrderId to, Set<Status> statuses) {
        return Optional.of(new ArrayList<>(orderRepository.findByIdBetweenAndStatusIsIn(from, to, statuses)));
    }
}
