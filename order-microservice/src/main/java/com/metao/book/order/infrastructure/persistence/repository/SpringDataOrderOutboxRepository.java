package com.metao.book.order.infrastructure.persistence.repository;

import com.metao.book.order.infrastructure.persistence.entity.OrderOutboxJpaEntity;
import com.metao.book.outbox.application.OutboxStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataOrderOutboxRepository extends JpaRepository<OrderOutboxJpaEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select event
        from OrderOutboxJpaEntity event
        where (event.status = :pending and event.nextAttemptAt <= :now)
           or (event.status = :inProgress and event.leaseUntil <= :now)
        order by event.occurredAt asc
        """)
    List<OrderOutboxJpaEntity> findClaimable(
        @Param("now") Instant now,
        @Param("pending") OutboxStatus pending,
        @Param("inProgress") OutboxStatus inProgress,
        Pageable pageable
    );
}
