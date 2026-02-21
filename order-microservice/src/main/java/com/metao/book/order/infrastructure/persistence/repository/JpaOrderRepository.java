package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.infrastructure.persistence.entity.OrderEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, String> {

    @EntityGraph(attributePaths = "items")
    Optional<OrderEntity> findById(String orderId);

    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findByCustomerId(CustomerId customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "items")
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :orderId")
    Optional<OrderEntity> findByIdForUpdate(@Param("orderId") String orderId);
}
