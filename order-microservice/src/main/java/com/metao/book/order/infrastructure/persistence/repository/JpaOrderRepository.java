package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.infrastructure.persistence.entity.OrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, CustomerId> {

    Optional<OrderEntity> findById(String orderId);

    List<OrderEntity> findByCustomerId(CustomerId customerId);
}