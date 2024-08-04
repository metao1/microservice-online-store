package com.metao.book.order.infrastructure.repository;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<OrderEntity, OrderId> {

    @Query("""
        select distinct o
            from OrderEntity o
            where o.id = :orderId
        """)
    Optional<OrderEntity> findByOrderId(OrderId orderId);

    Page<OrderEntity> findAll(
        Specification<OrderEntity> spec,
        Pageable pageable
    );
}
