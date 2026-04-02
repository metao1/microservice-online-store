package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.infrastructure.persistence.entity.OrderJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, String> {

    @EntityGraph(attributePaths = "items")
    @Override
    Optional<OrderJpaEntity> findById(String orderId);

    @EntityGraph(attributePaths = "items")
    List<OrderJpaEntity> findByUserId(UserId userId);

    @EntityGraph(attributePaths = "items")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.id = :orderId")
    Optional<OrderJpaEntity> findByIdForUpdate(@Param("orderId") String orderId);
}
