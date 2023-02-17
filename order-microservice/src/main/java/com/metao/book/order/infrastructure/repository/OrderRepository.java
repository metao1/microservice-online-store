package com.metao.book.order.infrastructure.repository;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import com.metao.book.order.domain.Status;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, OrderId> {

    @Override
    Optional<OrderEntity> findById(OrderId orderId);

    List<OrderEntity> findByIdBetweenAndStatusIsIn(
        OrderId fromOrderId,
        OrderId toOrderId,
        Set<Status> statusSet
    );
}
