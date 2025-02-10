package com.metao.book.order.application.cart;

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
            from order o
                where o.orderId = :orderId
        """)
    Optional<OrderEntity> findByOrderId(String orderId);

    Page<OrderEntity> findAll(
        Specification<OrderEntity> spec,
        Pageable pageable
    );
}
