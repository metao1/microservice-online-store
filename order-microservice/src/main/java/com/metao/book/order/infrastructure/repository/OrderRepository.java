package com.metao.book.order.infrastructure.repository;

import com.metao.book.order.domain.OrderEntity;
import com.metao.book.order.domain.OrderId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, OrderId> {

    Page<OrderEntity> findAll(
        Specification<OrderEntity> spec,
        Pageable pageable
    );
}
